package com.healthtrail.common.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.config.HealthTrailConfig;
import com.healthtrail.common.config.HealthTrailStorageConfig;
import com.healthtrail.common.constant.Constants;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode.Internal;
import com.healthtrail.common.utils.ServletHolderUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.File;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 默认文件存储服务。
 *
 * <p>这里集中承接 LOCAL / MINIO 两种实现，
 * 目的是最大程度复用原有 `FileUploadUtils` 静态入口，
 * 让报告上传、附件上传、通用上传接口不需要各自维护一套存储判断逻辑。
 */
@Service
@RequiredArgsConstructor
public class DefaultFileStorageService implements FileStorageService {

    /**
     * 本地磁盘存储标识。
     */
    private static final String STORAGE_LOCAL = "LOCAL";

    /**
     * MinIO 对象存储标识。
     */
    private static final String STORAGE_MINIO = "MINIO";

    private final HealthTrailStorageConfig storageConfig;

    @Override
    public String upload(String subDir, String fileName, MultipartFile file) throws Exception {
        if (isMinioStorage()) {
            return uploadToMinio(subDir, fileName, file);
        }
        return uploadToLocal(subDir, fileName, file);
    }

    @Override
    public byte[] readBytes(String fileReference) throws Exception {
        if (isMinioStorage()) {
            return readBytesFromMinio(fileReference);
        }
        return readBytesFromLocal(fileReference);
    }

    @Override
    public String getAccessUrl(String fileReference) {
        if (StrUtil.isBlank(fileReference)) {
            return fileReference;
        }
        if (isHttpUrl(fileReference)) {
            // 历史数据里可能已经落过“完整 HTTP 链接”，并且尾部还带了签名、缩略图、
            // 限流或样式之类的查询参数。
            // 对当前项目来说，图片和附件现在都走公开桶访问，不需要把这些参数继续透传给前端，
            // 否则前端看到的链接会又长又不稳定，部分参数过期后还会导致资源打不开。
            return removeUrlQuery(fileReference);
        }

        if (isMinioStorage()) {
            return buildMinioPublicUrl(normalizeMinioObjectName(fileReference));
        }

        // 本地存储仍沿用项目现有 /profile/** 静态资源映射，
        // 因此这里统一补全成当前服务的绝对访问地址，前端无需再自己拼 host。
        return ServletHolderUtil.getContextUrl() + normalizeLocalRelativePath(fileReference);
    }

    /**
     * 供业务层记录当前存储类型使用。
     *
     * <p>例如附件表需要把 storageProvider 落库，
     * 这样后续排查“这条文件到底在本地盘还是 MinIO”时会更直接。
     */
    public String getStorageType() {
        return isMinioStorage() ? STORAGE_MINIO : STORAGE_LOCAL;
    }

    private String uploadToLocal(String subDir, String fileName, MultipartFile file) throws Exception {
        File destination = new File(HealthTrailConfig.getFileBaseDir() + File.separator + subDir + File.separator + fileName);
        if (!destination.exists() && destination.getParentFile() != null && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }
        file.transferTo(destination);
        return buildLocalRelativePath(subDir, fileName);
    }

    private String uploadToMinio(String subDir, String fileName, MultipartFile file) throws Exception {
        validateMinioConfig();
        String objectName = buildMinioObjectName(subDir, fileName);
        getMinioClient().putObject(PutObjectArgs.builder()
            .bucket(storageConfig.getMinio().getBucket())
            .object(objectName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(StrUtil.blankToDefault(file.getContentType(), "application/octet-stream"))
            .build());
        return objectName;
    }

    private byte[] readBytesFromLocal(String fileReference) {
        String relativePath = normalizeLocalRelativePath(fileReference);
        String fileBaseDir = HealthTrailConfig.getFileBaseDir();
        String suffixPath = StrUtil.removePrefix(relativePath, "/" + Constants.RESOURCE_PREFIX + "/");
        String absolutePath = fileBaseDir + File.separator + suffixPath.replace("/", File.separator);
        return FileUtil.readBytes(absolutePath);
    }

    private byte[] readBytesFromMinio(String fileReference) throws Exception {
        validateMinioConfig();
        String objectName = normalizeMinioObjectName(fileReference);
        try (var response = getMinioClient().getObject(GetObjectArgs.builder()
            .bucket(storageConfig.getMinio().getBucket())
            .object(objectName)
            .build())) {
            return response.readAllBytes();
        }
    }

    private boolean isMinioStorage() {
        String type = storageConfig.getType();
        if (StrUtil.isBlank(type)) {
            return false;
        }
        if (STORAGE_LOCAL.equalsIgnoreCase(type)) {
            return false;
        }
        if (STORAGE_MINIO.equalsIgnoreCase(type)) {
            return true;
        }
        throw new ApiException(Internal.INVALID_PARAMETER, "healthtrail.storage.type=" + type);
    }

    private void validateMinioConfig() {
        if (StrUtil.hasBlank(storageConfig.getMinio().getEndpoint(), storageConfig.getMinio().getBucket(),
            storageConfig.getMinio().getAccessKey(), storageConfig.getMinio().getSecretKey())) {
            throw new ApiException(Internal.INVALID_PARAMETER, "MinIO storage config is incomplete");
        }
    }

    private MinioClient getMinioClient() {
        String scheme = Boolean.TRUE.equals(storageConfig.getMinio().getUseSsl()) ? Constants.HTTPS : Constants.HTTP;
        return MinioClient.builder()
            .endpoint(scheme + storageConfig.getMinio().getEndpoint())
            .credentials(storageConfig.getMinio().getAccessKey(), storageConfig.getMinio().getSecretKey())
            .build();
    }

    private String buildLocalRelativePath(String subDir, String fileName) {
        return StrUtil.format("/{}/{}/{}", Constants.RESOURCE_PREFIX, subDir, fileName);
    }

    private String buildMinioObjectName(String subDir, String fileName) {
        return StrUtil.format("{}/{}", StrUtil.removeSuffix(subDir, "/"), fileName);
    }

    private String buildMinioPublicUrl(String objectName) {
        String customPrefix = storageConfig.getMinio().getPublicUrlPrefix();
        if (StrUtil.isNotBlank(customPrefix)) {
            return StrUtil.addSuffixIfNot(customPrefix, "/") + objectName;
        }

        String scheme = Boolean.TRUE.equals(storageConfig.getMinio().getUseSsl()) ? Constants.HTTPS : Constants.HTTP;
        return StrUtil.format("{}{}/{}/{}", scheme, storageConfig.getMinio().getEndpoint(),
            storageConfig.getMinio().getBucket(), objectName);
    }

    /**
     * 兼容本地存储场景下的两类输入：
     * 1. 已经是相对路径：/profile/report/a.pdf
     * 2. 已经被上层拼成完整 URL：http://host/profile/report/a.pdf
     *
     * <p>这样下载、解析链路都可以直接复用，不会因为传参来源不同而失败。
     */
    private String normalizeLocalRelativePath(String fileReference) {
        if (isHttpUrl(fileReference)) {
            try {
                return URI.create(fileReference).getPath();
            } catch (Exception ignore) {
                return fileReference;
            }
        }
        return fileReference;
    }

    /**
     * 把 MinIO 场景下的各种“文件引用形式”统一收敛成 object key。
     *
     * <p>当前允许传入：
     * 1. 纯 object key：report/a.pdf
     * 2. 带 bucket 的 path：/healthtrail-prod/report/a.pdf
     * 3. 完整公开访问 URL：https://host/healthtrail-prod/report/a.pdf
     */
    private String normalizeMinioObjectName(String fileReference) {
        String normalized = fileReference;
        if (isHttpUrl(fileReference)) {
            try {
                normalized = URI.create(fileReference).getPath();
            } catch (Exception ignore) {
                normalized = fileReference;
            }
        }

        normalized = StrUtil.removePrefix(normalized, "/");
        String bucketPrefix = storageConfig.getMinio().getBucket() + "/";
        if (StrUtil.startWith(normalized, bucketPrefix)) {
            normalized = StrUtil.removePrefix(normalized, bucketPrefix);
        }
        return normalized;
    }

    /**
     * 当前 Hutool 版本没有 startWithAnyIgnoreCase，
     * 因此这里统一封装一层，避免协议判断散落在多个方法里。
     */
    private boolean isHttpUrl(String value) {
        return StrUtil.startWithIgnoreCase(value, Constants.HTTP)
            || StrUtil.startWithIgnoreCase(value, Constants.HTTPS);
    }

    /**
     * 去掉 URL 上的 query / fragment，只保留稳定的对象访问主路径。
     *
     * <p>例如：
     * 1. https://host/bucket/a.png?x-oss-process=... -> https://host/bucket/a.png
     * 2. https://host/bucket/a.png?token=...#preview -> https://host/bucket/a.png
     *
     * <p>这样做的目标不是“重新签名”，而是明确告诉上层：
     * 当前系统下发给前端的应该是可长期复用的公开对象地址，而不是一次性的带参数临时链接。
     */
    private String removeUrlQuery(String url) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        try {
            URI uri = URI.create(url);
            return new URI(
                uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null,
                null
            ).toString();
        } catch (Exception ignore) {
            int queryIndex = url.indexOf('?');
            int fragmentIndex = url.indexOf('#');
            int cutIndex;
            if (queryIndex >= 0 && fragmentIndex >= 0) {
                cutIndex = Math.min(queryIndex, fragmentIndex);
            } else if (queryIndex >= 0) {
                cutIndex = queryIndex;
            } else {
                cutIndex = fragmentIndex;
            }
            return cutIndex >= 0 ? url.substring(0, cutIndex) : url;
        }
    }
}

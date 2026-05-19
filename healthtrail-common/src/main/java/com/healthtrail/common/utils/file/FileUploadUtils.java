package com.healthtrail.common.utils.file;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.healthtrail.common.config.HealthTrailConfig;
import com.healthtrail.common.constant.Constants;
import com.healthtrail.common.storage.DefaultFileStorageService;
import com.healthtrail.common.storage.FileStorageService;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.exception.error.ErrorCode.Business;
import com.healthtrail.common.exception.error.ErrorCode.Internal;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 文件上传工具类
 *
 * @author valarchie
 */
public class FileUploadUtils {

    /**
     * 默认大小 50M
     */
    public static final long MAX_FILE_SIZE = 50 * Constants.MB;

    /**
     * 默认的文件名最大长度 127
     */
    public static final int MAX_FILE_NAME_LENGTH = 127;

    /**
     * 允许上传和下载的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png",
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            // pdf
            "pdf"};

    private FileUploadUtils() {
    }


    /**
     * 根据文件路径上传
     *
     * @param subDir 相对应用的基目录
     * @param file 上传的文件
     * @return 文件名称
     */
    public static String upload(String subDir, MultipartFile file) {
        try {
            return upload(subDir, file, ALLOWED_EXTENSIONS);
        } catch (Exception e) {
            throw new ApiException(Business.UPLOAD_FILE_FAILED, e.getMessage());
        }
    }

    /**
     * 文件上传
     *
     * @param subDir 相对应用的子目录
     * @param file 上传的文件
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws IOException 比如读写文件出错时
     */
    public static String upload(String subDir, MultipartFile file, String[] allowedExtension)
        throws IOException {
        isAllowedUpload(file, allowedExtension);
        String fileName = generateFilename(file);
        try {
            return getFileStorageService().upload(subDir, fileName, file);
        } catch (Exception e) {
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(e);
        }
    }

    /**
     * 把存储标识转换成最终可访问地址。
     *
     * <p>这里统一给：
     * 1. 前端列表图片；
     * 2. 报告原文件入口；
     * 3. 通用上传接口返回值
     * 做最终 URL 适配，避免业务层反复判断本地盘还是 MinIO。
     */
    public static String getAccessUrl(String fileReference) {
        return getFileStorageService().getAccessUrl(fileReference);
    }

    /**
     * 按存储标识读取文件字节。
     *
     * <p>下载接口、报告解析等需要拿到文件原始内容的场景统一走这里，
     * 这样后续即使再接其他对象存储，调用方也不用改。
     */
    public static byte[] readBytes(String fileReference) {
        try {
            return getFileStorageService().readBytes(fileReference);
        } catch (Exception e) {
            throw new ApiException(Business.COMMON_FILE_NOT_ALLOWED_TO_DOWNLOAD, fileReference);
        }
    }

    /**
     * 返回当前启用的存储类型。
     *
     * <p>附件表等业务表需要把存储提供方一起记录下来时，
     * 可以通过这个入口拿到统一口径，避免再去直接读配置。
     */
    public static String getStorageType() {
        return SpringUtil.getBean(DefaultFileStorageService.class).getStorageType();
    }


    /**
     * 将文件保存到服务器
     *
     * @param file 文件
     * @param subDir 子目录
     * @param fileName 文件名
     */
    static void saveFileToLocal(MultipartFile file, String subDir, String fileName) throws IOException {
        if (StrUtil.isEmpty(subDir) || StrUtil.isEmpty(fileName)) {
            throw new ApiException(Internal.INVALID_PARAMETER, "subDir or fileName");
        }

        File destination = new File(getFileAbsolutePath(subDir, fileName));
        if (!destination.exists()) {
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
        }

        file.transferTo(destination);
    }

    /**
     * 获取文件的相对地址
     *
     * @param subDir avatar
     * @param fileName test.jpg
     * @return /profile/avatar/test.jpg
     */
    static String getRelativeFileUrl(String subDir, String fileName) {
        // 相对地址用于网络请求  所以不使用File.separate
        return StrUtil.format("/{}/{}/{}", Constants.RESOURCE_PREFIX, subDir, fileName);
    }

    /**
     * 检测文件是否可以上传
     *
     * @param file 文件
     * @param allowedExtension 允许的文件类型列表
     */
    static void isAllowedUpload(MultipartFile file, String[] allowedExtension) {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();

        if (fileNameLength > MAX_FILE_NAME_LENGTH) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_NAME_EXCEED_MAX_LENGTH, MAX_FILE_NAME_LENGTH);
        }

        long size = file.getSize();
        if (size > MAX_FILE_SIZE) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_SIZE_EXCEED_MAX_SIZE, MAX_FILE_SIZE / Constants.MB);
        }

        String extension = getFileExtension(file);
        if (!isExtensionAllowed(extension, allowedExtension)) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_TYPE_NOT_ALLOWED,
                StrUtil.join(",", (Object[]) allowedExtension));
        }
    }


    /**
     * 检查文件是否可下载
     *
     * @param resource 需要下载的文件
     * @return true 正常 false 非法
     */
    public static boolean isAllowDownload(String resource) {
        // 禁止目录上跳级别
        return !StrUtil.contains(resource, "..") &&
            // 检查允许下载的文件规则
            StrUtil.containsAnyIgnoreCase(FileNameUtil.getSuffix(resource), ALLOWED_EXTENSIONS);
    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     */
    static boolean isExtensionAllowed(String extension, String[] allowedExtension) {
        if (allowedExtension == null || allowedExtension.length == 0) {
            return true;
        }
        return StrUtil.containsAnyIgnoreCase(extension, allowedExtension);
    }

    /**
     * 获取文件名的后缀
     *
     * @param file 表单文件
     * @return 后缀名
     */
    static String getFileExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StrUtil.isEmpty(extension)) {
            MimeType mimeType = MimeTypeUtils.parseMimeType(Objects.requireNonNull(file.getContentType()));
            extension = mimeType.getSubtype();
        }
        return extension;
    }

    /**
     * 编码文件名
     */
    static String generateFilename(MultipartFile file) {
        return StrUtil.format("{}_{}_{}.{}",
            DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_PATTERN),
            FilenameUtils.getBaseName(file.getOriginalFilename()),
            IdUtil.simpleUUID(),
            getFileExtension(file));
    }


    /**
     * 下载文件名重新编码
     *
     * @param fileName 真实文件名
     */
    public static HttpHeaders getDownloadHeader(String fileName) {
        String randomFileName = System.currentTimeMillis() + "_" + fileName;
        String fileNameUrlEncoded = URLUtil.encode(randomFileName, CharsetUtil.CHARSET_UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", String.format("attachment;filename=%s", fileNameUrlEncoded));
        return headers;
    }


    public static String getFileAbsolutePath(String subDir, String fileName) {
        return HealthTrailConfig.getFileBaseDir() + File.separator + subDir + File.separator + fileName;
    }

    private static FileStorageService getFileStorageService() {
        return SpringUtil.getBean(FileStorageService.class);
    }


}

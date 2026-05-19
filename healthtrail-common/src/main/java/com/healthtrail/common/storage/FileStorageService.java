package com.healthtrail.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务。
 *
 * <p>对上层统一屏蔽本地磁盘和 MinIO 的差异。
 * 上层只需要关注三件事：
 * 1. 上传文件；
 * 2. 按存储标识读取文件；
 * 3. 把存储标识转换成最终访问地址。
 */
public interface FileStorageService {

    /**
     * 上传文件并返回存储标识。
     *
     * <p>返回值说明：
     * 1. LOCAL：返回相对资源路径，例如 /profile/report/a.pdf
     * 2. MINIO：返回对象 key，例如 report/a.pdf
     */
    String upload(String subDir, String fileName, MultipartFile file) throws Exception;

    /**
     * 按存储标识读取文件内容。
     */
    byte[] readBytes(String fileReference) throws Exception;

    /**
     * 把存储标识转换成最终可访问 URL。
     */
    String getAccessUrl(String fileReference);
}

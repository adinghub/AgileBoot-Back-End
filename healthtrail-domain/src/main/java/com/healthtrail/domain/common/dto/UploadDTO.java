package com.healthtrail.domain.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 上传文件结果
 * @author valarchie
 */
@Data
@Builder
public class UploadDTO {

    /** 文件访问地址 */
    private String url;
    /** 文件名 */
    private String fileName;
    /** 新文件名 */
    private String newFileName;
    /** 原始文件名 */
    private String originalFilename;

}

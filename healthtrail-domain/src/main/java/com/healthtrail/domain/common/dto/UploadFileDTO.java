package com.healthtrail.domain.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传文件结果
 * @author valarchie
 */
@Data
@NoArgsConstructor
public class UploadFileDTO {

    public UploadFileDTO(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    /** 文件访问地址 */
    private String imgUrl;

}

package com.healthtrail.admin.customize.service.login.dto;

import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class CaptchaDTO {

    /** 验证码是否开启 */
    private Boolean isCaptchaOn;
    /** 验证码缓存键 */
    private String captchaCodeKey;
    /** 验证码图片Base64 */
    private String captchaCodeImg;

}

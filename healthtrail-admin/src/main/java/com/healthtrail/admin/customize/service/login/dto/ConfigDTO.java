package com.healthtrail.admin.customize.service.login.dto;

import com.healthtrail.common.enums.dictionary.DictionaryData;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class ConfigDTO {

    /** 验证码是否开启 */
    private Boolean isCaptchaOn;
    /** 前端字典数据 */
    private Map<String, List<DictionaryData>> dictionary;

}

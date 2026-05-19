package com.healthtrail.common.enums.dictionary;

import com.healthtrail.common.enums.DictionaryEnum;
import lombok.Data;

/**
 * 字典模型类
 * @author valarchie
 */
@Data
public class DictionaryData {

    /**
     * 字典标签
     */
    private String label;
    /**
     * 字典值
     */
    private Object value;
    /**
     * CSS样式标签
     */
    private String cssTag;

    @SuppressWarnings("rawtypes")
    public DictionaryData(DictionaryEnum enumType) {
        if (enumType != null) {
            this.label = enumType.description();
            this.value = enumType.getValue();
            this.cssTag = enumType.cssTag();
        }
    }

}

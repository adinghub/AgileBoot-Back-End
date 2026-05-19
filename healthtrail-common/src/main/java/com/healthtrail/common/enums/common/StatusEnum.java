package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;
import com.healthtrail.common.enums.DictionaryEnum;

/**
 * 除非表有特殊指明的话，一般用这个枚举代表 status字段
 * @author valarchie
 */
@Dictionary(name = "common.status")
public enum StatusEnum implements DictionaryEnum<Integer> {
    /**
     * 启用状态
     */
    ENABLE(1, "正常", CssTag.PRIMARY),
    /**
     * 停用状态
     */
    DISABLE(0, "停用", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    StatusEnum(int value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }

}

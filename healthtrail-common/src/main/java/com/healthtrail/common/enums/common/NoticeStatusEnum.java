package com.healthtrail.common.enums.common;

import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;
import com.healthtrail.common.enums.DictionaryEnum;

/**
 * 对应sys_notice的 status字段
 * @author valarchie
 */
@Dictionary(name = "sysNotice.status")
public enum NoticeStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 正常状态
     */
    OPEN(1, "正常", CssTag.PRIMARY),
    /**
     * 关闭状态
     */
    CLOSE(0, "关闭", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    NoticeStatusEnum(int value, String description, String cssTag) {
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

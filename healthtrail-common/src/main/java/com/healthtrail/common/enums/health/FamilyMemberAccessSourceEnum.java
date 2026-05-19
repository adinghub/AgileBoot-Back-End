package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 家庭成员访问来源枚举。
 *
 * <p>同一个成员既可能是“我自己创建的成员”，也可能是“别人共享给我的成员”，
 * 前端列表需要据此给出不同的文案和操作按钮，所以这里单独保留访问来源。
 */
@Dictionary(name = "health.familyMemberAccessSource")
public enum FamilyMemberAccessSourceEnum implements DictionaryEnum<String> {

    /**
     * 自己创建并持有的成员。
     */
    OWNER("OWNER", "我的成员", CssTag.PRIMARY),

    /**
     * 别人共享给当前账号的成员。
     */
    SHARED("SHARED", "共享成员", CssTag.INFO);

    private final String value;
    private final String description;
    private final String cssTag;

    FamilyMemberAccessSourceEnum(String value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public String getValue() {
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

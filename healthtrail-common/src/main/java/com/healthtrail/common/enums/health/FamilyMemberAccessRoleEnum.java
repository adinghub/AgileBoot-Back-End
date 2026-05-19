package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 家庭成员协同权限角色枚举。
 *
 * <p>二期“家庭共享/邀请”落地后，一个成员不再只属于单一账号的可见范围，
 * 因此这里把访问角色显式枚举出来，方便后端权限判断和 App 前端做能力显隐：
 * 1. OWNER：主账号，拥有全部权限
 * 2. EDITOR：协同管理账号，可查看并维护成员相关健康业务
 * 3. VIEWER：只读账号，仅查看，不允许修改
 */
@Dictionary(name = "health.familyMemberAccessRole")
public enum FamilyMemberAccessRoleEnum implements DictionaryEnum<String> {

    /**
     * 主账号。
     */
    OWNER("OWNER", "主账号", CssTag.PRIMARY),

    /**
     * 协同管理。
     */
    EDITOR("EDITOR", "协同管理", CssTag.SUCCESS),

    /**
     * 只读查看。
     */
    VIEWER("VIEWER", "只读查看", CssTag.INFO);

    private final String value;
    private final String description;
    private final String cssTag;

    FamilyMemberAccessRoleEnum(String value, String description, String cssTag) {
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

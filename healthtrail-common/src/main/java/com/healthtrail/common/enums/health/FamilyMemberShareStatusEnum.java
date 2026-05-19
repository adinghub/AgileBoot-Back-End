package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 家庭成员共享状态枚举。
 *
 * <p>共享关系采用单独状态而不是直接物理删除，主要是为了保留协同审计轨迹，
 * 以及给后续“暂停共享/恢复共享”预留扩展空间。
 */
@Dictionary(name = "health.familyMemberShareStatus")
public enum FamilyMemberShareStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 已停用。
     */
    DISABLED(0, "已停用", CssTag.DANGER),

    /**
     * 已生效。
     */
    ENABLED(1, "已生效", CssTag.SUCCESS);

    private final int value;
    private final String description;
    private final String cssTag;

    FamilyMemberShareStatusEnum(int value, String description, String cssTag) {
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

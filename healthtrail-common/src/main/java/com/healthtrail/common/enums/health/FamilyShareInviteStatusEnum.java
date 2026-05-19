package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 家庭共享邀请状态枚举。
 *
 * <p>邀请链路单独保留状态，是为了让“生成邀请码、待接受、已接受、已取消、已过期”
 * 这些节点都能被清晰追踪，便于 App 端做邀请状态提示和后续排查。
 */
@Dictionary(name = "health.familyShareInviteStatus")
public enum FamilyShareInviteStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 待接受。
     */
    PENDING(0, "待接受", CssTag.WARNING),

    /**
     * 已接受。
     */
    ACCEPTED(1, "已接受", CssTag.SUCCESS),

    /**
     * 已取消。
     */
    CANCELED(2, "已取消", CssTag.INFO),

    /**
     * 已过期。
     */
    EXPIRED(3, "已过期", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    FamilyShareInviteStatusEnum(int value, String description, String cssTag) {
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

package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * App 消息中心已读状态枚举。
 *
 * <p>消息中心和 Push 派发是两条并行维度：
 * 1. Push 负责“是否尽快把通知送到设备”
 * 2. 已读状态负责“用户是否已经在 App 内看过这条消息”
 *
 * <p>因此即使 Push 发送失败，
 * 这条消息也依然可以保留在消息中心等待用户下次进入 App 查看。
 */
@Dictionary(name = "health.appMessageReadStatus")
public enum HealthAppMessageReadStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 用户尚未查看。
     */
    UNREAD(0, "未读", CssTag.WARNING),

    /**
     * 用户已经查看或手动标记已读。
     */
    READ(1, "已读", CssTag.PRIMARY);

    private final int value;

    private final String description;

    private final String cssTag;

    HealthAppMessageReadStatusEnum(int value, String description, String cssTag) {
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

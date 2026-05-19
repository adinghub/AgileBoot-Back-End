package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * App 消息推送发送状态枚举。
 *
 * <p>该状态只描述“消息中心这条消息最近一次向设备派发的结果”，
 * 不代表用户是否已经阅读。
 *
 * <p>之所以单独保存这层状态，是为了同时满足：
 * 1. App 消息中心要能保留消息内容
 * 2. 运维排查时要知道 Push 是否成功送出
 * 3. 后续如果接入重试或多通道发送，仍然有状态基础可复用
 */
@Dictionary(name = "health.appMessageSendStatus")
public enum HealthAppMessageSendStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 消息已创建，但还未成功发送。
     */
    PENDING(0, "待发送", CssTag.WARNING),

    /**
     * 最近一次发送成功。
     */
    SUCCESS(1, "发送成功", CssTag.PRIMARY),

    /**
     * 最近一次发送失败。
     */
    FAILED(2, "发送失败", CssTag.DANGER);

    private final int value;

    private final String description;

    private final String cssTag;

    HealthAppMessageSendStatusEnum(int value, String description, String cssTag) {
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

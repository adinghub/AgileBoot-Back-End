package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 用药提醒发送状态枚举。
 *
 * <p>该枚举描述的是“提醒消息是否已经被系统成功派发”，
 * 它和 `MedicationReminderStatusEnum` 表示的“用户是否处理本次提醒”是两条不同维度：
 * 1. `MedicationReminderStatusEnum` 关注用户是否已服药、已跳过、已过期
 * 2. `MedicationReminderNotifyStatusEnum` 关注系统是否已经把这条提醒真正发出去
 *
 * <p>这样后续无论对接 App Push、短信还是站内信，都可以沿用同一套发送状态管理。
 */
@Dictionary(name = "health.medicationReminderNotifyStatus")
public enum MedicationReminderNotifyStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 提醒已生成，但还没有进入成功发送状态。
     */
    PENDING(0, "待发送", CssTag.WARNING),

    /**
     * 提醒已经成功交给当前通知通道。
     */
    SUCCESS(1, "发送成功", CssTag.PRIMARY),

    /**
     * 系统尝试发送过，但通道返回失败，后续允许重试。
     */
    FAILED(2, "发送失败", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;

    MedicationReminderNotifyStatusEnum(int value, String description, String cssTag) {
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

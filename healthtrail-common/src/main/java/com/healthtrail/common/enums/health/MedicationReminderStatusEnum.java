package com.healthtrail.common.enums.health;

import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.CssTag;
import com.healthtrail.common.enums.dictionary.Dictionary;

/**
 * 用药提醒状态枚举。
 *
 * <p>提醒记录和用药计划不同，它描述的是某一次具体提醒的执行结果。
 * 因此这里单独定义状态枚举，方便后续做“今日待处理”“已服药统计”“漏服统计”等业务扩展。
 */
@Dictionary(name = "health.medicationReminderStatus")
public enum MedicationReminderStatusEnum implements DictionaryEnum<Integer> {

    /**
     * 等待用户处理。
     */
    PENDING(0, "待处理", CssTag.WARNING),

    /**
     * 用户已确认服药。
     */
    TAKEN(1, "已服药", CssTag.PRIMARY),

    /**
     * 用户主动跳过本次提醒。
     */
    SKIPPED(2, "已跳过", CssTag.DANGER),

    /**
     * 到点后长期未处理，系统视为过期。
     */
    EXPIRED(3, "已过期", CssTag.INFO);

    private final int value;
    private final String description;
    private final String cssTag;

    MedicationReminderStatusEnum(int value, String description, String cssTag) {
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

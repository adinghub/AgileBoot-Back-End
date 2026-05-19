package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * 首页待跟进任务类型枚举。
 *
 * <p>首页任务流虽然会聚合多个模块的数据，
 * 但落库和操作时仍然需要明确知道任务真正来源于哪个业务对象。
 */
public enum HealthFollowUpTaskTypeEnum {

    /**
     * 来源于用药提醒。
     */
    REMINDER("REMINDER", "用药提醒任务"),

    /**
     * 来源于报告异常建议。
     */
    REPORT_ADVICE("REPORT_ADVICE", "报告建议任务"),

    /**
     * 来源于后台运营任务。
     *
     * <p>这类任务不直接来自提醒或报告原始数据，
     * 而是由后台运营人员按用户维度投放到首页任务流中的“主动引导任务”。
     */
    OPERATION("OPERATION", "运营任务");

    private final String value;

    private final String description;

    HealthFollowUpTaskTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按任务类型值解析枚举。
     */
    public static HealthFollowUpTaskTypeEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthFollowUpTaskTypeEnum taskTypeEnum : values()) {
            if (Objects.equals(taskTypeEnum.getValue(), value)) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}

package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * 首页任务快捷延后选项枚举。
 *
 * <p>该枚举用于给 App 前端提供固定的快捷延后动作，
 * 避免所有客户端都自己重复计算“今晚”“明天早上”这类业务时间。
 */
public enum HealthFollowUpDelayOptionEnum {

    /**
     * 30 分钟后。
     */
    AFTER_30_MINUTES("AFTER_30_MINUTES", "30分钟后"),

    /**
     * 今天晚上 20:00。
     */
    TONIGHT("TONIGHT", "今晚20点"),

    /**
     * 明天早上 08:00。
     */
    TOMORROW_MORNING("TOMORROW_MORNING", "明天早上8点");

    private final String value;

    private final String description;

    HealthFollowUpDelayOptionEnum(String value, String description) {
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
     * 按选项值解析枚举。
     */
    public static HealthFollowUpDelayOptionEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthFollowUpDelayOptionEnum optionEnum : values()) {
            if (Objects.equals(optionEnum.getValue(), value)) {
                return optionEnum;
            }
        }
        return null;
    }
}

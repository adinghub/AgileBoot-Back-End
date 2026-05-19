package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * App Push 设备级失败原因分类枚举。
 *
 * <p>消息中心表里的 `send_result_message` 适合直接给人看，
 * 但后台统计、告警和审计更需要“统一口径”的分类字段。
 * 因此设备级推送日志额外维护一份失败原因分类，避免：
 * 1. 同一类问题被不同文案打散；
 * 2. 后台无法按失败类型聚合；
 * 3. 后续监控规则只能依赖模糊文本匹配。
 */
public enum HealthAppPushDeliveryFailureReasonCategoryEnum {

    /**
     * 当前用户没有任何可用的活跃设备。
     */
    NO_ACTIVE_DEVICE("NO_ACTIVE_DEVICE", "无活跃设备"),

    /**
     * 命中的设备处于停用状态。
     */
    DEVICE_DISABLED("DEVICE_DISABLED", "设备已停用"),

    /**
     * 设备缺少有效 Token。
     */
    DEVICE_TOKEN_EMPTY("DEVICE_TOKEN_EMPTY", "设备Token缺失"),

    /**
     * 厂商或外部网关明确拒绝了本次发送。
     */
    VENDOR_REJECTED("VENDOR_REJECTED", "厂商拒绝"),

    /**
     * 调用外部通道时发生异常，例如超时、配置缺失、网关不可用等。
     */
    CHANNEL_EXCEPTION("CHANNEL_EXCEPTION", "通道异常"),

    /**
     * 仍然无法明确归类的失败。
     */
    UNKNOWN("UNKNOWN", "未知原因");

    private final String value;

    private final String description;

    HealthAppPushDeliveryFailureReasonCategoryEnum(String value, String description) {
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
     * 根据存储值反查枚举。
     */
    public static HealthAppPushDeliveryFailureReasonCategoryEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthAppPushDeliveryFailureReasonCategoryEnum item : values()) {
            if (Objects.equals(item.getValue(), value)) {
                return item;
            }
        }
        return null;
    }
}

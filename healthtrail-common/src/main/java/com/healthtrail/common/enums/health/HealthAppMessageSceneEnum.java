package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * App 消息中心业务场景枚举。
 *
 * <p>消息中心不是一个独立业务，
 * 它只是把各业务模块发给 App 用户的重要消息做统一归档。
 * 因此需要通过业务场景明确知道：
 * 1. 这条消息来自哪个模块
 * 2. 点击后应该如何解析跳转
 * 3. 后续做统计、筛选或运营审计时如何分类
 */
public enum HealthAppMessageSceneEnum {

    /**
     * 用药提醒消息。
     */
    MEDICATION_REMINDER("MEDICATION_REMINDER", "用药提醒"),

    /**
     * 体检报告跟进建议消息。
     */
    REPORT_FOLLOW_UP("REPORT_FOLLOW_UP", "报告跟进"),

    /**
     * 低库存提醒消息。
     */
    LOW_STOCK_ALERT("LOW_STOCK_ALERT", "低库存提醒"),

    /**
     * 药品近效期提醒消息。
     */
    NEAR_EXPIRY_ALERT("NEAR_EXPIRY_ALERT", "近效期提醒");

    private final String value;

    private final String description;

    HealthAppMessageSceneEnum(String value, String description) {
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
     * 按业务场景值解析枚举。
     */
    public static HealthAppMessageSceneEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthAppMessageSceneEnum sceneEnum : values()) {
            if (Objects.equals(sceneEnum.getValue(), value)) {
                return sceneEnum;
            }
        }
        return null;
    }
}

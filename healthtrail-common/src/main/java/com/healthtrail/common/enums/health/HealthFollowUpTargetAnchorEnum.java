package com.healthtrail.common.enums.health;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

/**
 * 首页待跟进任务跳转锚点枚举。
 *
 * <p>该枚举用于描述“进入目标页面后建议优先定位到哪个业务区域”，
 * 不直接绑定前端真实组件 ID，只提供稳定语义编码。
 */
public enum HealthFollowUpTargetAnchorEnum {

    /**
     * 提醒反馈区。
     *
     * <p>适合直接落到“已服药 / 已跳过”操作区域。
     */
    REMINDER_FEEDBACK("REMINDER_FEEDBACK", "提醒反馈区"),

    /**
     * 报告建议区。
     */
    REPORT_ADVICE("REPORT_ADVICE", "报告建议区"),

    /**
     * 异常指标区。
     */
    REPORT_ABNORMAL_ITEMS("REPORT_ABNORMAL_ITEMS", "异常指标区"),

    /**
     * 原始报告区。
     *
     * <p>适合优先查看原始 PDF / 图片 / 上传文件信息的场景。
     */
    REPORT_ORIGINAL_FILE("REPORT_ORIGINAL_FILE", "原始报告区"),

    /**
     * 消息列表区。
     */
    MESSAGE_LIST("MESSAGE_LIST", "消息列表区"),

    /**
     * 用药计划列表区。
     */
    PLAN_LIST("PLAN_LIST", "用药计划列表区"),

    /**
     * 今日提醒列表区。
     */
    REMINDER_LIST("REMINDER_LIST", "今日提醒列表区"),

    /**
     * 慢病专项看板区。
     *
     * <p>用于首页任务或消息跳转到慢病专项后，提示 App 优先展示专项看板与趋势摘要。
     */
    CHRONIC_DISEASE_DASHBOARD("CHRONIC_DISEASE_DASHBOARD", "慢病专项看板区");

    private final String value;

    private final String description;

    HealthFollowUpTargetAnchorEnum(String value, String description) {
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
     * 按锚点编码解析枚举。
     */
    public static HealthFollowUpTargetAnchorEnum fromValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (HealthFollowUpTargetAnchorEnum anchorEnum : values()) {
            if (Objects.equals(anchorEnum.getValue(), value)) {
                return anchorEnum;
            }
        }
        return null;
    }
}

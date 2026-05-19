package com.healthtrail.domain.health.report.dto;

import lombok.Data;

/**
 * 体检报告单条建议返回对象。
 *
 * <p>每条建议尽量做到“可解释、可执行、可落地”：
 * 1. 说明建议来自哪个异常项
 * 2. 给出当前阶段建议关注什么
 * 3. 给出 App 端下一步可以引导用户去做什么动作
 */
@Data
public class HealthReportAdviceItemDTO {

    /**
     * 建议类型。
     * 当前一期主要有：复查、观察、用药联动、定期管理。
     */
    private String adviceType;

    /**
     * 建议标题。
     */
    private String title;

    /**
     * 建议内容。
     */
    private String content;

    /**
     * 建议动作提示。
     * 例如：查看现有用药提醒、安排复查、持续观察等。
     */
    private String actionText;

    /**
     * 关联的指标结果ID。
     */
    private Long sourceItemId;

    /**
     * 来源指标名称。
     */
    private String sourceItemName;

    /**
     * 来源指标结果值。
     */
    private String sourceResultValue;

    /**
     * 来源指标异常标记。
     */
    private Integer abnormalFlag;

    /**
     * 来源指标异常标记名称。
     */
    private String abnormalFlagName;

    /**
     * 建议优先级，数值越小越优先展示。
     */
    private Integer priority;
}

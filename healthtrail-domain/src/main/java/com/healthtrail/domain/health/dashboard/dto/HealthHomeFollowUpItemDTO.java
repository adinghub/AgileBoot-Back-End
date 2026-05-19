package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import lombok.Data;

/**
 * 首页待跟进事项 DTO。
 *
 * <p>首页待跟进事项的定位是“任务流”，
 * 它不关心数据原本来自哪个模块，而是把用户现在最需要处理的事情统一收拢成一组待办。
 *
 * <p>当前阶段主要汇总三类任务：
 * 1. 今日仍未处理的用药提醒
 * 2. 最近异常报告对应的后续跟进建议
 * 3. 慢病复查、健康问题复查等运营任务
 */
@Data
public class HealthHomeFollowUpItemDTO {

    /**
     * 任务类型。
     * 当前一期主要有：REMINDER、REPORT_ADVICE、OPERATION。
     */
    private String taskType;

    /**
     * 任务标题。
     */
    private String title;

    /**
     * 任务摘要。
     */
    private String content;

    /**
     * 前端动作提示。
     */
    private String actionText;

    /**
     * 关联成员ID。
     */
    private Long memberId;

    /**
     * 关联成员业务编码。
     */
    private String memberCode;

    /**
     * 关联成员名称。
     */
    private String memberName;

    /**
     * 关联提醒ID。
     * 如果该任务来自提醒，则回填该字段。
     */
    private Long reminderId;

    /**
     * 关联报告ID。
     * 如果该任务来自异常报告，则回填该字段。
     */
    private Long reportId;

    /**
     * 关联运营任务ID。
     * 如果该任务来自慢病复查或健康问题复查等运营任务，则回填该字段。
     */
    private Long operationTaskId;

    /**
     * 排序优先级，数值越小越靠前。
     */
    private Integer priority;

    /**
     * 首页个性化排序权重。
     * 数值越大，越应该优先展示。
     */
    private Integer sortWeight;

    /**
     * 建议跟进时间。
     * 对提醒任务来说通常就是计划提醒时间；
     * 对报告任务来说通常取报告日期或创建时间做近似排序。
     */
    private Date followUpTime;

    /**
     * 已读状态。
     * 0 未读，1 已读。
     */
    private Integer readStatus;

    /**
     * 已读时间。
     */
    private Date readTime;

    /**
     * 个性化排序说明。
     */
    private String sortReason;

    /**
     * 原始业务页跳转参数。
     *
     * <p>首页点击任务卡片时，前端可优先使用该对象做统一跳转。
     */
    private HealthFollowUpNavigationDTO navigation;
}

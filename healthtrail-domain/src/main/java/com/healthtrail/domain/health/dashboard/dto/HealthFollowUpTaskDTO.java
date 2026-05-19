package com.healthtrail.domain.health.dashboard.dto;

import java.util.Date;
import lombok.Data;

/**
 * 首页任务中心任务 DTO。
 *
 * <p>任务中心和首页任务流不同：
 * 1. 首页任务流强调“现在最需要做什么”
 * 2. 任务中心强调“这些任务目前处于什么状态、是否还能恢复”
 *
 * <p>因此这里返回更完整的任务记录信息，方便前端做列表页展示。
 */
@Data
public class HealthFollowUpTaskDTO {

    /** 任务ID */
    private Long taskId;

    /** 任务类型 */
    private String taskType;

    /** 任务类型名称 */
    private String taskTypeName;

    /** 来源业务ID */
    private Long sourceId;

    /** 任务状态 */
    private Integer taskStatus;

    /** 任务状态名称 */
    private String taskStatusName;

    /** 成员ID。 */
    private Long memberId;

    /**
     * 关联成员业务编码。
     */
    private String memberCode;

    /** 成员姓名。 */
    private String memberName;

    /**
     * 任务标题。
     * 通常来自原始提醒或原始报告。
     */
    private String title;

    /**
     * 任务摘要。
     */
    private String content;

    /**
     * 操作备注。
     */
    private String actionRemark;

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
     * 延后到期时间。
     */
    private Date delayedUntil;

    /**
     * 完成时间。
     */
    private Date completeTime;

    /**
     * 原始业务时间。
     * 对提醒来说是提醒时间，对报告来说是报告日期/创建时间。
     */
    private Date sourceTime;

    /**
     * 当前是否允许恢复。
     */
    private Boolean canRestore;

    /**
     * 个性化排序说明。
     *
     * <p>任务中心和首页任务流都会对用户解释“为什么这条任务会排在前面”，
     * 方便产品、测试和前端联调时快速核对排序策略是否符合预期。
     */
    private String sortReason;

    /**
     * 原始业务页跳转参数。
     */
    private HealthFollowUpNavigationDTO navigation;
}

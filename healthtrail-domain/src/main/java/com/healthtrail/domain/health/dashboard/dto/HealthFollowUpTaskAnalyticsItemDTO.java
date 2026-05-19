package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页任务行为分析中的“按任务类型统计”明细。
 *
 * <p>首页和任务中心都不需要拿到完整原始任务列表再自己分组统计，
 * 因此服务端直接把每种任务类型的核心分布提前汇总好。
 */
@Data
public class HealthFollowUpTaskAnalyticsItemDTO {

    /** 任务类型 */
    private String taskType;

    /** 任务类型名称 */
    private String taskTypeName;

    /** 任务数量 */
    private Integer taskCount;

    /** 未读数量 */
    private Integer unreadCount;

    /** 待跟进数量 */
    private Integer pendingCount;

    /** 已延后数量 */
    private Integer delayedCount;

    /** 已完成数量 */
    private Integer completedCount;

    /** 已忽略数量 */
    private Integer ignoredCount;
}

package com.healthtrail.domain.health.dashboard.dto;

import java.util.List;
import lombok.Data;

/**
 * 首页任务行为分析 DTO。
 *
 * <p>该对象服务两个场景：
 * 1. 首页只展示精简版分析卡片
 * 2. 任务中心展示更完整的行为统计和趋势
 *
 * <p>因此这里采用“总览 + 分布 + 趋势”组合结构，
 * 既能支持首屏摘要，也能支持任务中心进一步展开。
 */
@Data
public class HealthFollowUpTaskAnalyticsDTO {

    /**
     * 当前任务记录总数。
     */
    private Integer totalTaskCount;

    /** 未读任务数量 */
    private Integer unreadTaskCount;

    /** 待跟进任务数量 */
    private Integer pendingTaskCount;

    /** 已延后任务数量 */
    private Integer delayedTaskCount;

    /** 已完成任务数量 */
    private Integer completedTaskCount;

    /** 已忽略任务数量 */
    private Integer ignoredTaskCount;

    /**
     * 已读行为次数。
     */
    private Integer readActionCount;

    /** 延后行为次数 */
    private Integer delayActionCount;

    /** 完成行为次数 */
    private Integer completeActionCount;

    /** 忽略行为次数 */
    private Integer ignoreActionCount;

    /** 恢复行为次数 */
    private Integer restoreActionCount;

    /**
     * 已读率。
     *
     * <p>口径：已读任务数 / 全部任务数。
     */
    private Double readRate;

    /**
     * 完成率。
     *
     * <p>口径：已完成任务数 / 全部任务数。
     */
    private Double completionRate;

    /**
     * 最近趋势覆盖天数。
     */
    private Integer recentDays;

    /**
     * 按任务类型聚合统计。
     */
    private List<HealthFollowUpTaskAnalyticsItemDTO> taskTypeStats;

    /**
     * 最近若干天行为趋势。
     */
    private List<HealthFollowUpTaskActionTrendPointDTO> recentActionTrend;
}

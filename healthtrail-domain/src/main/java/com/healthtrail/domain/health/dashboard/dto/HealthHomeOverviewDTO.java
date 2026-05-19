package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页健康看板顶部总览 DTO。
 *
 * <p>该对象只保留首页最核心、最稳定的统计口径，
 * 避免前端因为口径频繁变动而反复调整展示逻辑。
 */
@Data
public class HealthHomeOverviewDTO {

    /**
     * 家庭成员数量。
     */
    private Integer memberCount;

    /**
     * 体检报告数量。
     */
    private Integer reportCount;

    /**
     * 今日提醒总数。
     */
    private Integer todayReminderCount;

    /**
     * 今日待处理提醒数量。
     */
    private Integer todayPendingReminderCount;

    /**
     * 今日已服药提醒数量。
     */
    private Integer todayTakenReminderCount;

    /**
     * 今日已跳过提醒数量。
     */
    private Integer todaySkippedReminderCount;

    /**
     * 今日已过期提醒数量。
     */
    private Integer todayExpiredReminderCount;

    /**
     * 首页待跟进事项数量。
     *
     * <p>这里的口径不是全量历史任务，而是当前首页返回的聚合任务总数，
     * 主要方便前端在首页顶部直接显示一个“待跟进”数字角标。
     */
    private Integer followUpCount;

    /**
     * 未读消息数量。
     *
     * <p>该字段主要给首页消息中心入口做数字角标，
     * 让用户在首页就能感知是否有新的系统消息尚未查看。
     */
    private Long unreadMessageCount;
}

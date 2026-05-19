package com.healthtrail.domain.health.message.dto;

import lombok.Data;

/**
 * 后台消息统计总览 DTO。
 *
 * <p>该对象承接后台统计卡片区最核心、最稳定的指标，
 * 方便前端直接渲染顶部概览卡片。
 */
@Data
public class HealthAppMessageStatisticsOverviewDTO {

    /**
     * 消息总量。
     */
    private Long totalMessageCount;

    /**
     * 未读消息量。
     */
    private Long unreadMessageCount;

    /**
     * 发送成功消息量。
     */
    private Long sendSuccessCount;

    /**
     * 发送失败消息量。
     */
    private Long sendFailedCount;

    /**
     * 待发送消息量。
     */
    private Long pendingSendCount;

    /**
     * 涉及的 App 用户数量。
     */
    private Long ownerUserCount;

    /**
     * 今日新增消息量。
     */
    private Long todayCreatedCount;

    /**
     * 今日失败消息量。
     */
    private Long todayFailedCount;
}

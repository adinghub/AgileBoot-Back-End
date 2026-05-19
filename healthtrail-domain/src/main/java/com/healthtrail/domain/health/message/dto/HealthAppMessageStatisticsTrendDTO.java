package com.healthtrail.domain.health.message.dto;

import lombok.Data;

/**
 * 后台消息统计趋势 DTO。
 *
 * <p>当前用于表达按天聚合的消息趋势，
 * 方便后台直接接折线图或堆叠柱状图。
 */
@Data
public class HealthAppMessageStatisticsTrendDTO {

    /**
     * 统计日期，格式 yyyy-MM-dd。
     */
    private String statDate;

    /**
     * 当日新增消息量。
     */
    private Long createdCount;

    /**
     * 当日发送成功消息量。
     */
    private Long successCount;

    /**
     * 当日发送失败消息量。
     */
    private Long failedCount;
}

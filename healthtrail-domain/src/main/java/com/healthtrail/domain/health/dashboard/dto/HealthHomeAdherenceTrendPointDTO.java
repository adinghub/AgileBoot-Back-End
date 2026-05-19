package com.healthtrail.domain.health.dashboard.dto;

import lombok.Data;

/**
 * 首页依从率趋势点 DTO。
 *
 * <p>该结构直接服务于首页依从性统计图，
 * 用于表达最近几天的服药完成情况变化。
 */
@Data
public class HealthHomeAdherenceTrendPointDTO {

    /**
     * 统计日期文本，格式建议为 yyyy-MM-dd。
     */
    private String statDate;

    /**
     * 依从率。
     */
    private Double adherenceRate;

    /**
     * 已服药数量。
     */
    private Integer takenReminderCount;

    /**
     * 已跳过数量。
     */
    private Integer skippedReminderCount;

    /**
     * 已过期数量。
     */
    private Integer expiredReminderCount;
}

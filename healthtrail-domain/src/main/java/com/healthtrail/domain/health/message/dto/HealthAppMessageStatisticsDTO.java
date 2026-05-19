package com.healthtrail.domain.health.message.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台消息统计看板 DTO。
 *
 * <p>该对象是后台消息统计页的一次性聚合返回：
 * 1. 顶部总览卡片
 * 2. 场景分布
 * 3. 发送状态分布
 * 4. 最近趋势
 * 5. 失败原因排行
 */
@Data
public class HealthAppMessageStatisticsDTO {

    /**
     * 顶部总览统计。
     */
    private HealthAppMessageStatisticsOverviewDTO overview;

    /**
     * 业务场景分布。
     */
    private List<HealthAppMessageStatisticsBucketDTO> sceneDistributions;

    /**
     * 发送状态分布。
     */
    private List<HealthAppMessageStatisticsBucketDTO> sendStatusDistributions;

    /**
     * 最近趋势。
     */
    private List<HealthAppMessageStatisticsTrendDTO> recentTrends;

    /**
     * 失败原因排行。
     *
     * <p>该列表主要给后台快速定位“为什么最近发不出去”，
     * 适合直接渲染成 Top 排行表或横向柱状图。
     */
    private List<HealthAppMessageStatisticsBucketDTO> failureReasonDistributions;
}

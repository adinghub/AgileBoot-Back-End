package com.healthtrail.domain.health.push.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台 App 设备统计 DTO。
 */
@Data
public class HealthAppPushDeviceStatisticsDTO {

    /**
     * 当前启用设备数。
     */
    private Long enabledDeviceCount;

    /**
     * 今日活跃设备数。
     */
    private Long todayActiveDeviceCount;

    /**
     * 近 7 天活跃设备数。
     */
    private Long recent7DayActiveDeviceCount;

    /**
     * 近 30 天未活跃设备数。
     */
    private Long stale30DayDeviceCount;

    /**
     * 已停用设备数。
     */
    private Long disabledDeviceCount;

    /**
     * 平台分布。
     */
    private List<HealthAppPushStatisticsBucketDTO> platformDistributions;
}

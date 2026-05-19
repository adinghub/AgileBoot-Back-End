package com.healthtrail.domain.health.push.dto;

import lombok.Data;

/**
 * App Push 平台维度发送统计 DTO。
 *
 * <p>后台排查时经常需要快速回答：
 * 1. 是不是某个平台整体异常；
 * 2. 某个平台最近失败是否明显升高；
 *
 * <p>因此这里单独保留成功数、失败数和总数，方便前端自行计算成功率。
 */
@Data
public class HealthAppPushDeliveryPlatformStatisticsDTO {

    /**
     * 推送平台编码。
     */
    private String pushPlatform;

    /**
     * 推送平台名称。
     */
    private String pushPlatformName;

    /**
     * 设备级发送成功数量。
     */
    private Long successCount;

    /**
     * 设备级发送失败数量。
     */
    private Long failedCount;

    /**
     * 总发送数量。
     */
    private Long totalCount;
}

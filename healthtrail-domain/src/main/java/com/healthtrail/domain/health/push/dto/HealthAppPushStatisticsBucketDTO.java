package com.healthtrail.domain.health.push.dto;

import lombok.Data;

/**
 * App Push 后台统计分布桶 DTO。
 *
 * <p>设备平台分布、失败原因分布等简单统计都可以复用这个结构，
 * 保持前端图表接口形态统一。
 */
@Data
public class HealthAppPushStatisticsBucketDTO {

    /**
     * 分布编码。
     */
    private String code;

    /**
     * 分布名称。
     */
    private String name;

    /**
     * 数量。
     */
    private Long count;
}

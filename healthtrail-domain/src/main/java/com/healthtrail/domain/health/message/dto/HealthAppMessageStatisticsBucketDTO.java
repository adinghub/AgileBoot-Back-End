package com.healthtrail.domain.health.message.dto;

import lombok.Data;

/**
 * 后台消息统计分布桶 DTO。
 *
 * <p>该对象适用于场景分布、发送状态分布等简单柱状图或饼图结构。
 */
@Data
public class HealthAppMessageStatisticsBucketDTO {

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

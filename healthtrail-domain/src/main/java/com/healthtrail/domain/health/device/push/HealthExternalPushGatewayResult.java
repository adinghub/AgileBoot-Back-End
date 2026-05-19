package com.healthtrail.domain.health.device.push;

import lombok.Builder;
import lombok.Data;

/**
 * 外部 Push 网关调用结果。
 */
@Data
@Builder
public class HealthExternalPushGatewayResult {

    /**
     * 是否成功。
     */
    private boolean success;

    /**
     * 返回通道。
     */
    private String channel;

    /**
     * 返回说明。
     */
    private String message;

    /**
     * 厂商或网关返回的结果码。
     *
     * <p>当前不同网关的结构可能不完全一致，
     * 因此这里统一收敛成字符串，后续审计表直接原样保存。
     */
    private String vendorCode;

    /**
     * 厂商或网关返回的原始说明。
     */
    private String vendorMessage;
}

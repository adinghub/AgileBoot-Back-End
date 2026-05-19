package com.healthtrail.domain.health.message.dto;

import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台 App 消息审计详情 DTO。
 *
 * <p>详情页在列表基础上继续补充：
 * 1. 原始 payload JSON
 * 2. 解析后的标准业务载荷
 *
 * <p>这样后台既能看原始落库值，
 * 也能看结构化后的业务语义。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HealthAppMessageAdminDetailDTO extends HealthAppMessageAdminDTO {

    /**
     * 原始业务透传载荷 JSON。
     */
    private String payloadJson;

    /**
     * 解析后的业务透传载荷。
     */
    private HealthAppPushPayloadDTO payload;
}

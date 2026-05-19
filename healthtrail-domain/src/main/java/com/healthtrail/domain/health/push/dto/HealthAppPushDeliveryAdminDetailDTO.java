package com.healthtrail.domain.health.push.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台 App Push 设备级派发审计详情 DTO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HealthAppPushDeliveryAdminDetailDTO extends HealthAppPushDeliveryAdminDTO {

    /**
     * 消息标题快照。
     */
    private String messageTitle;

    /**
     * 消息正文快照。
     */
    private String messageContent;

    /**
     * 业务透传载荷 JSON。
     */
    private String payloadJson;

    /**
     * 同一消息同一重试批次下，其他设备的发送结果。
     */
    private List<HealthAppPushDeliveryAdminDTO> sameBatchDeliveries;
}

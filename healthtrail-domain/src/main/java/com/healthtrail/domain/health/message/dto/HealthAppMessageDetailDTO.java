package com.healthtrail.domain.health.message.dto;

import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App 消息详情 DTO。
 *
 * <p>详情页在列表基础上继续返回完整业务载荷，
 * 方便前端需要时展示更完整的透传信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HealthAppMessageDetailDTO extends HealthAppMessageDTO {

    /**
     * 完整业务透传载荷。
     */
    private HealthAppPushPayloadDTO payload;
}

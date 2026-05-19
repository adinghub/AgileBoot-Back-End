package com.healthtrail.domain.health.message.dto;

import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import lombok.Builder;
import lombok.Data;

/**
 * App 消息创建请求。
 *
 * <p>该对象是消息中心内部使用的标准入参，
 * 用于把不同业务模块发来的通知意图收敛成同一种消息记录。
 */
@Data
@Builder
public class HealthAppMessageCreateRequest {

    /**
     * 归属 App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 家庭成员名称快照。
     */
    private String memberName;

    /**
     * 业务场景编码。
     */
    private String businessScene;

    /**
     * 业务主键ID。
     */
    private Long businessId;

    /**
     * 消息标题。
     */
    private String messageTitle;

    /**
     * 消息正文。
     */
    private String messageContent;

    /**
     * 统一业务透传载荷。
     */
    private HealthAppPushPayloadDTO payload;

    /**
     * 消息去重键。
     *
     * <p>同一用户下如果该值相同，
     * 则认为是同一条业务消息，不重复创建消息记录。
     */
    private String dedupKey;
}

package com.healthtrail.domain.health.message.notify;

import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import lombok.Builder;
import lombok.Data;

/**
 * App 消息重发通知载荷。
 *
 * <p>该对象用于承接“后台人工重发”场景下真正交给发送器的数据。
 * 它不直接暴露数据库实体，而是只保留发送过程关心的关键字段：
 * 1. 发给谁
 * 2. 发什么标题正文
 * 3. 携带什么业务透传载荷
 *
 * <p>这样后续如果需要把人工重发切换到真实厂商 Push SDK，
 * 发送器仍然只需要消费这一份统一结构。
 */
@Data
@Builder
public class HealthAppMessageNotice {

    /**
     * 消息ID。
     */
    private Long messageId;

    /**
     * App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     *
     * <p>后台在设备级推送审计中仍然希望知道消息关联的是哪位家庭成员，
     * 因此这里把成员ID一并带到发送器侧。
     */
    private Long memberId;

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
}

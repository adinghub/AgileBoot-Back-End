package com.healthtrail.domain.health.medication.notify;

import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * 用药提醒通知载荷。
 *
 * <p>该对象用于承接“真正发给通知通道的数据”，
 * 它刻意不直接暴露数据库实体，而是收敛为发送场景关心的字段，
 * 这样后续替换为 App Push、短信、公众号模板消息时，发送器可以直接消费这份标准载荷。
 */
@Data
@Builder
public class MedicationReminderNotice {

    /**
     * 关联的消息中心消息ID。
     *
     * <p>提醒业务真正发 Push 之前会先在消息中心落一条消息，
     * 这里把消息ID带上，是为了让设备级派发审计能和消息整体审计打通。
     */
    private Long messageId;

    /**
     * 提醒记录ID。
     */
    private Long reminderId;

    /**
     * App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员ID。
     */
    private Long memberId;

    /**
     * 家庭成员姓名。
     */
    private String memberName;

    /**
     * 药品名称快照。
     */
    private String drugName;

    /**
     * 计划提醒时间。
     */
    private Date scheduledTime;

    /**
     * 服药剂量。
     */
    private BigDecimal doseAmount;

    /**
     * 剂量单位。
     */
    private String doseUnit;

    /**
     * 服药时机。
     */
    private String mealTiming;

    /**
     * 推送业务透传载荷。
     *
     * <p>这里复用首页任务体系中的跳转参数和推荐动作结构，
     * 让 App 从通知入口进入时，能和首页任务、任务详情保持同一套业务语义。
     */
    private HealthAppPushPayloadDTO pushPayload;
}

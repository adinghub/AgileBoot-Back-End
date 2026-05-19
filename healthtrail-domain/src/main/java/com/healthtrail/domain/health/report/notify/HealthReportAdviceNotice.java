package com.healthtrail.domain.health.report.notify;

import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import lombok.Builder;
import lombok.Data;

/**
 * 报告建议通知载荷。
 *
 * <p>该对象用于承接“真正发给通知通道的报告建议业务数据”，
 * 它与具体厂商 Push SDK 无关，只保留发送场景真正关心的字段。
 *
 * <p>这样后续无论接：
 * 1. 设备 Push
 * 2. 站内信
 * 3. 消息中心
 *
 * <p>都可以直接复用这份标准载荷。
 */
@Data
@Builder
public class HealthReportAdviceNotice {

    /**
     * 关联的消息中心消息ID。
     */
    private Long messageId;

    /**
     * 报告ID。
     */
    private Long reportId;

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
     * 报告名称。
     */
    private String reportName;

    /**
     * 建议摘要。
     */
    private String adviceSummary;

    /**
     * 推送业务透传载荷。
     *
     * <p>这里直接复用首页任务体系中的跳转参数和推荐动作结构，
     * 保证 App 从通知入口进入和从首页任务入口进入时看到的是同一套业务语义。
     */
    private HealthAppPushPayloadDTO pushPayload;
}

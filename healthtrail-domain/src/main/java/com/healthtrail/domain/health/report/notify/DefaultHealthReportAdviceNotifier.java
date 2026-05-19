package com.healthtrail.domain.health.report.notify;

import com.healthtrail.common.utils.jackson.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认报告建议通知发送器。
 *
 * <p>当前仓库还没有真正接入设备 Push 或消息中心时，
 * 这里先用日志模拟发送：
 * 1. 让报告建议通知链路先完整跑通
 * 2. 不阻塞后续真实发送能力接入
 * 3. 后续若接入真实发送器，可通过新增 `@Primary` 实现覆盖
 */
@Slf4j
@Component
public class DefaultHealthReportAdviceNotifier implements HealthReportAdviceNotifier {

    /**
     * 当前默认通过日志模拟发送成功。
     */
    @Override
    public HealthReportAdviceNotifyResult notify(HealthReportAdviceNotice notice) {
        log.info("模拟发送报告建议通知成功，报告ID：{}，用户ID：{}，成员：{}，报告名称：{}，透传载荷：{}",
            notice.getReportId(), notice.getOwnerUserId(), notice.getMemberName(), notice.getReportName(),
            notice.getPushPayload() == null ? "{}" : JacksonUtil.to(notice.getPushPayload()));
        return HealthReportAdviceNotifyResult.builder()
            .success(true)
            .channel("LOG_SIMULATION")
            .message("默认日志发送器已接收该报告建议通知")
            .build();
    }
}

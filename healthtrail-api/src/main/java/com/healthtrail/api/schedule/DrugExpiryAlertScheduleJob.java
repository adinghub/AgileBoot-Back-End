package com.healthtrail.api.schedule;

import com.healthtrail.domain.health.drug.DrugApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 药品近效期提醒定时任务。
 *
 * <p>该任务放在 `healthtrail-api` 启动端中，
 * 避免后台管理端和 App 服务端同时执行巡检，造成重复提醒。
 *
 * <p>本次需求明确要求“只提醒，不响铃”，
 * 因此调度只负责批次巡检和站内消息落库，不调用设备 Push。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrugExpiryAlertScheduleJob {

    /**
     * 默认近效期提醒窗口为 7 天。
     */
    private static final int DEFAULT_ALERT_DAYS = 7;

    private final DrugApplicationService drugApplicationService;

    /**
     * 近效期提醒检查天数。
     */
    @Value("${health.drug.expire-alert.schedule.alert-days:7}")
    private Integer alertDays;

    /**
     * 每日近效期巡检任务。
     *
     * <p>默认每天上午 9 点执行一次即可满足近效期提醒的业务节奏，
     * 不需要像用药提醒那样分钟级轮询。
     */
    @Scheduled(cron = "${health.drug.expire-alert.schedule.check-cron:0 0 9 * * ?}")
    public void inspectNearExpiryBatches() {
        int safeAlertDays = alertDays == null || alertDays <= 0 ? DEFAULT_ALERT_DAYS : alertDays;
        int createdAlertCount = drugApplicationService.inspectAndDispatchNearExpiryAlerts(safeAlertDays);
        if (createdAlertCount > 0) {
            log.info("药品近效期提醒巡检完成，窗口：{}天，本次新增提醒数量：{}", safeAlertDays, createdAlertCount);
        }
    }
}

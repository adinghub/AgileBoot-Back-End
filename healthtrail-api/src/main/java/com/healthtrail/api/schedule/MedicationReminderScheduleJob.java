package com.healthtrail.api.schedule;

import com.healthtrail.domain.health.medication.MedicationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 用药提醒定时任务。
 *
 * <p>这个任务放在 `healthtrail-api` 模块而不是公共模块里，目的是让它只跟随 App 服务实例启动。
 * 当前项目同时存在 `api` 和 `admin` 两个启动端，如果把定时任务放在公共模块中，
 * 两边进程都会执行同一批任务，容易造成提醒被重复补齐。
 *
 * <p>当前一期定时任务只负责两件事：
 * 1. 自动把已经过点但未处理的提醒标记为已过期
 * 2. 自动补齐未来窗口期内缺失的提醒记录
 *
 * <p>这样后续接入 App Push、站内消息、短信提醒时，可以直接消费稳定的提醒记录表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicationReminderScheduleJob {

    /**
     * 默认补齐未来 7 天提醒。
     * 这个窗口既能覆盖近期提醒，又不会把生成量放得过大。
     */
    private static final int DEFAULT_GENERATE_WINDOW_DAYS = 7;

    /**
     * 默认每次最多派发 100 条提醒，避免单次任务拉太长。
     */
    private static final int DEFAULT_DISPATCH_BATCH_SIZE = 100;

    /**
     * 默认失败最多重试 3 次。
     */
    private static final int DEFAULT_NOTIFY_MAX_RETRY_COUNT = 3;

    private final MedicationApplicationService medicationApplicationService;

    /**
     * 未来提醒补齐天数，允许通过配置覆盖。
     */
    @Value("${health.medication.reminder.schedule.generate-window-days:7}")
    private Integer generateWindowDays;

    /**
     * 单次派发批大小。
     */
    @Value("${health.medication.reminder.schedule.dispatch-batch-size:100}")
    private Integer dispatchBatchSize;

    /**
     * 最大重试次数。
     */
    @Value("${health.medication.reminder.schedule.notify-max-retry-count:3}")
    private Integer notifyMaxRetryCount;

    /**
     * 自动过期任务。
     *
     * <p>默认每 5 分钟执行一次，把过点未处理的提醒统一改成“已过期”。
     */
    @Scheduled(cron = "${health.medication.reminder.schedule.expire-cron:0 */5 * * * ?}")
    public void expireOverdueReminders() {
        int expiredCount = medicationApplicationService.expireOverdueReminders();
        if (expiredCount > 0) {
            log.info("用药提醒自动过期任务执行完成，本次过期提醒数量：{}", expiredCount);
        }
    }

    /**
     * 自动补齐任务。
     *
     * <p>默认每 6 小时执行一次，补齐未来窗口期内缺失的提醒记录。
     * 这里用“补齐”而不是“重建”，是为了尽可能不影响已存在的提醒状态。
     */
    @Scheduled(cron = "${health.medication.reminder.schedule.generate-cron:0 10 0/6 * * ?}")
    public void supplementUpcomingReminders() {
        int safeGenerateWindowDays = generateWindowDays == null || generateWindowDays <= 0
            ? DEFAULT_GENERATE_WINDOW_DAYS : generateWindowDays;
        int createdCount = medicationApplicationService.supplementUpcomingReminders(safeGenerateWindowDays);
        if (createdCount > 0) {
            log.info("用药提醒自动补齐任务执行完成，补齐窗口：{}天，本次新增提醒数量：{}",
                safeGenerateWindowDays, createdCount);
        }
    }

    /**
     * 到点提醒派发任务。
     *
     * <p>默认每分钟执行一次，把已经到点但尚未发送成功的提醒交给发送器处理。
     * 当前默认发送器是日志模拟实现，后续替换真实 Push 通道后，这里不需要再改业务调度逻辑。
     */
    @Scheduled(cron = "${health.medication.reminder.schedule.dispatch-cron:0 * * * * ?}")
    public void dispatchDueReminders() {
        int safeBatchSize = dispatchBatchSize == null || dispatchBatchSize <= 0
            ? DEFAULT_DISPATCH_BATCH_SIZE : dispatchBatchSize;
        int safeNotifyMaxRetryCount = notifyMaxRetryCount == null || notifyMaxRetryCount <= 0
            ? DEFAULT_NOTIFY_MAX_RETRY_COUNT : notifyMaxRetryCount;
        int dispatchedCount = medicationApplicationService.dispatchDueReminders(safeBatchSize, safeNotifyMaxRetryCount);
        if (dispatchedCount > 0) {
            log.info("用药提醒派发任务执行完成，批大小：{}，最大重试次数：{}，本次成功派发数量：{}",
                safeBatchSize, safeNotifyMaxRetryCount, dispatchedCount);
        }
    }
}

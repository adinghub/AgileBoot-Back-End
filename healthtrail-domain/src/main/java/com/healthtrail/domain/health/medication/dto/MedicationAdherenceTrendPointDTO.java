package com.healthtrail.domain.health.medication.dto;

import java.util.Date;
import lombok.Data;

/**
 * 用药依从率趋势点返回对象。
 */
@Data
public class MedicationAdherenceTrendPointDTO {

    /** 提醒日期 */
    private Date reminderDate;

    /** 计划提醒总数 */
    private Integer scheduledReminderCount;

    /** 已服药提醒数量 */
    private Integer takenReminderCount;

    /** 已跳过提醒数量 */
    private Integer skippedReminderCount;

    /** 已过期提醒数量 */
    private Integer expiredReminderCount;

    /** 待处理提醒数量 */
    private Integer pendingReminderCount;

    /** 依从率 */
    private Double adherenceRate;
}

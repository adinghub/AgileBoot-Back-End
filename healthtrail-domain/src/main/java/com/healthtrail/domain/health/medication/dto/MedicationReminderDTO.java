package com.healthtrail.domain.health.medication.dto;

import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用药提醒返回对象。
 */
@Data
@NoArgsConstructor
public class MedicationReminderDTO {

    /** 提醒ID。 */
    private Long reminderId;

    /** 计划ID。 */
    private Long planId;

    /**
     * 计划业务编码。
     */
    private String planCode;

    /** 成员ID。 */
    private Long memberId;

    /**
     * 成员业务编码。
     */
    private String memberCode;

    /** 提醒日期。 */
    private Date reminderDate;

    /** 计划提醒时间。 */
    private Date scheduledTime;

    /** 药品名称。 */
    private String drugName;

    /** 剂量。 */
    private BigDecimal doseAmount;

    /** 剂量单位。 */
    private String doseUnit;

    /** 餐前餐后。 */
    private String mealTiming;

    /** 提醒状态。 */
    private Integer reminderStatus;

    /**
     * 消息发送状态。
     */
    private Integer notifyStatus;

    /**
     * 最近一次发送时间。
     */
    private Date notifyTime;

    /** 反馈时间。 */
    private Date feedbackTime;

    /** 跳过原因。 */
    private String skipReason;

    public MedicationReminderDTO(HealthMedicationReminderEntity entity) {
        if (entity != null) {
            this.reminderId = entity.getReminderId();
            this.planId = entity.getPlanId();
            this.memberId = entity.getMemberId();
            this.reminderDate = entity.getReminderDate();
            this.scheduledTime = entity.getScheduledTime();
            this.drugName = entity.getDrugNameSnapshot();
            this.doseAmount = entity.getDoseAmount();
            this.doseUnit = entity.getDoseUnit();
            this.mealTiming = HealthAppI18n.mealTimingName(entity.getMealTiming());
            this.reminderStatus = entity.getReminderStatus();
            this.notifyStatus = entity.getNotifyStatus();
            this.notifyTime = entity.getNotifyTime();
            this.feedbackTime = entity.getFeedbackTime();
            this.skipReason = entity.getSkipReason();
        }
    }
}

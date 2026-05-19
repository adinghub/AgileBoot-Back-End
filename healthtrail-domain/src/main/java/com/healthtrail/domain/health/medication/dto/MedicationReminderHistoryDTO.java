package com.healthtrail.domain.health.medication.dto;

import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 历史服药记录返回对象。
 *
 * <p>它和“今日提醒”DTO 的区别在于：
 * 1. 历史页更强调成员维度、时间倒序和复盘字段
 * 2. 需要同时服务记录页和依从率统计明细弹层
 */
@Data
@NoArgsConstructor
public class MedicationReminderHistoryDTO {

    /** 提醒ID。 */
    private Long reminderId;

    /** 用药计划ID。 */
    private Long planId;

    /**
     * 用药计划业务编码。
     */
    private String planCode;

    /** 成员ID。 */
    private Long memberId;

    /**
     * 家庭成员业务编码。
     */
    private String memberCode;

    /** 成员姓名。 */
    private String memberName;

    /** 提醒日期 */
    private Date reminderDate;

    /** 计划提醒时间。 */
    private Date scheduledTime;

    /** 药品名称。 */
    private String drugName;

    /** 剂量。 */
    private BigDecimal doseAmount;

    /** 剂量单位。 */
    private String doseUnit;

    /** 就餐时间类型 */
    private String mealTiming;

    /** 提醒状态 */
    private Integer reminderStatus;

    /** 消息发送状态 */
    private Integer notifyStatus;

    /** 反馈时间 */
    private Date feedbackTime;

    /** 跳过原因。 */
    private String skipReason;

    public MedicationReminderHistoryDTO(HealthMedicationReminderEntity entity) {
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
            this.feedbackTime = entity.getFeedbackTime();
            this.skipReason = entity.getSkipReason();
        }
    }
}

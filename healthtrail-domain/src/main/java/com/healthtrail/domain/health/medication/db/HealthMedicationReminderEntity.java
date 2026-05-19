package com.healthtrail.domain.health.medication.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 用药提醒记录表，记录每一次具体的用药提醒实例及其反馈状态
 *
 * <p>它表示某一个具体时间点的一次提醒实例，
 * 会随着用户反馈转变为已服药、已跳过或已过期状态。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("medication_reminder")
@ApiModel(value = "HealthMedicationReminderEntity对象", description = "健康系统用药提醒记录表")
public class HealthMedicationReminderEntity extends BaseEntity<HealthMedicationReminderEntity> {

    private static final long serialVersionUID = 1L;

    /** 提醒记录主键ID */
    @ApiModelProperty("提醒记录ID")
    @TableId(value = "reminder_id", type = IdType.AUTO)
    private Long reminderId;

    /** 提醒归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的用药计划ID */
    @ApiModelProperty("用药计划ID")
    @TableField("plan_id")
    private Long planId;

    /** 用药的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 提醒所属日期 */
    @ApiModelProperty("提醒日期")
    @TableField("reminder_date")
    private Date reminderDate;

    /** 计划提醒的具体时间 */
    @ApiModelProperty("计划提醒时间")
    @TableField("scheduled_time")
    private Date scheduledTime;

    /** 生成提醒时的药品名称快照 */
    @ApiModelProperty("药品名称快照")
    @TableField("drug_name_snapshot")
    private String drugNameSnapshot;

    /** 本次服药的剂量 */
    @ApiModelProperty("每次剂量")
    @TableField("dose_amount")
    private BigDecimal doseAmount;

    /** 剂量单位快照 */
    @ApiModelProperty("剂量单位")
    @TableField("dose_unit")
    private String doseUnit;

    /** 服药时机快照 */
    @ApiModelProperty("服药时机")
    @TableField("meal_timing")
    private String mealTiming;

    /** 提醒状态，如待提醒、已服药、已跳过、已过期 */
    @ApiModelProperty("提醒状态")
    @TableField("reminder_status")
    private Integer reminderStatus;

    /** Push通知发送状态 */
    @ApiModelProperty("发送状态")
    @TableField("notify_status")
    private Integer notifyStatus;

    /** 最近一次Push通知发送时间 */
    @ApiModelProperty("最近一次发送时间")
    @TableField("notify_time")
    private Date notifyTime;

    /** Push通知发送失败的原因 */
    @ApiModelProperty("发送失败原因")
    @TableField("notify_fail_reason")
    private String notifyFailReason;

    /** Push通知发送重试次数 */
    @ApiModelProperty("发送重试次数")
    @TableField("notify_retry_count")
    private Integer notifyRetryCount;

    /** 用户对提醒的反馈时间 */
    @ApiModelProperty("反馈时间")
    @TableField("feedback_time")
    private Date feedbackTime;

    /** 用户跳过服药的原因 */
    @ApiModelProperty("跳过原因")
    @TableField("skip_reason")
    private String skipReason;

    @Override
    public Serializable pkVal() {
        return this.reminderId;
    }
}

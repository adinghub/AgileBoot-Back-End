package com.healthtrail.domain.health.medication.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * 用药计划表，定义家庭成员的长期用药方案及提醒规则
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("medication_plan")
@ApiModel(value = "HealthMedicationPlanEntity对象", description = "健康系统用药计划表")
public class HealthMedicationPlanEntity extends BaseEntity<HealthMedicationPlanEntity> {

    private static final long serialVersionUID = 1L;

    /** 用药计划主键ID */
    @ApiModelProperty("用药计划ID")
    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;

    /** 用药计划业务编码，用于前端展示 */
    @ApiModelProperty("用药计划业务编码，优先给前端展示使用")
    @TableField("plan_code")
    private String planCode;

    /** 计划归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 用药的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 关联的药品ID */
    @ApiModelProperty("药品ID")
    @TableField("drug_id")
    private Long drugId;

    /** 自定义药品名称，当未关联系统药品时使用 */
    @ApiModelProperty("自定义药名")
    @TableField("custom_drug_name")
    private String customDrugName;

    /** 用药计划开始日期 */
    @ApiModelProperty("开始日期")
    @TableField("start_date")
    private Date startDate;

    /**
     * 用药计划结束日期，为空表示长期用药计划。
     *
     * <p>这里显式使用 updateStrategy = FieldStrategy.IGNORED，是为了支持
     * "编辑计划时把结束日期清空，恢复为长期计划" 这种业务动作。
     *
     * <p>如果沿用 MyBatis-Plus 默认的 NOT_NULL 更新策略，当前端传入 null 时，
     * updateById() 会直接跳过 end_date 字段，数据库中旧结束日期会被悄悄保留，
     * 最终表现为"页面看起来已经清空，但保存后又变回原来的结束日期"。
     */
    @ApiModelProperty("结束日期")
    @TableField(value = "end_date", updateStrategy = FieldStrategy.IGNORED)
    private Date endDate;

    /** 每日提醒时间点列表JSON，如["08:00","20:00"] */
    @ApiModelProperty("提醒时间点JSON")
    @TableField("reminder_times_json")
    private String reminderTimesJson;

    /** 服药时机，如饭前、饭后、随餐 */
    @ApiModelProperty("服药时机")
    @TableField("meal_timing")
    private String mealTiming;

    /** 每次服药的剂量 */
    @ApiModelProperty("每次剂量")
    @TableField("dose_amount")
    private BigDecimal doseAmount;

    /** 剂量单位，如片、粒、ml */
    @ApiModelProperty("剂量单位")
    @TableField("dose_unit")
    private String doseUnit;

    /** 用药频率类型，如每天、每周、间隔小时 */
    @ApiModelProperty("频率类型")
    @TableField("frequency_type")
    private String frequencyType;

    /** 每周需用药的星期JSON，如[1,3,5]表示周一三五 */
    @ApiModelProperty("每周提醒星期JSON")
    @TableField("weekly_days_json")
    private String weeklyDaysJson;

    /** 间隔小时数，用于按小时间隔的用药方案 */
    @ApiModelProperty("每几小时提醒一次")
    @TableField("interval_hours")
    private Integer intervalHours;

    /** 间隔天数，用于按天间隔的用药方案 */
    @ApiModelProperty("每几天提醒一次")
    @TableField("interval_days")
    private Integer intervalDays;

    /** 分阶段剂量规则JSON */
    @ApiModelProperty("阶段剂量规则")
    @TableField("dose_rule")
    private String doseRule;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    /** 计划状态，1正常 0停用 */
    @ApiModelProperty("状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    @Override
    public Serializable pkVal() {
        return this.planId;
    }
}

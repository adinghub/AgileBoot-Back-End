package com.healthtrail.domain.health.medication.dto;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用药计划返回对象。
 */
@Data
@NoArgsConstructor
public class MedicationPlanDTO {

    /** 计划ID。 */
    private Long planId;

    /**
     * 用药计划业务编码。
     */
    private String planCode;

    /** 归属用户ID。 */
    private Long ownerUserId;

    /** 成员ID。 */
    private Long memberId;

    /**
     * 家庭成员业务编码。
     *
     * <p>计划详情和提醒详情都需要同时展示成员标识，
     * 因此直接把成员编码带出来，前端不需要再额外二次查询。
     */
    private String memberCode;

    /** 药品ID。 */
    private Long drugId;

    /**
     * 药品业务编码。
     *
     * <p>当计划绑定标准药品时，前端可以直接显示药品编码；
     * 若当前计划使用的是自定义药名，则该字段保持 null。
     */
    private String drugCode;

    /** 自定义药名。 */
    private String customDrugName;

    /** 药品名称。 */
    private String drugName;

    /** 开始日期。 */
    private Date startDate;

    /** 结束日期。 */
    private Date endDate;

    /** 提醒时间。 */
    private List<String> reminderTimes;

    /** 餐前餐后。 */
    private String mealTiming;

    /** 剂量。 */
    private BigDecimal doseAmount;

    /** 剂量单位。 */
    private String doseUnit;

    /** 频率类型。 */
    private String frequencyType;

    /** 每周用药日。 */
    private List<Integer> weeklyDays;

    /** 间隔小时。 */
    private Integer intervalHours;

    /** 间隔天数。 */
    private Integer intervalDays;

    /** 剂量规则。 */
    private String doseRule;

    /** 备注。 */
    private String remark;

    /** 计划状态。 */
    private Integer status;

    public MedicationPlanDTO(HealthMedicationPlanEntity entity) {
        if (entity != null) {
            this.planId = entity.getPlanId();
            this.planCode = entity.getPlanCode();
            this.ownerUserId = entity.getOwnerUserId();
            this.memberId = entity.getMemberId();
            this.drugId = entity.getDrugId();
            this.customDrugName = entity.getCustomDrugName();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.reminderTimes = StrUtil.isBlank(entity.getReminderTimesJson())
                ? Collections.emptyList()
                : JacksonUtil.fromList(entity.getReminderTimesJson(), String.class);
            this.mealTiming = HealthAppI18n.mealTimingName(entity.getMealTiming());
            this.doseAmount = entity.getDoseAmount();
            this.doseUnit = entity.getDoseUnit();
            this.frequencyType = entity.getFrequencyType();
            this.weeklyDays = StrUtil.isBlank(entity.getWeeklyDaysJson())
                ? Collections.emptyList()
                : JacksonUtil.fromList(entity.getWeeklyDaysJson(), Integer.class);
            this.intervalHours = entity.getIntervalHours();
            this.intervalDays = entity.getIntervalDays();
            this.doseRule = entity.getDoseRule();
            this.remark = entity.getRemark();
            this.status = entity.getStatus();
        }
    }
}

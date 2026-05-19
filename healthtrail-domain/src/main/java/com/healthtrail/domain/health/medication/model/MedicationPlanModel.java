package com.healthtrail.domain.health.medication.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.drug.query.DrugQuery;
import com.healthtrail.domain.health.family.model.FamilyMemberModelFactory;
import com.healthtrail.domain.health.medication.command.AddMedicationPlanCommand;
import com.healthtrail.domain.health.medication.command.UpdateMedicationPlanCommand;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用药计划领域模型。
 *
 * <p>该模型负责处理计划层面的核心规则：
 * 1. 家庭成员归属校验
 * 2. 药品或自定义药名校验
 * 3. 开始结束日期合法性校验
 * 4. 提醒时间格式与去重处理
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MedicationPlanModel extends HealthMedicationPlanEntity {

    /**
     * HH:mm 格式校验。
     */
    private static final String TIME_PATTERN = "^([01]\\d|2[0-3]):[0-5]\\d$";

    /** 用药计划数据库服务 */
    private HealthMedicationPlanService medicationPlanService;

    /** 药品数据库服务 */
    private HealthDrugService drugService;

    public MedicationPlanModel(HealthMedicationPlanService medicationPlanService,
        FamilyMemberModelFactory familyMemberModelFactory, HealthDrugService drugService) {
        this.medicationPlanService = medicationPlanService;
        this.drugService = drugService;
    }

    public MedicationPlanModel(HealthMedicationPlanEntity entity, HealthMedicationPlanService medicationPlanService,
        FamilyMemberModelFactory familyMemberModelFactory, HealthDrugService drugService) {
        this(medicationPlanService, familyMemberModelFactory, drugService);
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 从新增命令中装载计划字段。
     */
    public void loadAddCommand(AddMedicationPlanCommand command, Long ownerUserId) {
        if (command == null) {
            return;
        }
        this.setOwnerUserId(ownerUserId);
        this.setMemberId(command.getMemberId());
        this.setDrugId(command.getDrugId());
        this.setCustomDrugName(command.getCustomDrugName());
        this.setStartDate(command.getStartDate());
        this.setEndDate(command.getEndDate());
        this.setMealTiming(StrUtil.blankToDefault(command.getMealTiming(), "NO_LIMIT"));
        this.setDoseAmount(command.getDoseAmount());
        this.setDoseUnit(command.getDoseUnit());
        this.setFrequencyType(StrUtil.blankToDefault(command.getFrequencyType(), "DAILY"));
        this.setWeeklyDaysJson(JacksonUtil.to(normalizeWeeklyDays(command.getWeeklyDays())));
        this.setIntervalHours(command.getIntervalHours());
        this.setIntervalDays(command.getIntervalDays());
        this.setDoseRule(command.getDoseRule());
        this.setRemark(command.getRemark());
        this.setStatus(Objects.requireNonNullElse(command.getStatus(), StatusEnum.ENABLE.getValue()));
        this.setReminderTimesJson(JacksonUtil.to(new LinkedHashSet<>(command.getReminderTimes())));
    }

    /**
     * 从修改命令装载计划字段。
     */
    public void loadUpdateCommand(UpdateMedicationPlanCommand command, Long ownerUserId) {
        if (command != null) {
            loadAddCommand(command, ownerUserId);
            this.setPlanId(command.getPlanId());
        }
    }

    /**
     * 校验当前计划是否属于指定 App 用户。
     */
    public void checkOwnedByUser(Long ownerUserId) {
        if (getPlanId() == null || ownerUserId == null || !medicationPlanService.isOwnedByUser(getPlanId(), ownerUserId)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getPlanId(), "用药计划");
        }
    }

    /**
     * 校验计划字段。
     *
     * @param reminderTimes 提醒时间点列表
     * @param ownerUserId 当前登录 App 用户ID
     */
    public void checkFields(List<String> reminderTimes, Long ownerUserId) {
        if (getDrugId() == null && StrUtil.isBlank(getCustomDrugName())) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DRUG_NAME_REQUIRED);
        }

        if (getDrugId() != null) {
            DrugEntity drugEntity = drugService.getById(getDrugId());
            boolean currentUserDrug = drugEntity != null && Objects.equals(drugEntity.getOwnerUserId(), getOwnerUserId());
            boolean systemDrug = drugEntity != null
                && Objects.equals(drugEntity.getOwnerUserId(), DrugQuery.SYSTEM_DRUG_OWNER_ID);
            if (!currentUserDrug && !systemDrug) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getDrugId(), "药品");
            }

            // 自动库存扣减要求“计划剂量单位”和“药品库存单位”统一。
            // 因此这里在计划落库阶段就提前拦截，避免提醒执行时才发现单位无法对齐。
            if (currentUserDrug && StrUtil.isNotBlank(drugEntity.getStockUnit())) {
                if (StrUtil.isBlank(getDoseUnit())) {
                    throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_UNIT_REQUIRED_FOR_STOCK_TRACKING);
                }
                if (!Objects.equals(StrUtil.trim(getDoseUnit()), StrUtil.trim(drugEntity.getStockUnit()))) {
                    throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_UNIT_NOT_MATCH_STOCK_UNIT);
                }
                if (MedicationDoseStageRule.hasDoseUnitMismatch(getDoseRule(), drugEntity.getStockUnit())) {
                    throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_UNIT_NOT_MATCH_STOCK_UNIT);
                }
            }
        }

        if (getStartDate() != null && getEndDate() != null && getStartDate().after(getEndDate())) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DATE_RANGE_INVALID);
        }

        MedicationDoseStageRule.validate(getDoseRule());

        if (reminderTimes == null || reminderTimes.isEmpty()) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_REMINDER_TIMES_EMPTY);
        }

        for (String reminderTime : reminderTimes) {
            if (StrUtil.isBlank(reminderTime) || !ReUtil.isMatch(TIME_PATTERN, reminderTime)) {
                throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_REMINDER_TIME_FORMAT_INVALID);
            }
        }

        String frequencyType = StrUtil.blankToDefault(getFrequencyType(), "DAILY");
        if (Objects.equals(frequencyType, "WEEKLY") && getWeeklyDays().isEmpty()) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_WEEKLY_DAYS_REQUIRED);
        }
        if (Objects.equals(frequencyType, "INTERVAL_HOURS")
            && (getIntervalHours() == null || getIntervalHours() < 1 || getIntervalHours() > 24)) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_INTERVAL_HOURS_INVALID);
        }
        if (Objects.equals(frequencyType, "INTERVAL_DAYS")
            && (getIntervalDays() == null || getIntervalDays() < 1 || getIntervalDays() > 365)) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_INTERVAL_DAYS_INVALID);
        }
    }

    /**
     * 获取计划的提醒时间列表。
     */
    public List<String> getReminderTimes() {
        return StrUtil.isBlank(getReminderTimesJson())
            ? java.util.Collections.emptyList()
            : JacksonUtil.fromList(getReminderTimesJson(), String.class);
    }

    /**
     * 获取每周提醒的星期列表。
     */
    public List<Integer> getWeeklyDays() {
        return StrUtil.isBlank(getWeeklyDaysJson())
            ? Collections.emptyList()
            : JacksonUtil.fromList(getWeeklyDaysJson(), Integer.class);
    }

    /**
     * 获取本计划实际要使用的药品名称。
     * 如果绑定了当前用户已录入的药品，则优先使用药品名称；否则使用自定义药名。
     */
    public String getDisplayDrugName() {
        if (getDrugId() != null) {
            DrugEntity drugEntity = drugService.getById(getDrugId());
            if (drugEntity != null) {
                return drugEntity.getDrugName();
            }
        }
        return getCustomDrugName();
    }

    /**
     * 统一规范化星期列表。
     *
     * <p>这里要做去重和范围过滤，原因是：
     * 1. 前端可能因为多选回填、旧数据兼容等原因传出重复值
     * 2. 领域层需要对星期值做最后一道兜底，避免把非法数字直接落库
     */
    private List<Integer> normalizeWeeklyDays(List<Integer> weeklyDays) {
        if (weeklyDays == null || weeklyDays.isEmpty()) {
            return Collections.emptyList();
        }
        return weeklyDays.stream()
            .filter(Objects::nonNull)
            .filter(day -> day >= 1 && day <= 7)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}

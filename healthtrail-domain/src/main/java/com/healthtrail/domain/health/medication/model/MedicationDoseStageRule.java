package com.healthtrail.domain.health.medication.model;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.health.medication.dto.MedicationDoseStageDTO;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * 阶段剂量规则解析器。
 *
 * <p>dose_rule 字段历史上曾允许保存自由文本。为了兼容旧数据，本类只把 JSON 数组解析为可计算规则；
 * 非 JSON 文本仍然保留为说明文字，但不会参与提醒剂量自动计算。
 */
public final class MedicationDoseStageRule {

    private MedicationDoseStageRule() {
    }

    /**
     * 解析结构化阶段剂量。
     */
    public static List<MedicationDoseStageDTO> parseStructuredStages(String doseRule) {
        if (StrUtil.isBlank(doseRule) || !StrUtil.trim(doseRule).startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            return JacksonUtil.fromList(doseRule, MedicationDoseStageDTO.class);
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_RULE_INVALID);
        }
    }

    /**
     * 校验阶段剂量字段。
     */
    public static void validate(String doseRule) {
        List<MedicationDoseStageDTO> stages = parseStructuredStages(doseRule);
        for (int index = 0; index < stages.size(); index++) {
            MedicationDoseStageDTO stage = stages.get(index);
            boolean isLast = index == stages.size() - 1;
            if (stage == null || stage.getDoseAmount() == null || StrUtil.isBlank(stage.getDoseUnit())) {
                throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_RULE_INVALID);
            }
            Integer durationDays = stage.getDurationDays();
            if (!isLast && durationDays == null) {
                throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_RULE_INVALID);
            }
            if (durationDays != null && (durationDays < 1 || durationDays > 3650)) {
                throw new ApiException(ErrorCode.Business.APP_MEDICATION_PLAN_DOSE_RULE_INVALID);
            }
        }
    }

    /**
     * 计算指定日期应该使用的阶段剂量。
     */
    public static MedicationDoseSnapshot resolveDoseForDate(String doseRule, Date planStartDate, Date currentDate) {
        List<MedicationDoseStageDTO> stages = parseStructuredStages(doseRule);
        if (stages.isEmpty() || planStartDate == null || currentDate == null) {
            return null;
        }

        long dayIndex = DateUtil.betweenDay(DateUtil.beginOfDay(planStartDate), DateUtil.beginOfDay(currentDate), true) + 1;
        int cursorStartDay = 1;
        for (MedicationDoseStageDTO stage : stages) {
            Integer durationDays = stage.getDurationDays();
            if (durationDays == null) {
                return toSnapshot(stage);
            }
            int cursorEndDay = cursorStartDay + durationDays - 1;
            if (dayIndex >= cursorStartDay && dayIndex <= cursorEndDay) {
                return toSnapshot(stage);
            }
            cursorStartDay = cursorEndDay + 1;
        }
        return null;
    }

    public static boolean hasDoseUnitMismatch(String doseRule, String requiredDoseUnit) {
        if (StrUtil.isBlank(requiredDoseUnit)) {
            return false;
        }
        return parseStructuredStages(doseRule).stream()
            .filter(Objects::nonNull)
            .map(MedicationDoseStageDTO::getDoseUnit)
            .anyMatch(stageDoseUnit -> !Objects.equals(StrUtil.trim(stageDoseUnit), StrUtil.trim(requiredDoseUnit)));
    }

    private static MedicationDoseSnapshot toSnapshot(MedicationDoseStageDTO stage) {
        if (stage == null || stage.getDoseAmount() == null || StrUtil.isBlank(stage.getDoseUnit())) {
            return null;
        }
        MedicationDoseSnapshot snapshot = new MedicationDoseSnapshot();
        snapshot.setDoseAmount(stage.getDoseAmount());
        snapshot.setDoseUnit(stage.getDoseUnit());
        return snapshot;
    }

    /**
     * 写入提醒记录时使用的剂量快照。
     */
    @Data
    public static class MedicationDoseSnapshot {

        /** 剂量。 */
        private BigDecimal doseAmount;

        /** 剂量单位。 */
        private String doseUnit;
    }
}

package com.healthtrail.admin.controller.health;

import cn.hutool.core.date.DateUtil;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import com.healthtrail.domain.health.medication.dto.MedicationAdherenceStatisticsDTO;
import com.healthtrail.domain.health.medication.dto.MedicationAdherenceTrendPointDTO;
import com.healthtrail.domain.health.medication.dto.MedicationReminderHistoryDTO;
import com.healthtrail.domain.health.medication.query.MedicationReminderHistoryQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用药历史与依从率管理接口，对全量提醒记录做查询、统计和趋势分析。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/medication")
@Tag(name = "后台用药历史API", description = "后台用药历史与依从率管理接口")
public class HealthMedicationHistoryController extends BaseController {

    private final HealthMedicationReminderService medicationReminderService;
    private final HealthFamilyMemberService familyMemberService;
    private final HealthMedicationPlanService medicationPlanService;

    @Operation(summary = "后台历史服药记录列表")
    @PreAuthorize("@permission.has('health:medicationHistory:list')")
    @GetMapping("/history")
    public ResponseDTO<PageDTO<MedicationReminderHistoryDTO>> history(MedicationReminderHistoryQuery query) {
        validateQuery(query);
        Page<HealthMedicationReminderEntity> page = medicationReminderService.page(query.toPage(), buildHistoryQueryWrapper(query));
        List<MedicationReminderHistoryDTO> records = page.getRecords().stream()
            .map(this::buildHistoryDTO)
            .collect(Collectors.toList());
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "后台用药依从率统计")
    @PreAuthorize("@permission.has('health:medicationHistory:query')")
    @GetMapping("/adherence/statistics")
    public ResponseDTO<MedicationAdherenceStatisticsDTO> statistics(MedicationReminderHistoryQuery query) {
        validateQuery(query);
        List<HealthMedicationReminderEntity> reminders = medicationReminderService.list(buildHistoryQueryWrapper(query));

        MedicationAdherenceStatisticsDTO dto = new MedicationAdherenceStatisticsDTO();
        dto.setMemberId(query.getMemberId());
        dto.setMemberName(resolveMemberName(query.getMemberId()));
        dto.setStartDate(query.getStartDate());
        dto.setEndDate(query.getEndDate());
        dto.setScheduledReminderCount(reminders.size());
        dto.setTakenReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.TAKEN.getValue()));
        dto.setSkippedReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.SKIPPED.getValue()));
        dto.setExpiredReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.EXPIRED.getValue()));
        dto.setPendingReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.PENDING.getValue()));

        int denominator = dto.getTakenReminderCount() + dto.getSkippedReminderCount() + dto.getExpiredReminderCount();
        dto.setAdherenceRate(denominator <= 0 ? 0D : roundRate(dto.getTakenReminderCount() * 100D / denominator));
        return ResponseDTO.ok(dto);
    }

    @Operation(summary = "后台用药依从率趋势")
    @PreAuthorize("@permission.has('health:medicationHistory:query')")
    @GetMapping("/adherence/trend")
    public ResponseDTO<List<MedicationAdherenceTrendPointDTO>> trend(MedicationReminderHistoryQuery query) {
        validateQuery(query);
        List<HealthMedicationReminderEntity> reminders = medicationReminderService.list(buildHistoryQueryWrapper(query));
        if (reminders.isEmpty()) {
            return ResponseDTO.ok(Collections.emptyList());
        }

        Map<Long, List<HealthMedicationReminderEntity>> groupedReminders = reminders.stream()
            .filter(item -> item.getReminderDate() != null)
            .collect(Collectors.groupingBy(item -> DateUtil.beginOfDay(item.getReminderDate()).getTime()));
        List<MedicationAdherenceTrendPointDTO> trends = groupedReminders.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> buildTrendPoint(DateUtil.date(entry.getKey()), entry.getValue()))
            .collect(Collectors.toList());
        return ResponseDTO.ok(trends);
    }

    private QueryWrapper<HealthMedicationReminderEntity> buildHistoryQueryWrapper(MedicationReminderHistoryQuery query) {
        QueryWrapper<HealthMedicationReminderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(query.getMemberId() != null, "member_id", query.getMemberId())
            .eq(query.getPlanId() != null, "plan_id", query.getPlanId())
            .eq(query.getReminderStatus() != null, "reminder_status", query.getReminderStatus())
            .ge(query.getStartDate() != null, "scheduled_time", DateUtil.beginOfDay(query.getStartDate()))
            .le(query.getEndDate() != null, "scheduled_time", DateUtil.endOfDay(query.getEndDate()))
            .orderByDesc("scheduled_time")
            .orderByDesc("reminder_id");
        return queryWrapper;
    }

    private MedicationReminderHistoryDTO buildHistoryDTO(HealthMedicationReminderEntity entity) {
        MedicationReminderHistoryDTO dto = new MedicationReminderHistoryDTO(entity);
        dto.setMemberName(resolveMemberName(entity.getMemberId()));
        dto.setMemberCode(resolveMemberCode(entity.getMemberId()));
        dto.setPlanCode(resolvePlanCode(entity.getPlanId()));
        return dto;
    }

    private MedicationAdherenceTrendPointDTO buildTrendPoint(Date reminderDate, List<HealthMedicationReminderEntity> reminders) {
        MedicationAdherenceTrendPointDTO dto = new MedicationAdherenceTrendPointDTO();
        dto.setReminderDate(reminderDate);
        dto.setScheduledReminderCount(reminders.size());
        dto.setTakenReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.TAKEN.getValue()));
        dto.setSkippedReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.SKIPPED.getValue()));
        dto.setExpiredReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.EXPIRED.getValue()));
        dto.setPendingReminderCount(countReminderStatus(reminders, MedicationReminderStatusEnum.PENDING.getValue()));

        int denominator = dto.getTakenReminderCount() + dto.getSkippedReminderCount() + dto.getExpiredReminderCount();
        dto.setAdherenceRate(denominator <= 0 ? 0D : roundRate(dto.getTakenReminderCount() * 100D / denominator));
        return dto;
    }

    private int countReminderStatus(List<HealthMedicationReminderEntity> reminders, Integer reminderStatus) {
        return (int) reminders.stream()
            .filter(item -> Objects.equals(item.getReminderStatus(), reminderStatus))
            .count();
    }

    private void validateQuery(MedicationReminderHistoryQuery query) {
        if (query == null) {
            return;
        }
        if (query.getStartDate() != null && query.getEndDate() != null && query.getStartDate().after(query.getEndDate())) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_HISTORY_DATE_RANGE_INVALID);
        }
    }

    private String resolveMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? "已删除成员" : memberEntity.getMemberName();
    }

    /**
     * 回填成员编码。
     *
     * <p>后台历史页面经常需要客服根据截图定位对象，
     * 因此这里直接把成员编码一起带到列表对象中。
     */
    private String resolveMemberCode(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? null : memberEntity.getMemberCode();
    }

    /**
     * 回填计划编码。
     */
    private String resolvePlanCode(Long planId) {
        if (planId == null) {
            return null;
        }
        HealthMedicationPlanEntity planEntity = medicationPlanService.getById(planId);
        return planEntity == null ? null : planEntity.getPlanCode();
    }

    private double roundRate(double rawRate) {
        return Math.round(rawRate * 100D) / 100D;
    }
}

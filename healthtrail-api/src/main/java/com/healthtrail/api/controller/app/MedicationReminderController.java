package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.health.medication.MedicationApplicationService;
import com.healthtrail.domain.health.medication.command.SkipMedicationReminderCommand;
import com.healthtrail.domain.health.medication.dto.MedicationAdherenceStatisticsDTO;
import com.healthtrail.domain.health.medication.dto.MedicationAdherenceTrendPointDTO;
import com.healthtrail.domain.health.medication.dto.MedicationReminderHistoryDTO;
import com.healthtrail.domain.health.medication.dto.MedicationReminderDTO;
import com.healthtrail.domain.health.medication.query.MedicationReminderHistoryQuery;
import com.healthtrail.domain.health.medication.query.MedicationReminderQuery;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端用药提醒控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/medication/reminders")
@Tag(name = "用药提醒API", description = "App端用药提醒接口")
public class MedicationReminderController extends BaseController {

    private final MedicationApplicationService medicationApplicationService;

    /**
     * 获取今日提醒列表。
     */
    @Operation(summary = "今日提醒列表")
    @GetMapping("/today")
    public ResponseDTO<List<MedicationReminderDTO>> today(MedicationReminderQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(medicationApplicationService.getTodayReminderList(query));
    }

    /**
     * 获取历史服药记录分页列表。
     */
    @Operation(summary = "历史服药记录")
    @GetMapping("/history")
    public ResponseDTO<PageDTO<MedicationReminderHistoryDTO>> history(MedicationReminderHistoryQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(medicationApplicationService.getReminderHistory(query));
    }

    /**
     * 获取依从率统计摘要。
     */
    @Operation(summary = "用药依从率统计")
    @GetMapping("/history/statistics")
    public ResponseDTO<MedicationAdherenceStatisticsDTO> statistics(MedicationReminderHistoryQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(medicationApplicationService.getAdherenceStatistics(query));
    }

    /**
     * 获取依从率趋势列表。
     */
    @Operation(summary = "用药依从率趋势")
    @GetMapping("/history/trend")
    public ResponseDTO<List<MedicationAdherenceTrendPointDTO>> trend(MedicationReminderHistoryQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(medicationApplicationService.getAdherenceTrend(query));
    }

    /**
     * 标记本次提醒为已服药。
     */
    @Operation(summary = "标记已服药")
    @PostMapping("/{reminderId}/take")
    public ResponseDTO<Void> take(@PathVariable Long reminderId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        medicationApplicationService.markReminderTaken(reminderId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 撤销本次提醒的已服药反馈。
     */
    @Operation(summary = "撤销已服药")
    @PostMapping("/{reminderId}/untake")
    public ResponseDTO<Void> untake(@PathVariable Long reminderId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        medicationApplicationService.revertReminderTaken(reminderId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 标记本次提醒为已跳过。
     */
    @Operation(summary = "标记已跳过")
    @PostMapping("/{reminderId}/skip")
    public ResponseDTO<Void> skip(@PathVariable Long reminderId,
        @Valid @RequestBody(required = false) SkipMedicationReminderCommand skipCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        medicationApplicationService.markReminderSkipped(reminderId, skipCommand, currentUserId);
        return ResponseDTO.ok();
    }
}

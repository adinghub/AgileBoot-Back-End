package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.insight.HealthInsightApplicationService;
import com.healthtrail.domain.health.insight.command.CreateVoiceDiaryEntryCommand;
import com.healthtrail.domain.health.insight.command.CreateCareActionTaskCommand;
import com.healthtrail.domain.health.insight.command.CareActionTaskActionCommand;
import com.healthtrail.domain.health.insight.command.ConfirmReviewSuggestionCommand;
import com.healthtrail.domain.health.insight.command.SaveChronicDiaryEntryCommand;
import com.healthtrail.domain.health.insight.command.SaveDailyIndicatorRecordCommand;
import com.healthtrail.domain.health.insight.command.UpdateHealthProblemStatusCommand;
import com.healthtrail.domain.health.insight.command.LinkHealthProblemChronicProfileCommand;
import com.healthtrail.domain.health.chronic.command.SaveChronicDiseaseProfileCommand;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseProfileDTO;
import com.healthtrail.domain.health.insight.dto.ChronicDiaryEntryDTO;
import com.healthtrail.domain.health.insight.dto.ChronicDiseaseTemplateDTO;
import com.healthtrail.domain.health.insight.dto.HealthProblemCenterDTO;
import com.healthtrail.domain.health.insight.dto.HealthProblemChronicProfileDTO;
import com.healthtrail.domain.health.insight.dto.HealthProblemStatusLogDTO;
import com.healthtrail.domain.health.insight.dto.MedicalVisitPackageExportDTO;
import com.healthtrail.domain.health.insight.dto.HealthInsightWorkbenchDTO;
import com.healthtrail.domain.health.insight.dto.HealthLongTermManagementDTO;
import com.healthtrail.domain.health.insight.dto.HealthPrivacyDataExportDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 健康洞察控制器。
 *
 * <p>该控制器提供“复查闭环、指标预警、就医资料包、慢病日记、照护看板、
 * 用药安全、AI 周报、语音录入、健康时间线、长期管理”的统一 App 入口。
 * Controller 只负责鉴权和入参承接，具体聚合逻辑全部放在应用服务里，
 * 避免入口层逐渐堆积业务规则。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/health-insights")
@Tag(name = "健康洞察", description = "App端健康洞察、慢病日记、语音记录与时间线能力")
public class HealthInsightController extends BaseController {

    private final HealthInsightApplicationService healthInsightApplicationService;

    /**
     * 查询健康洞察工作台。
     */
    @Operation(summary = "健康洞察工作台")
    @GetMapping("/workbench")
    public ResponseDTO<HealthInsightWorkbenchDTO> workbench() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.getWorkbench(currentUserId));
    }

    /**
     * 查询家庭健康长期管理数据。
     */
    @Operation(summary = "家庭健康长期管理")
    @GetMapping("/long-term-management")
    public ResponseDTO<HealthLongTermManagementDTO> longTermManagement() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.getLongTermManagement(currentUserId));
    }

    /**
     * 保存日常指标记录。
     */
    @Operation(summary = "保存日常指标记录")
    @PostMapping("/daily-indicators")
    public ResponseDTO<ChronicDiaryEntryDTO> createDailyIndicatorRecord(
        @Valid @RequestBody SaveDailyIndicatorRecordCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.createDailyIndicatorRecord(command, currentUserId));
    }

    /**
     * 创建家庭照护任务。
     */
    @Operation(summary = "创建家庭照护任务")
    @PostMapping("/care-action-tasks")
    public ResponseDTO<Void> createCareActionTask(@Valid @RequestBody CreateCareActionTaskCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthInsightApplicationService.createCareActionTask(command, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 确认复查建议。
     */
    @Operation(summary = "确认复查建议")
    @PostMapping("/review-suggestions/confirm")
    public ResponseDTO<Void> confirmReviewSuggestion(@Valid @RequestBody ConfirmReviewSuggestionCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthInsightApplicationService.confirmReviewSuggestion(command, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 完成家庭照护任务。
     */
    @Operation(summary = "完成家庭照护任务")
    @PostMapping("/care-action-tasks/{operationTaskId}/complete")
    public ResponseDTO<Void> completeCareActionTask(@PathVariable Long operationTaskId,
        @Valid @RequestBody CareActionTaskActionCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthInsightApplicationService.completeCareActionTask(operationTaskId, command, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 取消家庭照护任务。
     */
    @Operation(summary = "取消家庭照护任务")
    @PostMapping("/care-action-tasks/{operationTaskId}/cancel")
    public ResponseDTO<Void> cancelCareActionTask(@PathVariable Long operationTaskId,
        @Valid @RequestBody CareActionTaskActionCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthInsightApplicationService.cancelCareActionTask(operationTaskId, command, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 查询隐私数据导出内容。
     */
    @Operation(summary = "隐私数据导出内容")
    @GetMapping("/long-term-management/privacy-export")
    public ResponseDTO<HealthPrivacyDataExportDTO> privacyDataExport() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.buildPrivacyDataExport(currentUserId));
    }
    /**
     * 查询健康问题中心。
     */
    @Operation(summary = "健康问题中心")
    @GetMapping("/health-problems")
    public ResponseDTO<HealthProblemCenterDTO> healthProblems(@RequestParam(required = false) Long memberId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.getHealthProblemCenter(memberId, currentUserId));
    }

    /**
     * 更新健康问题状态。
     *
     * <p>健康问题属于具体家庭成员，所以 Controller 只负责接收问题 ID 和目标状态，
     * 具体的成员编辑权限校验、状态值落库和返回卡片重建都收口到应用服务，避免 App 绕过权限直接改状态。
     */
    @Operation(summary = "更新健康问题状态")
    @PutMapping("/health-problems/{problemId}/status")
    public ResponseDTO<HealthProblemCenterDTO.ProblemItemDTO> updateHealthProblemStatus(@PathVariable Long problemId,
        @Valid @RequestBody UpdateHealthProblemStatusCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.updateHealthProblemStatus(problemId, command,
            currentUserId));
    }

    /**
     * 查询健康问题状态变更历史。
     *
     * <p>状态历史用于 App 展示问题处理时间线。读取只要求当前账号可以访问问题所属成员，
     * 写入仍然由状态更新动作统一控制，避免前端直接构造历史记录。
     */
    @Operation(summary = "健康问题状态变更历史")
    @GetMapping("/health-problems/{problemId}/status-logs")
    public ResponseDTO<List<HealthProblemStatusLogDTO>> healthProblemStatusLogs(@PathVariable Long problemId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.listHealthProblemStatusLogs(problemId, currentUserId));
    }
    /**
     * 将健康问题加入复查提醒。
     */
    @Operation(summary = "健康问题复查提醒")
    @PostMapping("/health-problems/{problemId}/review-task")
    public ResponseDTO<Void> createHealthProblemReviewTask(@PathVariable Long problemId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthInsightApplicationService.createHealthProblemReviewTask(problemId, currentUserId);
        return ResponseDTO.ok();
    }
    /**
     * 查询健康问题可关联的慢病专项候选。
     */
    @Operation(summary = "健康问题慢病专项候选")
    @GetMapping("/health-problems/{problemId}/chronic-profile-candidates")
    public ResponseDTO<List<HealthProblemChronicProfileDTO>> chronicProfileCandidates(@PathVariable Long problemId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.listHealthProblemChronicProfileCandidates(problemId,
            currentUserId));
    }

    /**
     * 关联健康问题和慢病专项。
     */
    @Operation(summary = "关联健康问题和慢病专项")
    @PostMapping("/health-problems/{problemId}/chronic-profiles")
    public ResponseDTO<HealthProblemChronicProfileDTO> linkChronicProfile(@PathVariable Long problemId,
        @Valid @RequestBody LinkHealthProblemChronicProfileCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.linkHealthProblemToChronicProfile(problemId, command,
            currentUserId));
    }

    /**
     * 解除健康问题和慢病专项关联。
     */
    @Operation(summary = "解除健康问题和慢病专项关联")
    @DeleteMapping("/health-problems/{problemId}/chronic-profiles/{profileId}")
    public ResponseDTO<Void> unlinkChronicProfile(@PathVariable Long problemId, @PathVariable Long profileId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthInsightApplicationService.unlinkHealthProblemFromChronicProfile(problemId, profileId, currentUserId);
        return ResponseDTO.ok();
    }
    /**
     * 生成就医资料包导出数据。
     */
    @Operation(summary = "就医资料包导出数据")
    @GetMapping("/medical-visit-package/export-data")
    public ResponseDTO<MedicalVisitPackageExportDTO> medicalVisitPackageExport(
        @RequestParam(required = false) Long memberId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.buildMedicalVisitPackageExport(memberId, currentUserId));
    }

    /**
     * 查询慢病专项模板。
     */
    @Operation(summary = "慢病专项模板列表")
    @GetMapping("/chronic-disease-templates")
    public ResponseDTO<List<ChronicDiseaseTemplateDTO>> chronicDiseaseTemplates() {
        return ResponseDTO.ok(healthInsightApplicationService.listChronicDiseaseTemplates());
    }
    /**
     * 从慢病模板创建专项档案。
     *
     * <p>该入口只是健康洞察页的快捷建档入口，真正的建档校验继续复用慢病专项服务，
     * 这样病种配置校验、重复档案校验、默认目标填充不会在模板入口再写一套分支。
     */
    @Operation(summary = "从慢病模板创建专项档案")
    @PostMapping("/chronic-disease-templates/create-profile")
    public ResponseDTO<ChronicDiseaseProfileDTO> createProfileFromTemplate(
        @Valid @RequestBody SaveChronicDiseaseProfileCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.createProfileFromTemplate(command, currentUserId));
    }

    /**
     * 保存慢病日记。
     */
    @Operation(summary = "保存慢病日记")
    @PostMapping("/chronic-diary")
    public ResponseDTO<ChronicDiaryEntryDTO> createDiaryEntry(
        @Valid @RequestBody SaveChronicDiaryEntryCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.createDiaryEntry(command, currentUserId));
    }

    /**
     * 保存语音识别后的健康记录。
     */
    @Operation(summary = "保存语音健康记录")
    @PostMapping("/voice-diary")
    public ResponseDTO<ChronicDiaryEntryDTO> createVoiceDiaryEntry(
        @Valid @RequestBody CreateVoiceDiaryEntryCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.createVoiceDiaryEntry(command, currentUserId));
    }

    /**
     * 查询健康时间线。
     */
    @Operation(summary = "健康时间线")
    @GetMapping("/timeline")
    public ResponseDTO<List<HealthInsightWorkbenchDTO.HealthTimelineItemDTO>> timeline(
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) Integer limit) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthInsightApplicationService.listTimeline(memberId, limit, currentUserId));
    }
}
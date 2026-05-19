package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.chronic.ChronicDiseaseApplicationService;
import com.healthtrail.domain.health.chronic.command.CancelChronicDiseaseReviewTaskCommand;
import com.healthtrail.domain.health.chronic.command.CreateChronicDiseaseReviewTaskCommand;
import com.healthtrail.domain.health.chronic.command.SaveChronicIndicatorTargetCommand;
import com.healthtrail.domain.health.chronic.command.SaveChronicDiseaseProfileCommand;
import com.healthtrail.domain.health.chronic.dto.ChronicIndicatorTargetDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseDashboardDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseProfileDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseReviewTaskDTO;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseTypeDTO;
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
 * App 慢病专项控制器。
 *
 * <p>这里统一承载“病种配置、专项档案、专项看板、指标目标”四类动作。
 * 控制器不理解具体慢病类型，所有病种差异交给后端配置表和应用服务解析，
 * 避免后续每新增一个慢病就增加一组路径或一组 Controller 方法。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/chronic-diseases")
@Tag(name = "慢病专项", description = "App端通用慢病专项档案与趋势看板能力")
public class ChronicDiseaseController extends BaseController {

    private final ChronicDiseaseApplicationService chronicDiseaseApplicationService;

    /**
     * 查询启用中的慢病病种配置。
     */
    @Operation(summary = "慢病病种配置列表")
    @GetMapping("/types")
    public ResponseDTO<List<ChronicDiseaseTypeDTO>> types() {
        return ResponseDTO.ok(chronicDiseaseApplicationService.listEnabledDiseaseTypes());
    }

    /**
     * 查询当前账号可访问的慢病专项档案。
     */
    @Operation(summary = "慢病专项档案列表")
    @GetMapping
    public ResponseDTO<List<ChronicDiseaseProfileDTO>> list(@RequestParam(required = false) Long memberId,
        @RequestParam(required = false) Integer profileStatus, @RequestParam(required = false) String keyword) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.listProfiles(memberId, profileStatus, keyword, currentUserId));
    }

    /**
     * 新增慢病专项档案。
     */
    @Operation(summary = "新增慢病专项档案")
    @PostMapping
    public ResponseDTO<ChronicDiseaseProfileDTO> create(@Valid @RequestBody SaveChronicDiseaseProfileCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.createProfile(command, currentUserId));
    }

    /**
     * 查询慢病专项详情看板。
     */
    @Operation(summary = "慢病专项详情看板")
    @GetMapping("/{profileId}")
    public ResponseDTO<ChronicDiseaseDashboardDTO> detail(@PathVariable Long profileId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.getProfileDashboard(profileId, currentUserId));
    }

    /**
     * 一键创建慢病专项复查任务。
     *
     * <p>该动作只负责把指定慢病档案挂到首页待跟进任务流，
     * 任务点击后的真实落点仍由返回的导航编码和 App 统一跳转层处理。
     */
    @Operation(summary = "创建慢病专项复查任务")
    @PostMapping("/{profileId}/review-task")
    public ResponseDTO<ChronicDiseaseReviewTaskDTO> createReviewTask(@PathVariable Long profileId,
        @Valid @RequestBody(required = false) CreateChronicDiseaseReviewTaskCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.createReviewTask(profileId, command, currentUserId));
    }

    /**
     * 取消慢病专项复查任务。
     *
     * <p>该动作只关闭已经创建的复查提醒，不修改慢病专项本身，用户后续仍可重新创建新的复查任务。
     */
    @Operation(summary = "取消慢病专项复查任务")
    @PostMapping("/{profileId}/review-task/{operationTaskId}/cancel")
    public ResponseDTO<Void> cancelReviewTask(@PathVariable Long profileId, @PathVariable Long operationTaskId,
        @Valid @RequestBody(required = false) CancelChronicDiseaseReviewTaskCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        chronicDiseaseApplicationService.cancelReviewTask(profileId, operationTaskId, command, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 删除慢病专项复查任务。
     *
     * <p>该动作适合用户误建复查提醒后直接移除。删除只处理复查提醒，不删除慢病专项档案，
     * 后续用户仍可以重新创建新的复查任务。
     */
    @Operation(summary = "删除慢病专项复查任务")
    @DeleteMapping("/{profileId}/review-task/{operationTaskId}")
    public ResponseDTO<Void> deleteReviewTask(@PathVariable Long profileId, @PathVariable Long operationTaskId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        chronicDiseaseApplicationService.deleteReviewTask(profileId, operationTaskId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 查询慢病专项指标目标范围。
     */
    @Operation(summary = "慢病指标目标范围列表")
    @GetMapping("/{profileId}/indicator-targets")
    public ResponseDTO<List<ChronicIndicatorTargetDTO>> indicatorTargets(@PathVariable Long profileId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.listIndicatorTargets(profileId, currentUserId));
    }

    /**
     * 保存慢病专项指标目标范围。
     */
    @Operation(summary = "保存慢病指标目标范围")
    @PostMapping("/{profileId}/indicator-targets")
    public ResponseDTO<ChronicIndicatorTargetDTO> saveIndicatorTarget(@PathVariable Long profileId,
        @Valid @RequestBody SaveChronicIndicatorTargetCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.saveIndicatorTarget(profileId, command, currentUserId));
    }

    /**
     * 删除慢病专项指标目标范围。
     */
    @Operation(summary = "删除慢病指标目标范围")
    @DeleteMapping("/{profileId}/indicator-targets/{targetId}")
    public ResponseDTO<Void> deleteIndicatorTarget(@PathVariable Long profileId, @PathVariable Long targetId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        chronicDiseaseApplicationService.deleteIndicatorTarget(profileId, targetId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 修改慢病专项档案。
     */
    @Operation(summary = "修改慢病专项档案")
    @PutMapping("/{profileId}")
    public ResponseDTO<ChronicDiseaseProfileDTO> update(@PathVariable Long profileId,
        @Valid @RequestBody SaveChronicDiseaseProfileCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(chronicDiseaseApplicationService.updateProfile(profileId, command, currentUserId));
    }

    /**
     * 删除慢病专项档案。
     */
    @Operation(summary = "删除慢病专项档案")
    @DeleteMapping("/{profileId}")
    public ResponseDTO<Void> delete(@PathVariable Long profileId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        chronicDiseaseApplicationService.deleteProfile(profileId, currentUserId);
        return ResponseDTO.ok();
    }
}


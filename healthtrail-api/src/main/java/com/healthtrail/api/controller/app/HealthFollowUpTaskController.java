package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.dashboard.HealthFollowUpTaskApplicationService;
import com.healthtrail.domain.health.dashboard.command.BatchFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.CompleteFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.DelayFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.IgnoreFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.ReadFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.command.RestoreFollowUpTaskCommand;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskAnalyticsDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskBatchResultDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskDetailDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskLogDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpTaskDTO;
import com.healthtrail.domain.health.dashboard.query.HealthFollowUpTaskQuery;
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
 * 首页待跟进任务控制器。
 *
 * <p>该控制器只处理“首页任务流”的操作行为，
 * 不直接替代提醒模块、报告模块本身的业务接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/follow-up/tasks")
@Tag(name = "首页待跟进任务API", description = "App端首页待跟进任务操作接口")
public class HealthFollowUpTaskController extends BaseController {

    private final HealthFollowUpTaskApplicationService healthFollowUpTaskApplicationService;

    /**
     * 查询任务中心列表。
     */
    @Operation(summary = "任务中心列表")
    @GetMapping
    public ResponseDTO<List<HealthFollowUpTaskDTO>> list(HealthFollowUpTaskQuery query) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.getTaskList(query, currentUserId));
    }

    /**
     * 查询任务行为分析摘要。
     */
    @Operation(summary = "任务行为分析")
    @GetMapping("/analytics")
    public ResponseDTO<HealthFollowUpTaskAnalyticsDTO> analytics() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.getTaskAnalytics(currentUserId, 7));
    }

    /**
     * 查询任务详情。
     *
     * <p>该接口适用于任务中心详情页，
     * 直接返回任务基础信息、按钮可操作性以及时间线，减少前端二次拼装。
     */
    @Operation(summary = "任务详情")
    @GetMapping("/{taskId}")
    public ResponseDTO<HealthFollowUpTaskDetailDTO> detail(@PathVariable Long taskId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.getTaskDetail(taskId, currentUserId));
    }

    /**
     * 查询指定任务的操作日志。
     *
     * <p>前端可在任务中心详情页中展示一条清晰的时间线，
     * 帮助用户回顾这条任务什么时候被延后、忽略、恢复或完成。
     */
    @Operation(summary = "任务操作日志")
    @GetMapping("/{taskId}/logs")
    public ResponseDTO<List<HealthFollowUpTaskLogDTO>> logs(@PathVariable Long taskId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.getTaskLogs(taskId, currentUserId));
    }

    /**
     * 标记首页任务为已读。
     */
    @Operation(summary = "标记待跟进任务已读")
    @PostMapping("/read")
    public ResponseDTO<Void> read(@Valid @RequestBody ReadFollowUpTaskCommand readCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthFollowUpTaskApplicationService.markTaskRead(readCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 延后首页任务。
     */
    @Operation(summary = "延后待跟进任务")
    @PostMapping("/delay")
    public ResponseDTO<Void> delay(@Valid @RequestBody DelayFollowUpTaskCommand delayCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthFollowUpTaskApplicationService.delayTask(delayCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 完成本轮首页任务。
     */
    @Operation(summary = "完成待跟进任务")
    @PostMapping("/complete")
    public ResponseDTO<Void> complete(@Valid @RequestBody CompleteFollowUpTaskCommand completeCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthFollowUpTaskApplicationService.completeTask(completeCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 忽略首页任务。
     */
    @Operation(summary = "忽略待跟进任务")
    @PostMapping("/ignore")
    public ResponseDTO<Void> ignore(@Valid @RequestBody IgnoreFollowUpTaskCommand ignoreCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthFollowUpTaskApplicationService.ignoreTask(ignoreCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 恢复首页任务。
     */
    @Operation(summary = "恢复待跟进任务")
    @PostMapping("/restore")
    public ResponseDTO<Void> restore(@Valid @RequestBody RestoreFollowUpTaskCommand restoreCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthFollowUpTaskApplicationService.restoreTask(restoreCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 批量标记任务已读。
     */
    @Operation(summary = "批量标记待跟进任务已读")
    @PostMapping("/batch-read")
    public ResponseDTO<HealthFollowUpTaskBatchResultDTO> batchRead(@Valid @RequestBody BatchFollowUpTaskCommand batchCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.batchReadTasks(batchCommand, currentUserId));
    }

    /**
     * 批量忽略任务。
     */
    @Operation(summary = "批量忽略待跟进任务")
    @PostMapping("/batch-ignore")
    public ResponseDTO<HealthFollowUpTaskBatchResultDTO> batchIgnore(
        @Valid @RequestBody BatchFollowUpTaskCommand batchCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.batchIgnoreTasks(batchCommand, currentUserId));
    }

    /**
     * 批量恢复任务。
     */
    @Operation(summary = "批量恢复待跟进任务")
    @PostMapping("/batch-restore")
    public ResponseDTO<HealthFollowUpTaskBatchResultDTO> batchRestore(
        @Valid @RequestBody BatchFollowUpTaskCommand batchCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.batchRestoreTasks(batchCommand, currentUserId));
    }

    /**
     * 批量完成任务。
     */
    @Operation(summary = "批量完成待跟进任务")
    @PostMapping("/batch-complete")
    public ResponseDTO<HealthFollowUpTaskBatchResultDTO> batchComplete(
        @Valid @RequestBody BatchFollowUpTaskCommand batchCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthFollowUpTaskApplicationService.batchCompleteTasks(batchCommand, currentUserId));
    }
}

package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.dashboard.HealthOperationTaskApplicationService;
import com.healthtrail.domain.health.dashboard.command.AddHealthOperationTaskCommand;
import com.healthtrail.domain.health.dashboard.command.UpdateHealthOperationTaskCommand;
import com.healthtrail.domain.health.dashboard.dto.HealthOperationTaskDTO;
import com.healthtrail.domain.health.dashboard.query.HealthOperationTaskQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台首页运营任务控制器。
 *
 * <p>该控制器用于给后台管理端提供“首页运营任务”的增删改查能力，
 * 让运营人员能够把指定任务直接投放到目标 App 用户的首页任务流。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/operation-tasks")
@Tag(name = "后台首页运营任务API", description = "后台首页运营任务维护接口")
public class HealthOperationTaskController extends BaseController {

    private final HealthOperationTaskApplicationService operationTaskApplicationService;

    /**
     * 分页查询运营任务列表。
     */
    @Operation(summary = "首页运营任务列表")
    @PreAuthorize("@permission.has('health:operationTask:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthOperationTaskDTO>> list(HealthOperationTaskQuery query) {
        return ResponseDTO.ok(operationTaskApplicationService.getTaskPage(query));
    }

    /**
     * 查询运营任务详情。
     */
    @Operation(summary = "首页运营任务详情")
    @PreAuthorize("@permission.has('health:operationTask:query')")
    @GetMapping("/{operationTaskId}")
    public ResponseDTO<HealthOperationTaskDTO> getInfo(@PathVariable Long operationTaskId) {
        return ResponseDTO.ok(operationTaskApplicationService.getTaskInfo(operationTaskId));
    }

    /**
     * 新增运营任务。
     */
    @Operation(summary = "新增首页运营任务")
    @PreAuthorize("@permission.has('health:operationTask:add')")
    @AccessLog(title = "首页运营任务", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddHealthOperationTaskCommand command) {
        operationTaskApplicationService.addTask(command);
        return ResponseDTO.ok();
    }

    /**
     * 修改运营任务。
     */
    @Operation(summary = "修改首页运营任务")
    @PreAuthorize("@permission.has('health:operationTask:edit')")
    @AccessLog(title = "首页运营任务", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{operationTaskId}")
    public ResponseDTO<Void> edit(@PathVariable Long operationTaskId,
        @Valid @RequestBody UpdateHealthOperationTaskCommand command) {
        command.setOperationTaskId(operationTaskId);
        operationTaskApplicationService.updateTask(command);
        return ResponseDTO.ok();
    }

    /**
     * 删除运营任务。
     */
    @Operation(summary = "删除首页运营任务")
    @PreAuthorize("@permission.has('health:operationTask:remove')")
    @AccessLog(title = "首页运营任务", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{operationTaskId}")
    public ResponseDTO<Void> remove(@PathVariable Long operationTaskId) {
        operationTaskApplicationService.removeTask(operationTaskId);
        return ResponseDTO.ok();
    }
}

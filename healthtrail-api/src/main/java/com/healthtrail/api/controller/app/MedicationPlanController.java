package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.medication.MedicationApplicationService;
import com.healthtrail.domain.health.medication.command.AddMedicationPlanCommand;
import com.healthtrail.domain.health.medication.command.CopyMedicationPlanCommand;
import com.healthtrail.domain.health.medication.command.UpdateMedicationPlanCommand;
import com.healthtrail.domain.health.medication.dto.MedicationPlanDTO;
import com.healthtrail.domain.health.medication.query.MedicationPlanQuery;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * App 端用药计划控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/medication/plans")
@Tag(name = "用药计划API", description = "App端用药计划接口")
public class MedicationPlanController extends BaseController {

    private final MedicationApplicationService medicationApplicationService;

    /**
     * 获取当前用户的用药计划列表。
     */
    @Operation(summary = "用药计划列表")
    @GetMapping
    public ResponseDTO<List<MedicationPlanDTO>> list(MedicationPlanQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(medicationApplicationService.getMedicationPlanList(query));
    }

    /**
     * 获取单个用药计划详情。
     */
    @Operation(summary = "用药计划详情")
    @GetMapping("/{planId}")
    public ResponseDTO<MedicationPlanDTO> getInfo(@PathVariable Long planId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(medicationApplicationService.getMedicationPlanInfo(planId, currentUserId));
    }

    /**
     * 新增用药计划。
     */
    @Operation(summary = "新增用药计划")
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddMedicationPlanCommand addCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        medicationApplicationService.addMedicationPlan(addCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 复制一条已有用药计划。
     *
     * <p>该接口面向“常用计划复制”场景，
     * 允许前端只修改少量关键字段，快速生成一条新计划。
     */
    @Operation(summary = "复制用药计划")
    @PostMapping("/{planId}/copy")
    public ResponseDTO<MedicationPlanDTO> copy(@PathVariable Long planId,
        @Valid @RequestBody CopyMedicationPlanCommand copyCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(medicationApplicationService.copyMedicationPlan(planId, copyCommand, currentUserId));
    }

    /**
     * 修改用药计划。
     */
    @Operation(summary = "修改用药计划")
    @PutMapping("/{planId}")
    public ResponseDTO<Void> edit(@PathVariable Long planId, @Valid @RequestBody UpdateMedicationPlanCommand updateCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        updateCommand.setPlanId(planId);
        medicationApplicationService.updateMedicationPlan(updateCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 删除用药计划。
     */
    @Operation(summary = "删除用药计划")
    @DeleteMapping("/{planId}")
    public ResponseDTO<Void> remove(@PathVariable Long planId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        medicationApplicationService.removeMedicationPlan(planId, currentUserId);
        return ResponseDTO.ok();
    }
}

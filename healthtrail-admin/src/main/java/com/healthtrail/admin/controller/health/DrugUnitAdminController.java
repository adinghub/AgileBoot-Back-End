package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.drug.unit.DrugUnitApplicationService;
import com.healthtrail.domain.health.drug.unit.command.AddDrugUnitCommand;
import com.healthtrail.domain.health.drug.unit.command.UpdateDrugUnitCommand;
import com.healthtrail.domain.health.drug.unit.dto.DrugUnitDTO;
import com.healthtrail.domain.health.drug.unit.query.DrugUnitQuery;
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
 * 后台药品单位管理接口，提供药品单位的增删改查能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/drug-units")
@Tag(name = "后台药品单位API", description = "后台药品单位管理接口")
public class DrugUnitAdminController extends BaseController {

    private final DrugUnitApplicationService drugUnitApplicationService;

    /**
     * 分页查询药品单位。
     */
    @Operation(summary = "药品单位列表")
    @PreAuthorize("@permission.has('health:drugUnit:list')")
    @GetMapping
    public ResponseDTO<PageDTO<DrugUnitDTO>> list(DrugUnitQuery query) {
        return ResponseDTO.ok(drugUnitApplicationService.getUnitPage(query));
    }

    /**
     * 查询药品单位详情。
     */
    @Operation(summary = "药品单位详情")
    @PreAuthorize("@permission.has('health:drugUnit:query')")
    @GetMapping("/{unitId}")
    public ResponseDTO<DrugUnitDTO> getInfo(@PathVariable Long unitId) {
        return ResponseDTO.ok(drugUnitApplicationService.getUnitInfo(unitId));
    }

    /**
     * 新增药品单位。
     */
    @Operation(summary = "新增药品单位")
    @PreAuthorize("@permission.has('health:drugUnit:add')")
    @AccessLog(title = "药品单位", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddDrugUnitCommand command) {
        drugUnitApplicationService.addUnit(command);
        return ResponseDTO.ok();
    }

    /**
     * 修改药品单位。
     */
    @Operation(summary = "修改药品单位")
    @PreAuthorize("@permission.has('health:drugUnit:edit')")
    @AccessLog(title = "药品单位", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{unitId}")
    public ResponseDTO<Void> edit(@PathVariable Long unitId, @Valid @RequestBody UpdateDrugUnitCommand command) {
        command.setUnitId(unitId);
        drugUnitApplicationService.updateUnit(command);
        return ResponseDTO.ok();
    }

    /**
     * 删除药品单位。
     */
    @Operation(summary = "删除药品单位")
    @PreAuthorize("@permission.has('health:drugUnit:remove')")
    @AccessLog(title = "药品单位", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{unitId}")
    public ResponseDTO<Void> remove(@PathVariable Long unitId) {
        drugUnitApplicationService.removeUnit(unitId);
        return ResponseDTO.ok();
    }
}

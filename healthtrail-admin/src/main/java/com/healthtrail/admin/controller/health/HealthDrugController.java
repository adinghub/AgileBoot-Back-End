package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.drug.DrugApplicationService;
import com.healthtrail.domain.health.drug.command.AddDrugCommand;
import com.healthtrail.domain.health.drug.command.UpdateDrugCommand;
import com.healthtrail.domain.health.drug.dto.DrugDTO;
import com.healthtrail.domain.health.drug.query.DrugQuery;
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
 * 后台系统药品控制器。
 *
 * <p>后台入口只维护“系统下发药品”，不直接操作某个 App 用户的个人药品。
 * 这样职责边界更清晰：
 * 1. 后台负责维护公共药品底库
 * 2. App 用户负责维护自己的补充药品
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/drugs")
@Tag(name = "后台药品API", description = "后台系统下发药品管理接口")
public class HealthDrugController extends BaseController {

    private final DrugApplicationService drugApplicationService;

    /**
     * 分页查询系统下发药品列表。
     * 这里强制只查 `owner_user_id = 0` 的记录，避免后台列表误混入用户个人药品。
     */
    @Operation(summary = "系统药品列表")
    @PreAuthorize("@permission.has('health:drug:list')")
    @GetMapping
    public ResponseDTO<PageDTO<DrugDTO>> list(DrugQuery query) {
        query.setOnlySystemDrugs(true);
        return ResponseDTO.ok(drugApplicationService.getDrugList(query));
    }

    /**
     * 查询系统下发药品详情。
     */
    @Operation(summary = "系统药品详情")
    @PreAuthorize("@permission.has('health:drug:query')")
    @GetMapping("/{drugId}")
    public ResponseDTO<DrugDTO> getInfo(@PathVariable Long drugId) {
        return ResponseDTO.ok(drugApplicationService.getSystemDrugInfo(drugId));
    }

    /**
     * 新增系统下发药品。
     * 新增后 App 端即可在药品列表和用药计划里看到该药品。
     */
    @Operation(summary = "新增系统药品")
    @PreAuthorize("@permission.has('health:drug:add')")
    @AccessLog(title = "系统药品", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddDrugCommand addCommand) {
        drugApplicationService.addSystemDrug(addCommand);
        return ResponseDTO.ok();
    }

    /**
     * 修改系统下发药品。
     */
    @Operation(summary = "修改系统药品")
    @PreAuthorize("@permission.has('health:drug:edit')")
    @AccessLog(title = "系统药品", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{drugId}")
    public ResponseDTO<Void> edit(@PathVariable Long drugId, @Valid @RequestBody UpdateDrugCommand updateCommand) {
        updateCommand.setDrugId(drugId);
        drugApplicationService.updateSystemDrug(updateCommand);
        return ResponseDTO.ok();
    }

    /**
     * 删除系统下发药品。
     */
    @Operation(summary = "删除系统药品")
    @PreAuthorize("@permission.has('health:drug:remove')")
    @AccessLog(title = "系统药品", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{drugId}")
    public ResponseDTO<Void> remove(@PathVariable Long drugId) {
        drugApplicationService.removeSystemDrug(drugId);
        return ResponseDTO.ok();
    }
}

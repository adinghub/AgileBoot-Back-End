package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.chronic.ChronicDiseaseApplicationService;
import com.healthtrail.domain.health.chronic.command.SaveChronicDiseaseTypeCommand;
import com.healthtrail.domain.health.chronic.command.UpdateChronicDiseaseTypeCommand;
import com.healthtrail.domain.health.chronic.dto.ChronicDiseaseTypeDTO;
import com.healthtrail.domain.health.chronic.query.ChronicDiseaseTypeQuery;
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
 * 后台慢病病种配置控制器。
 *
 * <p>该控制器维护的是慢病专项的“病种模板底座”，不是用户个人慢病档案。
 * 病种配置启用后，App 新增专项、模板建档、指标趋势匹配都会走同一套通用配置。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/chronic-disease-types")
@Tag(name = "后台慢病病种配置", description = "维护慢病专项可选病种、关注指标和默认建议")
public class HealthChronicDiseaseTypeController extends BaseController {

    private final ChronicDiseaseApplicationService chronicDiseaseApplicationService;

    /** 分页查询慢病病种配置。 */
    @Operation(summary = "慢病病种配置列表")
    @PreAuthorize("@permission.has('health:chronicDiseaseType:list')")
    @GetMapping
    public ResponseDTO<PageDTO<ChronicDiseaseTypeDTO>> list(ChronicDiseaseTypeQuery query) {
        return ResponseDTO.ok(chronicDiseaseApplicationService.getDiseaseTypePage(query));
    }

    /** 查询慢病病种配置详情。 */
    @Operation(summary = "慢病病种配置详情")
    @PreAuthorize("@permission.has('health:chronicDiseaseType:query')")
    @GetMapping("/{typeId}")
    public ResponseDTO<ChronicDiseaseTypeDTO> getInfo(@PathVariable Long typeId) {
        return ResponseDTO.ok(chronicDiseaseApplicationService.getDiseaseTypeInfo(typeId));
    }

    /** 新增慢病病种配置。 */
    @Operation(summary = "新增慢病病种配置")
    @PreAuthorize("@permission.has('health:chronicDiseaseType:add')")
    @AccessLog(title = "慢病病种配置", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody SaveChronicDiseaseTypeCommand command) {
        chronicDiseaseApplicationService.addDiseaseType(command);
        return ResponseDTO.ok();
    }

    /** 修改慢病病种配置。 */
    @Operation(summary = "修改慢病病种配置")
    @PreAuthorize("@permission.has('health:chronicDiseaseType:edit')")
    @AccessLog(title = "慢病病种配置", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{typeId}")
    public ResponseDTO<Void> edit(@PathVariable Long typeId,
        @Valid @RequestBody UpdateChronicDiseaseTypeCommand command) {
        command.setTypeId(typeId);
        chronicDiseaseApplicationService.updateDiseaseType(command);
        return ResponseDTO.ok();
    }

    /** 删除慢病病种配置。 */
    @Operation(summary = "删除慢病病种配置")
    @PreAuthorize("@permission.has('health:chronicDiseaseType:remove')")
    @AccessLog(title = "慢病病种配置", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{typeId}")
    public ResponseDTO<Void> remove(@PathVariable Long typeId) {
        chronicDiseaseApplicationService.removeDiseaseType(typeId);
        return ResponseDTO.ok();
    }
}
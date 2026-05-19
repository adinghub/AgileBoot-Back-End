package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.report.HealthIndicatorTemplateApplicationService;
import com.healthtrail.domain.health.report.command.AddHealthIndicatorTemplateCommand;
import com.healthtrail.domain.health.report.command.UpdateHealthIndicatorTemplateCommand;
import com.healthtrail.domain.health.report.dto.HealthIndicatorTemplateDTO;
import com.healthtrail.domain.health.report.query.HealthIndicatorTemplateQuery;
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
 * 后台指标模板管理接口，提供体检指标模板的增删改查能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/report-indicator-templates")
@Tag(name = "后台指标模板API", description = "后台体检指标模板管理接口")
public class HealthIndicatorTemplateController extends BaseController {

    private final HealthIndicatorTemplateApplicationService indicatorTemplateApplicationService;

    /**
     * 分页查询模板列表。
     */
    @Operation(summary = "指标模板列表")
    @PreAuthorize("@permission.has('health:indicatorTemplate:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthIndicatorTemplateDTO>> list(HealthIndicatorTemplateQuery query) {
        return ResponseDTO.ok(indicatorTemplateApplicationService.getTemplatePage(query));
    }

    /**
     * 查询模板详情。
     */
    @Operation(summary = "指标模板详情")
    @PreAuthorize("@permission.has('health:indicatorTemplate:query')")
    @GetMapping("/{templateId}")
    public ResponseDTO<HealthIndicatorTemplateDTO> getInfo(@PathVariable Long templateId) {
        return ResponseDTO.ok(indicatorTemplateApplicationService.getTemplateInfo(templateId));
    }

    /**
     * 新增模板。
     */
    @Operation(summary = "新增指标模板")
    @PreAuthorize("@permission.has('health:indicatorTemplate:add')")
    @AccessLog(title = "指标模板", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddHealthIndicatorTemplateCommand command) {
        indicatorTemplateApplicationService.addTemplate(command);
        return ResponseDTO.ok();
    }

    /**
     * 修改模板。
     */
    @Operation(summary = "修改指标模板")
    @PreAuthorize("@permission.has('health:indicatorTemplate:edit')")
    @AccessLog(title = "指标模板", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{templateId}")
    public ResponseDTO<Void> edit(@PathVariable Long templateId,
        @Valid @RequestBody UpdateHealthIndicatorTemplateCommand command) {
        command.setTemplateId(templateId);
        indicatorTemplateApplicationService.updateTemplate(command);
        return ResponseDTO.ok();
    }

    /**
     * 删除模板。
     */
    @Operation(summary = "删除指标模板")
    @PreAuthorize("@permission.has('health:indicatorTemplate:remove')")
    @AccessLog(title = "指标模板", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{templateId}")
    public ResponseDTO<Void> remove(@PathVariable Long templateId) {
        indicatorTemplateApplicationService.removeTemplate(templateId);
        return ResponseDTO.ok();
    }
}

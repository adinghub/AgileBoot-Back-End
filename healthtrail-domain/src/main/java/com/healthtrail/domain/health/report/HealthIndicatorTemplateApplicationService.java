package com.healthtrail.domain.health.report;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.report.command.AddHealthIndicatorTemplateCommand;
import com.healthtrail.domain.health.report.command.UpdateHealthIndicatorTemplateCommand;
import com.healthtrail.domain.health.report.db.HealthIndicatorTemplateEntity;
import com.healthtrail.domain.health.report.db.HealthIndicatorTemplateService;
import com.healthtrail.domain.health.report.dto.HealthIndicatorTemplateDTO;
import com.healthtrail.domain.health.report.query.HealthIndicatorTemplateQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 指标模板应用服务。
 *
 * <p>该服务聚焦后台模板维护主流程，避免在 Controller 中散落字段校验与默认值逻辑。
 */
@Service
@RequiredArgsConstructor
public class HealthIndicatorTemplateApplicationService {

    /** 指标模板数据库服务 */
    private final HealthIndicatorTemplateService indicatorTemplateService;

    /**
     * 分页查询模板列表。
     */
    public PageDTO<HealthIndicatorTemplateDTO> getTemplatePage(HealthIndicatorTemplateQuery query) {
        Page<HealthIndicatorTemplateEntity> page = indicatorTemplateService.page(query.toPage(), query.toQueryWrapper());
        List<HealthIndicatorTemplateDTO> records = page.getRecords().stream()
            .map(HealthIndicatorTemplateDTO::new)
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 查询模板详情。
     */
    public HealthIndicatorTemplateDTO getTemplateInfo(Long templateId) {
        HealthIndicatorTemplateEntity entity = loadTemplate(templateId);
        return new HealthIndicatorTemplateDTO(entity);
    }

    /**
     * 新增模板。
     */
    public void addTemplate(AddHealthIndicatorTemplateCommand command) {
        HealthIndicatorTemplateEntity entity = new HealthIndicatorTemplateEntity();
        BeanUtil.copyProperties(command, entity, "templateId");
        fillDefaultFields(entity);
        validateFields(entity);
        indicatorTemplateService.save(entity);
    }

    /**
     * 修改模板。
     */
    public void updateTemplate(UpdateHealthIndicatorTemplateCommand command) {
        HealthIndicatorTemplateEntity entity = loadTemplate(command.getTemplateId());
        BeanUtil.copyProperties(command, entity, "templateId");
        fillDefaultFields(entity);
        validateFields(entity);
        indicatorTemplateService.updateById(entity);
    }

    /**
     * 删除模板。
     */
    public void removeTemplate(Long templateId) {
        loadTemplate(templateId);
        indicatorTemplateService.removeById(templateId);
    }

    /**
     * 加载单个模板。
     */
    private HealthIndicatorTemplateEntity loadTemplate(Long templateId) {
        HealthIndicatorTemplateEntity entity = indicatorTemplateService.getById(templateId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, templateId, "指标模板");
        }
        return entity;
    }

    /**
     * 补齐默认字段。
     */
    private void fillDefaultFields(HealthIndicatorTemplateEntity entity) {
        entity.setItemCode(StrUtil.blankToDefault(entity.getItemCode(), null));
        entity.setReportType(StrUtil.blankToDefault(entity.getReportType(), null));
        entity.setSort(entity.getSort() == null ? 0 : entity.getSort());
        entity.setStatus(entity.getStatus() == null ? StatusEnum.ENABLE.getValue() : entity.getStatus());
    }

    /**
     * 模板字段校验。
     */
    private void validateFields(HealthIndicatorTemplateEntity entity) {
        if (StrUtil.isBlank(entity.getItemName())) {
            throw new ApiException(ErrorCode.Business.HEALTH_INDICATOR_TEMPLATE_ITEM_NAME_REQUIRED);
        }
        if (entity.getReferenceMin() != null && entity.getReferenceMax() != null
            && entity.getReferenceMin().compareTo(entity.getReferenceMax()) > 0) {
            throw new ApiException(ErrorCode.Business.HEALTH_INDICATOR_TEMPLATE_REFERENCE_RANGE_INVALID);
        }
    }
}

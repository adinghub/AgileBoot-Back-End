package com.healthtrail.domain.health.report.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 指标模板服务接口。
 */
public interface HealthIndicatorTemplateService extends IService<HealthIndicatorTemplateEntity> {

    /**
     * 按报告类型、指标编码、指标名称匹配一个启用中的模板。
     *
     * @param reportType 报告类型
     * @param itemCode 指标编码
     * @param itemName 指标名称
     * @return 命中的模板，不存在时返回 null
     */
    HealthIndicatorTemplateEntity matchEnabledTemplate(String reportType, String itemCode, String itemName);
}

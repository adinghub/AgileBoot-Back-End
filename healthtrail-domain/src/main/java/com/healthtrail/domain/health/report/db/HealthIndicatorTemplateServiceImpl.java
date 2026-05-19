package com.healthtrail.domain.health.report.db;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 指标模板服务实现。
 */
@Service
public class HealthIndicatorTemplateServiceImpl
    extends ServiceImpl<HealthIndicatorTemplateMapper, HealthIndicatorTemplateEntity>
    implements HealthIndicatorTemplateService {

    @Override
    public HealthIndicatorTemplateEntity matchEnabledTemplate(String reportType, String itemCode, String itemName) {
        QueryWrapper<HealthIndicatorTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", StatusEnum.ENABLE.getValue())
            .eq(StrUtil.isNotBlank(reportType), "report_type", reportType)
            .and(wrapper -> wrapper.eq(StrUtil.isNotBlank(itemCode), "item_code", itemCode)
                .or()
                .eq("item_name", itemName))
            .orderByAsc("sort")
            .orderByDesc("template_id")
            .last("limit 1");
        return this.getOne(queryWrapper);
    }
}

package com.healthtrail.domain.health.report.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.report.db.HealthIndicatorTemplateEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 指标模板查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthIndicatorTemplateQuery extends AbstractPageQuery<HealthIndicatorTemplateEntity> {

    /** 报告类型。 */
    private String reportType;

    /** 项目编码。 */
    private String itemCode;

    /** keyword。 */
    private String keyword;

    /** 状态。 */
    private Integer status;

    @Override
    public QueryWrapper<HealthIndicatorTemplateEntity> addQueryCondition() {
        QueryWrapper<HealthIndicatorTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(reportType), "report_type", reportType)
            .eq(StrUtil.isNotBlank(itemCode), "item_code", itemCode)
            .eq(status != null, "status", status)
            .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                .like("item_name", keyword)
                .or()
                .like("item_code", keyword))
            .orderByAsc("sort")
            .orderByDesc("template_id");
        return queryWrapper;
    }
}

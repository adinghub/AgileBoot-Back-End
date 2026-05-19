package com.healthtrail.domain.health.chronic.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.chronic.db.HealthChronicDiseaseTypeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 慢病病种配置查询对象。
 *
 * <p>后台维护页按编码、名称、分类和状态检索即可覆盖主要运营场景；
 * 具体关注指标仍在详情和编辑弹窗中维护，避免列表筛选条件过度复杂。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ChronicDiseaseTypeQuery extends AbstractPageQuery<HealthChronicDiseaseTypeEntity> {

    /** 病种编码 */
    private String diseaseCode;

    /** 病种名称 */
    private String diseaseName;

    /** 病种分类 */
    private String diseaseCategory;

    /** 状态 */
    private Integer status;

    @Override
    public QueryWrapper<HealthChronicDiseaseTypeEntity> addQueryCondition() {
        QueryWrapper<HealthChronicDiseaseTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(diseaseCode), "disease_code", diseaseCode)
            .like(StrUtil.isNotBlank(diseaseName), "disease_name", diseaseName)
            .like(StrUtil.isNotBlank(diseaseCategory), "disease_category", diseaseCategory)
            .eq(status != null, "status", status)
            .orderByAsc("sort")
            .orderByAsc("type_id");
        return queryWrapper;
    }
}

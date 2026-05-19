package com.healthtrail.domain.health.drug.unit.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.drug.unit.db.DrugUnitEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 药品单位查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DrugUnitQuery extends AbstractPageQuery<DrugUnitEntity> {

    /**
     * 关键字，支持单位名称、编码、别名联合搜索。
     */
    private String keyword;

    /**
     * 状态。
     */
    private Integer status;

    @Override
    public QueryWrapper<DrugUnitEntity> addQueryCondition() {
        QueryWrapper<DrugUnitEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(status != null, "status", status)
            .and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                .like("unit_name", keyword)
                .or()
                .like("unit_code", keyword)
                .or()
                .like("unit_alias", keyword))
            .orderByAsc("sort")
            .orderByAsc("unit_id");
        return queryWrapper;
    }
}

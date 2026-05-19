package com.healthtrail.domain.health.drug.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 药品查询对象。
 *
 * <p>药品库天然存在较大的数据量，因此这里采用分页查询，方便 App 做搜索与列表加载。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DrugQuery extends AbstractPageQuery<DrugEntity> {

    /**
     * 系统下发药品使用固定归属用户ID 0 表示。
     */
    public static final long SYSTEM_DRUG_OWNER_ID = 0L;

    /**
     * 当前登录 App 用户ID。
     * 在 App 端场景下，该字段用于定位“当前用户自己的药品”。
     */
    private Long ownerUserId;

    /**
     * 是否同时包含系统下发药品。
     * App 端默认需要查“自己的药品 + 系统药品”两部分数据。
     */
    private Boolean includeSystemDrugs;

    /**
     * 是否仅查询系统下发药品。
     * 后台药品管理默认只维护系统药品，因此需要这个开关。
     */
    private Boolean onlySystemDrugs;

    /**
     * 关键字，支持药品名称、通用名、商品名联合搜索。
     */
    private String keyword;

    /**
     * 药品类型，例如 OTC 或 RX。
     */
    private String drugType;

    /**
     * 状态，通常前端只查启用药品。
     */
    private Integer status;

    @Override
    public QueryWrapper<DrugEntity> addQueryCondition() {
        QueryWrapper<DrugEntity> queryWrapper = new QueryWrapper<>();
        if (Boolean.TRUE.equals(onlySystemDrugs)) {
            queryWrapper.eq("owner_user_id", SYSTEM_DRUG_OWNER_ID);
        } else if (ownerUserId != null && Boolean.TRUE.equals(includeSystemDrugs)) {
            queryWrapper.and(wrapper -> wrapper.eq("owner_user_id", ownerUserId)
                .or()
                .eq("owner_user_id", SYSTEM_DRUG_OWNER_ID));
        } else {
            queryWrapper.eq(ownerUserId != null, "owner_user_id", ownerUserId);
        }

        queryWrapper.and(StrUtil.isNotBlank(keyword), wrapper -> wrapper
                .like("drug_name", keyword)
                .or()
                .like("generic_name", keyword)
                .or()
                .like("brand_name", keyword))
            .eq(StrUtil.isNotBlank(drugType), "drug_type", drugType)
            .eq(status != null, "status", status);
        return queryWrapper;
    }
}

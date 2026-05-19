package com.healthtrail.domain.system.member.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.member.db.MemberFeatureEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员权益查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberFeatureQuery extends AbstractPageQuery<MemberFeatureEntity> {

    private String featureCode;
    private String featureName;
    private String featureType;
    private Integer status;

    @Override
    public QueryWrapper<MemberFeatureEntity> addQueryCondition() {
        QueryWrapper<MemberFeatureEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(featureCode), "feature_code", featureCode)
            .like(StrUtil.isNotBlank(featureName), "feature_name", featureName)
            .eq(StrUtil.isNotBlank(featureType), "feature_type", featureType)
            .eq(status != null, "status", status)
            .orderByAsc("feature_sort")
            .orderByDesc("member_feature_id");
        return queryWrapper;
    }
}

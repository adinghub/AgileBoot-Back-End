package com.healthtrail.domain.health.family.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractQuery;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 家庭成员查询对象。
 *
 * <p>App 端一期列表场景不需要分页，这里只保留最常用的筛选维度。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FamilyMemberQuery extends AbstractQuery<HealthFamilyMemberEntity> {

    /**
     * 归属 App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 成员姓名关键字。
     */
    private String memberName;

    /**
     * 状态筛选。
     */
    private Integer status;

    @Override
    public QueryWrapper<HealthFamilyMemberEntity> addQueryCondition() {
        return new QueryWrapper<HealthFamilyMemberEntity>()
            .eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .like(StrUtil.isNotBlank(memberName), "member_name", memberName)
            .eq(status != null, "status", status);
    }
}

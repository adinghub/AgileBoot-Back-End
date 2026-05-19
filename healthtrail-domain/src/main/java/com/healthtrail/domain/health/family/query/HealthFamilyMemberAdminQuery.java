package com.healthtrail.domain.health.family.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台家庭共享管理成员分页查询对象。
 *
 * <p>后台页并不是直接展示“某个登录 App 用户自己的成员”，
 * 而是需要从全局视角检索当前系统里所有可管理的家庭成员，
 * 所以这里单独定义后台查询对象，避免和 App 端“按当前登录人可见范围”混在一起。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthFamilyMemberAdminQuery extends AbstractPageQuery<HealthFamilyMemberEntity> {

    /**
     * 主账号 App 用户ID。
     */
    private Long ownerUserId;

    /**
     * 家庭成员姓名关键字。
     */
    private String memberName;

    /**
     * 成员状态。
     */
    private Integer status;

    @Override
    public QueryWrapper<HealthFamilyMemberEntity> addQueryCondition() {
        return new QueryWrapper<HealthFamilyMemberEntity>()
            .eq(ownerUserId != null, "owner_user_id", ownerUserId)
            .like(StrUtil.isNotBlank(memberName), "member_name", memberName)
            .eq(status != null, "status", status)
            .orderByDesc("member_id");
    }
}

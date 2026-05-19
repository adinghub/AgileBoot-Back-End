package com.healthtrail.domain.system.member.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.member.db.UserMemberEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户会员查询对象。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserMemberQuery extends AbstractPageQuery<UserMemberEntity> {

    private Long userId;
    private Long memberLevelId;
    private String status;
    private String sourceType;

    @Override
    public QueryWrapper<UserMemberEntity> addQueryCondition() {
        QueryWrapper<UserMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "user_id", userId)
            .eq(memberLevelId != null, "member_level_id", memberLevelId)
            .eq(StrUtil.isNotBlank(status), "status", status)
            .eq(StrUtil.isNotBlank(sourceType), "source_type", sourceType)
            .orderByDesc("user_member_id");
        return queryWrapper;
    }
}

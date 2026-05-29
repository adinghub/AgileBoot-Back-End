package com.healthtrail.domain.system.role.query;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UnallocatedRoleQuery extends AbstractPageQuery<SysUserEntity> {

    private Long roleId;
    private String username;
    private String phoneNumber;

    public QueryWrapper<SysUserEntity> addQueryCondition() {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(username), "username", username)
            .like(StrUtil.isNotEmpty(phoneNumber), "phone_number", phoneNumber)
            .and(roleId != null, o -> o.ne("role_id", roleId)
                .or().isNull("role_id")
                .or().eq("role_id", 0));

        return queryWrapper;
    }
    
}

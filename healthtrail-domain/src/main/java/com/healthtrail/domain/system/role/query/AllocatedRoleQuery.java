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
public class AllocatedRoleQuery extends AbstractPageQuery<SysUserEntity> {

    private Long roleId;
    private String username;
    private String phoneNumber;

    @Override
    public QueryWrapper<SysUserEntity> addQueryCondition() {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(roleId != null, "role_id", roleId)
            .like(StrUtil.isNotEmpty(username), "username", username)
            .like(StrUtil.isNotEmpty(phoneNumber), "phone_number", phoneNumber);

        return queryWrapper;
    }

}

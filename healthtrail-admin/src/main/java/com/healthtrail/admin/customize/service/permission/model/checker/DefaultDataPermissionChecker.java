package com.healthtrail.admin.customize.service.permission.model.checker;

import com.healthtrail.infrastructure.user.web.SystemLoginUser;
import com.healthtrail.admin.customize.service.permission.model.AbstractDataPermissionChecker;
import com.healthtrail.admin.customize.service.permission.model.DataCondition;
import com.healthtrail.domain.system.dept.db.SysDeptService;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 默认数据权限检查器，对所有数据操作均返回拒绝。
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultDataPermissionChecker extends AbstractDataPermissionChecker {

    private SysDeptService deptService;

    @Override
    public boolean check(SystemLoginUser loginUser, DataCondition condition) {
        return false;
    }

}

package com.healthtrail.admin.customize.service.permission.model.checker;

import com.healthtrail.infrastructure.user.web.SystemLoginUser;
import com.healthtrail.admin.customize.service.permission.model.AbstractDataPermissionChecker;
import com.healthtrail.admin.customize.service.permission.model.DataCondition;
import com.healthtrail.domain.system.dept.db.SysDeptService;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 仅本人数据权限检查器，只允许操作自己的数据。
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlySelfDataPermissionChecker extends AbstractDataPermissionChecker {

    private SysDeptService deptService;

    @Override
    public boolean check(SystemLoginUser loginUser, DataCondition condition) {
        if (condition == null || loginUser == null) {
            return false;
        }

        if (loginUser.getUserId() == null || condition.getTargetUserId() == null) {
            return false;
        }

        Long currentUserId = loginUser.getUserId();
        Long targetUserId = condition.getTargetUserId();
        return Objects.equals(currentUserId, targetUserId);
    }

}

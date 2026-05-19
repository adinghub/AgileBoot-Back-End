package com.healthtrail.admin.customize.service.permission.model;

import com.healthtrail.infrastructure.user.web.SystemLoginUser;
import com.healthtrail.domain.system.dept.db.SysDeptService;
import lombok.Data;

/**
 * 数据权限检查器抽象基类，定义通用的权限校验模板方法。
 * @author valarchie
 */
@Data
public abstract class AbstractDataPermissionChecker {

    private SysDeptService deptService;

    /**
     * 检测当前用户对于 给定条件的数据 是否有权限
     *
     * @param loginUser 登录用户
     * @param condition 条件
     * @return 校验结果
     */
    public abstract boolean check(SystemLoginUser loginUser, DataCondition condition);

}

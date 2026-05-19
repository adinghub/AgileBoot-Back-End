package com.healthtrail.domain.system.user.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.system.dept.model.DeptModelFactory;
import com.healthtrail.domain.system.post.model.PostModelFactory;
import com.healthtrail.domain.system.role.model.RoleModelFactory;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.user.db.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class UserModelFactory {

    /** 用户数据库服务 */
    private final SysUserService userService;

    /** 岗位领域模型工厂 */
    private final PostModelFactory postModelFactory;

    /** 部门领域模型工厂 */
    private final DeptModelFactory deptModelFactory;

    /** 角色领域模型工厂 */
    private final RoleModelFactory roleModelFactory;

    public UserModel loadById(Long userId) {
        SysUserEntity byId = userService.getById(userId);
        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, userId, "用户");
        }
        return new UserModel(byId, userService, postModelFactory, deptModelFactory, roleModelFactory);
    }

    public UserModel create() {
        return new UserModel(userService, postModelFactory, deptModelFactory, roleModelFactory);
    }

}

package com.healthtrail.domain.system.user.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.config.HealthTrailConfig;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.exception.error.ErrorCode.Business;
import com.healthtrail.domain.system.dept.model.DeptModelFactory;
import com.healthtrail.domain.system.post.model.PostModelFactory;
import com.healthtrail.domain.system.role.model.RoleModelFactory;
import com.healthtrail.domain.system.user.command.AddUserCommand;
import com.healthtrail.domain.system.user.command.UpdateProfileCommand;
import com.healthtrail.domain.system.user.command.UpdateUserCommand;
import com.healthtrail.domain.system.user.command.UpdateUserPasswordCommand;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import com.healthtrail.infrastructure.user.web.SystemLoginUser;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.user.db.SysUserService;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台用户领域模型，负责用户的增删改、密码管理及相关实体存在性校验。
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserModel extends SysUserEntity {

    /** 用户数据库服务 */
    private SysUserService userService;

    /** 岗位模型工厂 */
    private PostModelFactory postModelFactory;

    /** 部门模型工厂 */
    private DeptModelFactory deptModelFactory;

    /** 角色模型工厂 */
    private RoleModelFactory roleModelFactory;

    public UserModel(SysUserEntity entity, SysUserService userService, PostModelFactory postModelFactory,
        DeptModelFactory deptModelFactory, RoleModelFactory roleModelFactory) {
        this(userService, postModelFactory, deptModelFactory, roleModelFactory);

        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    public UserModel(SysUserService userService, PostModelFactory postModelFactory,
        DeptModelFactory deptModelFactory, RoleModelFactory roleModelFactory) {
        this.userService = userService;
        this.postModelFactory = postModelFactory;
        this.deptModelFactory = deptModelFactory;
        this.roleModelFactory = roleModelFactory;
    }

    /** 从新增命令装载用户字段 */
    public void loadAddUserCommand(AddUserCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "userId");
        }
    }

    /** 从更新命令装载用户字段 */
    public void loadUpdateUserCommand(UpdateUserCommand command) {
        if (command != null) {
            loadAddUserCommand(command);
        }
    }

    /** 从个人资料更新命令装载字段 */
    public void loadUpdateProfileCommand(UpdateProfileCommand command) {
        if (command != null) {
            this.setSex(command.getSex());
            this.setNickname(command.getNickName());
            this.setPhoneNumber(command.getPhoneNumber());
            this.setEmail(command.getEmail());
        }
    }

    /** 校验用户名是否唯一 */
    public void checkUsernameIsUnique() {
        if (userService.isUserNameDuplicated(getUsername())) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE);
        }
    }

    /** 校验手机号是否唯一 */
    public void checkPhoneNumberIsUnique() {
        if (StrUtil.isNotEmpty(getPhoneNumber()) && userService.isPhoneDuplicated(getPhoneNumber(),
            getUserId())) {
            throw new ApiException(ErrorCode.Business.USER_PHONE_NUMBER_IS_NOT_UNIQUE);
        }
    }

    /** 校验关联的岗位、部门、角色是否存在 */
    public void checkFieldRelatedEntityExist() {

        if (getPostId() != null) {
            postModelFactory.loadById(getPostId());
        }

        if (getDeptId() != null) {
            deptModelFactory.loadById(getDeptId());
        }

        if (getRoleId() != null) {
            roleModelFactory.loadById(getRoleId());
        }

    }

    /** 校验邮箱是否唯一 */
    public void checkEmailIsUnique() {
        if (StrUtil.isNotEmpty(getEmail()) && userService.isEmailDuplicated(getEmail(), getUserId())) {
            throw new ApiException(ErrorCode.Business.USER_EMAIL_IS_NOT_UNIQUE);
        }
    }

    /** 校验当前用户是否允许被删除（不允许删除自己或超级管理员） */
    public void checkCanBeDelete(SystemLoginUser loginUser) {
        if (Objects.equals(getUserId(), loginUser.getUserId())
            || this.getIsAdmin()) {
            throw new ApiException(ErrorCode.Business.USER_CURRENT_USER_CAN_NOT_BE_DELETE);
        }
    }

    /** 修改用户密码，需校验旧密码正确且新密码不同于旧密码 */
    public void modifyPassword(UpdateUserPasswordCommand command) {
        if (!AuthenticationUtils.matchesPassword(command.getOldPassword(), getPassword())) {
            throw new ApiException(ErrorCode.Business.USER_PASSWORD_IS_NOT_CORRECT);
        }

        if (AuthenticationUtils.matchesPassword(command.getNewPassword(), getPassword())) {
            throw new ApiException(ErrorCode.Business.USER_NEW_PASSWORD_IS_THE_SAME_AS_OLD);
        }
        setPassword(AuthenticationUtils.encryptPassword(command.getNewPassword()));
    }

    /** 重置用户密码 */
    public void resetPassword(String newPassword) {
        setPassword(AuthenticationUtils.encryptPassword(newPassword));
    }

    @Override
    public boolean updateById() {
        if (this.getIsAdmin() && HealthTrailConfig.isDemoEnabled()) {
            throw new ApiException(Business.USER_ADMIN_CAN_NOT_BE_MODIFY);
        }

       return super.updateById();
    }

}

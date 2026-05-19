package com.healthtrail.infrastructure.user.web;

import com.healthtrail.infrastructure.user.base.BaseLoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 后台管理系统登录用户模型，扩展BaseLoginUser，包含管理员标识、部门ID和角色信息。
 * @author valarchie
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SystemLoginUser extends BaseLoginUser {

    private static final long serialVersionUID = 1L;

    /** 是否为管理员 */
    private boolean isAdmin;
    /** 所属部门ID */
    private Long deptId;
    /** 角色信息 */
    private RoleInfo roleInfo;
    /** 当超过这个时间则触发刷新缓存时间 */
    private Long autoRefreshCacheTime;


    public SystemLoginUser(Long userId, Boolean isAdmin, String username, String password, RoleInfo roleInfo,
        Long deptId) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.username = username;
        this.password = password;
        this.roleInfo = roleInfo;
        this.deptId = deptId;
    }

    public RoleInfo getRoleInfo() {
        return roleInfo;
    }

    public Long getRoleId() {
        return getRoleInfo().getRoleId();
    }

    public Long getDeptId() {
        return deptId;
    }


}

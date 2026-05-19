package com.healthtrail.infrastructure.user.web;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.SetUtils;

/**
 * 角色信息模型，封装角色ID、数据权限范围、菜单权限和部门集合。
 * @author valarchie
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleInfo {

    public static final RoleInfo EMPTY_ROLE = new RoleInfo();
    /** 管理员角色ID */
    public static final long ADMIN_ROLE_ID = -1;
    /** 管理员角色标识 */
    public static final String ADMIN_ROLE_KEY = "admin";
    /** 全部权限通配符 */
    public static final String ALL_PERMISSIONS = "*:*:*";
    /** 管理员权限集合 */
    public static final Set<String> ADMIN_PERMISSIONS = SetUtils.hashSet(ALL_PERMISSIONS);


    public RoleInfo(Long roleId, String roleKey, DataScopeEnum dataScope, Set<Long> deptIdSet,
        Set<String> menuPermissions, Set<Long> menuIds) {
        this.roleId = roleId;
        this.roleKey = roleKey;
        this.dataScope = dataScope;
        this.deptIdSet = deptIdSet;
        this.menuPermissions = menuPermissions != null ? menuPermissions : SetUtils.emptySet();
        this.menuIds = menuIds != null ? menuIds : SetUtils.emptySet();
    }

    /** 角色ID */
    private Long roleId;
    /** 角色名称 */
    private String roleName;
    /** 数据权限范围 */
    private DataScopeEnum dataScope;
    /** 授权部门ID集合 */
    private Set<Long> deptIdSet;
    /** 角色标识 */
    private String roleKey;
    /** 菜单权限字符串集合 */
    private Set<String> menuPermissions;
    /** 菜单ID集合 */
    private Set<Long> menuIds;

}

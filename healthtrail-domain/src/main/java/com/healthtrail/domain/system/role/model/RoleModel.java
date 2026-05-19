package com.healthtrail.domain.system.role.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.exception.error.ErrorCode.Business;
import com.healthtrail.domain.system.role.command.AddRoleCommand;
import com.healthtrail.domain.system.role.command.UpdateRoleCommand;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.domain.system.role.db.SysRoleEntity;
import com.healthtrail.domain.system.role.db.SysRoleMenuEntity;
import com.healthtrail.domain.system.role.db.SysRoleMenuService;
import com.healthtrail.domain.system.role.db.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色领域模型，负责角色的增删改、菜单关联管理及数据权限校验。
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RoleModel extends SysRoleEntity {

    /** 角色关联的菜单ID列表 */
    private List<Long> menuIds;

    /** 自定义数据权限时关联的部门ID列表 */
    private List<Long> deptIds;

    /** 角色数据库服务 */
    private SysRoleService roleService;

    /** 角色菜单关联数据库服务 */
    private SysRoleMenuService roleMenuService;

    public RoleModel(SysRoleService roleService, SysRoleMenuService roleMenuService) {
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
    }

    public RoleModel(SysRoleEntity entity, SysRoleService roleService, SysRoleMenuService roleMenuService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
    }

    /** 从新增命令装载角色字段 */
    public void loadAddCommand(AddRoleCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "roleId");
        }
    }

    /** 从更新命令装载角色字段 */
    public void loadUpdateCommand(UpdateRoleCommand command) {
        if (command != null) {
            loadAddCommand(command);
        }
    }

    /** 校验角色名称是否唯一 */
    public void checkRoleNameUnique() {
        if (roleService.isRoleNameDuplicated(getRoleId(), getRoleName())) {
            throw new ApiException(ErrorCode.Business.ROLE_NAME_IS_NOT_UNIQUE, getRoleName());
        }
    }

    /** 校验角色是否已分配给用户，已分配则不允许删除 */
    public void checkRoleCanBeDelete() {
        if (roleService.isAssignedToUsers(getRoleId())) {
            throw new ApiException(Business.ROLE_ALREADY_ASSIGN_TO_USER, getRoleName());
        }
    }

    /** 校验角色权限标识是否唯一 */
    public void checkRoleKeyUnique() {
        if (roleService.isRoleKeyDuplicated(getRoleId(), getRoleKey())) {
            throw new ApiException(ErrorCode.Business.ROLE_KEY_IS_NOT_UNIQUE, getRoleKey());
        }
    }

    /** 校验角色是否启用 */
    public void checkRoleAvailable() {
        if (StatusEnum.DISABLE.getValue().equals(getStatus())) {
            throw new ApiException(Business.ROLE_IS_NOT_AVAILABLE, getRoleName());
        }
    }

    /** 根据部门ID列表生成部门数据权限集合字符串 */
    public void generateDeptIdSet() {
        if (deptIds == null) {
            setDeptIdSet("");
            return;
        }

        if (deptIds.size() > new HashSet<>(deptIds).size()) {
            throw new ApiException(ErrorCode.Business.ROLE_DATA_SCOPE_DUPLICATED_DEPT);
        }

        String deptIdSet = StrUtil.join(",", deptIds);
        setDeptIdSet(deptIdSet);
    }



    @Override
    public boolean insert() {
        super.insert();
        return saveMenus();
    }

    @Override
    public boolean updateById() {
        // 清空之前的角色菜单关联
        cleanOldMenus();
        saveMenus();
        return super.updateById();
    }

    @Override
    public boolean deleteById() {
        // 清空之前的角色菜单关联
        cleanOldMenus();
        return super.deleteById();
    }

    /** 清空角色原有的菜单关联 */
    private void cleanOldMenus() {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, getRoleId());

        roleMenuService.remove(queryWrapper);
    }

    /** 批量保存角色的菜单关联 */
    private boolean saveMenus() {
        List<SysRoleMenuEntity> list = new ArrayList<>();
        if (getMenuIds() != null) {
            for (Long menuId : getMenuIds()) {
                SysRoleMenuEntity rm = new SysRoleMenuEntity();
                rm.setRoleId(getRoleId());
                rm.setMenuId(menuId);
                list.add(rm);
            }
            return roleMenuService.saveBatch(list);
        }
        return false;
    }

}

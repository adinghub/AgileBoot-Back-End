package com.healthtrail.domain.system.role.model;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.system.role.db.SysRoleEntity;
import com.healthtrail.domain.system.role.db.SysRoleMenuEntity;
import com.healthtrail.domain.system.role.db.SysRoleMenuService;
import com.healthtrail.domain.system.role.db.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class RoleModelFactory {

    /** 角色数据库服务 */
    private final SysRoleService roleService;

    /** 角色菜单关联数据库服务 */
    private final SysRoleMenuService roleMenuService;

    public RoleModel loadById(Long roleId) {
        SysRoleEntity byId = roleService.getById(roleId);
        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, roleId, "角色");
        }

        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        List<Long> menuIds = roleMenuService.list(queryWrapper).stream().map(SysRoleMenuEntity::getMenuId)
            .collect(Collectors.toList());
        List<Long> deptIds = StrUtil.split(byId.getDeptIdSet(), ",").stream()
            .map(Convert::toLong).collect( Collectors.toList());

        RoleModel roleModel = new RoleModel(byId, roleService, roleMenuService);

        roleModel.setMenuIds(menuIds);
        roleModel.setDeptIds(deptIds);

        return roleModel;
    }

    public RoleModel create() {
        return new RoleModel(roleService, roleMenuService);
    }


}

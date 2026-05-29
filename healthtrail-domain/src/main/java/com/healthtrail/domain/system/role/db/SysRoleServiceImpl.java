package com.healthtrail.domain.system.role.db;

import com.healthtrail.domain.system.menu.db.SysMenuEntity;
import com.healthtrail.domain.system.menu.db.SysMenuMapper;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    private final SysUserMapper userMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public boolean isRoleNameDuplicated(Long roleId, String roleName) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(roleId != null, "role_id", roleId)
            .eq("role_name", roleName);
        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public boolean isRoleKeyDuplicated(Long roleId, String roleKey) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(roleId != null, "role_id", roleId)
            .eq("role_key", roleKey);
        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public boolean isAssignedToUsers(Long roleId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return userMapper.exists(queryWrapper);
    }

    @Override
    public List<SysMenuEntity> getMenuListByRoleId(Long roleId) {
        QueryWrapper<SysRoleEntity> roleQuery = new QueryWrapper<>();
        roleQuery.eq("role_id", roleId).eq("status", 1);
        if (this.baseMapper.selectOne(roleQuery) == null) {
            return Collections.emptyList();
        }

        Set<Long> menuIds = getRoleMenuIds(roleId);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<SysMenuEntity> menuQuery = new QueryWrapper<>();
        menuQuery.in("menu_id", menuIds).eq("status", 1);
        return menuMapper.selectList(menuQuery);
    }

    private Set<Long> getRoleMenuIds(Long roleId) {
        QueryWrapper<SysRoleMenuEntity> roleMenuQuery = new QueryWrapper<>();
        roleMenuQuery.eq("role_id", roleId);
        return roleMenuMapper.selectList(roleMenuQuery).stream()
            .map(SysRoleMenuEntity::getMenuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}

package com.healthtrail.domain.system.menu.db;

import com.healthtrail.domain.system.role.db.SysRoleMenuEntity;
import com.healthtrail.domain.system.role.db.SysRoleMenuMapper;
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
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserMapper userMapper;


    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        Set<Long> roleMenuIds = getRoleMenuIds(roleId);
        if (roleMenuIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<SysMenuEntity> menuQuery = new QueryWrapper<>();
        menuQuery.select("menu_id").in("menu_id", roleMenuIds);
        return this.list(menuQuery).stream()
            .map(SysMenuEntity::getMenuId)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isMenuNameDuplicated(String menuName, Long menuId, Long parentId) {
        QueryWrapper<SysMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("menu_name", menuName)
            .ne(menuId != null, "menu_id", menuId)
            .eq(parentId != null, "parent_id", parentId);
        return this.baseMapper.exists(queryWrapper);
    }


    @Override
    public boolean hasChildrenMenu(Long menuId) {
        QueryWrapper<SysMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", menuId);
        return baseMapper.exists(queryWrapper);
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean isMenuAssignToRoles(Long menuId) {
        QueryWrapper<SysRoleMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("menu_id", menuId);
        return roleMenuMapper.exists(queryWrapper);
    }



    @Override
    public List<SysMenuEntity> getMenuListByUserId(Long userId) {
        SysUserEntity user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return Collections.emptyList();
        }

        Set<Long> roleMenuIds = getRoleMenuIds(user.getRoleId());
        if (roleMenuIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<SysMenuEntity> menuQuery = new QueryWrapper<>();
        menuQuery.in("menu_id", roleMenuIds)
            .eq("status", 1)
            .orderByAsc("parent_id");
        return this.list(menuQuery);
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

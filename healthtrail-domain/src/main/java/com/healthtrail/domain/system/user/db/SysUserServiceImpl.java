package com.healthtrail.domain.system.user.db;

import cn.hutool.core.bean.BeanUtil;
import com.healthtrail.common.core.page.AbstractPageQuery;
import com.healthtrail.domain.system.dept.db.SysDeptEntity;
import com.healthtrail.domain.system.dept.db.SysDeptMapper;
import com.healthtrail.domain.system.menu.db.SysMenuEntity;
import com.healthtrail.domain.system.menu.db.SysMenuMapper;
import com.healthtrail.domain.system.post.db.SysPostEntity;
import com.healthtrail.domain.system.post.db.SysPostMapper;
import com.healthtrail.domain.system.role.db.SysRoleMenuEntity;
import com.healthtrail.domain.system.role.db.SysRoleMenuMapper;
import com.healthtrail.domain.system.role.db.SysRoleEntity;
import com.healthtrail.domain.system.role.db.SysRoleMapper;
import com.healthtrail.domain.system.user.query.SearchUserQuery;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserService {

    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysDeptMapper deptMapper;

    @Override
    public boolean isUserNameDuplicated(String username) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.baseMapper.exists(queryWrapper);
    }


    @Override
    public boolean isPhoneDuplicated(String phone, Long userId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(userId != null, "user_id", userId)
            .eq("phone_number", phone);
        return baseMapper.exists(queryWrapper);
    }


    @Override
    public boolean isEmailDuplicated(String email, Long userId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(userId != null, "user_id", userId)
            .eq("email", email);
        return baseMapper.exists(queryWrapper);
    }


    @Override
    public SysRoleEntity getRoleOfUser(Long userId) {
        SysUserEntity user = this.getById(userId);
        if (user == null || user.getRoleId() == null) {
            return null;
        }
        return roleMapper.selectById(user.getRoleId());
    }


    @Override
    public SysPostEntity getPostOfUser(Long userId) {
        SysUserEntity user = this.getById(userId);
        if (user == null || user.getPostId() == null) {
            return null;
        }
        return postMapper.selectById(user.getPostId());
    }


    @Override
    public Set<String> getMenuPermissionsForUser(Long userId) {
        SysUserEntity user = this.getById(userId);
        if (user == null || user.getRoleId() == null) {
            return Collections.emptySet();
        }

        QueryWrapper<SysRoleEntity> roleQuery = new QueryWrapper<>();
        roleQuery.eq("role_id", user.getRoleId()).eq("status", 1);
        SysRoleEntity role = roleMapper.selectOne(roleQuery);
        if (role == null) {
            return Collections.emptySet();
        }

        Set<Long> menuIds = getRoleMenuIds(role.getRoleId());
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }

        QueryWrapper<SysMenuEntity> menuQuery = new QueryWrapper<>();
        menuQuery.in("menu_id", menuIds).eq("status", 1);
        return menuMapper.selectList(menuQuery).stream()
            .map(SysMenuEntity::getPermission)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    @Override
    public SysUserEntity getUserByUserName(String userName) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userName);
        return this.getOne(queryWrapper);
    }


    @Override
    public Page<SysUserEntity> getUserListByRole(AbstractPageQuery<SysUserEntity> query) {
        return this.page(query.toPage(), query.toQueryWrapper());
    }


    @Override
    public Page<SearchUserDO> getUserList(AbstractPageQuery<SearchUserDO> query) {
        QueryWrapper<SysUserEntity> userQuery = buildUserQueryWrapper(query);
        Page<SearchUserDO> targetPage = query.toPage();
        Page<SysUserEntity> userPage = this.page(new Page<>(targetPage.getCurrent(), targetPage.getSize()), userQuery);
        return toSearchUserPage(userPage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private QueryWrapper<SysUserEntity> buildUserQueryWrapper(AbstractPageQuery<SearchUserDO> query) {
        QueryWrapper<SysUserEntity> userQuery = (QueryWrapper<SysUserEntity>) (QueryWrapper) query.toQueryWrapper();
        if (query instanceof SearchUserQuery) {
            SearchUserQuery<?> searchUserQuery = (SearchUserQuery<?>) query;
            Long deptId = searchUserQuery.getDeptId();
            if (deptId != null) {
                userQuery.in("dept_id", getDeptAndChildIds(deptId));
            }
        }
        return userQuery;
    }

    private Set<Long> getRoleMenuIds(Long roleId) {
        QueryWrapper<SysRoleMenuEntity> roleMenuQuery = new QueryWrapper<>();
        roleMenuQuery.eq("role_id", roleId);
        return roleMenuMapper.selectList(roleMenuQuery).stream()
            .map(SysRoleMenuEntity::getMenuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> getDeptAndChildIds(Long deptId) {
        Set<Long> deptIds = new LinkedHashSet<>();
        deptIds.add(deptId);
        deptMapper.selectList(new QueryWrapper<>()).stream()
            .filter(dept -> isChildDept(dept, deptId))
            .map(SysDeptEntity::getDeptId)
            .filter(Objects::nonNull)
            .forEach(deptIds::add);
        return deptIds;
    }

    private boolean isChildDept(SysDeptEntity dept, Long parentId) {
        if (dept == null || dept.getAncestors() == null || parentId == null) {
            return false;
        }
        String parentIdText = String.valueOf(parentId);
        for (String ancestor : dept.getAncestors().split(",")) {
            if (parentIdText.equals(ancestor.trim())) {
                return true;
            }
        }
        return false;
    }

    private Page<SearchUserDO> toSearchUserPage(Page<SysUserEntity> userPage) {
        Map<Long, SysDeptEntity> deptMap = userPage.getRecords().stream()
            .map(SysUserEntity::getDeptId)
            .filter(Objects::nonNull)
            .distinct()
            .map(deptMapper::selectById)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(SysDeptEntity::getDeptId, Function.identity()));

        List<SearchUserDO> records = userPage.getRecords().stream()
            .map(user -> toSearchUserDO(user, deptMap))
            .collect(Collectors.toList());

        Page<SearchUserDO> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    private SearchUserDO toSearchUserDO(SysUserEntity user, Map<Long, SysDeptEntity> deptMap) {
        SearchUserDO searchUser = new SearchUserDO();
        BeanUtil.copyProperties(user, searchUser);

        SysDeptEntity dept = deptMap.get(user.getDeptId());
        if (dept != null) {
            searchUser.setDeptName(dept.getDeptName());
            searchUser.setDeptLeader(dept.getLeaderName());
        }
        return searchUser;
    }

}

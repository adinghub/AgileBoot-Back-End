package com.healthtrail.domain.system.role;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.common.cache.CacheCenter;
import com.healthtrail.domain.system.role.command.AddRoleCommand;
import com.healthtrail.domain.system.role.command.UpdateDataScopeCommand;
import com.healthtrail.domain.system.role.command.UpdateRoleCommand;
import com.healthtrail.domain.system.role.command.UpdateStatusCommand;
import com.healthtrail.domain.system.role.dto.RoleDTO;
import com.healthtrail.domain.system.role.model.RoleModel;
import com.healthtrail.domain.system.role.model.RoleModelFactory;
import com.healthtrail.domain.system.role.query.AllocatedRoleQuery;
import com.healthtrail.domain.system.role.query.RoleQuery;
import com.healthtrail.domain.system.role.query.UnallocatedRoleQuery;
import com.healthtrail.domain.system.user.dto.UserDTO;
import com.healthtrail.domain.system.user.model.UserModel;
import com.healthtrail.domain.system.user.model.UserModelFactory;
import com.healthtrail.domain.system.role.db.SysRoleEntity;
import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.menu.db.SysMenuService;
import com.healthtrail.domain.system.role.db.SysRoleService;
import com.healthtrail.domain.system.user.db.SysUserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 角色管理应用服务 */
@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    /** 角色领域模型工厂 */
    private final RoleModelFactory roleModelFactory;

    /** 用户领域模型工厂 */
    private final UserModelFactory userModelFactory;

    /** 角色数据库服务 */
    private final SysRoleService roleService;

    /** 用户数据库服务 */
    private final SysUserService userService;

    /** 菜单数据库服务 */
    private final SysMenuService menuService;


    public PageDTO<RoleDTO> getRoleList(RoleQuery query) {
        Page<SysRoleEntity> page = roleService.page(query.toPage(), query.toQueryWrapper());
        List<RoleDTO> records = page.getRecords().stream().map(RoleDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public RoleDTO getRoleInfo(Long roleId) {
        SysRoleEntity byId = roleService.getById(roleId);
        RoleDTO roleDTO = new RoleDTO(byId);
        List<Long> selectedDeptList = StrUtil.split(byId.getDeptIdSet(), ",")
            // `Long(String)` 在新版本 JDK 中已经标记为过时，
            // 这里改成 `Long.valueOf(...)`，既能保持原有语义，
            // 也能消除编译阶段的 deprecation warning。
            .stream().filter(StrUtil::isNotEmpty).map(Long::valueOf).collect(Collectors.toList());
        roleDTO.setSelectedDeptList(selectedDeptList);
        roleDTO.setSelectedMenuList(menuService.getMenuIdsByRoleId(roleId));
        return roleDTO;
    }


    public void addRole(AddRoleCommand addCommand) {
        RoleModel roleModel = roleModelFactory.create();
        roleModel.loadAddCommand(addCommand);

        roleModel.checkRoleNameUnique();
        roleModel.checkRoleKeyUnique();

        roleModel.insert();
    }

    public void deleteRoleByBulk(List<Long> roleIds) {
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                RoleModel roleModel = roleModelFactory.loadById(roleId);

                roleModel.checkRoleCanBeDelete();

                roleModel.deleteById();
            }
        }
    }


    public void updateRole(UpdateRoleCommand updateCommand) {
        RoleModel roleModel = roleModelFactory.loadById(updateCommand.getRoleId());
        roleModel.loadUpdateCommand(updateCommand);

        roleModel.checkRoleKeyUnique();
        roleModel.checkRoleNameUnique();

        roleModel.updateById();
    }

    public void updateStatus(UpdateStatusCommand command) {
        RoleModel roleModel = roleModelFactory.loadById(command.getRoleId());

        roleModel.setStatus(command.getStatus());

        roleModel.updateById();
    }

    public void updateDataScope(UpdateDataScopeCommand command) {
        RoleModel roleModel = roleModelFactory.loadById(command.getRoleId());

        roleModel.setDeptIds(command.getDeptIds());
        roleModel.setDataScope(command.getDataScope());
        roleModel.generateDeptIdSet();

        roleModel.updateById();
    }


    public PageDTO<UserDTO> getAllocatedUserList(AllocatedRoleQuery query) {
        Page<SysUserEntity> page = userService.getUserListByRole(query);
        List<UserDTO> dtoList = page.getRecords().stream().map(UserDTO::new).collect(Collectors.toList());
        return new PageDTO<>(dtoList, page.getTotal());
    }

    public PageDTO<UserDTO> getUnallocatedUserList(UnallocatedRoleQuery query) {
        Page<SysUserEntity> page = userService.getUserListByRole(query);
        List<UserDTO> dtoList = page.getRecords().stream().map(UserDTO::new).collect(Collectors.toList());
        return new PageDTO<>(dtoList, page.getTotal());
    }

    public void deleteRoleOfUserByBulk(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        for (Long userId : userIds) {
            LambdaUpdateWrapper<SysUserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SysUserEntity::getRoleId, null).eq(SysUserEntity::getUserId, userId);

            userService.update(updateWrapper);

            CacheCenter.userCache.delete(userId);
        }
    }

    public void addRoleOfUserByBulk(Long roleId, List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        RoleModel roleModel = roleModelFactory.loadById(roleId);
        roleModel.checkRoleAvailable();

        for (Long userId : userIds) {
            UserModel user = userModelFactory.loadById(userId);
            user.setRoleId(roleId);
            user.updateById();

            CacheCenter.userCache.delete(userId);
        }
    }


}

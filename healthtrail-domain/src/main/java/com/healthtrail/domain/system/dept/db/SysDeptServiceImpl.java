package com.healthtrail.domain.system.dept.db;

import com.healthtrail.domain.system.user.db.SysUserEntity;
import com.healthtrail.domain.system.user.db.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptService {

    private final SysUserMapper userMapper;


    @Override
    public boolean isDeptNameDuplicated(String deptName, Long deptId, Long parentId) {
        QueryWrapper<SysDeptEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_name", deptName)
            .ne(deptId != null, "dept_id", deptId)
            .eq(parentId != null, "parent_id", parentId);

        return this.baseMapper.exists(queryWrapper);
    }


    @Override
    public boolean hasChildrenDept(Long deptId, Boolean enabled) {
        QueryWrapper<SysDeptEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(Boolean.TRUE.equals(enabled), "status", 1);
        return this.baseMapper.selectList(queryWrapper).stream()
            .anyMatch(dept -> Objects.equals(dept.getParentId(), deptId) || isChildDept(dept, deptId));
    }


    @Override
    public boolean isChildOfTheDept(Long parentId, Long childId) {
        return isChildDept(this.getById(childId), parentId);
    }


    @Override
    public boolean isDeptAssignedToUsers(Long deptId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_id", deptId);
        return userMapper.exists(queryWrapper);
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

}

package com.healthtrail.domain.system.dept.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.system.dept.command.AddDeptCommand;
import com.healthtrail.domain.system.dept.command.UpdateDeptCommand;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.domain.system.dept.db.SysDeptEntity;
import com.healthtrail.domain.system.dept.db.SysDeptService;
import java.util.Objects;

/**
 * 部门领域模型，负责部门的增删改命令装载、唯一性校验和祖级路径生成。
 *
 * @author valarchie
 */
public class DeptModel extends SysDeptEntity {

    /** 部门数据库服务 */
    private final SysDeptService deptService;

    public DeptModel(SysDeptService deptService) {
        this.deptService = deptService;
    }

    public DeptModel(SysDeptEntity entity, SysDeptService deptService) {
        if (entity != null) {
            // 如果大数据量的话  可以用MapStruct优化
            BeanUtil.copyProperties(entity, this);
        }
        this.deptService = deptService;
    }

    /** 从新增命令装载部门字段 */
    public void loadAddCommand(AddDeptCommand addCommand) {
        this.setParentId(addCommand.getParentId());
        this.setDeptName(addCommand.getDeptName());
        this.setOrderNum(addCommand.getOrderNum());
        this.setLeaderName(addCommand.getLeaderName());
        this.setPhone(addCommand.getPhone());
        this.setEmail(addCommand.getEmail());
        this.setStatus(addCommand.getStatus());
    }

    /** 从更新命令装载部门字段 */
    public void loadUpdateCommand(UpdateDeptCommand updateCommand) {
        loadAddCommand(updateCommand);
        setStatus(Convert.toInt(updateCommand.getStatus(), 0));
    }

    /** 校验部门名称在同一父级下是否唯一 */
    public void checkDeptNameUnique() {
        if (deptService.isDeptNameDuplicated(getDeptName(), getDeptId(), getParentId())) {
            throw new ApiException(ErrorCode.Business.DEPT_NAME_IS_NOT_UNIQUE, getDeptName());
        }
    }

    /** 校验父部门不能选择自己 */
    public void checkParentIdConflict() {
        if (Objects.equals(getParentId(), getDeptId())) {
            throw new ApiException(ErrorCode.Business.DEPT_PARENT_ID_IS_NOT_ALLOWED_SELF);
        }
    }

    /** 校验是否存在子部门，有则不允许删除 */
    public void checkHasChildDept() {
        if (deptService.hasChildrenDept(getDeptId(), null)) {
            throw new ApiException(ErrorCode.Business.DEPT_EXIST_CHILD_DEPT_NOT_ALLOW_DELETE);
        }
    }

    /** 校验部门是否已分配给用户，已分配则不允许删除 */
    public void checkDeptAssignedToUsers() {
        if (deptService.isDeptAssignedToUsers(getDeptId())) {
            throw new ApiException(ErrorCode.Business.DEPT_EXIST_LINK_USER_NOT_ALLOW_DELETE);
        }
    }

    /** 根据父级部门生成祖级路径列表 */
    public void generateAncestors() {

        // 处理 getParentId 可能为 null 的情况
        if (getParentId() == null || getParentId() == 0) {
            setAncestors(String.valueOf(getParentId() == null ? 0 : getParentId()));
            return;
        }

        SysDeptEntity parentDept = deptService.getById(getParentId());

        // 检查 parentDept 是否为 null 或者状态为禁用
        if (parentDept == null || StatusEnum.DISABLE.equals(
            BasicEnumUtil.fromValue(StatusEnum.class, parentDept.getStatus()))) {
            throw new ApiException(ErrorCode.Business.DEPT_PARENT_DEPT_NO_EXIST_OR_DISABLED);
        }

        // 处理 parentDept.getAncestors() 可能为 null 的情况
        String ancestors = parentDept.getAncestors() == null ? "" : parentDept.getAncestors();
        setAncestors(ancestors + "," + getParentId());
    }


    /**
     * 校验部门状态是否允许变更。
     * DDD 有些阻抗，如果为了追求性能的话还是得通过数据库的方式来判断。
     */
    public void checkStatusAllowChange() {
        if (StatusEnum.DISABLE.getValue().equals(getStatus()) &&
            deptService.hasChildrenDept(getDeptId(), true)) {
            throw new ApiException(ErrorCode.Business.DEPT_STATUS_ID_IS_NOT_ALLOWED_CHANGE);
        }

    }

}

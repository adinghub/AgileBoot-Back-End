package com.healthtrail.domain.system.role.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色信息表，定义角色及对应的数据权限范围
 *
 * @author valarchie
 * @since 2022-10-02
 */
@Getter
@Setter
@TableName("sys_role")
@ApiModel(value = "SysRoleEntity对象", description = "角色信息表")
public class SysRoleEntity extends BaseEntity<SysRoleEntity> {

    private static final long serialVersionUID = 1L;

    /** 角色主键ID */
    @ApiModelProperty("角色ID")
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /** 角色名称 */
    @ApiModelProperty("角色名称")
    @TableField("role_name")
    private String roleName;

    /** 角色权限标识字符串，用于后端鉴权 */
    @ApiModelProperty("角色权限字符串")
    @TableField("role_key")
    private String roleKey;

    /** 角色显示排序号 */
    @ApiModelProperty("显示顺序")
    @TableField("role_sort")
    private Integer roleSort;

    /** 数据权限范围：1全部 2自定义 3本部门 4本部门及以下 5仅本人 */
    @ApiModelProperty("数据范围（1：全部数据权限 2：自定数据权限 3: 本部门数据权限 4: 本部门及以下数据权限 5: 本人权限）")
    @TableField("data_scope")
    private Integer dataScope;

    /** 自定义数据权限时，角色拥有的部门ID集合 */
    @ApiModelProperty("角色所拥有的部门数据权限")
    @TableField("dept_id_set")
    private String deptIdSet;

    /** 角色状态，1正常 0停用 */
    @ApiModelProperty("角色状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.roleId;
    }

}

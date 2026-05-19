package com.healthtrail.domain.system.dept.db;

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
 * 系统部门组织架构表，支持树形层级结构
 *
 * @author valarchie
 * @since 2022-10-02
 */
@Getter
@Setter
@TableName("sys_dept")
@ApiModel(value = "SysDeptEntity对象", description = "部门表")
public class SysDeptEntity extends BaseEntity<SysDeptEntity> {

    private static final long serialVersionUID = 1L;

    /** 部门主键ID */
    @ApiModelProperty("部门id")
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;

    /** 父部门ID，用于构建部门树形结构 */
    @ApiModelProperty("父部门id")
    @TableField("parent_id")
    private Long parentId;

    /** 祖级列表，记录从根节点到当前部门的全部祖先ID路径 */
    @ApiModelProperty("祖级列表")
    @TableField("ancestors")
    private String ancestors;

    /** 部门名称 */
    @ApiModelProperty("部门名称")
    @TableField("dept_name")
    private String deptName;

    /** 在同级部门中的显示排序号 */
    @ApiModelProperty("显示顺序")
    @TableField("order_num")
    private Integer orderNum;

    /** 部门负责人的用户ID */
    @TableField("leader_id")
    private Long leaderId;

    /** 部门负责人姓名 */
    @ApiModelProperty("负责人")
    @TableField("leader_name")
    private String leaderName;

    /** 部门联系电话 */
    @ApiModelProperty("联系电话")
    @TableField("phone")
    private String phone;

    /** 部门联系邮箱 */
    @ApiModelProperty("邮箱")
    @TableField("email")
    private String email;

    /** 部门状态，0正常 1停用 */
    @ApiModelProperty("部门状态（0正常 1停用）")
    @TableField("status")
    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.deptId;
    }

}

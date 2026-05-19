package com.healthtrail.domain.system.user.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统管理用户表，存储后台管理员账号信息
 *
 * @author valarchie
 * @since 2023-02-27
 */
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "SysUserEntity对象", description = "用户信息表")
public class SysUserEntity extends BaseEntity<SysUserEntity> {

    private static final long serialVersionUID = 1L;

    /** 用户主键ID */
    @ApiModelProperty("用户ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 用户所属岗位ID */
    @ApiModelProperty("职位id")
    @TableField("post_id")
    private Long postId;

    /** 用户所属角色ID */
    @ApiModelProperty("角色id")
    @TableField("role_id")
    private Long roleId;

    /** 用户所属部门ID */
    @ApiModelProperty("部门ID")
    @TableField("dept_id")
    private Long deptId;

    /** 登录用户名 */
    @ApiModelProperty("用户账号")
    @TableField("username")
    private String username;

    /** 用户显示昵称 */
    @ApiModelProperty("用户昵称")
    @TableField("nickname")
    private String nickname;

    /** 用户类型，00表示系统内置用户 */
    @ApiModelProperty("用户类型（00系统用户）")
    @TableField("user_type")
    private Integer userType;

    /** 用户邮箱地址 */
    @ApiModelProperty("用户邮箱")
    @TableField("email")
    private String email;

    /** 用户手机号码 */
    @ApiModelProperty("手机号码")
    @TableField("phone_number")
    private String phoneNumber;

    /** 用户性别，0男 1女 2未知 */
    @ApiModelProperty("用户性别（0男 1女 2未知）")
    @TableField("sex")
    private Integer sex;

    /** 用户头像URL地址 */
    @ApiModelProperty("头像地址")
    @TableField("avatar")
    private String avatar;

    /** 加密后的用户密码 */
    @ApiModelProperty("密码")
    @TableField("password")
    private String password;

    /** 账号状态，1正常 2停用 3冻结 */
    @ApiModelProperty("帐号状态（1正常 2停用 3冻结）")
    @TableField("status")
    private Integer status;

    /** 用户最后登录的IP地址 */
    @ApiModelProperty("最后登录IP")
    @TableField("login_ip")
    private String loginIp;

    /** 用户最后登录时间 */
    @ApiModelProperty("最后登录时间")
    @TableField("login_date")
    private Date loginDate;

    /** 是否为超级管理员，1是 0否 */
    @ApiModelProperty("超级管理员标志（1是，0否）")
    @TableField("is_admin")
    private Boolean isAdmin;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.userId;
    }

}

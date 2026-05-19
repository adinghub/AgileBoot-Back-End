package com.healthtrail.domain.health.user.db;

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
 * App用户信息表，专门服务于移动端App用户，与后台管理员账号体系独立
 *
 * <p>该表只服务于移动 App 用户，不承载后台管理员、角色、部门等后台概念，
 * 这样后续健康系统的家庭成员、报告、提醒等业务都可以围绕这张表独立演进。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("app_user")
@ApiModel(value = "HealthAppUserEntity对象", description = "健康系统App用户表")
public class HealthAppUserEntity extends BaseEntity<HealthAppUserEntity> {

    private static final long serialVersionUID = 1L;

    /** App用户主键ID */
    @ApiModelProperty("App用户ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 用户手机号码 */
    @ApiModelProperty("手机号")
    @TableField("mobile")
    private String mobile;

    /** 用户昵称 */
    @ApiModelProperty("昵称")
    @TableField("nickname")
    private String nickname;

    /** 用户头像URL地址 */
    @ApiModelProperty("头像地址")
    @TableField("avatar")
    private String avatar;

    /** 加密后的用户密码 */
    @ApiModelProperty("密码")
    @TableField("password")
    private String password;

    /** 账号状态，1正常 0停用 */
    @ApiModelProperty("账号状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    /** 用户最后登录的IP地址 */
    @ApiModelProperty("最后登录IP")
    @TableField("last_login_ip")
    private String lastLoginIp;

    /** 用户最后登录时间 */
    @ApiModelProperty("最后登录时间")
    @TableField("last_login_time")
    private Date lastLoginTime;

    /** 用户注册来源，如微信小程序、App */
    @ApiModelProperty("注册来源")
    @TableField("register_source")
    private String registerSource;

    @Override
    public Serializable pkVal() {
        return this.userId;
    }
}

package com.healthtrail.domain.system.log.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统登录访问记录表，记录每次登录的IP、浏览器、状态等信息
 *
 * @author valarchie
 * @since 2022-10-02
 */
@Getter
@Setter
@TableName("sys_login_info")
@ApiModel(value = "SysLoginInfoEntity对象", description = "系统访问记录")
public class SysLoginInfoEntity extends Model<SysLoginInfoEntity> {

    private static final long serialVersionUID = 1L;

    /** 登录记录主键ID */
    @ApiModelProperty("访问ID")
    @TableId(value = "info_id", type = IdType.AUTO)
    private Long infoId;

    /** 登录用户名 */
    @ApiModelProperty("用户账号")
    @TableField("username")
    private String username;

    /** 登录时的IP地址 */
    @ApiModelProperty("登录IP地址")
    @TableField("ip_address")
    private String ipAddress;

    /** 根据IP解析的登录地点 */
    @ApiModelProperty("登录地点")
    @TableField("login_location")
    private String loginLocation;

    /** 登录使用的浏览器类型 */
    @ApiModelProperty("浏览器类型")
    @TableField("browser")
    private String browser;

    /** 登录时的操作系统 */
    @ApiModelProperty("操作系统")
    @TableField("operation_system")
    private String operationSystem;

    /** 登录结果状态，1成功 0失败 */
    @ApiModelProperty("登录状态（1成功 0失败）")
    @TableField("status")
    private Integer status;

    /** 登录失败时的提示消息 */
    @ApiModelProperty("提示消息")
    @TableField("msg")
    private String msg;

    /** 登录发生时间 */
    @ApiModelProperty("访问时间")
    @TableField("login_time")
    private Date loginTime;

    /** 逻辑删除标记，0未删除 1已删除 */
    @ApiModelProperty("逻辑删除")
    @TableField("deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;


    @Override
    public Serializable pkVal() {
        return this.infoId;
    }

}

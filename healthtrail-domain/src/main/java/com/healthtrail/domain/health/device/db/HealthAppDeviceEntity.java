package com.healthtrail.domain.health.device.db;

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
 * App设备信息表，记录用户绑定的推送设备及Token
 *
 * <p>这张表专门承接"某个 App 用户当前绑定了哪些推送设备"：
 * 1. 支持一个账号绑定多个设备
 * 2. 支持同一设备切换账号时重新绑定
 * 3. 为后续真实 Push 发送提供设备 Token 数据源
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("app_device")
@ApiModel(value = "HealthAppDeviceEntity对象", description = "健康系统App设备表")
public class HealthAppDeviceEntity extends BaseEntity<HealthAppDeviceEntity> {

    private static final long serialVersionUID = 1L;

    /** 设备记录主键ID */
    @ApiModelProperty("设备ID")
    @TableId(value = "device_id", type = IdType.AUTO)
    private Long deviceId;

    /** 设备归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 设备唯一编码，用于去重识别 */
    @ApiModelProperty("设备唯一编码")
    @TableField("device_code")
    private String deviceCode;

    /** 推送平台类型，如JPush、APNs等 */
    @ApiModelProperty("推送平台")
    @TableField("push_platform")
    private String pushPlatform;

    /** 推送服务注册返回的设备Token */
    @ApiModelProperty("设备Token")
    @TableField("device_token")
    private String deviceToken;

    /** 设备硬件型号 */
    @ApiModelProperty("设备型号")
    @TableField("device_model")
    private String deviceModel;

    /** 设备生产厂商 */
    @ApiModelProperty("设备厂商")
    @TableField("manufacturer")
    private String manufacturer;

    /** 设备操作系统版本 */
    @ApiModelProperty("系统版本")
    @TableField("os_version")
    private String osVersion;

    /** 当前安装的App版本号 */
    @ApiModelProperty("App版本")
    @TableField("app_version")
    private String appVersion;

    /** 设备最后一次活跃时间 */
    @ApiModelProperty("最后活跃时间")
    @TableField("last_active_time")
    private Date lastActiveTime;

    /** 设备状态，1正常 0停用 */
    @ApiModelProperty("状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    @Override
    public Serializable pkVal() {
        return this.deviceId;
    }
}

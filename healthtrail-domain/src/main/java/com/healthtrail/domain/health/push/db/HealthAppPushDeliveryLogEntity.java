package com.healthtrail.domain.health.push.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * App Push设备级派发审计表，记录每次Push命中各设备的分发结果
 *
 * <p>该表专门记录"一次 Push 最终命中了哪些设备、每台设备当时的结果如何"，
 * 用来补齐消息中心表只有"整体最近一次结果"而缺少设备级现场的问题。
 *
 * <p>注意这张表保存的是"发送现场快照"，因此像：
 * 1. 设备状态快照；
 * 2. 脱敏后的 Token；
 * 3. 失败原因分类；
 *
 * <p>都需要在发送当下写入，避免后续设备状态变化后无法回放历史现场。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("app_push_delivery_log")
@ApiModel(value = "HealthAppPushDeliveryLogEntity对象", description = "健康系统App Push设备级派发审计表")
public class HealthAppPushDeliveryLogEntity extends BaseEntity<HealthAppPushDeliveryLogEntity> {

    private static final long serialVersionUID = 1L;

    /** 派发审计记录主键ID */
    @ApiModelProperty("设备级派发审计ID")
    @TableId(value = "delivery_id", type = IdType.AUTO)
    private Long deliveryId;

    /** 关联的消息ID */
    @ApiModelProperty("消息ID")
    @TableField("message_id")
    private Long messageId;

    /** 派发目标用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 业务场景编码 */
    @ApiModelProperty("业务场景编码")
    @TableField("business_scene")
    private String businessScene;

    /** 关联的业务数据主键ID */
    @ApiModelProperty("业务主键ID")
    @TableField("business_id")
    private Long businessId;

    /** 目标设备ID */
    @ApiModelProperty("设备ID")
    @TableField("device_id")
    private Long deviceId;

    /** 目标设备唯一编码 */
    @ApiModelProperty("设备唯一编码")
    @TableField("device_code")
    private String deviceCode;

    /** 推送平台类型 */
    @ApiModelProperty("推送平台")
    @TableField("push_platform")
    private String pushPlatform;

    /** 发送当时的设备状态快照 */
    @ApiModelProperty("发送当时的设备状态快照")
    @TableField("device_status_snapshot")
    private Integer deviceStatusSnapshot;

    /** 脱敏后的设备推送Token */
    @ApiModelProperty("脱敏后的设备Token")
    @TableField("device_token_masked")
    private String deviceTokenMasked;

    /** 使用的发送推送通道 */
    @ApiModelProperty("发送通道")
    @TableField("send_channel")
    private String sendChannel;

    /** 发送状态，如成功、失败 */
    @ApiModelProperty("发送状态")
    @TableField("send_status")
    private Integer sendStatus;

    /** 失败原因分类，如Token无效、设备离线等 */
    @ApiModelProperty("失败原因分类")
    @TableField("failure_reason_category")
    private String failureReasonCategory;

    /** 失败原因的详细说明 */
    @ApiModelProperty("失败原因说明")
    @TableField("failure_reason_message")
    private String failureReasonMessage;

    /** 推送厂商返回的结果码 */
    @ApiModelProperty("厂商结果码")
    @TableField("vendor_code")
    private String vendorCode;

    /** 推送厂商返回的结果说明 */
    @ApiModelProperty("厂商结果说明")
    @TableField("vendor_message")
    private String vendorMessage;

    /** 本条消息对该设备的第几次重试 */
    @ApiModelProperty("本条消息针对该设备的第几次尝试")
    @TableField("retry_no")
    private Integer retryNo;

    /** Push发送时间 */
    @ApiModelProperty("发送时间")
    @TableField("send_time")
    private Date sendTime;

    @Override
    public Serializable pkVal() {
        return this.deliveryId;
    }
}

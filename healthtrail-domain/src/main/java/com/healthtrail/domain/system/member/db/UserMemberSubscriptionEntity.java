package com.healthtrail.domain.system.member.db;

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
 * 用户会员订阅表，管理会员自动续费订阅周期及状态
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("user_member_subscription")
@ApiModel(value = "UserMemberSubscriptionEntity对象", description = "用户会员订阅表")
public class UserMemberSubscriptionEntity extends BaseEntity<UserMemberSubscriptionEntity> {

    private static final long serialVersionUID = 1L;

    /** 订阅主键ID */
    @TableId(value = "subscription_id", type = IdType.AUTO)
    @ApiModelProperty("订阅ID")
    private Long subscriptionId;

    /** 订阅编号，唯一业务标识 */
    @TableField("subscription_no")
    @ApiModelProperty("订阅编号")
    private String subscriptionNo;

    /** 订阅所属用户ID */
    @TableField("user_id")
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 订阅的会员等级ID */
    @TableField("member_level_id")
    @ApiModelProperty("会员等级ID")
    private Long memberLevelId;

    /** 订阅当前状态，如正常、已取消、已过期 */
    @TableField("subscription_status")
    @ApiModelProperty("订阅状态")
    private String subscriptionStatus;

    /** 是否开启自动续费 */
    @TableField("auto_renew")
    @ApiModelProperty("是否自动续费")
    private Integer autoRenew;

    /** 当前订阅周期开始时间 */
    @TableField("current_period_start_time")
    @ApiModelProperty("当前周期开始时间")
    private Date currentPeriodStartTime;

    /** 当前订阅周期结束时间 */
    @TableField("current_period_end_time")
    @ApiModelProperty("当前周期结束时间")
    private Date currentPeriodEndTime;

    /** 下次预计续费时间 */
    @TableField("next_renew_time")
    @ApiModelProperty("下次续费时间")
    private Date nextRenewTime;

    /** 最近一次续费时间 */
    @TableField("last_renew_time")
    @ApiModelProperty("最近续费时间")
    private Date lastRenewTime;

    /** 连续续费失败的次数 */
    @TableField("failed_renew_count")
    @ApiModelProperty("连续失败次数")
    private Integer failedRenewCount;

    /** 用户取消订阅的时间 */
    @TableField("cancel_time")
    @ApiModelProperty("取消时间")
    private Date cancelTime;

    /** 取消订阅的原因 */
    @TableField("cancel_reason")
    @ApiModelProperty("取消原因")
    private String cancelReason;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.subscriptionId;
    }
}

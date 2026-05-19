package com.healthtrail.domain.system.member.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户会员订单表，记录会员购买、续费、兑换等订单信息
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("user_member_order")
@ApiModel(value = "UserMemberOrderEntity对象", description = "用户会员订单表")
public class UserMemberOrderEntity extends BaseEntity<UserMemberOrderEntity> {

    private static final long serialVersionUID = 1L;

    /** 订单主键ID */
    @TableId(value = "user_member_order_id", type = IdType.AUTO)
    @ApiModelProperty("用户会员订单ID")
    private Long userMemberOrderId;

    /** 订单号，唯一业务标识 */
    @TableField("order_no")
    @ApiModelProperty("订单号")
    private String orderNo;

    /** 关联的订阅ID */
    @TableField("subscription_id")
    @ApiModelProperty("订阅ID")
    private Long subscriptionId;

    /** 下单用户ID */
    @TableField("user_id")
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 购买的会员等级ID */
    @TableField("member_level_id")
    @ApiModelProperty("会员等级ID")
    private Long memberLevelId;

    /** 订单类型，如购买、续费、升级 */
    @TableField("order_type")
    @ApiModelProperty("订单类型")
    private String orderType;

    /** 订单当前状态，如待支付、已支付、已取消 */
    @TableField("order_status")
    @ApiModelProperty("订单状态")
    private String orderStatus;

    /** 订单来源类型，如移动端、管理后台 */
    @TableField("source_type")
    @ApiModelProperty("来源类型")
    private String sourceType;

    /** 订单金额 */
    @TableField("order_amount")
    @ApiModelProperty("订单金额")
    private BigDecimal orderAmount;

    /** 支付完成时间 */
    @TableField("pay_time")
    @ApiModelProperty("支付时间")
    private Date payTime;

    /** 会员生效开始时间 */
    @TableField("effective_start_time")
    @ApiModelProperty("会员开始时间")
    private Date effectiveStartTime;

    /** 会员生效结束时间 */
    @TableField("effective_end_time")
    @ApiModelProperty("会员结束时间")
    private Date effectiveEndTime;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.userMemberOrderId;
    }
}

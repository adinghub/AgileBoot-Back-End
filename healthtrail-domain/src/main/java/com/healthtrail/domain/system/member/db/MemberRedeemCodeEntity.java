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
 * 会员兑换码表，管理兑换码的生成、状态及兑换记录
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_redeem_code")
@ApiModel(value = "MemberRedeemCodeEntity对象", description = "会员兑换码表")
public class MemberRedeemCodeEntity extends BaseEntity<MemberRedeemCodeEntity> {

    private static final long serialVersionUID = 1L;

    /** 兑换码主键ID */
    @TableId(value = "member_redeem_code_id", type = IdType.AUTO)
    @ApiModelProperty("会员兑换码ID")
    private Long memberRedeemCodeId;

    /** 批量生成时的批次号 */
    @TableField("batch_no")
    @ApiModelProperty("批次号")
    private String batchNo;

    /** 兑换码字符串 */
    @TableField("redeem_code")
    @ApiModelProperty("兑换码")
    private String redeemCode;

    /** 兑换后获得的会员等级ID */
    @TableField("member_level_id")
    @ApiModelProperty("会员等级ID")
    private Long memberLevelId;

    /** 生成时的等级编码快照 */
    @TableField("level_code_snapshot")
    @ApiModelProperty("等级编码快照")
    private String levelCodeSnapshot;

    /** 生成时的等级名称快照 */
    @TableField("level_name_snapshot")
    @ApiModelProperty("等级名称快照")
    private String levelNameSnapshot;

    /** 生成时的价格快照 */
    @TableField("price_snapshot")
    @ApiModelProperty("价格快照")
    private BigDecimal priceSnapshot;

    /** 生成时的有效天数快照 */
    @TableField("duration_days_snapshot")
    @ApiModelProperty("时长快照")
    private Integer durationDaysSnapshot;

    /** 兑换码当前状态，如未使用、已兑换、已过期 */
    @TableField("code_status")
    @ApiModelProperty("兑换码状态")
    private String codeStatus;

    /** 兑换该码的用户ID */
    @TableField("redeemed_user_id")
    @ApiModelProperty("兑换用户ID")
    private Long redeemedUserId;

    /** 兑换时间 */
    @TableField("redeemed_time")
    @ApiModelProperty("兑换时间")
    private Date redeemedTime;

    /** 兑换码过期时间 */
    @TableField("expire_time")
    @ApiModelProperty("过期时间")
    private Date expireTime;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberRedeemCodeId;
    }
}

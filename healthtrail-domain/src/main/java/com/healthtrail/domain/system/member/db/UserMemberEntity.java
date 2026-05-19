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
 * 用户与会员等级关系表，记录用户当前持有的会员及其有效期
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("user_member")
@ApiModel(value = "UserMemberEntity对象", description = "用户会员关系表")
public class UserMemberEntity extends BaseEntity<UserMemberEntity> {

    private static final long serialVersionUID = 1L;

    /** 用户会员关系主键ID */
    @TableId(value = "user_member_id", type = IdType.AUTO)
    @ApiModelProperty("用户会员ID")
    private Long userMemberId;

    /** 会员所属用户ID */
    @TableField("user_id")
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 会员等级ID */
    @TableField("member_level_id")
    @ApiModelProperty("会员等级ID")
    private Long memberLevelId;

    /** 会员生效开始时间 */
    @TableField("effective_start_time")
    @ApiModelProperty("会员开始时间")
    private Date effectiveStartTime;

    /** 会员生效结束时间 */
    @TableField("effective_end_time")
    @ApiModelProperty("会员结束时间")
    private Date effectiveEndTime;

    /** 会员当前状态，如有效、已过期 */
    @TableField("status")
    @ApiModelProperty("会员状态")
    private String status;

    /** 会员来源类型，如购买、兑换、赠送 */
    @TableField("source_type")
    @ApiModelProperty("来源类型")
    private String sourceType;

    /** 来源记录ID，关联订单或兑换码等 */
    @TableField("source_id")
    @ApiModelProperty("来源记录ID")
    private Long sourceId;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.userMemberId;
    }
}

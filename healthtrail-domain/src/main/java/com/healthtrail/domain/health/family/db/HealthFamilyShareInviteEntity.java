package com.healthtrail.domain.health.family.db;

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
 * 家庭共享邀请表，记录家庭成员共享邀请的生成与接受过程
 *
 * <p>当前版本采用"邀请码"方式衔接两个 App 账号，
 * 先生成邀请，再由另一账号输入邀请码完成接受。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("family_share_invite")
@ApiModel(value = "HealthFamilyShareInviteEntity对象", description = "健康系统家庭共享邀请表")
public class HealthFamilyShareInviteEntity extends BaseEntity<HealthFamilyShareInviteEntity> {

    private static final long serialVersionUID = 1L;

    /** 邀请记录主键ID */
    @ApiModelProperty("共享邀请ID")
    @TableId(value = "invite_id", type = IdType.AUTO)
    private Long inviteId;

    /** 被邀请共享的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 发起邀请的主账号用户ID */
    @ApiModelProperty("主账号App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 分享给对方的邀请码 */
    @ApiModelProperty("邀请码")
    @TableField("invite_code")
    private String inviteCode;

    /** 接受邀请的用户ID */
    @ApiModelProperty("接受邀请的App用户ID")
    @TableField("invitee_user_id")
    private Long inviteeUserId;

    /** 邀请时的共享角色权限 */
    @ApiModelProperty("邀请角色")
    @TableField("share_role")
    private String shareRole;

    /** 邀请状态，如待接受、已接受、已过期 */
    @ApiModelProperty("邀请状态")
    @TableField("invite_status")
    private Integer inviteStatus;

    /** 邀请码过期时间 */
    @ApiModelProperty("过期时间")
    @TableField("expire_time")
    private Date expireTime;

    /** 被邀请方接受的时间 */
    @ApiModelProperty("接受时间")
    @TableField("accepted_time")
    private Date acceptedTime;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.inviteId;
    }
}

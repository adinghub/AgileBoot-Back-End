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
 * 家庭成员共享关系表，记录家庭成员已被共享给哪个协同账号
 *
 * <p>该表描述"某个家庭成员已经被共享给哪个 App 账号"，
 * 它和邀请表的职责区别如下：
 * 1. 邀请表记录邀请过程
 * 2. 共享关系表记录最终生效的协同权限
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("family_member_share")
@ApiModel(value = "HealthFamilyMemberShareEntity对象", description = "健康系统家庭成员共享关系表")
public class HealthFamilyMemberShareEntity extends BaseEntity<HealthFamilyMemberShareEntity> {

    private static final long serialVersionUID = 1L;

    /** 共享关系主键ID */
    @ApiModelProperty("共享关系ID")
    @TableId(value = "share_id", type = IdType.AUTO)
    private Long shareId;

    /** 被共享的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 家庭成员的主账号用户ID */
    @ApiModelProperty("主账号App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 被授权协同查看的用户ID */
    @ApiModelProperty("协同账号App用户ID")
    @TableField("collaborator_user_id")
    private Long collaboratorUserId;

    /** 关联的邀请记录ID */
    @ApiModelProperty("来源邀请ID")
    @TableField("invite_id")
    private Long inviteId;

    /** 共享角色权限，如只读、可编辑 */
    @ApiModelProperty("共享角色")
    @TableField("share_role")
    private String shareRole;

    /** 共享关系状态，如正常、已解除 */
    @ApiModelProperty("共享状态")
    @TableField("share_status")
    private Integer shareStatus;

    /** 协同方接受共享的时间 */
    @ApiModelProperty("接受时间")
    @TableField("accepted_time")
    private Date acceptedTime;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.shareId;
    }
}

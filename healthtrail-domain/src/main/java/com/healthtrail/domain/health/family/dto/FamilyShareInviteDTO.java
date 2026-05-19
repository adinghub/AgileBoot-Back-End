package com.healthtrail.domain.health.family.dto;

import com.healthtrail.common.enums.health.FamilyMemberAccessRoleEnum;
import com.healthtrail.common.enums.health.FamilyShareInviteStatusEnum;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.family.db.HealthFamilyShareInviteEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家庭共享邀请返回对象。
 */
@Data
@NoArgsConstructor
public class FamilyShareInviteDTO {

    /** 邀请ID。 */
    private Long inviteId;

    /** 成员ID。 */
    private Long memberId;

    /** 归属用户ID。 */
    private Long ownerUserId;

    /** 邀请码。 */
    private String inviteCode;

    /** 接受用户ID。 */
    private Long inviteeUserId;

    /** 接受手机号。 */
    private String inviteeMobile;

    /** 接受昵称。 */
    private String inviteeNickname;

    /** 共享角色。 */
    private String shareRole;

    /** 共享角色名。 */
    private String shareRoleName;

    /** 邀请状态。 */
    private Integer inviteStatus;

    /** 邀请状态名。 */
    private String inviteStatusName;

    /** 过期时间。 */
    private Date expireTime;

    /** 接受时间。 */
    private Date acceptedTime;

    /** 备注。 */
    private String remark;

    public FamilyShareInviteDTO(HealthFamilyShareInviteEntity entity) {
        if (entity != null) {
            this.inviteId = entity.getInviteId();
            this.memberId = entity.getMemberId();
            this.ownerUserId = entity.getOwnerUserId();
            this.inviteCode = entity.getInviteCode();
            this.inviteeUserId = entity.getInviteeUserId();
            this.shareRole = entity.getShareRole();
            FamilyMemberAccessRoleEnum roleEnum = resolveRole(entity.getShareRole());
            this.shareRoleName = roleEnum == null ? null : HealthAppI18n.familyAccessRoleName(roleEnum.getValue());
            this.inviteStatus = entity.getInviteStatus();
            FamilyShareInviteStatusEnum statusEnum = resolveStatus(entity.getInviteStatus());
            this.inviteStatusName = statusEnum == null ? null : HealthAppI18n.familyInviteStatusName(statusEnum.getValue());
            this.expireTime = entity.getExpireTime();
            this.acceptedTime = entity.getAcceptedTime();
            this.remark = entity.getRemark();
        }
    }

    private FamilyMemberAccessRoleEnum resolveRole(String shareRoleValue) {
        for (FamilyMemberAccessRoleEnum item : FamilyMemberAccessRoleEnum.values()) {
            if (item.getValue().equals(shareRoleValue)) {
                return item;
            }
        }
        return null;
    }

    private FamilyShareInviteStatusEnum resolveStatus(Integer inviteStatusValue) {
        if (inviteStatusValue == null) {
            return null;
        }
        for (FamilyShareInviteStatusEnum item : FamilyShareInviteStatusEnum.values()) {
            if (item.getValue().equals(inviteStatusValue)) {
                return item;
            }
        }
        return null;
    }
}

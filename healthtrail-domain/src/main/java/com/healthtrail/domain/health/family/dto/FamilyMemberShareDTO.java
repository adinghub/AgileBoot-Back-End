package com.healthtrail.domain.health.family.dto;

import com.healthtrail.common.enums.health.FamilyMemberAccessRoleEnum;
import com.healthtrail.common.enums.health.FamilyMemberShareStatusEnum;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberShareEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家庭成员协同账号返回对象。
 */
@Data
@NoArgsConstructor
public class FamilyMemberShareDTO {

    /** 共享关系ID。 */
    private Long shareId;

    /** 成员ID。 */
    private Long memberId;

    /** 归属用户ID。 */
    private Long ownerUserId;

    /** 协同用户ID。 */
    private Long collaboratorUserId;

    /** 协同手机号。 */
    private String collaboratorMobile;

    /** 协同昵称。 */
    private String collaboratorNickname;

    /** 共享角色。 */
    private String shareRole;

    /** 共享角色名。 */
    private String shareRoleName;

    /** 共享状态。 */
    private Integer shareStatus;

    /** 共享状态名。 */
    private String shareStatusName;

    /** 接受时间。 */
    private Date acceptedTime;

    /** 备注。 */
    private String remark;

    public FamilyMemberShareDTO(HealthFamilyMemberShareEntity entity) {
        if (entity != null) {
            this.shareId = entity.getShareId();
            this.memberId = entity.getMemberId();
            this.ownerUserId = entity.getOwnerUserId();
            this.collaboratorUserId = entity.getCollaboratorUserId();
            this.shareRole = entity.getShareRole();
            FamilyMemberAccessRoleEnum roleEnum = resolveRole(entity.getShareRole());
            this.shareRoleName = roleEnum == null ? null : HealthAppI18n.familyAccessRoleName(roleEnum.getValue());
            this.shareStatus = entity.getShareStatus();
            FamilyMemberShareStatusEnum statusEnum = resolveStatus(entity.getShareStatus());
            this.shareStatusName = statusEnum == null ? null : HealthAppI18n.familyShareStatusName(statusEnum.getValue());
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

    private FamilyMemberShareStatusEnum resolveStatus(Integer shareStatusValue) {
        if (shareStatusValue == null) {
            return null;
        }
        for (FamilyMemberShareStatusEnum item : FamilyMemberShareStatusEnum.values()) {
            if (item.getValue().equals(shareStatusValue)) {
                return item;
            }
        }
        return null;
    }
}

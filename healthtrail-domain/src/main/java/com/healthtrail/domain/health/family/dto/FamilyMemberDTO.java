package com.healthtrail.domain.health.family.dto;

import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家庭成员返回对象。
 */
@Data
@NoArgsConstructor
public class FamilyMemberDTO {

    /** 成员ID。 */
    private Long memberId;

    /**
     * 家庭成员业务编码。
     *
     * <p>移动端与后台管理端展示时，优先展示该编码而不是内部主键，
     * 这样更适合作为客服沟通、截图排障和业务录入时的人类可读标识。
     */
    private String memberCode;

    /** 归属用户ID。 */
    private Long ownerUserId;

    /** 成员姓名。 */
    private String memberName;

    /** 性别。 */
    private Integer gender;

    /** 出生日期。 */
    private Date birthday;

    /** 家庭关系。 */
    private String relationType;

    /** 身高。 */
    private BigDecimal height;

    /** 体重。 */
    private BigDecimal weight;

    /** 血型。 */
    private String bloodType;

    /** 过敏史。 */
    private String allergyHistory;

    /** 慢病史。 */
    private String chronicHistory;

    /** 备注。 */
    private String remark;

    /** 成员状态。 */
    private Integer status;

    /**
     * 当前登录账号访问该成员的来源。
     * OWNER 表示我自己创建的成员，SHARED 表示别人共享给我的成员。
     */
    private String accessSource;

    /**
     * 访问来源名称。
     */
    private String accessSourceName;

    /**
     * 当前登录账号对该成员的访问角色。
     */
    private String accessRole;

    /**
     * 访问角色名称。
     */
    private String accessRoleName;

    /**
     * 是否是主账号。
     */
    private Boolean isOwner;

    /**
     * 是否允许编辑。
     */
    private Boolean canEdit;

    public FamilyMemberDTO(HealthFamilyMemberEntity entity) {
        if (entity != null) {
            this.memberId = entity.getMemberId();
            this.memberCode = entity.getMemberCode();
            this.ownerUserId = entity.getOwnerUserId();
            this.memberName = entity.getMemberName();
            this.gender = entity.getGender();
            this.birthday = entity.getBirthday();
            this.relationType = entity.getRelationType();
            this.height = entity.getHeight();
            this.weight = entity.getWeight();
            this.bloodType = entity.getBloodType();
            this.allergyHistory = entity.getAllergyHistory();
            this.chronicHistory = entity.getChronicHistory();
            this.remark = entity.getRemark();
            this.status = entity.getStatus();
        }
    }
}

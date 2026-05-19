package com.healthtrail.domain.health.family.dto;

import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台家庭共享成员管理列表对象。
 *
 * <p>后台页面除了要看成员基础信息，还要一眼看到：
 * 1. 该成员属于哪个 App 主账号
 * 2. 当前启用中的协同人数
 * 3. 当前待处理的邀请码数量
 * 这样运维或客服在排查问题时，不需要再点进多层页面确认。
 */
@Data
@NoArgsConstructor
public class HealthFamilyMemberAdminDTO {

    /** 成员ID */
    private Long memberId;

    /**
     * 家庭成员业务编码。
     *
     * <p>后台列表页和客服排障场景优先使用该编码，
     * 避免直接暴露数据库主键给业务人员。
     */
    private String memberCode;

    /** 归属用户ID */
    private Long ownerUserId;

    /** 归属用户昵称 */
    private String ownerNickname;

    /** 归属用户手机号 */
    private String ownerMobile;

    /** 成员姓名 */
    private String memberName;

    /** 性别 */
    private Integer gender;

    /** 出生日期 */
    private Date birthday;

    /** 家庭关系 */
    private String relationType;

    /** 身高 */
    private BigDecimal height;

    /** 体重 */
    private BigDecimal weight;

    /** 血型 */
    private String bloodType;

    /** 备注 */
    private String remark;

    /** 成员状态 */
    private Integer status;

    /** 活跃协同人数 */
    private Integer activeCollaboratorCount;

    /** 待处理邀请码数量 */
    private Integer pendingInviteCount;

    public HealthFamilyMemberAdminDTO(HealthFamilyMemberEntity entity) {
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
            this.remark = entity.getRemark();
            this.status = entity.getStatus();
        }
    }
}

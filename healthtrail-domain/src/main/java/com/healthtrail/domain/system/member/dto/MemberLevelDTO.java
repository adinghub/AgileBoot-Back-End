package com.healthtrail.domain.system.member.dto;

import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
/** MemberLevel返回对象。 */
public class MemberLevelDTO {
    /** memberLevelId。 */
    private Long memberLevelId;
    /** 等级编码。 */
    private String levelCode;
    /** 等级名称。 */
    private String levelName;
    /** levelSort。 */
    private Integer levelSort;
    /** price。 */
    private BigDecimal price;
    /** durationDays。 */
    private Integer durationDays;
    /** benefitDesc。 */
    private String benefitDesc;
    /** 状态。 */
    private Integer status;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private Date createTime;
    /** currentLevel。 */
    private Boolean currentLevel;
    /** 是否可Subscribe。 */
    private Boolean canSubscribe;
    /** unavailableReason。 */
    private String unavailableReason;

    public MemberLevelDTO(MemberLevelEntity entity) {
        if (entity != null) {
            this.memberLevelId = entity.getMemberLevelId();
            this.levelCode = entity.getLevelCode();
            this.levelName = entity.getLevelName();
            this.levelSort = entity.getLevelSort();
            this.price = entity.getPrice();
            this.durationDays = entity.getDurationDays();
            this.benefitDesc = entity.getBenefitDesc();
            this.status = entity.getStatus();
            this.remark = entity.getRemark();
            this.createTime = entity.getCreateTime();
        }
    }
}

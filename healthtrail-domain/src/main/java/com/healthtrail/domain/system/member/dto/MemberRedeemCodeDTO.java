package com.healthtrail.domain.system.member.dto;

import com.healthtrail.domain.system.member.db.MemberRedeemCodeEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRedeemCodeDTO {
    private Long memberRedeemCodeId;
    private String batchNo;
    private String redeemCode;
    private Long memberLevelId;
    private String levelCodeSnapshot;
    private String levelNameSnapshot;
    private BigDecimal priceSnapshot;
    private Integer durationDaysSnapshot;
    private String codeStatus;
    private Long redeemedUserId;
    private Date redeemedTime;
    private Date expireTime;
    private String remark;
    private Date createTime;

    public MemberRedeemCodeDTO(MemberRedeemCodeEntity entity) {
        if (entity != null) {
            this.memberRedeemCodeId = entity.getMemberRedeemCodeId();
            this.batchNo = entity.getBatchNo();
            this.redeemCode = entity.getRedeemCode();
            this.memberLevelId = entity.getMemberLevelId();
            this.levelCodeSnapshot = entity.getLevelCodeSnapshot();
            this.levelNameSnapshot = entity.getLevelNameSnapshot();
            this.priceSnapshot = entity.getPriceSnapshot();
            this.durationDaysSnapshot = entity.getDurationDaysSnapshot();
            this.codeStatus = entity.getCodeStatus();
            this.redeemedUserId = entity.getRedeemedUserId();
            this.redeemedTime = entity.getRedeemedTime();
            this.expireTime = entity.getExpireTime();
            this.remark = entity.getRemark();
            this.createTime = entity.getCreateTime();
        }
    }
}

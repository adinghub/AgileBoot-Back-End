package com.healthtrail.domain.system.member.dto;

import com.healthtrail.domain.system.member.db.MemberFeatureEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberFeatureDTO {
    private Long memberFeatureId;
    private String featureCode;
    private String featureName;
    private String featureType;
    private String quotaPeriodType;
    private Integer freeEnabled;
    private Integer freeLimitValue;
    private Integer featureSort;
    private Integer status;
    private Integer isBuiltin;
    private String remark;
    private Date createTime;

    public MemberFeatureDTO(MemberFeatureEntity entity) {
        if (entity != null) {
            this.memberFeatureId = entity.getMemberFeatureId();
            this.featureCode = entity.getFeatureCode();
            this.featureName = entity.getFeatureName();
            this.featureType = entity.getFeatureType();
            this.quotaPeriodType = entity.getQuotaPeriodType();
            this.freeEnabled = entity.getFreeEnabled();
            this.freeLimitValue = entity.getFreeLimitValue();
            this.featureSort = entity.getFeatureSort();
            this.status = entity.getStatus();
            this.isBuiltin = entity.getIsBuiltin();
            this.remark = entity.getRemark();
            this.createTime = entity.getCreateTime();
        }
    }
}

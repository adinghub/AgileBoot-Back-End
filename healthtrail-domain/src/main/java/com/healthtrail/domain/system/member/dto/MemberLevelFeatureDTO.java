package com.healthtrail.domain.system.member.dto;

import lombok.Data;

@Data
public class MemberLevelFeatureDTO {
    private Long memberFeatureId;
    private String featureCode;
    private String featureName;
    private String featureType;
    private String quotaPeriodType;
    private Integer freeEnabled;
    private Integer freeLimitValue;
    private Boolean configured;
    private Integer enabled;
    private Integer limitValue;
    private String remark;
}

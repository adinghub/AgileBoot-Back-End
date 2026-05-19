package com.healthtrail.domain.system.member.dto;

import java.util.List;
import lombok.Data;

@Data
public class MemberRedeemCodeBatchDTO {
    private String batchNo;
    private Long memberLevelId;
    private String levelCode;
    private String levelName;
    private Integer generatedCount;
    private java.util.Date expireTime;
    private List<String> codes;
}

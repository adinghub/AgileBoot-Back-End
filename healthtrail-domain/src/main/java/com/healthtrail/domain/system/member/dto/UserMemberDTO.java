package com.healthtrail.domain.system.member.dto;

import java.util.Date;
import lombok.Data;

@Data
public class UserMemberDTO {
    private Long userMemberId;
    private Long userId;
    private String mobile;
    private String nickname;
    private Long memberLevelId;
    private String levelCode;
    private String levelName;
    private Date effectiveStartTime;
    private Date effectiveEndTime;
    private String status;
    private String sourceType;
    private Long sourceId;
    private String remark;
}

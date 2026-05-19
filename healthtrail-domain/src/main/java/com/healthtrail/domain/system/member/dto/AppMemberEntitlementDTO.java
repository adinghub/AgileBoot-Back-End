package com.healthtrail.domain.system.member.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
/** AppMemberEntitlement返回对象。 */
public class AppMemberEntitlementDTO {
    /** 用户ID。 */
    private Long userId;
    /** 是否有有效会员。 */
    private Boolean hasActiveMember;
    /** 当前会员等级ID。 */
    private Long currentMemberLevelId;
    /** 当前等级编码。 */
    private String currentLevelCode;
    /** 当前等级名称。 */
    private String currentLevelName;
    /** 生效开始时间。 */
    private Date effectiveStartTime;
    /** 生效结束时间。 */
    private Date effectiveEndTime;
    /** 明细列表。 */
    private List<AppMemberEntitlementItemDTO> items;
}

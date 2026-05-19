package com.healthtrail.domain.system.member.dto;

import java.util.Date;
import lombok.Data;

@Data
/** MemberSubscription返回对象。 */
public class MemberSubscriptionDTO {
    /** 是否有有效会员。 */
    private Boolean hasActiveMember;
    /** memberLevelId。 */
    private Long memberLevelId;
    /** 等级编码。 */
    private String levelCode;
    /** 等级名称。 */
    private String levelName;
    /** 状态。 */
    private String status;
    /** 生效开始时间。 */
    private Date effectiveStartTime;
    /** 生效结束时间。 */
    private Date effectiveEndTime;
    /** 剩余天数。 */
    private Integer remainingDays;
    /** 是否自动续费。 */
    private Integer autoRenew;
    /** 订阅状态。 */
    private String subscriptionStatus;
    /** 下次续费时间。 */
    private Date nextRenewTime;
    /** 取消原因。 */
    private String cancelReason;
}

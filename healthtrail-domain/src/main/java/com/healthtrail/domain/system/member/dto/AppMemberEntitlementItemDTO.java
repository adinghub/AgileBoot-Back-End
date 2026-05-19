package com.healthtrail.domain.system.member.dto;

import lombok.Data;

@Data
/** AppMemberEntitlementItem返回对象。 */
public class AppMemberEntitlementItemDTO {
    /** 权益编码。 */
    private String featureCode;
    /** 权益名称。 */
    private String featureName;
    /** featureType。 */
    private String featureType;
    /** entitlementSource。 */
    private String entitlementSource;
    /** enabled。 */
    private Integer enabled;
    /** 权益上限。 */
    private Integer limitValue;
    /** quotaPeriodType。 */
    private String quotaPeriodType;
    /** periodKey。 */
    private String periodKey;
    /** usedCount。 */
    private Integer usedCount;
    /** remAIningCount。 */
    private Integer remainingCount;
    /** message。 */
    private String message;
}

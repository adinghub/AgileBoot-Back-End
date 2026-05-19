package com.healthtrail.domain.system.member.dto;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
/** MemberSubscriptionOrder返回对象。 */
public class MemberSubscriptionOrderDTO {
    /** userMemberOrderId。 */
    private Long userMemberOrderId;
    /** 订单号。 */
    private String orderNo;
    /** memberLevelId。 */
    private Long memberLevelId;
    /** 等级编码。 */
    private String levelCode;
    /** 等级名称。 */
    private String levelName;
    /** orderType。 */
    private String orderType;
    /** 订单状态。 */
    private String orderStatus;
    /** sourceType。 */
    private String sourceType;
    /** orderAmount。 */
    private BigDecimal orderAmount;
    /** payTime。 */
    private Date payTime;
    /** 生效开始时间。 */
    private Date effectiveStartTime;
    /** 生效结束时间。 */
    private Date effectiveEndTime;
    /** 备注。 */
    private String remark;
}

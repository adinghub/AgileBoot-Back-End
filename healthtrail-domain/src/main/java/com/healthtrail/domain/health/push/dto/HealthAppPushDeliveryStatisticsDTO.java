package com.healthtrail.domain.health.push.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台 App Push 设备级派发统计 DTO。
 */
@Data
public class HealthAppPushDeliveryStatisticsDTO {

    /** totalDeliveryCount。 */
    private Long totalDeliveryCount;

    /** successDeliveryCount。 */
    private Long successDeliveryCount;

    /** fAIledDeliveryCount。 */
    private Long failedDeliveryCount;

    /** uniqueMessageCount。 */
    private Long uniqueMessageCount;

    /** todayDeliveryCount。 */
    private Long todayDeliveryCount;

    /** todayMessageSuccessCount。 */
    private Long todayMessageSuccessCount;

    /** todayDeviceSuccessCount。 */
    private Long todayDeviceSuccessCount;

    /** todayDeviceFailedCount。 */
    private Long todayDeviceFailedCount;

    /** noActiveDeviceFAIledCount。 */
    private Long noActiveDeviceFailedCount;

    /** fAIlureReasonDistributions列表。 */
    private List<HealthAppPushStatisticsBucketDTO> failureReasonDistributions;

    /** platformStatistics列表。 */
    private List<HealthAppPushDeliveryPlatformStatisticsDTO> platformStatistics;
}

package com.healthtrail.domain.health.push.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台 App 设备详情 DTO。
 *
 * <p>详情页在列表字段基础上，额外补充最近推送汇总与最近推送明细，
 * 方便后台从“当前这台设备能不能推”继续追到“最近到底推成了没有”。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HealthAppPushDeviceAdminDetailDTO extends HealthAppPushDeviceAdminDTO {

    /**
     * 最近一次失败原因说明。
     */
    private String lastFailureReasonMessage;

    /**
     * 近 7 天设备级推送总次数。
     */
    private Long recent7DayPushTotalCount;

    /**
     * 近 7 天成功率百分比文本，方便前端直接展示。
     */
    private String recent7DayPushSuccessRate;

    /**
     * 最近设备级推送明细。
     */
    private List<HealthAppPushDeliveryAdminDTO> recentDeliveries;
}

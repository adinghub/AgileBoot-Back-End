package com.healthtrail.domain.health.push.dto;

import java.util.Date;
import lombok.Data;

/**
 * 后台 App 设备管理列表 DTO。
 */
@Data
public class HealthAppPushDeviceAdminDTO {

    private Long deviceId;

    private Long ownerUserId;

    private String ownerUserMobile;

    private String ownerUserNickname;

    private String deviceCode;

    private String pushPlatform;

    private String deviceModel;

    private String manufacturer;

    private String osVersion;

    private String appVersion;

    private Integer status;

    private String statusName;

    private String activeStatus;

    private String activeStatusName;

    private Date lastActiveTime;

    private Date lastPushTime;

    private Integer lastPushSendStatus;

    private String lastPushSendStatusName;

    private String lastPushFailureReasonCategory;

    private String lastPushFailureReasonCategoryName;

    private String lastPushFailureReasonMessage;

    private Long recent7DayPushSuccessCount;

    private Long recent7DayPushFailedCount;
}

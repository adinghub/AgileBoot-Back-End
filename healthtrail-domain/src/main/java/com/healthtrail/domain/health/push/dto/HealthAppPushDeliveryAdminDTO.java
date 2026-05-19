package com.healthtrail.domain.health.push.dto;

import java.util.Date;
import lombok.Data;

/**
 * 后台 App Push 设备级派发审计列表 DTO。
 */
@Data
public class HealthAppPushDeliveryAdminDTO {

    private Long deliveryId;

    private Long messageId;

    private String businessScene;

    private String businessSceneName;

    private Long businessId;

    private Long ownerUserId;

    private String ownerUserMobile;

    private String ownerUserNickname;

    private Long memberId;

    private String memberName;

    private Long deviceId;

    private String deviceCode;

    private String pushPlatform;

    private Integer deviceStatusSnapshot;

    private String deviceStatusSnapshotName;

    private String deviceTokenMasked;

    private String sendChannel;

    private Integer sendStatus;

    private String sendStatusName;

    private String failureReasonCategory;

    private String failureReasonCategoryName;

    private String failureReasonMessage;

    private String vendorCode;

    private String vendorMessage;

    private Integer retryNo;

    private Date sendTime;

    private Date createTime;
}

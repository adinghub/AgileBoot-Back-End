package com.healthtrail.domain.health.device.dto;

import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * App 设备返回对象。
 */
@Data
@NoArgsConstructor
public class AppDeviceDTO {

    /** 设备ID。 */
    private Long deviceId;

    /** 设备编码。 */
    private String deviceCode;

    /** 推送平台。 */
    private String pushPlatform;

    /** 设备型号。 */
    private String deviceModel;

    /** 厂商名称。 */
    private String manufacturer;

    /** 系统版本。 */
    private String osVersion;

    /** App 版本。 */
    private String appVersion;

    /** 最近活跃时间。 */
    private Date lastActiveTime;

    /** 设备状态。 */
    private Integer status;

    public AppDeviceDTO(HealthAppDeviceEntity entity) {
        if (entity != null) {
            this.deviceId = entity.getDeviceId();
            this.deviceCode = entity.getDeviceCode();
            this.pushPlatform = entity.getPushPlatform();
            this.deviceModel = entity.getDeviceModel();
            this.manufacturer = entity.getManufacturer();
            this.osVersion = entity.getOsVersion();
            this.appVersion = entity.getAppVersion();
            this.lastActiveTime = entity.getLastActiveTime();
            this.status = entity.getStatus();
        }
    }
}

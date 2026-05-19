package com.healthtrail.domain.health.device.model;

import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import com.healthtrail.domain.health.device.db.HealthAppDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * App 设备模型工厂。
 */
@Component
@RequiredArgsConstructor
public class AppDeviceModelFactory {

    /** 设备数据库服务 */
    private final HealthAppDeviceService appDeviceService;

    public AppDeviceModel create() {
        return new AppDeviceModel();
    }

    public AppDeviceModel loadByDeviceCode(String deviceCode) {
        HealthAppDeviceEntity entity = appDeviceService.getByDeviceCode(deviceCode);
        return entity == null ? null : new AppDeviceModel(entity);
    }
}

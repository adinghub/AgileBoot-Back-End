package com.healthtrail.domain.health.device.db;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * App 设备数据库服务接口。
 */
public interface HealthAppDeviceService extends IService<HealthAppDeviceEntity> {

    /**
     * 根据设备编码查询设备。
     *
     * @param deviceCode 设备编码
     * @return 设备实体
     */
    HealthAppDeviceEntity getByDeviceCode(String deviceCode);

    /**
     * 查询某个用户当前启用中的设备。
     *
     * @param ownerUserId App 用户ID
     * @return 启用中的设备列表
     */
    List<HealthAppDeviceEntity> listActiveDevices(Long ownerUserId);
}

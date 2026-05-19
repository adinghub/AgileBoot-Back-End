package com.healthtrail.domain.health.device.db;

import com.healthtrail.common.enums.common.StatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * App 设备数据库服务实现。
 */
@Service
public class HealthAppDeviceServiceImpl extends ServiceImpl<HealthAppDeviceMapper, HealthAppDeviceEntity>
    implements HealthAppDeviceService {

    @Override
    public HealthAppDeviceEntity getByDeviceCode(String deviceCode) {
        QueryWrapper<HealthAppDeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_code", deviceCode);
        return this.getOne(queryWrapper, false);
    }

    @Override
    public List<HealthAppDeviceEntity> listActiveDevices(Long ownerUserId) {
        QueryWrapper<HealthAppDeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_user_id", ownerUserId)
            .eq("status", StatusEnum.ENABLE.getValue())
            .orderByDesc("last_active_time");
        return this.list(queryWrapper);
    }
}

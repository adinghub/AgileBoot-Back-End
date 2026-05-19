package com.healthtrail.domain.health.push.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * App Push 设备级派发审计数据库服务实现。
 */
@Service
public class HealthAppPushDeliveryLogServiceImpl
    extends ServiceImpl<HealthAppPushDeliveryLogMapper, HealthAppPushDeliveryLogEntity>
    implements HealthAppPushDeliveryLogService {
}

package com.healthtrail.domain.health.chronic.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 慢病专项指标目标范围数据库服务实现。
 */
@Service
public class HealthChronicIndicatorTargetServiceImpl
    extends ServiceImpl<HealthChronicIndicatorTargetMapper, HealthChronicIndicatorTargetEntity>
    implements HealthChronicIndicatorTargetService {
}

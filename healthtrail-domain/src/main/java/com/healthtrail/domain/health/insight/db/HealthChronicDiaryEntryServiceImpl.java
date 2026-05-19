package com.healthtrail.domain.health.insight.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 慢病日记数据库服务实现。
 */
@Service
public class HealthChronicDiaryEntryServiceImpl
    extends ServiceImpl<HealthChronicDiaryEntryMapper, HealthChronicDiaryEntryEntity>
    implements HealthChronicDiaryEntryService {
}
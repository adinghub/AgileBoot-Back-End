package com.healthtrail.domain.health.chronic.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户慢病专项档案数据库服务实现。
 */
@Service
public class HealthChronicDiseaseProfileServiceImpl
    extends ServiceImpl<HealthChronicDiseaseProfileMapper, HealthChronicDiseaseProfileEntity>
    implements HealthChronicDiseaseProfileService {
}

package com.healthtrail.domain.health.chronic.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 慢病病种配置数据库服务实现。
 */
@Service
public class HealthChronicDiseaseTypeServiceImpl
    extends ServiceImpl<HealthChronicDiseaseTypeMapper, HealthChronicDiseaseTypeEntity>
    implements HealthChronicDiseaseTypeService {
}

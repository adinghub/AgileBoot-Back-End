package com.healthtrail.domain.health.drug.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 临时用药记录数据库服务实现。
 */
@Service
public class HealthDrugTemporaryMedicationRecordServiceImpl
    extends ServiceImpl<HealthDrugTemporaryMedicationRecordMapper, HealthDrugTemporaryMedicationRecordEntity>
    implements HealthDrugTemporaryMedicationRecordService {
}

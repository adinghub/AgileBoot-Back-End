package com.healthtrail.domain.health.drug.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 药品库存流水数据库服务实现。
 */
@Service
public class HealthDrugStockLogServiceImpl extends ServiceImpl<HealthDrugStockLogMapper, HealthDrugStockLogEntity>
    implements HealthDrugStockLogService {
}

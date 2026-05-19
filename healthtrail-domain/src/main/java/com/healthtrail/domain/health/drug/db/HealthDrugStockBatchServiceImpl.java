package com.healthtrail.domain.health.drug.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 药品批号效期库存数据库服务实现。
 */
@Service
public class HealthDrugStockBatchServiceImpl extends ServiceImpl<HealthDrugStockBatchMapper, HealthDrugStockBatchEntity>
    implements HealthDrugStockBatchService {
}

package com.healthtrail.domain.health.drug.unit.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 药品单位数据库服务实现。
 */
@Service
public class DrugUnitServiceImpl
    extends ServiceImpl<DrugUnitMapper, DrugUnitEntity>
    implements DrugUnitService {
}

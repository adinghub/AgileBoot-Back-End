package com.healthtrail.domain.health.drug.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 药品数据库服务实现。
 */
@Service
public class HealthDrugServiceImpl extends ServiceImpl<HealthDrugMapper, DrugEntity> implements HealthDrugService {
}

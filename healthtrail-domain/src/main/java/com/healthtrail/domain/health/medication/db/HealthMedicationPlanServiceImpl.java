package com.healthtrail.domain.health.medication.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用药计划数据库服务实现。
 */
@Service
public class HealthMedicationPlanServiceImpl extends ServiceImpl<HealthMedicationPlanMapper, HealthMedicationPlanEntity>
    implements HealthMedicationPlanService {

    @Override
    public boolean isOwnedByUser(Long planId, Long ownerUserId) {
        // 用药计划只能由归属账号操作。
        QueryWrapper<HealthMedicationPlanEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plan_id", planId)
            .eq("owner_user_id", ownerUserId);
        return this.baseMapper.exists(queryWrapper);
    }
}

package com.healthtrail.domain.health.medication.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用药计划数据库服务接口。
 */
public interface HealthMedicationPlanService extends IService<HealthMedicationPlanEntity> {

    /**
     * 校验计划是否归属于指定 App 用户。
     *
     * @param planId 用药计划ID
     * @param ownerUserId App 用户ID
     * @return true 表示归属关系成立
     */
    boolean isOwnedByUser(Long planId, Long ownerUserId);
}

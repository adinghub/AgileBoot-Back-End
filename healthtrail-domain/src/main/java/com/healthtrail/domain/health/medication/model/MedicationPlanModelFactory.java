package com.healthtrail.domain.health.medication.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import com.healthtrail.domain.health.family.model.FamilyMemberModelFactory;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用药计划模型工厂。
 */
@Component
@RequiredArgsConstructor
public class MedicationPlanModelFactory {

    /** 用药计划数据库服务 */
    private final HealthMedicationPlanService medicationPlanService;

    /** 家庭成员模型工厂 */
    private final FamilyMemberModelFactory familyMemberModelFactory;

    /** 药品数据库服务 */
    private final HealthDrugService drugService;

    public MedicationPlanModel create() {
        return new MedicationPlanModel(medicationPlanService, familyMemberModelFactory, drugService);
    }

    public MedicationPlanModel loadById(Long planId) {
        HealthMedicationPlanEntity entity = medicationPlanService.getById(planId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, planId, "用药计划");
        }
        return new MedicationPlanModel(entity, medicationPlanService, familyMemberModelFactory, drugService);
    }
}

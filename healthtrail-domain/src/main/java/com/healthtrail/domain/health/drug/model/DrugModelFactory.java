package com.healthtrail.domain.health.drug.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import com.healthtrail.domain.health.drug.db.HealthDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 药品模型工厂。
 */
@Component
@RequiredArgsConstructor
public class DrugModelFactory {

    /** 药品数据库服务 */
    private final HealthDrugService drugService;

    public DrugModel create() {
        return new DrugModel(drugService);
    }

    public DrugModel loadById(Long drugId) {
        DrugEntity entity = drugService.getById(drugId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, drugId, "药品");
        }
        return new DrugModel(entity, drugService);
    }
}

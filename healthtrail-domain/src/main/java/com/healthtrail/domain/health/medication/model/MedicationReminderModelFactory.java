package com.healthtrail.domain.health.medication.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用药提醒模型工厂。
 */
@Component
@RequiredArgsConstructor
public class MedicationReminderModelFactory {

    /** 用药提醒数据库服务 */
    private final HealthMedicationReminderService medicationReminderService;

    public MedicationReminderModel loadById(Long reminderId) {
        HealthMedicationReminderEntity entity = medicationReminderService.getById(reminderId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, reminderId, "用药提醒");
        }
        return new MedicationReminderModel(entity, medicationReminderService);
    }
}

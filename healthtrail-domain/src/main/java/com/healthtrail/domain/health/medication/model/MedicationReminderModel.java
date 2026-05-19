package com.healthtrail.domain.health.medication.model;

import cn.hutool.core.bean.BeanUtil;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.medication.command.SkipMedicationReminderCommand;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationReminderService;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用药提醒领域模型。
 *
 * <p>该模型负责控制提醒状态转换，
 * 避免出现已服药后还能再次跳过等非法流转，同时也承接“过期后补录服药”的补记场景。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class MedicationReminderModel extends HealthMedicationReminderEntity {

    /** 用药提醒数据库服务 */
    private HealthMedicationReminderService medicationReminderService;

    public MedicationReminderModel(HealthMedicationReminderService medicationReminderService) {
        this.medicationReminderService = medicationReminderService;
    }

    public MedicationReminderModel(HealthMedicationReminderEntity entity,
        HealthMedicationReminderService medicationReminderService) {
        this(medicationReminderService);
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 校验提醒是否归属于当前登录用户。
     */
    public void checkOwnedByUser(Long ownerUserId) {
        if (getReminderId() == null || ownerUserId == null
            || !medicationReminderService.isOwnedByUser(getReminderId(), ownerUserId)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getReminderId(), "用药提醒");
        }
    }

    /**
     * 校验提醒当前是否允许被用户标记为已服药。
     *
     * <p>当前除了“待处理”外，也允许从“已过期”补录成“已服药”：
     * 1. 用户忘记当时点击，事后仍然可以补记
     * 2. 补记后依旧会进入正常的库存扣减与统计链路
     * 3. 但已经是“已跳过 / 已服药”的提醒，仍然不允许重复标记
     */
    public void checkCanTake() {
        Integer reminderStatus = getReminderStatus();
        if (!MedicationReminderStatusEnum.PENDING.getValue().equals(reminderStatus)
            && !MedicationReminderStatusEnum.EXPIRED.getValue().equals(reminderStatus)) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_REMINDER_STATUS_NOT_ALLOW_OPERATE);
        }
    }

    /**
     * 校验提醒当前是否允许被用户标记为已跳过。
     *
     * <p>跳过仍然只允许在待处理阶段执行，
     * 这样可以避免“已经过期很久的提醒又被补录为跳过”带来语义混乱。
     */
    public void checkCanSkip() {
        if (!MedicationReminderStatusEnum.PENDING.getValue().equals(getReminderStatus())) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_REMINDER_STATUS_NOT_ALLOW_OPERATE);
        }
    }

    /**
     * 标记为已服药。
     */
    public void markTaken() {
        checkCanTake();
        this.setReminderStatus(MedicationReminderStatusEnum.TAKEN.getValue());
        this.setFeedbackTime(new Date());
        this.setSkipReason(null);
    }

    /**
     * 标记为已跳过。
     */
    public void markSkipped(SkipMedicationReminderCommand skipCommand) {
        checkCanSkip();
        this.setReminderStatus(MedicationReminderStatusEnum.SKIPPED.getValue());
        this.setFeedbackTime(new Date());
        this.setSkipReason(skipCommand == null ? null : skipCommand.getSkipReason());
    }

    /**
     * 撤销“已服药”反馈。
     *
     * <p>这里故意只允许从“已服药”退回：
     * 1. 防止把“已跳过”等其它终态错误回滚；
     * 2. 如果提醒时间已经过去，则回到“已过期”，贴合补录服药后的撤销语义；
     * 3. 如果提醒时间还没过，则回到“待处理”，保持原有误触撤销体验。
     */
    public void revertTaken() {
        if (!MedicationReminderStatusEnum.TAKEN.getValue().equals(getReminderStatus())) {
            throw new ApiException(ErrorCode.Business.APP_MEDICATION_REMINDER_STATUS_NOT_ALLOW_OPERATE);
        }
        Date now = new Date();
        boolean shouldRestoreExpired = getScheduledTime() != null && !getScheduledTime().after(now);
        this.setReminderStatus(shouldRestoreExpired
            ? MedicationReminderStatusEnum.EXPIRED.getValue()
            : MedicationReminderStatusEnum.PENDING.getValue());
        this.setFeedbackTime(null);
        this.setSkipReason(null);
    }
}

package com.healthtrail.domain.health.drug.dto;

import com.healthtrail.domain.health.drug.db.HealthDrugStockLogEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品库存流水 DTO。
 *
 * <p>这个对象专门给 App 端药品详情页使用，目标是把“库存为什么变化”直接讲清楚：
 * 1. 本次是什么类型的变化
 * 2. 变化前后库存分别是多少
 * 3. 是否和提醒、计划、临时用药记录有关
 * 4. 操作发生在什么时间
 *
 * <p>之所以不直接把实体暴露给 Controller，是因为：
 * 1. App 端不需要完整表结构语义
 * 2. 后续如果要补展示字段或兼容旧字段，DTO 更容易演进
 * 3. 可以把 createTime 这类基类字段稳定收口到对外契约里
 */
@Data
@NoArgsConstructor
public class DrugStockLogDTO {

    /** 库存流水ID */
    private Long logId;

    /** 库存变更类型 */
    private String changeType;

    /** 变更前库存数量 */
    private BigDecimal beforeQuantity;

    /** 变更数量 */
    private BigDecimal changeQuantity;

    /** 变更后库存数量 */
    private BigDecimal afterQuantity;

    /** 库存单位快照 */
    private String stockUnitSnapshot;

    /** 预警阈值快照 */
    private BigDecimal alertThresholdSnapshot;

    /** 关联用药计划ID */
    private Long relatedPlanId;

    /** 关联提醒记录ID */
    private Long relatedReminderId;

    /** 关联临时用药记录ID */
    private Long relatedTempMedicationId;

    /** 操作备注 */
    private String operationRemark;

    /** 创建时间 */
    private Date createTime;

    public DrugStockLogDTO(HealthDrugStockLogEntity entity) {
        if (entity == null) {
            return;
        }
        this.logId = entity.getLogId();
        this.changeType = entity.getChangeType();
        this.beforeQuantity = entity.getBeforeQuantity();
        this.changeQuantity = entity.getChangeQuantity();
        this.afterQuantity = entity.getAfterQuantity();
        this.stockUnitSnapshot = entity.getStockUnitSnapshot();
        this.alertThresholdSnapshot = entity.getAlertThresholdSnapshot();
        this.relatedPlanId = entity.getRelatedPlanId();
        this.relatedReminderId = entity.getRelatedReminderId();
        this.relatedTempMedicationId = entity.getRelatedTempMedicationId();
        this.operationRemark = entity.getOperationRemark();
        this.createTime = entity.getCreateTime();
    }
}

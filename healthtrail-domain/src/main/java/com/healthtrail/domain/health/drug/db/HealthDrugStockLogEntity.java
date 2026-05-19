package com.healthtrail.domain.health.drug.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 药品库存流水表，记录每次库存变更的详细审计信息
 *
 * <p>该表用于沉淀每次库存变化的前后数量和触发来源，
 * 方便后续排查"为什么库存变了""是补库存还是服药扣减"。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("drug_stock_log")
@ApiModel(value = "HealthDrugStockLogEntity对象", description = "健康系统药品库存流水表")
public class HealthDrugStockLogEntity extends BaseEntity<HealthDrugStockLogEntity> {

    private static final long serialVersionUID = 1L;

    /** 库存流水主键ID */
    @ApiModelProperty("流水ID")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /** 流水归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的药品ID */
    @ApiModelProperty("药品ID")
    @TableField("drug_id")
    private Long drugId;

    /** 库存变更类型，如补库存、服药扣减、过期清理 */
    @ApiModelProperty("库存变更类型")
    @TableField("change_type")
    private String changeType;

    /** 本次变更前的库存总量 */
    @ApiModelProperty("变更前库存")
    @TableField("before_quantity")
    private BigDecimal beforeQuantity;

    /** 本次变更的数量，增加为正数，扣减为负数 */
    @ApiModelProperty("本次变更数量，增加为正，扣减为负")
    @TableField("change_quantity")
    private BigDecimal changeQuantity;

    /** 本次变更后的库存总量 */
    @ApiModelProperty("变更后库存")
    @TableField("after_quantity")
    private BigDecimal afterQuantity;

    /** 变更时的库存单位快照 */
    @ApiModelProperty("库存单位快照")
    @TableField("stock_unit_snapshot")
    private String stockUnitSnapshot;

    /** 变更时的预警阈值快照 */
    @ApiModelProperty("预警阈值快照")
    @TableField("alert_threshold_snapshot")
    private BigDecimal alertThresholdSnapshot;

    /** 关联的用药计划ID */
    @ApiModelProperty("关联用药计划ID")
    @TableField("related_plan_id")
    private Long relatedPlanId;

    /** 关联的用药提醒ID */
    @ApiModelProperty("关联提醒ID")
    @TableField("related_reminder_id")
    private Long relatedReminderId;

    /** 关联的临时用药记录ID */
    @ApiModelProperty("关联临时用药记录ID")
    @TableField("related_temp_medication_id")
    private Long relatedTempMedicationId;

    /** 操作备注说明 */
    @ApiModelProperty("操作备注")
    @TableField("operation_remark")
    private String operationRemark;

    @Override
    public Serializable pkVal() {
        return this.logId;
    }
}

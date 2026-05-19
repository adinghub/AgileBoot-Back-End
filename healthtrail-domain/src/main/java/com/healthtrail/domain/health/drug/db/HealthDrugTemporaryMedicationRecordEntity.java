package com.healthtrail.domain.health.drug.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 临时用药记录表，记录非计划内的偶然用药行为
 *
 * <p>这张表专门承载"偶尔吃一次药"的业务语义，
 * 目的是把"库存为什么减少了"与"这次药是谁用的、什么时候用的、因为什么用的"分开保存：
 * 1. drug_stock_log 继续负责库存审计
 * 2. 这张表负责临时用药本身的业务记录
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("drug_temporary_medication_record")
@ApiModel(value = "HealthDrugTemporaryMedicationRecordEntity对象", description = "健康系统药品临时用药记录表")
public class HealthDrugTemporaryMedicationRecordEntity extends BaseEntity<HealthDrugTemporaryMedicationRecordEntity> {

    private static final long serialVersionUID = 1L;

    /** 临时用药记录主键ID */
    @ApiModelProperty("临时用药记录ID")
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /** 记录归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 用药的家庭成员ID，可为空 */
    @ApiModelProperty("家庭成员ID，可为空")
    @TableField(value = "member_id", updateStrategy = FieldStrategy.IGNORED)
    private Long memberId;

    /** 使用的药品ID */
    @ApiModelProperty("药品ID")
    @TableField("drug_id")
    private Long drugId;

    /** 本次使用的药品数量 */
    @ApiModelProperty("本次使用数量")
    @TableField("used_quantity")
    private BigDecimal usedQuantity;

    /** 使用时的库存单位快照 */
    @ApiModelProperty("库存单位快照")
    @TableField("stock_unit_snapshot")
    private String stockUnitSnapshot;

    /** 实际用药时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("实际用药时间")
    @TableField("use_time")
    private Date useTime;

    /** 用药用途或对应症状描述 */
    @ApiModelProperty("用途或症状")
    @TableField(value = "symptom", updateStrategy = FieldStrategy.IGNORED)
    private String symptom;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    /** 实际从库存扣减的数量 */
    @ApiModelProperty("实际扣减数量")
    @TableField("deducted_quantity")
    private BigDecimal deductedQuantity;

    @Override
    public Serializable pkVal() {
        return this.recordId;
    }
}

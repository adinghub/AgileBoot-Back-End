package com.healthtrail.domain.health.drug.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.healthtrail.common.core.base.BaseEntity;
import com.healthtrail.infrastructure.mybatisplus.typehandler.BooleanSmallintTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 药品批号效期库存表，管理每个药品的批次库存明细
 *
 * <p>药品主档和库存批次明细采用一对多关系：
 * 1. drug 负责保存药品基础信息、库存单位、预警配置等主档数据
 * 2. drug_stock_batch 负责保存每一批库存的批号、效期和数量
 *
 * <p>这样做的好处是库存总量不需要在药品主表做冗余存储，
 * 查询时可以实时按批次汇总，扣减时也能按照效期顺序精确消费。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName(value = "drug_stock_batch", autoResultMap = true)
@ApiModel(value = "HealthDrugStockBatchEntity对象", description = "健康系统药品批号效期库存表")
public class HealthDrugStockBatchEntity extends BaseEntity<HealthDrugStockBatchEntity> {

    private static final long serialVersionUID = 1L;

    /** 批次库存主键ID */
    @ApiModelProperty("批次库存ID")
    @TableId(value = "batch_id", type = IdType.AUTO)
    private Long batchId;

    /** 批次归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的药品ID */
    @ApiModelProperty("药品ID")
    @TableField("drug_id")
    private Long drugId;

    /** 药品生产批号 */
    @ApiModelProperty("药品批号，可为空")
    @TableField(value = "batch_no", updateStrategy = FieldStrategy.IGNORED)
    private String batchNo;

    /** 药品有效期截止日期，为空且默认批次时为兜底库存桶 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("药品效期，可为空；为空且 defaultBatch = true 时表示默认批次")
    @TableField(value = "expire_date", updateStrategy = FieldStrategy.IGNORED)
    private Date expireDate;

    /** 是否为默认批次，即批号和效期都缺失时的兜底库存桶 */
    @ApiModelProperty("是否默认批次（批号和效期都缺失时的兜底库存桶）")
    @TableField(value = "is_default_batch", typeHandler = BooleanSmallintTypeHandler.class)
    private Boolean defaultBatch;

    /** 当前批次的实际库存数量 */
    @ApiModelProperty("当前批次库存数量")
    @TableField("stock_quantity")
    private BigDecimal stockQuantity;

    /** 当前近效期提醒周期内是否已经提醒过 */
    @ApiModelProperty("当前近效期提醒周期内是否已经提醒过")
    @TableField(value = "near_expire_notified", typeHandler = BooleanSmallintTypeHandler.class)
    private Boolean nearExpireNotified;

    /** 最近一次近效期提醒的发送时间 */
    @ApiModelProperty("最近一次近效期提醒发送时间")
    @TableField(value = "near_expire_notify_time", updateStrategy = FieldStrategy.IGNORED)
    private Date nearExpireNotifyTime;

    @Override
    public Serializable pkVal() {
        return this.batchId;
    }
}

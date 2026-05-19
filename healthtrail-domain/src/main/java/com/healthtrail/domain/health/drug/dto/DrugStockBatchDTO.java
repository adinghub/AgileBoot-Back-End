package com.healthtrail.domain.health.drug.dto;

import com.healthtrail.domain.health.drug.db.HealthDrugStockBatchEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品批号效期库存返回对象。
 *
 * <p>该对象用于把同一药品下的库存明细按“批号 + 效期”维度返回给前端，
 * 便于药柜详情页清晰展示每一批还剩多少库存。
 */
@Data
@NoArgsConstructor
public class DrugStockBatchDTO {

    /**
     * 批次库存ID。
     */
    private Long batchId;

    /**
     * 药品批号，可为空。
     */
    private String batchNo;

    /**
     * 药品效期。
     *
     * <p>普通批次通常会有值；
     * 默认批次允许为空，App 端应直接按“默认批次 / 无效期”语义展示，而不是误判成脏数据。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;

    /**
     * 是否默认批次。
     *
     * <p>当前默认批次指“批号和效期都没有录入时”的统一库存桶。
     * 单独把这个语义返回给前端后，App 就不需要再把“空批号 + 空效期”
     * 猜成“默认批次”，展示和后续交互都会更稳定。
     */
    private Boolean defaultBatch;

    /**
     * 当前批次库存数量。
     */
    private BigDecimal stockQuantity;

    public DrugStockBatchDTO(HealthDrugStockBatchEntity entity) {
        if (entity != null) {
            this.batchId = entity.getBatchId();
            this.batchNo = entity.getBatchNo();
            this.expireDate = entity.getExpireDate();
            this.defaultBatch = entity.getDefaultBatch();
            this.stockQuantity = entity.getStockQuantity();
        }
    }
}

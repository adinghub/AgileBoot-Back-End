package com.healthtrail.domain.health.drug.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 临时用药扣库存命令。
 *
 * <p>这条命令服务的是“没有计划、偶尔吃一次药，但仍然要扣库存”的场景，
 * 因此它和提醒反馈自动扣库存不同：
 * 1. 不依赖计划和提醒
 * 2. 允许额外登记成员、症状、备注
 * 3. 由用户主动发起，库存不足时直接失败
 */
@Data
public class TemporaryUseDrugStockCommand {

    /**
     * 使用该药的家庭成员ID。
     *
     * <p>该字段允许为空，表示用户这次只想扣库存，
     * 暂时不把这笔临时用药挂到具体家庭成员名下。
     */
    private Long memberId;

    /**
     * 本次临时用药扣减数量。
     */
    @NotNull(message = "临时用药数量不能为空")
    @DecimalMin(value = "0.01", message = "临时用药数量必须大于0")
    @Digits(integer = 8, fraction = 2, message = "临时用药数量最多支持8位整数和2位小数")
    private BigDecimal usedQuantity;

    /**
     * 实际用药时间。
     *
     * <p>为空时后端会自动回填当前时间，
     * 避免用户只是想快速扣库存时还必须额外选时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;

    /**
     * 用途 / 症状。
     */
    @Size(max = 100, message = "用途或症状长度不能超过100个字符")
    private String symptom;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}

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
 * 个人药品补库存命令。
 *
 * <p>这里采用“增量增加”的设计，而不是直接覆盖库存绝对值，
 * 这样更符合真实补货场景，也更利于库存流水审计。
 */
@Data
public class IncreaseDrugStockCommand {

    /**
     * 本次增加的库存数量。
     */
    @NotNull(message = "补库存数量不能为空")
    @DecimalMin(value = "0.01", message = "补库存数量必须大于0")
    @Digits(integer = 8, fraction = 2, message = "补库存数量最多支持8位整数和2位小数")
    private BigDecimal increaseQuantity;

    /**
     * 本次补库存对应的批号，可为空。
     *
     * <p>用户现场补货时并不一定每次都方便录入批号，
     * 因此这里只要求效期必填，批号允许为空。
     */
    @Size(max = 50, message = "药品批号长度不能超过50个字符")
    private String batchNo;

    /**
     * 本次补库存对应的效期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    /**
     * 库存单位。
     *
     * <p>只有当当前药品还没启用库存跟踪时才需要传，
     * 作为首次启用库存时的单位初始化值。
     */
    @Size(max = 20, message = "库存单位长度不能超过20个字符")
    private String stockUnit;

    /**
     * 库存单位ID。
     *
     * <p>当当前药品还没有明确单位、并且前端是从标准单位列表中选择时，
     * 可以通过该字段一次性把单位主数据和补库存动作一起提交给后端。
     */
    private Long stockUnitId;

    /**
     * 操作备注。
     */
    @Size(max = 255, message = "库存备注长度不能超过255个字符")
    private String operationRemark;
}

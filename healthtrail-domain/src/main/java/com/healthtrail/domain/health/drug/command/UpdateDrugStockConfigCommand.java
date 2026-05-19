package com.healthtrail.domain.health.drug.command;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 个人药品库存配置命令。
 *
 * <p>该命令主要用于维护：
 * 1. 库存单位
 * 2. 低库存预警值
 *
 * <p>不直接改库存绝对值，库存增量调整统一走补库存接口。
 */
@Data
public class UpdateDrugStockConfigCommand {

    /**
     * 库存单位。
     *
     * <p>允许为空，表示沿用当前药品已有单位；
     * 但如果当前药品还没有库存单位且本次要启用预警，则必须传值。
     */
    @Size(max = 20, message = "库存单位长度不能超过20个字符")
    private String stockUnit;

    /**
     * 库存单位ID。
     *
     * <p>推荐优先由前端传标准单位ID，后端再同步回填单位名称，
     * 这样库存配置、用药扣减和库存展示都能共享同一套标准单位口径。
     */
    private Long stockUnitId;

    /**
     * 自定义库存预警阈值。
     *
     * <p>传 null 表示关闭低库存提醒，但不会清空当前库存数量。
     */
    @DecimalMin(value = "0", message = "库存预警值不能小于0")
    @Digits(integer = 8, fraction = 2, message = "库存预警值最多支持8位整数和2位小数")
    private BigDecimal stockAlertThreshold;
}

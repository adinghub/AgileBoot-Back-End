package com.healthtrail.domain.health.drug.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 引入系统药品到个人药柜命令。
 *
 * <p>该命令不是“导入文本药品”，
 * 而是把一条系统共享药品复制成当前用户自己的个人药柜条目。
 * 复制完成后，用户即可对这条个人药品维护库存、备注和后续用药计划。
 */
@Data
public class ImportSystemDrugCommand {

    /**
     * 导入后的个人药品名称。
     *
     * <p>允许用户在引入时顺手改成更符合自己习惯的名称；
     * 如果为空，则默认沿用系统药品原名称。
     */
    @Size(max = 100, message = "个人药品名称长度不能超过100个字符")
    private String targetDrugName;

    /**
     * 初始库存数量。
     */
    @DecimalMin(value = "0", message = "库存数量不能小于0")
    @Digits(integer = 8, fraction = 2, message = "库存数量最多支持8位整数和2位小数")
    private BigDecimal stockQuantity;

    /**
     * 初始库存对应的批号，可为空。
     */
    @Size(max = 50, message = "药品批号长度不能超过50个字符")
    private String batchNo;

    /**
     * 初始库存对应的效期。
     *
     * <p>系统药品引入到个人药柜时，如果同时录入了库存，
     * 这里需要带上首批库存的效期，方便后续按批次展示和消耗。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    /**
     * 库存单位。
     */
    @Size(max = 20, message = "库存单位长度不能超过20个字符")
    private String stockUnit;

    /**
     * 库存单位ID。
     *
     * <p>引入系统药品时如果前端已经从标准单位列表中选中了单位，
     * 可以直接传这个字段，后端会自动把对应单位名称回填到药品档案中。
     */
    private Long stockUnitId;

    /**
     * 自定义库存预警阈值。
     */
    @DecimalMin(value = "0", message = "库存预警值不能小于0")
    @Digits(integer = 8, fraction = 2, message = "库存预警值最多支持8位整数和2位小数")
    private BigDecimal stockAlertThreshold;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 药品图片附件ID。
     *
     * <p>允许在“引入系统药品”时顺手更换成自己拍摄的包装图片，
     * 不传则沿用系统药品图片，系统药品也没有图片时再返回默认图片。
     */
    private Long imageAttachmentId;
}

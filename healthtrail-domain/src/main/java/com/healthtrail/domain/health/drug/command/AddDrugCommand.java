package com.healthtrail.domain.health.drug.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 新增药品命令对象。
 *
 * <p>该命令既会被 App 端“新增个人药品”使用，也会被后台“新增系统下发药品”使用，
 * 因此这里只保留药品本身的业务字段，不暴露归属字段，归属由应用服务按入口自行注入。
 */
@Data
public class AddDrugCommand {

    /**
     * 药品名称。
     */
    @NotBlank(message = "药品名称不能为空")
    @Size(max = 100, message = "药品名称长度不能超过100个字符")
    private String drugName;

    /**
     * 通用名。
     */
    @Size(max = 100, message = "通用名长度不能超过100个字符")
    private String genericName;

    /**
     * 商品名。
     */
    @Size(max = 100, message = "商品名长度不能超过100个字符")
    private String brandName;

    /**
     * 剂型。
     */
    @Size(max = 50, message = "剂型长度不能超过50个字符")
    private String dosageForm;

    /**
     * 规格。
     */
    @Size(max = 100, message = "规格长度不能超过100个字符")
    private String specification;

    /**
     * 适应症。
     */
    @Size(max = 1000, message = "适应症长度不能超过1000个字符")
    private String indication;

    /**
     * 用法用量。
     */
    @Size(max = 1000, message = "用法用量长度不能超过1000个字符")
    private String usageInstruction;

    /**
     * 不良反应。
     */
    @Size(max = 1000, message = "不良反应长度不能超过1000个字符")
    private String adverseReaction;

    /**
     * 禁忌。
     */
    @Size(max = 1000, message = "禁忌长度不能超过1000个字符")
    private String contraindication;

    /**
     * 生产厂家。
     */
    @Size(max = 200, message = "生产厂家长度不能超过200个字符")
    private String manufacturer;

    /**
     * 药品类型，例如 OTC / RX。
     */
    @Size(max = 20, message = "药品类型长度不能超过20个字符")
    private String drugType;

    /**
     * 当前库存数量。
     *
     * <p>这里允许为空，表示当前药品暂不启用库存跟踪；
     * 一旦填写，后续“标记已服药”会按提醒剂量自动扣减。
     */
    @DecimalMin(value = "0", message = "库存数量不能小于0")
    @Digits(integer = 8, fraction = 2, message = "库存数量最多支持8位整数和2位小数")
    private BigDecimal stockQuantity;

    /**
     * 初始库存对应的批号，可为空。
     *
     * <p>这里不直接把批号保存到药品主表，
     * 而是作为新增药品时首批库存明细的输入参数使用。
     */
    @Size(max = 50, message = "药品批号长度不能超过50个字符")
    private String batchNo;

    /**
     * 初始库存对应的效期。
     *
     * <p>当新增药品时同时录入了首批库存数量，就需要同步提供该批库存的效期，
     * 以便后续能够按效期顺序展示和扣减库存。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    /**
     * 库存单位。
     *
     * <p>库存自动扣减依赖统一单位，因此当启用库存跟踪时必须明确记录。
     * 一期不做“盒 -> 片”换算，前后端都按该单位直接读写库存数值。
     */
    @Size(max = 20, message = "库存单位长度不能超过20个字符")
    private String stockUnit;

    /**
     * 库存单位ID。
     *
     * <p>当前推荐前端优先传标准单位ID，后端再把单位名称回填到 `stockUnit`，
     * 这样既能满足“系统维护单位、前端只负责搜索选择”，
     * 也保留了后续兼容历史纯文本单位的扩展空间。
     */
    private Long stockUnitId;

    /**
     * 药品图片附件ID。
     *
     * <p>图片非强制上传，
     * 如果不传，接口返回时会自动回退到默认药品图片地址。
     */
    private Long imageAttachmentId;

    /**
     * 自定义库存预警阈值。
     *
     * <p>这里同样允许为空，表示仅记录库存、不做低库存提醒；
     * 如果传 `0`，表示只有库存扣减到 0 时才提醒。
     */
    @DecimalMin(value = "0", message = "库存预警值不能小于0")
    @Digits(integer = 8, fraction = 2, message = "库存预警值最多支持8位整数和2位小数")
    private BigDecimal stockAlertThreshold;

    /**
     * 状态，默认建议传 1。
     */
    private Integer status;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

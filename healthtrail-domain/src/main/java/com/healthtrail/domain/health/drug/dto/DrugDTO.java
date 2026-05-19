package com.healthtrail.domain.health.drug.dto;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.domain.health.drug.db.DrugEntity;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品返回对象。
 */
@Data
@NoArgsConstructor
public class DrugDTO {

    /** 药品ID。 */
    private Long drugId;

    /**
     * 药品业务编码。
     *
     * <p>无论是后台管理端还是 App 工作台，
     * 展示时都应该优先用该字段替代内部主键。
     */
    private String drugCode;

    /**
     * 归属用户ID。
     * 约定 0 表示系统下发药品，其余值表示某个 App 用户自己的药品。
     */
    private Long ownerUserId;

    /**
     * 是否为系统下发药品。
     * 前端可直接根据该字段决定是否展示“仅查看”或“不可编辑”的交互。
     */
    private Boolean systemDrug;

    /** 药品名称。 */
    private String drugName;

    /** 通用名。 */
    private String genericName;

    /** 商品名。 */
    private String brandName;

    /** 剂型。 */
    private String dosageForm;

    /** 规格。 */
    private String specification;

    /** 适应症。 */
    private String indication;

    /** 用法说明。 */
    private String usageInstruction;

    /** 不良反应。 */
    private String adverseReaction;

    /** 禁忌。 */
    private String contraindication;

    /** 生产厂家。 */
    private String manufacturer;

    /** 药品类型。 */
    private String drugType;

    /**
     * 来源系统药品ID。
     *
     * <p>只有“从系统药品引入到个人药柜”的条目才会有值；
     * 手工新增个人药品与系统药品本身都返回 null。
     */
    private Long sourceDrugId;

    /**
     * 是否由系统药品引入而来。
     */
    private Boolean importedFromSystem;

    /**
     * 当前库存数量。
     *
     * <p>只有个人药品才会真正参与库存扣减；
     * 系统药品固定返回 null，避免前端误以为系统药品也在做共享库存。
     */
    private BigDecimal stockQuantity;

    /**
     * 库存单位。
     */
    private String stockUnit;

    /**
     * 库存单位ID。
     *
     * <p>前端编辑药品或库存配置时可直接使用该字段回显单位选择器，
     * 不需要再额外把中文单位名反向映射成单位主数据。
     */
    private Long stockUnitId;

    /**
     * 自定义库存预警阈值。
     *
     * <p>为空表示“记录库存但不做低库存提醒”；
     * `0` 表示只有扣减到缺货时才提醒。
     */
    private BigDecimal stockAlertThreshold;

    /**
     * 当前药品是否启用了库存跟踪。
     *
     * <p>前端可以直接用这个字段决定：
     * 1. 是否显示库存卡片
     * 2. 是否提示“已服药后将自动扣减库存”
     */
    private Boolean stockTrackingEnabled;

    /**
     * 当前库存是否已经低于或等于预警阈值。
     *
     * <p>这个字段主要用于列表和详情页做即时视觉提示，
     * 不代表本次一定刚刚触发了预警消息。
     */
    private Boolean stockBelowAlert;

    /**
     * 按“批号 + 效期”维度返回的库存明细。
     *
     * <p>药品主表不再保存汇总库存，
     * 因此前端如果需要展示每个批次的库存，需要依赖这里的实时明细列表。
     */
    private List<DrugStockBatchDTO> stockBatches = Collections.emptyList();

    /**
     * 药品图片附件ID。
     */
    private Long imageAttachmentId;

    /**
     * 药品图片地址。
     *
     * <p>如果用户或后台没有上传图片，后端会统一返回默认图片地址，
     * 这样前端列表和详情页可以直接渲染，不需要自己再做兜底拼装。
     */
    private String imageUrl;

    /** 启用状态。 */
    private Integer status;

    /**
     * 备注。
     *
     * <p>后台系统药品编辑页面需要依赖详情接口做完整回填，
     * 如果这里不返回 remark，前端在“修改后保存”时就会把原备注覆盖为空。
     */
    private String remark;

    public DrugDTO(DrugEntity entity) {
        if (entity != null) {
            this.drugId = entity.getDrugId();
            this.drugCode = entity.getDrugCode();
            this.ownerUserId = entity.getOwnerUserId();
            this.systemDrug = entity.getOwnerUserId() != null && entity.getOwnerUserId().equals(0L);
            this.drugName = entity.getDrugName();
            this.genericName = entity.getGenericName();
            this.brandName = entity.getBrandName();
            this.dosageForm = entity.getDosageForm();
            this.specification = entity.getSpecification();
            this.indication = entity.getIndication();
            this.usageInstruction = entity.getUsageInstruction();
            this.adverseReaction = entity.getAdverseReaction();
            this.contraindication = entity.getContraindication();
            this.manufacturer = entity.getManufacturer();
            this.drugType = entity.getDrugType();
            this.sourceDrugId = entity.getSourceDrugId();
            this.importedFromSystem = Boolean.FALSE.equals(this.systemDrug) && entity.getSourceDrugId() != null;
            this.stockQuantity = entity.getStockQuantity();
            this.stockUnitId = entity.getStockUnitId();
            this.stockUnit = entity.getStockUnit();
            this.stockAlertThreshold = entity.getStockAlertThreshold();
            this.stockTrackingEnabled = Boolean.FALSE.equals(this.systemDrug) && StrUtil.isNotBlank(entity.getStockUnit());
            this.stockBelowAlert = this.stockTrackingEnabled
                && entity.getStockAlertThreshold() != null
                && entity.getStockQuantity() != null
                && entity.getStockQuantity().compareTo(entity.getStockAlertThreshold()) <= 0;
            this.imageAttachmentId = entity.getImageAttachmentId();
            this.status = entity.getStatus();
            this.remark = entity.getRemark();
        }
    }
}

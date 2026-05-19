package com.healthtrail.domain.health.drug.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.infrastructure.mybatisplus.typehandler.BooleanSmallintTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 药品主表，采用双层药品库模型支持系统预置药与个人药品
 *
 * <p>当前药品采用双层药品库模型：
 * 1. owner_user_id = 0 表示系统下发药品，由后台统一维护
 * 2. owner_user_id = App用户ID 表示用户自己的个人药品，由 App 端自己维护
 *
 * <p>这样设计以后，App 侧既可以直接使用系统预置的常用药，又保留用户补充家庭个性化药品的能力。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName(value = "drug", autoResultMap = true)
@ApiModel(value = "DrugEntity对象", description = "健康系统双层药品表")
public class DrugEntity extends BaseEntity<DrugEntity> {

    private static final long serialVersionUID = 1L;

    /** 药品主键ID */
    @ApiModelProperty("药品ID")
    @TableId(value = "drug_id", type = IdType.AUTO)
    private Long drugId;

    /** 药品业务编码，用于前端展示 */
    @ApiModelProperty("药品业务编码，优先给前端展示使用")
    @TableField("drug_code")
    private String drugCode;

    /** 药品归属用户ID，0表示系统下发药品 */
    @ApiModelProperty("归属App用户ID，0表示系统下发药品")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 药品通用名称 */
    @ApiModelProperty("药品名称")
    @TableField("drug_name")
    private String drugName;

    /** 药品通用名（国际非专利名） */
    @ApiModelProperty("通用名")
    @TableField("generic_name")
    private String genericName;

    /** 药品商品名（品牌名） */
    @ApiModelProperty("商品名")
    @TableField("brand_name")
    private String brandName;

    /** 剂型，如片剂、胶囊、注射液 */
    @ApiModelProperty("剂型")
    @TableField("dosage_form")
    private String dosageForm;

    /** 药品规格，如10mg/片 */
    @ApiModelProperty("规格")
    @TableField("specification")
    private String specification;

    /** 适应症描述 */
    @ApiModelProperty("适应症")
    @TableField("indication")
    private String indication;

    /** 用法用量说明 */
    @ApiModelProperty("用法用量")
    @TableField("usage_instruction")
    private String usageInstruction;

    /** 不良反应描述 */
    @ApiModelProperty("不良反应")
    @TableField("adverse_reaction")
    private String adverseReaction;

    /** 禁忌事项描述 */
    @ApiModelProperty("禁忌")
    @TableField("contraindication")
    private String contraindication;

    /** 药品生产厂家 */
    @ApiModelProperty("生产厂家")
    @TableField("manufacturer")
    private String manufacturer;

    /** 药品类型，如OTC非处方药、RX处方药 */
    @ApiModelProperty("药品类型，例如OTC或RX")
    @TableField("drug_type")
    private String drugType;

    /** 来源系统药品ID，仅个人引入系统药品时记录 */
    @ApiModelProperty("来源系统药品ID，仅个人引入系统药品时使用")
    @TableField("source_drug_id")
    private Long sourceDrugId;

    /** 当前库存数量，由批号效期库存明细实时汇总，不落库 */
    @ApiModelProperty("当前库存数量，由批号效期库存明细实时汇总，不直接落到药品主表")
    @TableField(exist = false)
    private BigDecimal stockQuantity;

    /** 库存单位ID，关联药品单位表 */
    @ApiModelProperty("库存单位ID，关联 drug_unit.unit_id，便于前端按标准单位选择")
    @TableField(value = "stock_unit_id", updateStrategy = FieldStrategy.IGNORED)
    private Long stockUnitId;

    /** 库存单位名称，如片、粒、ml */
    @ApiModelProperty("库存单位，例如片、粒、ml")
    @TableField(value = "stock_unit", updateStrategy = FieldStrategy.IGNORED)
    private String stockUnit;

    /** 药品图片附件ID */
    @ApiModelProperty("药品图片附件ID，关联 attachment.attachment_id")
    @TableField(value = "image_attachment_id", updateStrategy = FieldStrategy.IGNORED)
    private Long imageAttachmentId;

    /** 库存预警阈值，低于此值触发低库存提醒 */
    @ApiModelProperty("库存预警阈值，仅个人药品启用库存跟踪")
    @TableField(value = "stock_alert_threshold", updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal stockAlertThreshold;

    /** 当前预警周期内是否已发送过低库存提醒 */
    @ApiModelProperty("当前预警周期内是否已经发送过低库存提醒")
    @TableField(value = "low_stock_notified", typeHandler = BooleanSmallintTypeHandler.class)
    private Boolean lowStockNotified;

    /** 最近一次低库存提醒的发送时间 */
    @ApiModelProperty("最近一次低库存提醒发送时间")
    @TableField(value = "low_stock_notify_time", updateStrategy = FieldStrategy.IGNORED)
    private Date lowStockNotifyTime;

    /** 药品状态，1正常 0停用 */
    @ApiModelProperty("状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.drugId;
    }
}

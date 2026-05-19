package com.healthtrail.domain.health.drug.unit.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 药品单位表，为前端提供统一的单位选择数据源
 *
 * <p>药品单位表承担两层职责：
 * 1. 为前端提供统一的单位搜索与选择数据源
 * 2. 为药品主档和库存配置沉淀稳定的单位主数据ID
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("drug_unit")
@ApiModel(value = "DrugUnitEntity对象", description = "药品单位表")
public class DrugUnitEntity extends BaseEntity<DrugUnitEntity> {

    private static final long serialVersionUID = 1L;

    /** 单位主键ID */
    @ApiModelProperty("单位ID")
    @TableId(value = "unit_id", type = IdType.AUTO)
    private Long unitId;

    /** 单位唯一编码 */
    @ApiModelProperty("单位编码")
    @TableField("unit_code")
    private String unitCode;

    /** 单位显示名称 */
    @ApiModelProperty("单位名称")
    @TableField("unit_name")
    private String unitName;

    /** 单位别名，便于搜索匹配 */
    @ApiModelProperty("单位别名")
    @TableField(value = "unit_alias", updateStrategy = FieldStrategy.IGNORED)
    private String unitAlias;

    /** 小数位精度，用于前端数值输入校验 */
    @ApiModelProperty("小数位精度")
    @TableField("precision_scale")
    private Integer precisionScale;

    /** 排序号，越小越靠前 */
    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;

    /** 状态，1正常 0停用 */
    @ApiModelProperty("状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    /** 单位图标附件ID */
    @ApiModelProperty("图标附件ID")
    @TableField(value = "icon_attachment_id", updateStrategy = FieldStrategy.IGNORED)
    private Long iconAttachmentId;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.unitId;
    }
}

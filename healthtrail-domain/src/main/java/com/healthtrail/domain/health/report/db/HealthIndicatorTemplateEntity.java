package com.healthtrail.domain.health.report.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 体检指标模板表，维护指标的基础定义、参考范围和建议模板
 *
 * <p>该表给后台运营维护"指标基础定义"，供报告录入时自动补齐参考范围和建议模板。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("indicator_template")
@ApiModel(value = "HealthIndicatorTemplateEntity对象", description = "健康系统体检指标模板表")
public class HealthIndicatorTemplateEntity extends BaseEntity<HealthIndicatorTemplateEntity> {

    private static final long serialVersionUID = 1L;

    /** 指标模板主键ID */
    @ApiModelProperty("指标模板ID")
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /** 适用的报告类型 */
    @ApiModelProperty("报告类型")
    @TableField("report_type")
    private String reportType;

    /** 指标编码，唯一标识 */
    @ApiModelProperty("指标编码")
    @TableField("item_code")
    private String itemCode;

    /** 指标中文名称 */
    @ApiModelProperty("指标名称")
    @TableField("item_name")
    private String itemName;

    /** 指标结果的单位 */
    @ApiModelProperty("结果单位")
    @TableField("result_unit")
    private String resultUnit;

    /** 参考范围下限值 */
    @ApiModelProperty("参考范围下限")
    @TableField("reference_min")
    private BigDecimal referenceMin;

    /** 参考范围上限值 */
    @ApiModelProperty("参考范围上限")
    @TableField("reference_max")
    private BigDecimal referenceMax;

    /** 参考范围的文本描述 */
    @ApiModelProperty("参考范围文本")
    @TableField("reference_text")
    private String referenceText;

    /** 指标异常时的建议文案模板 */
    @ApiModelProperty("建议模板")
    @TableField("suggestion_template")
    private String suggestionTemplate;

    /** 排序号 */
    @ApiModelProperty("排序号")
    @TableField("sort")
    private Integer sort;

    /** 启用状态 */
    @ApiModelProperty("状态")
    @TableField("status")
    private Integer status;

    @Override
    public Serializable pkVal() {
        return this.templateId;
    }
}

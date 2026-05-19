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
 * 体检报告指标结果表，存储从报告中拆出的结构化指标数据
 *
 * <p>一条报告往往会拆出多条结构化指标结果，
 * 因此这里单独建表，而不是把所有结果 JSON 塞回主表。
 * 这样做的好处是：
 * 1. 后续更方便做异常筛选和统计
 * 2. 后续可以逐步接规则引擎或 AI 分析
 * 3. 也方便后台做审核和人工修正
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("report_item")
@ApiModel(value = "HealthReportItemEntity对象", description = "健康系统体检报告指标结果表")
public class HealthReportItemEntity extends BaseEntity<HealthReportItemEntity> {

    private static final long serialVersionUID = 1L;

    /** 指标结果主键ID */
    @ApiModelProperty("指标结果ID")
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /** 所属的体检报告ID */
    @ApiModelProperty("体检报告ID")
    @TableField("report_id")
    private Long reportId;

    /** 指标归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 指标编码 */
    @ApiModelProperty("指标编码")
    @TableField("item_code")
    private String itemCode;

    /** 标准化后的指标编码 */
    @ApiModelProperty("标准指标编码")
    @TableField("standard_item_code")
    private String standardItemCode;

    /** 指标中文名称 */
    @ApiModelProperty("指标名称")
    @TableField("item_name")
    private String itemName;

    /** 指标的检测结果值 */
    @ApiModelProperty("结果值")
    @TableField("result_value")
    private String resultValue;

    /** 结果值的单位 */
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

    /** 参考范围的文本原文 */
    @ApiModelProperty("参考范围原文")
    @TableField("reference_text")
    private String referenceText;

    /** 指标的解读说明 */
    @ApiModelProperty("指标解读")
    @TableField("item_interpretation")
    private String itemInterpretation;

    /** 异常标记，如偏高、偏低、正常 */
    @ApiModelProperty("异常标记")
    @TableField("abnormal_flag")
    private Integer abnormalFlag;

    /** 排序号 */
    @ApiModelProperty("排序号")
    @TableField("sort")
    private Integer sort;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.itemId;
    }
}

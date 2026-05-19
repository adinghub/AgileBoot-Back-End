package com.healthtrail.domain.health.chronic.db;

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
 * 慢病病种配置表，定义支持的慢病类型及其关注指标
 *
 * <p>慢病专项必须支持所有慢病，而不是在 Java 代码里写死"高血压 / 糖尿病"等少数分支。
 * 因此这里把病种、关注指标编码和关注关键词都配置化：
 * 1. 已知标准指标走 focus_indicator_codes_json 精准匹配；
 * 2. AI/人工录入尚未稳定标准编码时，走 focus_indicator_keywords_json 兜底匹配指标名称；
 * 3. 后续新增病种优先新增配置，不需要改慢病专项应用服务的判断分支。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_chronic_disease_type")
@ApiModel(value = "HealthChronicDiseaseTypeEntity对象", description = "健康系统慢病病种配置表")
public class HealthChronicDiseaseTypeEntity extends BaseEntity<HealthChronicDiseaseTypeEntity> {

    private static final long serialVersionUID = 1L;

    /** 慢病病种主键ID */
    @ApiModelProperty("慢病病种ID")
    @TableId(value = "type_id", type = IdType.AUTO)
    private Long typeId;

    /** 病种唯一编码 */
    @ApiModelProperty("病种编码")
    @TableField("disease_code")
    private String diseaseCode;

    /** 病种中文名称 */
    @ApiModelProperty("病种名称")
    @TableField("disease_name")
    private String diseaseName;

    /** 病种分类，如心血管、内分泌等 */
    @ApiModelProperty("病种分类")
    @TableField("disease_category")
    private String diseaseCategory;

    /** 重点关注指标编码列表JSON */
    @ApiModelProperty("重点关注指标编码JSON")
    @TableField("focus_indicator_codes_json")
    private String focusIndicatorCodesJson;

    /** 重点关注指标关键词列表JSON，用于兜底模糊匹配 */
    @ApiModelProperty("重点关注指标关键词JSON")
    @TableField("focus_indicator_keywords_json")
    private String focusIndicatorKeywordsJson;

    /** 默认管理目标描述 */
    @ApiModelProperty("默认目标摘要")
    @TableField("target_summary")
    private String targetSummary;

    /** 默认随访建议文本 */
    @ApiModelProperty("默认随访建议")
    @TableField("follow_up_suggestion")
    private String followUpSuggestion;

    /** 排序号，越小越靠前 */
    @ApiModelProperty("排序号")
    @TableField("sort")
    private Integer sort;

    /** 状态，1启用 0停用 */
    @ApiModelProperty("状态（1启用 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.typeId;
    }
}

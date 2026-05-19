package com.healthtrail.domain.health.chronic.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 慢病专项指标目标范围表，为每个慢病专项下的指标设定个性化目标值
 *
 * <p>该表保存"某个慢病专项下，某个指标的个性化目标"。目标不直接写入报告指标表，
 * 也不覆盖病种默认配置，而是在看板展示时与报告最新值动态比对。这样既能保留报告原始数据，
 * 又能让用户按自己的医嘱维护差异化目标范围。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_chronic_indicator_target")
@ApiModel(value = "HealthChronicIndicatorTargetEntity对象", description = "健康系统慢病专项指标目标范围表")
public class HealthChronicIndicatorTargetEntity extends BaseEntity<HealthChronicIndicatorTargetEntity> {

    private static final long serialVersionUID = 1L;

    /** 指标目标主键ID */
    @ApiModelProperty("指标目标ID")
    @TableId(value = "target_id", type = IdType.AUTO)
    private Long targetId;

    /** 目标归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 关联的慢病专项档案ID */
    @ApiModelProperty("慢病专项档案ID")
    @TableField("profile_id")
    private Long profileId;

    /** 指标编码，关联指标模板 */
    @ApiModelProperty("指标编码")
    @TableField("indicator_code")
    private String indicatorCode;

    /** 指标名称快照 */
    @ApiModelProperty("指标名称快照")
    @TableField("indicator_name")
    private String indicatorName;

    /** 目标值下限 */
    @ApiModelProperty("目标下限")
    @TableField(value = "target_min", updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal targetMin;

    /** 目标值上限 */
    @ApiModelProperty("目标上限")
    @TableField(value = "target_max", updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal targetMax;

    /** 非数值型目标的文字说明 */
    @ApiModelProperty("非数值型目标说明")
    @TableField(value = "target_text", updateStrategy = FieldStrategy.IGNORED)
    private String targetText;

    /** 指标单位快照 */
    @ApiModelProperty("指标单位快照")
    @TableField(value = "result_unit", updateStrategy = FieldStrategy.IGNORED)
    private String resultUnit;

    /** 状态，1启用 0停用 */
    @ApiModelProperty("状态（1启用 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.targetId;
    }
}

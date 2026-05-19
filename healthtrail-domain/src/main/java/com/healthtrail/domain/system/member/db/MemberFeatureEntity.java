package com.healthtrail.domain.system.member.db;

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
 * 会员权益定义表，定义系统提供的各类会员权益及额度配置
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_feature")
@ApiModel(value = "MemberFeatureEntity对象", description = "会员权益定义表")
public class MemberFeatureEntity extends BaseEntity<MemberFeatureEntity> {

    private static final long serialVersionUID = 1L;

    /** 会员权益主键ID */
    @TableId(value = "member_feature_id", type = IdType.AUTO)
    @ApiModelProperty("会员权益ID")
    private Long memberFeatureId;

    /** 权益唯一编码，用于程序识别 */
    @TableField("feature_code")
    @ApiModelProperty("权益编码")
    private String featureCode;

    /** 权益显示名称 */
    @TableField("feature_name")
    @ApiModelProperty("权益名称")
    private String featureName;

    /** 权益类型分类 */
    @TableField("feature_type")
    @ApiModelProperty("权益类型")
    private String featureType;

    /** 配额周期类型，如按天、按周、按月 */
    @TableField("quota_period_type")
    @ApiModelProperty("次数周期")
    private String quotaPeriodType;

    /** 免费用户默认是否启用该权益 */
    @TableField("free_enabled")
    @ApiModelProperty("免费默认是否启用")
    private Integer freeEnabled;

    /** 免费用户默认可用额度 */
    @TableField("free_limit_value")
    @ApiModelProperty("免费默认额度")
    private Integer freeLimitValue;

    /** 显示排序值，越小越靠前 */
    @TableField("feature_sort")
    @ApiModelProperty("排序值")
    private Integer featureSort;

    /** 启用状态 */
    @TableField("status")
    @ApiModelProperty("状态")
    private Integer status;

    /** 是否为系统内置权益，内置权益不可删除 */
    @TableField("is_builtin")
    @ApiModelProperty("是否内置")
    private Integer isBuiltin;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberFeatureId;
    }
}

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
 * 会员权益配额使用记录表，按周期统计用户对各项权益的使用次数
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_feature_quota_usage")
@ApiModel(value = "MemberFeatureQuotaUsageEntity对象", description = "会员权益次数使用记录表")
public class MemberFeatureQuotaUsageEntity extends BaseEntity<MemberFeatureQuotaUsageEntity> {

    private static final long serialVersionUID = 1L;

    /** 配额使用记录主键ID */
    @TableId(value = "member_feature_quota_usage_id", type = IdType.AUTO)
    @ApiModelProperty("使用记录ID")
    private Long memberFeatureQuotaUsageId;

    /** 使用权益的用户ID */
    @TableField("user_id")
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 关联的会员权益ID */
    @TableField("member_feature_id")
    @ApiModelProperty("会员权益ID")
    private Long memberFeatureId;

    /** 使用时的权益编码快照 */
    @TableField("feature_code_snapshot")
    @ApiModelProperty("权益编码快照")
    private String featureCodeSnapshot;

    /** 配额周期类型，如按天、按周、按月 */
    @TableField("quota_period_type")
    @ApiModelProperty("次数周期")
    private String quotaPeriodType;

    /** 当前周期的标识键，如2024-05-15表示当天 */
    @TableField("period_key")
    @ApiModelProperty("周期键")
    private String periodKey;

    /** 当前周期内已使用的次数 */
    @TableField("used_count")
    @ApiModelProperty("已使用次数")
    private Integer usedCount;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberFeatureQuotaUsageId;
    }
}

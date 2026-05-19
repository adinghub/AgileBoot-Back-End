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
 * 会员等级与权益关联矩阵表，定义每个等级下各权益的启用状态和额度
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_level_feature")
@ApiModel(value = "MemberLevelFeatureEntity对象", description = "会员等级权益矩阵表")
public class MemberLevelFeatureEntity extends BaseEntity<MemberLevelFeatureEntity> {

    private static final long serialVersionUID = 1L;

    /** 等级权益关联主键ID */
    @TableId(value = "member_level_feature_id", type = IdType.AUTO)
    @ApiModelProperty("会员等级权益ID")
    private Long memberLevelFeatureId;

    /** 关联的会员等级ID */
    @TableField("member_level_id")
    @ApiModelProperty("会员等级ID")
    private Long memberLevelId;

    /** 关联的会员权益ID */
    @TableField("member_feature_id")
    @ApiModelProperty("会员权益ID")
    private Long memberFeatureId;

    /** 该等级下该权益是否启用 */
    @TableField("enabled")
    @ApiModelProperty("是否启用")
    private Integer enabled;

    /** 覆盖权益默认额度的自定义值 */
    @TableField("limit_value")
    @ApiModelProperty("额度覆盖值")
    private Integer limitValue;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberLevelFeatureId;
    }
}

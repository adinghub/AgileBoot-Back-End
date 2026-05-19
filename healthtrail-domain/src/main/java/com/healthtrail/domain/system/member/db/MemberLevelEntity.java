package com.healthtrail.domain.system.member.db;

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
 * 会员等级定义表，定义各级会员的定价、时长和权益说明
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_level")
@ApiModel(value = "MemberLevelEntity对象", description = "会员等级表")
public class MemberLevelEntity extends BaseEntity<MemberLevelEntity> {

    private static final long serialVersionUID = 1L;

    /** 会员等级主键ID */
    @TableId(value = "member_level_id", type = IdType.AUTO)
    @ApiModelProperty("会员等级ID")
    private Long memberLevelId;

    /** 会员等级唯一编码 */
    @TableField("level_code")
    @ApiModelProperty("会员等级编码")
    private String levelCode;

    /** 会员等级显示名称 */
    @TableField("level_name")
    @ApiModelProperty("会员等级名称")
    private String levelName;

    /** 等级显示排序值，越小越靠前 */
    @TableField("level_sort")
    @ApiModelProperty("排序值")
    private Integer levelSort;

    /** 该等级的购买价格 */
    @TableField("price")
    @ApiModelProperty("价格")
    private BigDecimal price;

    /** 该等级默认有效天数 */
    @TableField("duration_days")
    @ApiModelProperty("默认时长")
    private Integer durationDays;

    /** 权益说明文本或富文本描述 */
    @TableField("benefit_desc")
    @ApiModelProperty("权益说明")
    private String benefitDesc;

    /** 启用状态 */
    @TableField("status")
    @ApiModelProperty("状态")
    private Integer status;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberLevelId;
    }
}

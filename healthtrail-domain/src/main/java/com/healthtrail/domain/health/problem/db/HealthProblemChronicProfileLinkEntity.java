package com.healthtrail.domain.health.problem.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 健康问题与慢病专项关联表，支持多对多关系及关联审计
 *
 * <p>一个健康问题可能同时服务于多个慢病专项，一个慢病专项也可能关联多个问题，
 * 所以这里使用独立关联表，而不是在 health_problem 或 health_chronic_disease_profile 上加单值字段。
 * 这样可以保留多对多关系，也方便后续记录关联来源、备注、时间和审计信息。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_problem_chronic_profile_link")
@ApiModel(value = "HealthProblemChronicProfileLinkEntity对象", description = "健康问题与慢病专项关联表")
public class HealthProblemChronicProfileLinkEntity extends BaseEntity<HealthProblemChronicProfileLinkEntity> {

    private static final long serialVersionUID = 1L;

    /** 关联记录主键ID */
    @ApiModelProperty("关联ID")
    @TableId(value = "link_id", type = IdType.AUTO)
    private Long linkId;

    /** 关联归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 关联的健康问题ID */
    @ApiModelProperty("健康问题ID")
    @TableField("problem_id")
    private Long problemId;

    /** 关联的慢病专项档案ID */
    @ApiModelProperty("慢病专项档案ID")
    @TableField("profile_id")
    private Long profileId;

    /** 关联关系的建立来源 */
    @ApiModelProperty("关联来源")
    @TableField("link_source")
    private String linkSource;

    /** 关联原因备注 */
    @ApiModelProperty("关联备注")
    @TableField("link_remark")
    private String linkRemark;

    /** 关联建立时间 */
    @ApiModelProperty("关联时间")
    @TableField("link_time")
    private Date linkTime;

    @Override
    public Serializable pkVal() {
        return this.linkId;
    }
}

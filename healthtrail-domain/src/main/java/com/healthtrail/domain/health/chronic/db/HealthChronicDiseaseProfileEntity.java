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
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户慢病专项档案表，记录家庭成员管理的慢病专项、目标及状态
 *
 * <p>这张表只保存"某个家庭成员正在管理哪些慢病专项"以及用户自己的目标/备注，
 * 不把高血压、糖尿病等具体疾病拆成多张表。这样可以保证：
 * 1. 任何慢病都能通过 disease_code 接入同一套档案、列表和趋势看板；
 * 2. 病种展示名、关注指标、随访建议从病种配置表读取，后续可配置化调整；
 * 3. App 和后端都围绕 profileId/diseaseCode 工作，不需要因为新增病种反复发版。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("health_chronic_disease_profile")
@ApiModel(value = "HealthChronicDiseaseProfileEntity对象", description = "健康系统用户慢病专项档案表")
public class HealthChronicDiseaseProfileEntity extends BaseEntity<HealthChronicDiseaseProfileEntity> {

    private static final long serialVersionUID = 1L;

    /** 慢病专项档案主键ID */
    @ApiModelProperty("慢病专项档案ID")
    @TableId(value = "profile_id", type = IdType.AUTO)
    private Long profileId;

    /** 档案归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 关联的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 慢病病种编码，关联病种配置表 */
    @ApiModelProperty("病种编码")
    @TableField("disease_code")
    private String diseaseCode;

    /** 建档时的病种名称快照 */
    @ApiModelProperty("病种名称快照")
    @TableField("disease_name_snapshot")
    private String diseaseNameSnapshot;

    /** 专项跟进状态：1跟进中 2已稳定 3已关闭 */
    @ApiModelProperty("专项状态（1跟进中 2已稳定 3已关闭）")
    @TableField("profile_status")
    private Integer profileStatus;

    /** 关注优先级：LOW低、MEDIUM中、HIGH高 */
    @ApiModelProperty("关注优先级（LOW/MEDIUM/HIGH）")
    @TableField("risk_level")
    private String riskLevel;

    /** 确诊或建档日期 */
    @ApiModelProperty("确诊或建档日期")
    @TableField(value = "diagnosed_date", updateStrategy = FieldStrategy.IGNORED)
    private Date diagnosedDate;

    /** 个性化的管理目标描述 */
    @ApiModelProperty("个性化管理目标")
    @TableField(value = "target_summary", updateStrategy = FieldStrategy.IGNORED)
    private String targetSummary;

    /** 当前健康情况的摘要描述 */
    @ApiModelProperty("当前情况摘要")
    @TableField(value = "current_summary", updateStrategy = FieldStrategy.IGNORED)
    private String currentSummary;

    /** 最近一次复盘日期 */
    @ApiModelProperty("最近复盘日期")
    @TableField(value = "last_review_date", updateStrategy = FieldStrategy.IGNORED)
    private Date lastReviewDate;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.profileId;
    }
}

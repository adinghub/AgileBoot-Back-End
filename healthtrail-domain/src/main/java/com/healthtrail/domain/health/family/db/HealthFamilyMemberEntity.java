package com.healthtrail.domain.health.family.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 家庭成员表，健康系统的核心业务主体，报告、提醒、用药等数据均挂在成员维度
 *
 * <p>家庭成员是健康系统中非常关键的业务主体，
 * 报告、提醒、药物计划等后续数据都应该优先挂在成员维度，而不是直接挂在 App 账号维度。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("family_member")
@ApiModel(value = "HealthFamilyMemberEntity对象", description = "健康系统家庭成员表")
public class HealthFamilyMemberEntity extends BaseEntity<HealthFamilyMemberEntity> {

    private static final long serialVersionUID = 1L;

    /** 家庭成员主键ID */
    @ApiModelProperty("家庭成员ID")
    @TableId(value = "member_id", type = IdType.AUTO)
    private Long memberId;

    /** 家庭成员业务编码，用于前端展示 */
    @ApiModelProperty("家庭成员业务编码，优先给前端展示使用")
    @TableField("member_code")
    private String memberCode;

    /** 成员归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 成员姓名 */
    @ApiModelProperty("成员姓名")
    @TableField("member_name")
    private String memberName;

    /** 性别 */
    @ApiModelProperty("性别")
    @TableField("gender")
    private Integer gender;

    /** 出生日期 */
    @ApiModelProperty("生日")
    @TableField("birthday")
    private Date birthday;

    /** 与主账号的关系类型，如本人、配偶、父母 */
    @ApiModelProperty("关系类型")
    @TableField("relation_type")
    private String relationType;

    /** 身高，单位厘米 */
    @ApiModelProperty("身高(cm)")
    @TableField("height")
    private BigDecimal height;

    /** 体重，单位千克 */
    @ApiModelProperty("体重(kg)")
    @TableField("weight")
    private BigDecimal weight;

    /** 血型 */
    @ApiModelProperty("血型")
    @TableField("blood_type")
    private String bloodType;

    /** 过敏史描述 */
    @ApiModelProperty("过敏史")
    @TableField("allergy_history")
    private String allergyHistory;

    /** 慢病史描述 */
    @ApiModelProperty("慢病史")
    @TableField("chronic_history")
    private String chronicHistory;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    /** 状态，1正常 0停用 */
    @ApiModelProperty("状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    @Override
    public Serializable pkVal() {
        return this.memberId;
    }
}

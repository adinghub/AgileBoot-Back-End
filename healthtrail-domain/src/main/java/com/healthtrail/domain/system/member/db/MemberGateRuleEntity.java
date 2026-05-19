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
 * 会员功能门禁规则表，定义各门禁点的具体限制策略和提示信息
 *
 * <p>门禁点描述"有哪些业务入口"，规则描述"这些入口当前怎么限制"。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_gate_rule")
@ApiModel(value = "MemberGateRuleEntity对象", description = "会员功能门禁规则表")
public class MemberGateRuleEntity extends BaseEntity<MemberGateRuleEntity> {

    private static final long serialVersionUID = 1L;

    /** 门禁规则主键ID */
    @TableId(value = "member_gate_rule_id", type = IdType.AUTO)
    @ApiModelProperty("门禁规则ID")
    private Long memberGateRuleId;

    /** 关联的门禁点ID */
    @TableField("member_gate_id")
    @ApiModelProperty("门禁点ID")
    private Long memberGateId;

    /** 策略类型，如按等级、按权益次数等 */
    @TableField("policy_type")
    @ApiModelProperty("策略类型")
    private String policyType;

    /** 绑定的会员权益编码 */
    @TableField("feature_code")
    @ApiModelProperty("绑定的权益编码")
    private String featureCode;

    /** 允许通过的会员等级白名单JSON */
    @TableField("allowed_level_codes_json")
    @ApiModelProperty("允许等级白名单JSON")
    private String allowedLevelCodesJson;

    /** 前端拒绝时的展示模式，如弹窗、占位等 */
    @TableField("deny_client_mode")
    @ApiModelProperty("前端失败展示模式")
    private String denyClientMode;

    /** 拒绝访问时的提示标题 */
    @TableField("deny_title")
    @ApiModelProperty("失败提示标题")
    private String denyTitle;

    /** 拒绝访问时的提示文案 */
    @TableField("deny_message")
    @ApiModelProperty("失败提示文案")
    private String denyMessage;

    /** 是否引导用户跳转到会员购买页 */
    @TableField("guide_member_page")
    @ApiModelProperty("是否引导去会员页")
    private Integer guideMemberPage;

    /** 规则匹配优先级，数值越小优先级越高 */
    @TableField("priority")
    @ApiModelProperty("优先级")
    private Integer priority;

    /** 启用状态 */
    @TableField("status")
    @ApiModelProperty("状态")
    private Integer status;

    /** 规则版本号，用于乐观锁控制 */
    @TableField("version_no")
    @ApiModelProperty("规则版本号")
    private Integer versionNo;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberGateRuleId;
    }
}

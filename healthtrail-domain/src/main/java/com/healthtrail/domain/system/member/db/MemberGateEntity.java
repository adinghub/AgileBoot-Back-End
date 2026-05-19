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
 * 会员功能门禁点表，定义系统中受会员权限控制的业务入口
 *
 * <p>这张表定义"系统里有哪些稳定的受控业务入口"，
 * 它表达的是业务动作，而不是会员权益本身。
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("member_gate")
@ApiModel(value = "MemberGateEntity对象", description = "会员功能门禁点表")
public class MemberGateEntity extends BaseEntity<MemberGateEntity> {

    private static final long serialVersionUID = 1L;

    /** 门禁点主键ID */
    @TableId(value = "member_gate_id", type = IdType.AUTO)
    @ApiModelProperty("门禁点ID")
    private Long memberGateId;

    /** 门禁点唯一编码，用于程序识别受控入口 */
    @TableField("gate_code")
    @ApiModelProperty("门禁点编码")
    private String gateCode;

    /** 门禁点显示名称 */
    @TableField("gate_name")
    @ApiModelProperty("门禁点名称")
    private String gateName;

    /** 门禁点作用域，如全局、按模块等 */
    @TableField("gate_scope")
    @ApiModelProperty("门禁点作用域")
    private String gateScope;

    /** 所属业务模块 */
    @TableField("biz_module")
    @ApiModelProperty("所属业务模块")
    private String bizModule;

    /** 生效终端类型，如APP、小程序、H5 */
    @TableField("terminal_type")
    @ApiModelProperty("终端类型")
    private String terminalType;

    /** 未匹配规则时的默认放行策略 */
    @TableField("default_policy_type")
    @ApiModelProperty("默认策略类型")
    private String defaultPolicyType;

    /** 启用状态 */
    @TableField("status")
    @ApiModelProperty("状态")
    private Integer status;

    /** 是否为系统内置门禁点，内置不可删除 */
    @TableField("is_builtin")
    @ApiModelProperty("是否内置")
    private Integer isBuiltin;

    /** 备注说明 */
    @TableField("remark")
    @ApiModelProperty("备注")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.memberGateId;
    }
}

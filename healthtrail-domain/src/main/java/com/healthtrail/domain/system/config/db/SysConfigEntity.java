package com.healthtrail.domain.system.config.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统参数配置表，存储系统级别的键值对配置项
 *
 * @author valarchie
 * @since 2022-11-03
 */
@Getter
@Setter
@TableName("sys_config")
@ApiModel(value = "SysConfigEntity对象", description = "参数配置表")
public class SysConfigEntity extends BaseEntity<SysConfigEntity> {

    private static final long serialVersionUID = 1L;

    /** 参数配置主键ID */
    @ApiModelProperty("参数主键")
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;

    /** 参数配置的中文名称 */
    @ApiModelProperty("配置名称")
    @TableField("config_name")
    private String configName;

    /** 参数配置的键名，用于程序读取 */
    @ApiModelProperty("配置键名")
    @TableField("config_key")
    private String configKey;

    /** 配置项的可选值选项列表 */
    @ApiModelProperty("可选的选项")
    @TableField("config_options")
    private String configOptions;

    /** 参数配置的当前值 */
    @ApiModelProperty("配置值")
    @TableField("config_value")
    private String configValue;

    /** 是否允许在前端界面修改此配置 */
    @ApiModelProperty("是否允许修改")
    @TableField("is_allow_change")
    private Boolean isAllowChange;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.configId;
    }

}

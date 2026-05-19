package com.healthtrail.domain.system.menu.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统菜单权限表，定义前端菜单及按钮权限
 *
 * @author valarchie
 * @since 2023-07-21
 */
@Getter
@Setter
@TableName("sys_menu")
@ApiModel(value = "SysMenuEntity对象", description = "菜单权限表")
public class SysMenuEntity extends BaseEntity<SysMenuEntity> {

    private static final long serialVersionUID = 1L;

    /** 菜单主键ID */
    @ApiModelProperty("菜单ID")
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    /** 菜单显示名称 */
    @ApiModelProperty("菜单名称")
    @TableField("menu_name")
    private String menuName;

    /** 菜单类型：1普通菜单 2目录 3内嵌iFrame 4外部链接 */
    @ApiModelProperty("菜单的类型(1为普通菜单2为目录3为iFrame4为外部网站)")
    @TableField("menu_type")
    private Integer menuType;

    /** 路由名称，需与前端Vue组件中defineOptions设置的name保持一致 */
    @ApiModelProperty("路由名称（需保持和前端对应的vue文件中的name保持一致defineOptions方法中设置的name）")
    @TableField("router_name")
    private String routerName;

    /** 父菜单ID，用于构建菜单树形结构 */
    @ApiModelProperty("父菜单ID")
    @TableField("parent_id")
    private Long parentId;

    /** 前端组件路径，对应前端view目录下的文件路径 */
    @ApiModelProperty("组件路径（对应前端项目view文件夹中的路径）")
    @TableField("path")
    private String path;

    /** 是否为按钮权限标识 */
    @ApiModelProperty("是否按钮")
    @TableField("is_button")
    private Boolean isButton;

    /** 权限标识字符串，用于后端鉴权 */
    @ApiModelProperty("权限标识")
    @TableField("permission")
    private String permission;

    /** 路由元信息JSON，前端根据此信息进行页面渲染控制 */
    @ApiModelProperty("路由元信息（前端根据这个信息进行逻辑处理）")
    @TableField("meta_info")
    private String metaInfo;

    /** 菜单状态，1启用 0停用 */
    @ApiModelProperty("菜单状态（1启用 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;


    @Override
    public Serializable pkVal() {
        return this.menuId;
    }

}

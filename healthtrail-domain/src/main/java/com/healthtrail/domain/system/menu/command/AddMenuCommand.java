package com.healthtrail.domain.system.menu.command;

import com.healthtrail.domain.system.menu.dto.MetaDTO;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 新增菜单命令
 * @author valarchie
 */
@Data
public class AddMenuCommand {

    /** 父菜单ID */
    private Long parentId;
    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;
    /** 路由名称 */
    private String routerName;
    /** 路由地址 */
    @Size(max = 200, message = "路由地址不能超过200个字符")
    private String path;
    /** 菜单状态 */
    private Integer status;
    /** 菜单类型 */
    private Integer menuType;
    /** 是否按钮 */
    private Boolean isButton;
    /** 权限标识 */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permission;
    /** 菜单元数据 */
    private MetaDTO meta;

}

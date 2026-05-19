package com.healthtrail.domain.system.menu.model;

import cn.hutool.core.bean.BeanUtil;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.exception.error.ErrorCode.Business;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.system.menu.command.AddMenuCommand;
import com.healthtrail.domain.system.menu.command.UpdateMenuCommand;
import com.healthtrail.common.enums.common.MenuTypeEnum;
import com.healthtrail.domain.system.menu.db.SysMenuEntity;
import com.healthtrail.domain.system.menu.db.SysMenuService;
import java.util.Objects;
import lombok.NoArgsConstructor;

/**
 * 菜单权限领域模型，负责菜单的增删改命令装载和业务规则校验。
 *
 * @author valarchie
 */
@NoArgsConstructor
public class MenuModel extends SysMenuEntity {

    /** 菜单数据库服务 */
    private SysMenuService menuService;

    public MenuModel(SysMenuService menuService) {
        this.menuService = menuService;
    }

    public MenuModel(SysMenuEntity entity, SysMenuService menuService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.menuService = menuService;
    }

    /** 从新增命令装载菜单字段 */
    public void loadAddCommand(AddMenuCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "menuId");

            String metaInfo = JacksonUtil.to(command.getMeta());
            this.setMetaInfo(metaInfo);
        }
    }

    /** 从更新命令装载菜单字段，不允许修改菜单类型 */
    public void loadUpdateCommand(UpdateMenuCommand command) {
        if (command != null) {
            if (!Objects.equals(this.getMenuType(), command.getMenuType()) && !this.getIsButton()) {
                throw new ApiException(Business.MENU_CAN_NOT_CHANGE_MENU_TYPE);
            }
            loadAddCommand(command);
        }
    }

    /** 校验菜单名称在同一层级下是否唯一 */
    public void checkMenuNameUnique() {
        if (menuService.isMenuNameDuplicated(getMenuName(), getMenuId(), getParentId())) {
            throw new ApiException(ErrorCode.Business.MENU_NAME_IS_NOT_UNIQUE);
        }
    }

    /**
     * 校验是否在Iframe或外链菜单下添加了按钮。
     * Iframe和外链跳转类型不允许添加子按钮。
     */
    public void checkAddButtonInIframeOrOutLink() {
        SysMenuEntity parentMenu = menuService.getById(getParentId());

        if (parentMenu != null && getIsButton() && (
            Objects.equals(parentMenu.getMenuType(), MenuTypeEnum.IFRAME.getValue())
                || Objects.equals(parentMenu.getMenuType(),MenuTypeEnum.OUTSIDE_LINK_REDIRECT.getValue())
        )) {
            throw new ApiException(Business.MENU_NOT_ALLOWED_TO_CREATE_BUTTON_ON_IFRAME_OR_OUT_LINK);
        }
    }

    /**
     * 校验是否在非目录类型下添加子菜单。
     * 只允许在目录菜单类型底下添加子菜单。
     */
    public void checkAddMenuNotInCatalog() {
        SysMenuEntity parentMenu = menuService.getById(getParentId());

        if (parentMenu != null && !getIsButton() && (
            !Objects.equals(parentMenu.getMenuType(), MenuTypeEnum.CATALOG.getValue())
        )) {
            throw new ApiException(Business.MENU_ONLY_ALLOWED_TO_CREATE_SUB_MENU_IN_CATALOG);
        }
    }

    /** 校验外链地址格式 */
    public void checkExternalLink() {
//        if (getIsExternal() && !HttpUtil.isHttp(getPath()) && !HttpUtil.isHttps(getPath())) {
//            throw new ApiException(ErrorCode.Business.MENU_EXTERNAL_LINK_MUST_BE_HTTP);
//        }
    }

    /** 校验父菜单不能选择自己 */
    public void checkParentIdConflict() {
        if (getMenuId().equals(getParentId())) {
            throw new ApiException(ErrorCode.Business.MENU_PARENT_ID_NOT_ALLOW_SELF);
        }
    }

    /** 校验是否存在子菜单，有则不允许删除 */
    public void checkHasChildMenus() {
        if (menuService.hasChildrenMenu(getMenuId())) {
            throw new ApiException(ErrorCode.Business.MENU_EXIST_CHILD_MENU_NOT_ALLOW_DELETE);
        }
    }

    /** 校验菜单是否已分配给角色，已分配则不允许删除 */
    public void checkMenuAlreadyAssignToRole() {
        if (menuService.isMenuAssignToRoles(getMenuId())) {
            throw new ApiException(ErrorCode.Business.MENU_ALREADY_ASSIGN_TO_ROLE_NOT_ALLOW_DELETE);
        }
    }

}

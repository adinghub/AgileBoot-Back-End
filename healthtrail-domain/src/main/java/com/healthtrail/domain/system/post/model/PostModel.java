package com.healthtrail.domain.system.post.model;

import cn.hutool.core.bean.BeanUtil;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.system.post.command.AddPostCommand;
import com.healthtrail.domain.system.post.command.UpdatePostCommand;
import com.healthtrail.domain.system.post.db.SysPostEntity;
import com.healthtrail.domain.system.post.db.SysPostService;
import lombok.NoArgsConstructor;

/**
 * 岗位领域模型，负责岗位的增删改命令装载以及名称编码唯一性校验。
 *
 * @author valarchie
 */
@NoArgsConstructor
public class PostModel extends SysPostEntity {

    /** 岗位数据库服务 */
    private SysPostService postService;

    public PostModel(SysPostService postService) {
        this.postService = postService;
    }

    public PostModel(SysPostEntity entity, SysPostService postService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.postService = postService;
    }

    /** 从新增命令装载岗位字段 */
    public void loadFromAddCommand(AddPostCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "postId");
        }
    }

    /** 从更新命令装载岗位字段 */
    public void loadFromUpdateCommand(UpdatePostCommand command) {
        if (command != null) {
            loadFromAddCommand(command);
        }
    }

    /** 校验岗位是否已分配给用户，已分配则不允许删除 */
    public void checkCanBeDelete() {
        if (postService.isAssignedToUsers(this.getPostId())) {
            throw new ApiException(ErrorCode.Business.POST_ALREADY_ASSIGNED_TO_USER_CAN_NOT_BE_DELETED);
        }
    }

    /** 校验岗位名称是否唯一 */
    public void checkPostNameUnique() {
        if (postService.isPostNameDuplicated(getPostId(), getPostName())) {
            throw new ApiException(ErrorCode.Business.POST_NAME_IS_NOT_UNIQUE, getPostName());
        }
    }

    /** 校验岗位编码是否唯一 */
    public void checkPostCodeUnique() {
        if (postService.isPostCodeDuplicated(getPostId(), getPostCode())) {
            throw new ApiException(ErrorCode.Business.POST_CODE_IS_NOT_UNIQUE, getPostCode());
        }
    }

}

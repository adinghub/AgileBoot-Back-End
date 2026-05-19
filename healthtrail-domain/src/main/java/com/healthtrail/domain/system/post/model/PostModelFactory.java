package com.healthtrail.domain.system.post.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode.Business;
import com.healthtrail.domain.system.post.db.SysPostEntity;
import com.healthtrail.domain.system.post.db.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 岗位领域模型工厂 */
@Component
@RequiredArgsConstructor
public class PostModelFactory {

    /** 岗位数据库服务 */
    private final SysPostService postService;

    public PostModel loadById(Long postId) {
        SysPostEntity byId = postService.getById(postId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, postId, "职位");
        }
        return new PostModel(byId, postService);
    }

    public PostModel create() {
        return new PostModel(postService);
    }

}

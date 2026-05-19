package com.healthtrail.domain.health.user.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * App 用户领域模型工厂。
 *
 * <p>统一负责创建空模型以及从数据库加载已有模型，
 * 让上层业务不需要关心实体复制和空值判断细节。
 */
@Component
@RequiredArgsConstructor
public class AppUserModelFactory {

    /** App用户数据库服务 */
    private final HealthAppUserService appUserService;

    public AppUserModel create() {
        return new AppUserModel(appUserService);
    }

    public AppUserModel loadById(Long userId) {
        HealthAppUserEntity entity = appUserService.getById(userId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.APP_USER_NON_EXIST);
        }
        return new AppUserModel(entity, appUserService);
    }
}

package com.healthtrail.domain.health.user.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * App 用户服务接口。
 *
 * <p>该服务只负责与 App 用户表直接相关的数据库操作，
 * 更复杂的业务编排放在 ApplicationService 和 Model 中处理。
 */
public interface HealthAppUserService extends IService<HealthAppUserEntity> {

    /**
     * 检查手机号是否已经被其他 App 账户占用。
     *
     * @param mobile 手机号
     * @param userId 当前用户ID，新增时传 null，更新时用于排除自己
     * @return true 表示已存在重复手机号
     */
    boolean isMobileDuplicated(String mobile, Long userId);

    /**
     * 通过手机号查询 App 用户。
     *
     * @param mobile 手机号
     * @return App 用户实体；不存在时返回 null
     */
    HealthAppUserEntity getByMobile(String mobile);
}

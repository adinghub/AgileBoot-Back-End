package com.healthtrail.domain.health.family.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 家庭共享邀请服务接口。
 */
public interface HealthFamilyShareInviteService extends IService<HealthFamilyShareInviteEntity> {

    /**
     * 根据邀请码查询邀请。
     *
     * @param inviteCode 邀请码
     * @return 邀请实体，不存在时返回 null
     */
    HealthFamilyShareInviteEntity getByInviteCode(String inviteCode);
}

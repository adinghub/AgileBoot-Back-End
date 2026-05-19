package com.healthtrail.domain.health.family.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 家庭成员共享关系服务接口。
 */
public interface HealthFamilyMemberShareService extends IService<HealthFamilyMemberShareEntity> {

    /**
     * 判断某个成员是否已经共享给指定协同账号。
     *
     * @param memberId 家庭成员ID
     * @param collaboratorUserId 协同账号ID
     * @return true 表示存在生效中的共享关系
     */
    boolean existsEnabledShare(Long memberId, Long collaboratorUserId);
}

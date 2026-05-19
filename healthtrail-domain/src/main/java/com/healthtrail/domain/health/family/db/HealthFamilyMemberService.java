package com.healthtrail.domain.health.family.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 家庭成员数据库服务接口。
 */
public interface HealthFamilyMemberService extends IService<HealthFamilyMemberEntity> {

    /**
     * 检查指定成员是否归属于某个 App 用户。
     *
     * @param memberId     家庭成员ID
     * @param ownerUserId  App 用户ID
     * @return true 表示归属关系成立
     */
    boolean isOwnedByUser(Long memberId, Long ownerUserId);
}

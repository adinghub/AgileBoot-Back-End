package com.healthtrail.domain.health.family.model;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 家庭成员模型工厂。
 */
@Component
@RequiredArgsConstructor
public class FamilyMemberModelFactory {

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    public FamilyMemberModel create() {
        return new FamilyMemberModel(familyMemberService);
    }

    public FamilyMemberModel loadById(Long memberId) {
        HealthFamilyMemberEntity entity = familyMemberService.getById(memberId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberId, "家庭成员");
        }
        return new FamilyMemberModel(entity, familyMemberService);
    }
}

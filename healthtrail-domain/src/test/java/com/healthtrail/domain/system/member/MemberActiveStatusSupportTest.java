package com.healthtrail.domain.system.member;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.healthtrail.domain.system.member.db.UserMemberEntity;
import com.healthtrail.domain.system.member.enums.UserMemberStatusEnum;
import java.util.Date;
import org.junit.jupiter.api.Test;

class MemberActiveStatusSupportTest {

    @Test
    void shouldTreatNullEndTimeAsLongTermActive() {
        UserMemberEntity entity = new UserMemberEntity();
        entity.setStatus(UserMemberStatusEnum.ACTIVE.name());
        entity.setEffectiveEndTime(null);

        assertTrue(MemberActiveStatusSupport.isMemberCurrentlyActive(entity, new Date()));
    }

    @Test
    void shouldTreatExpiredMemberAsInactiveEvenWhenStatusStillActive() {
        UserMemberEntity entity = new UserMemberEntity();
        entity.setStatus(UserMemberStatusEnum.ACTIVE.name());
        entity.setEffectiveEndTime(new Date(System.currentTimeMillis() - 60_000));

        assertFalse(MemberActiveStatusSupport.isMemberCurrentlyActive(entity, new Date()));
    }
}

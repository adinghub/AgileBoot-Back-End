package com.healthtrail.domain.system.member;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.healthtrail.domain.system.member.command.RedeemMemberCodeCommand;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.db.MemberRedeemCodeEntity;
import com.healthtrail.domain.system.member.db.MemberRedeemCodeService;
import com.healthtrail.domain.system.member.db.UserMemberEntity;
import com.healthtrail.domain.system.member.db.UserMemberService;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionDTO;
import com.healthtrail.domain.system.member.enums.MemberRedeemCodeStatusEnum;
import com.healthtrail.domain.system.member.enums.UserMemberOrderSourceTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberOrderTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberSourceTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberStatusEnum;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员兑换码应用服务。
 */
@Service
@RequiredArgsConstructor
public class MemberRedeemCodeApplicationService {

    /** 会员兑换码数据库服务 */
    private final MemberRedeemCodeService memberRedeemCodeService;
    /** 会员等级数据库服务 */
    private final MemberLevelService memberLevelService;
    /** 用户会员关系数据库服务 */
    private final UserMemberService userMemberService;
    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;
    /** 会员后台应用服务 */
    private final MemberApplicationService memberApplicationService;
    /** 会员订阅应用服务 */
    private final MemberSubscriptionApplicationService memberSubscriptionApplicationService;

    @Transactional(rollbackFor = Exception.class)
    public MemberSubscriptionDTO redeemCode(Long userId, RedeemMemberCodeCommand command) {
        HealthAppUserEntity appUserEntity = healthAppUserService.getById(userId);
        if (appUserEntity == null) {
            throw new ApiException(ErrorCode.Business.APP_USER_NON_EXIST);
        }
        MemberRedeemCodeEntity redeemCodeEntity = memberRedeemCodeService.lambdaQuery()
            .eq(MemberRedeemCodeEntity::getRedeemCode, command.getRedeemCode().trim().toUpperCase())
            .one();
        if (redeemCodeEntity == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_REDEEM_CODE_NOT_FOUND);
        }
        if (!Objects.equals(redeemCodeEntity.getCodeStatus(), MemberRedeemCodeStatusEnum.AVAILABLE.name())) {
            throw new ApiException(ErrorCode.Business.MEMBER_REDEEM_CODE_STATUS_INVALID);
        }
        if (redeemCodeEntity.getExpireTime() != null && redeemCodeEntity.getExpireTime().before(new Date())) {
            throw new ApiException(ErrorCode.Business.MEMBER_REDEEM_CODE_STATUS_INVALID);
        }
        MemberLevelEntity memberLevelEntity = memberLevelService.getById(redeemCodeEntity.getMemberLevelId());
        if (memberLevelEntity == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_LEVEL_DISABLED);
        }

        UserMemberEntity currentEntity = userMemberService.lambdaQuery()
            .eq(UserMemberEntity::getUserId, userId)
            .one();
        Date now = new Date();
        boolean hasActiveMember = MemberActiveStatusSupport.isMemberCurrentlyActive(currentEntity, now);
        Date nextStart = now;
        Date nextEnd = memberApplicationService.resolveEndTime(now, redeemCodeEntity.getDurationDaysSnapshot());

        if (hasActiveMember && currentEntity != null) {
            if (!Objects.equals(currentEntity.getMemberLevelId(), memberLevelEntity.getMemberLevelId())) {
                throw new ApiException(ErrorCode.Business.MEMBER_ACTIVE_LEVEL_CONFLICT);
            }
            nextStart = currentEntity.getEffectiveStartTime();
            Date baseEnd = currentEntity.getEffectiveEndTime() == null || currentEntity.getEffectiveEndTime().before(now)
                ? now
                : currentEntity.getEffectiveEndTime();
            nextEnd = memberApplicationService.resolveEndTime(baseEnd, redeemCodeEntity.getDurationDaysSnapshot());
            currentEntity.setEffectiveEndTime(nextEnd);
            currentEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
            currentEntity.setSourceType(UserMemberSourceTypeEnum.REDEEM_CODE.name());
            currentEntity.setSourceId(redeemCodeEntity.getMemberRedeemCodeId());
            currentEntity.setRemark("兑换码兑换成功");
            userMemberService.updateById(currentEntity);
        } else if (currentEntity != null) {
            currentEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            currentEntity.setEffectiveStartTime(nextStart);
            currentEntity.setEffectiveEndTime(nextEnd);
            currentEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
            currentEntity.setSourceType(UserMemberSourceTypeEnum.REDEEM_CODE.name());
            currentEntity.setSourceId(redeemCodeEntity.getMemberRedeemCodeId());
            currentEntity.setRemark("兑换码兑换成功");
            userMemberService.updateById(currentEntity);
        } else {
            UserMemberEntity saveEntity = new UserMemberEntity();
            saveEntity.setUserId(userId);
            saveEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            saveEntity.setEffectiveStartTime(nextStart);
            saveEntity.setEffectiveEndTime(nextEnd);
            saveEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
            saveEntity.setSourceType(UserMemberSourceTypeEnum.REDEEM_CODE.name());
            saveEntity.setSourceId(redeemCodeEntity.getMemberRedeemCodeId());
            saveEntity.setRemark("兑换码兑换成功");
            userMemberService.save(saveEntity);
        }

        redeemCodeEntity.setCodeStatus(MemberRedeemCodeStatusEnum.REDEEMED.name());
        redeemCodeEntity.setRedeemedUserId(userId);
        redeemCodeEntity.setRedeemedTime(now);
        memberRedeemCodeService.updateById(redeemCodeEntity);

        memberApplicationService.createMemberOrder(userId, memberLevelEntity, UserMemberOrderTypeEnum.REDEEM.name(),
            UserMemberOrderSourceTypeEnum.REDEEM_CODE.name(), redeemCodeEntity.getPriceSnapshot(),
            nextStart, nextEnd, "兑换码兑换成功", null);

        return memberSubscriptionApplicationService.getCurrentSubscription(userId);
    }
}

package com.healthtrail.domain.system.member;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.healthtrail.domain.system.member.command.ChangeMemberSubscriptionAutoRenewCommand;
import com.healthtrail.domain.system.member.command.SubscribeMemberCommand;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.db.UserMemberEntity;
import com.healthtrail.domain.system.member.db.UserMemberOrderEntity;
import com.healthtrail.domain.system.member.db.UserMemberOrderService;
import com.healthtrail.domain.system.member.db.UserMemberService;
import com.healthtrail.domain.system.member.db.UserMemberSubscriptionEntity;
import com.healthtrail.domain.system.member.db.UserMemberSubscriptionService;
import com.healthtrail.domain.system.member.dto.MemberLevelDTO;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionDTO;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionOrderDTO;
import com.healthtrail.domain.system.member.enums.UserMemberOrderSourceTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberOrderTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberSourceTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberStatusEnum;
import com.healthtrail.domain.system.member.enums.UserMemberSubscriptionStatusEnum;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员订阅应用服务。
 *
 * <p>首期先走“模拟开通成功”的方案，
 * 先把会员关系、订单、订阅骨架链路跑通，后续再接真实支付。
 */
@Service
@RequiredArgsConstructor
public class MemberSubscriptionApplicationService {

    /** 会员等级数据库服务 */
    private final MemberLevelService memberLevelService;
    /** 用户会员关系数据库服务 */
    private final UserMemberService userMemberService;
    /** 用户会员订阅数据库服务 */
    private final UserMemberSubscriptionService userMemberSubscriptionService;
    /** 用户会员订单数据库服务 */
    private final UserMemberOrderService userMemberOrderService;
    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;
    /** 会员后台应用服务 */
    private final MemberApplicationService memberApplicationService;

    public List<MemberLevelDTO> getAvailableLevels(Long userId) {
        Date now = new Date();
        UserMemberEntity currentUserMember = userMemberService.lambdaQuery()
            .eq(UserMemberEntity::getUserId, userId)
            .one();
        boolean hasActiveCurrentLevel = MemberActiveStatusSupport.isMemberCurrentlyActive(currentUserMember, now);
        Long currentLevelId = hasActiveCurrentLevel && currentUserMember != null ? currentUserMember.getMemberLevelId() : null;
        boolean hasOtherActiveLevel = hasActiveCurrentLevel;

        return memberLevelService.lambdaQuery()
            .eq(MemberLevelEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByAsc(MemberLevelEntity::getLevelSort)
            .list()
            .stream()
            .map(entity -> {
                MemberLevelDTO dto = new MemberLevelDTO(entity);
                dto.setCurrentLevel(Objects.equals(entity.getMemberLevelId(), currentLevelId));
                if (hasOtherActiveLevel && !Objects.equals(entity.getMemberLevelId(), currentLevelId)) {
                    dto.setCanSubscribe(false);
                    dto.setUnavailableReason("当前存在其他有效会员等级，暂不支持跨等级开通");
                } else {
                    dto.setCanSubscribe(true);
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    public MemberSubscriptionDTO getCurrentSubscription(Long userId) {
        UserMemberEntity currentEntity = userMemberService.lambdaQuery()
            .eq(UserMemberEntity::getUserId, userId)
            .one();
        UserMemberSubscriptionEntity subscriptionEntity = userMemberSubscriptionService.lambdaQuery()
            .eq(UserMemberSubscriptionEntity::getUserId, userId)
            .one();
        MemberSubscriptionDTO dto = new MemberSubscriptionDTO();
        boolean hasActiveMember = MemberActiveStatusSupport.isMemberCurrentlyActive(currentEntity);
        dto.setHasActiveMember(hasActiveMember);
        if (!hasActiveMember || currentEntity == null) {
            return dto;
        }
        MemberLevelEntity memberLevelEntity = memberLevelService.getById(currentEntity.getMemberLevelId());
        dto.setMemberLevelId(currentEntity.getMemberLevelId());
        dto.setLevelCode(memberLevelEntity == null ? null : memberLevelEntity.getLevelCode());
        dto.setLevelName(memberLevelEntity == null ? null : memberLevelEntity.getLevelName());
        dto.setStatus(currentEntity.getStatus());
        dto.setEffectiveStartTime(currentEntity.getEffectiveStartTime());
        dto.setEffectiveEndTime(currentEntity.getEffectiveEndTime());
        dto.setRemainingDays(calculateRemainingDays(currentEntity.getEffectiveEndTime()));
        dto.setAutoRenew(subscriptionEntity == null ? YesOrNoEnum.NO.getValue() : subscriptionEntity.getAutoRenew());
        dto.setSubscriptionStatus(subscriptionEntity == null ? null : subscriptionEntity.getSubscriptionStatus());
        dto.setNextRenewTime(subscriptionEntity == null ? null : subscriptionEntity.getNextRenewTime());
        dto.setCancelReason(subscriptionEntity == null ? null : subscriptionEntity.getCancelReason());
        return dto;
    }

    public List<MemberSubscriptionOrderDTO> getSubscriptionOrders(Long userId) {
        Map<Long, MemberLevelEntity> levelMap = memberLevelService.list().stream()
            .collect(Collectors.toMap(MemberLevelEntity::getMemberLevelId, entity -> entity, (left, right) -> left));
        return userMemberOrderService.lambdaQuery()
            .eq(UserMemberOrderEntity::getUserId, userId)
            .orderByDesc(UserMemberOrderEntity::getUserMemberOrderId)
            .last("limit 20")
            .list()
            .stream()
            .map(entity -> {
                MemberLevelEntity memberLevelEntity = levelMap.get(entity.getMemberLevelId());
                MemberSubscriptionOrderDTO dto = new MemberSubscriptionOrderDTO();
                dto.setUserMemberOrderId(entity.getUserMemberOrderId());
                dto.setOrderNo(entity.getOrderNo());
                dto.setMemberLevelId(entity.getMemberLevelId());
                dto.setLevelCode(memberLevelEntity == null ? null : memberLevelEntity.getLevelCode());
                dto.setLevelName(memberLevelEntity == null ? null : memberLevelEntity.getLevelName());
                dto.setOrderType(entity.getOrderType());
                dto.setOrderStatus(entity.getOrderStatus());
                dto.setSourceType(entity.getSourceType());
                dto.setOrderAmount(entity.getOrderAmount());
                dto.setPayTime(entity.getPayTime());
                dto.setEffectiveStartTime(entity.getEffectiveStartTime());
                dto.setEffectiveEndTime(entity.getEffectiveEndTime());
                dto.setRemark(entity.getRemark());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberSubscriptionDTO subscribe(Long userId, SubscribeMemberCommand command) {
        HealthAppUserEntity appUserEntity = healthAppUserService.getById(userId);
        if (appUserEntity == null) {
            throw new ApiException(ErrorCode.Business.APP_USER_NON_EXIST);
        }
        MemberLevelEntity memberLevelEntity = memberLevelService.getById(command.getMemberLevelId());
        if (memberLevelEntity == null || !Objects.equals(memberLevelEntity.getStatus(), StatusEnum.ENABLE.getValue())) {
            throw new ApiException(ErrorCode.Business.MEMBER_LEVEL_DISABLED);
        }
        UserMemberEntity currentEntity = userMemberService.lambdaQuery()
            .eq(UserMemberEntity::getUserId, userId)
            .one();
        Date now = new Date();
        boolean hasActiveMember = MemberActiveStatusSupport.isMemberCurrentlyActive(currentEntity, now);
        Date startTime = now;
        Date endTime;
        if (hasActiveMember
            && currentEntity != null
            && Objects.equals(currentEntity.getMemberLevelId(), memberLevelEntity.getMemberLevelId())) {
            startTime = currentEntity.getEffectiveStartTime();
            endTime = memberApplicationService.resolveEndTime(currentEntity.getEffectiveEndTime(), memberLevelEntity.getDurationDays());
            currentEntity.setEffectiveEndTime(endTime);
            currentEntity.setSourceType(UserMemberSourceTypeEnum.SUBSCRIPTION.name());
            currentEntity.setRemark(command.getRemark());
            userMemberService.updateById(currentEntity);
        } else {
            if (hasActiveMember
                && currentEntity != null
                && !Objects.equals(currentEntity.getMemberLevelId(), memberLevelEntity.getMemberLevelId())) {
                throw new ApiException(ErrorCode.Business.MEMBER_ACTIVE_LEVEL_CONFLICT);
            }
            endTime = memberApplicationService.resolveEndTime(now, memberLevelEntity.getDurationDays());
            if (currentEntity == null) {
                UserMemberEntity saveEntity = new UserMemberEntity();
                saveEntity.setUserId(userId);
                saveEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
                saveEntity.setEffectiveStartTime(startTime);
                saveEntity.setEffectiveEndTime(endTime);
                saveEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
                saveEntity.setSourceType(UserMemberSourceTypeEnum.SUBSCRIPTION.name());
                saveEntity.setRemark(command.getRemark());
                userMemberService.save(saveEntity);
            } else {
                currentEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
                currentEntity.setEffectiveStartTime(startTime);
                currentEntity.setEffectiveEndTime(endTime);
                currentEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
                currentEntity.setSourceType(UserMemberSourceTypeEnum.SUBSCRIPTION.name());
                currentEntity.setRemark(command.getRemark());
                userMemberService.updateById(currentEntity);
            }
        }

        UserMemberSubscriptionEntity subscriptionEntity = userMemberSubscriptionService.lambdaQuery()
            .eq(UserMemberSubscriptionEntity::getUserId, userId)
            .one();
        if (subscriptionEntity == null) {
            subscriptionEntity = new UserMemberSubscriptionEntity();
            subscriptionEntity.setSubscriptionNo("SUB" + IdUtil.getSnowflakeNextIdStr());
            subscriptionEntity.setUserId(userId);
            subscriptionEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            subscriptionEntity.setSubscriptionStatus(UserMemberSubscriptionStatusEnum.ACTIVE.name());
            subscriptionEntity.setAutoRenew(YesOrNoEnum.NO.getValue());
            subscriptionEntity.setCurrentPeriodStartTime(startTime);
            subscriptionEntity.setCurrentPeriodEndTime(endTime);
            subscriptionEntity.setNextRenewTime(endTime);
            subscriptionEntity.setRemark(command.getRemark());
            userMemberSubscriptionService.save(subscriptionEntity);
        } else {
            subscriptionEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            subscriptionEntity.setSubscriptionStatus(UserMemberSubscriptionStatusEnum.ACTIVE.name());
            subscriptionEntity.setCurrentPeriodStartTime(startTime);
            subscriptionEntity.setCurrentPeriodEndTime(endTime);
            subscriptionEntity.setNextRenewTime(endTime);
            subscriptionEntity.setRemark(command.getRemark());
            userMemberSubscriptionService.updateById(subscriptionEntity);
        }

        memberApplicationService.createMemberOrder(userId, memberLevelEntity, UserMemberOrderTypeEnum.OPEN.name(),
            UserMemberOrderSourceTypeEnum.APP_USER.name(), memberLevelEntity.getPrice(), startTime, endTime,
            command.getRemark(), subscriptionEntity.getSubscriptionId());

        return getCurrentSubscription(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberSubscriptionDTO changeAutoRenew(Long userId, ChangeMemberSubscriptionAutoRenewCommand command) {
        UserMemberSubscriptionEntity subscriptionEntity = userMemberSubscriptionService.lambdaQuery()
            .eq(UserMemberSubscriptionEntity::getUserId, userId)
            .one();
        if (subscriptionEntity == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_SUBSCRIPTION_NOT_FOUND);
        }
        subscriptionEntity.setAutoRenew(command.getEnabled());
        if (Objects.equals(command.getEnabled(), YesOrNoEnum.NO.getValue())) {
            subscriptionEntity.setCancelTime(new Date());
            subscriptionEntity.setCancelReason(command.getRemark());
        } else {
            subscriptionEntity.setCancelTime(null);
            subscriptionEntity.setCancelReason(null);
        }
        subscriptionEntity.setRemark(command.getRemark());
        userMemberSubscriptionService.updateById(subscriptionEntity);
        return getCurrentSubscription(userId);
    }

    private Integer calculateRemainingDays(Date effectiveEndTime) {
        if (effectiveEndTime == null) {
            return null;
        }
        long diff = effectiveEndTime.getTime() - System.currentTimeMillis();
        if (diff <= 0) {
            return 0;
        }
        return (int) Math.ceil(diff / 86400000D);
    }
}

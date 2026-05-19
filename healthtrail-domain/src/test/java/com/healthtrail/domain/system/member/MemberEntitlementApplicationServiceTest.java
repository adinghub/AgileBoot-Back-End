package com.healthtrail.domain.system.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.domain.system.member.db.MemberFeatureEntity;
import com.healthtrail.domain.system.member.db.MemberFeatureQuotaUsageEntity;
import com.healthtrail.domain.system.member.db.MemberFeatureQuotaUsageService;
import com.healthtrail.domain.system.member.db.MemberFeatureService;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelFeatureEntity;
import com.healthtrail.domain.system.member.db.MemberLevelFeatureService;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.db.UserMemberEntity;
import com.healthtrail.domain.system.member.db.UserMemberService;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementItemDTO;
import com.healthtrail.domain.system.member.enums.MemberFeatureQuotaPeriodEnum;
import com.healthtrail.domain.system.member.enums.UserMemberStatusEnum;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

@SuppressWarnings({"unchecked", "rawtypes"})
class MemberEntitlementApplicationServiceTest {

    private final MemberFeatureService memberFeatureService = mock(MemberFeatureService.class);
    private final MemberLevelService memberLevelService = mock(MemberLevelService.class);
    private final MemberLevelFeatureService memberLevelFeatureService = mock(MemberLevelFeatureService.class);
    private final MemberFeatureQuotaUsageService memberFeatureQuotaUsageService = mock(MemberFeatureQuotaUsageService.class);
    private final UserMemberService userMemberService = mock(UserMemberService.class);

    private final MemberEntitlementApplicationService memberEntitlementApplicationService =
        new MemberEntitlementApplicationService(
            memberFeatureService,
            memberLevelService,
            memberLevelFeatureService,
            memberFeatureQuotaUsageService,
            userMemberService
        );

    @Test
    void shouldBatchLoadQuotaUsageWhenBuildingCurrentEntitlements() {
        LambdaQueryChainWrapper<UserMemberEntity> userMemberQuery =
            mock(LambdaQueryChainWrapper.class, Answers.RETURNS_SELF);
        LambdaQueryChainWrapper<MemberLevelFeatureEntity> levelFeatureQuery =
            mock(LambdaQueryChainWrapper.class, Answers.RETURNS_SELF);
        LambdaQueryChainWrapper<MemberFeatureEntity> featureQuery =
            mock(LambdaQueryChainWrapper.class, Answers.RETURNS_SELF);
        LambdaQueryChainWrapper<MemberFeatureQuotaUsageEntity> quotaUsageQuery =
            mock(LambdaQueryChainWrapper.class, Answers.RETURNS_SELF);

        UserMemberEntity userMemberEntity = new UserMemberEntity();
        userMemberEntity.setUserId(1L);
        userMemberEntity.setMemberLevelId(2L);
        userMemberEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
        userMemberEntity.setEffectiveEndTime(new Date(System.currentTimeMillis() + 86_400_000L));

        MemberLevelEntity currentLevel = new MemberLevelEntity();
        currentLevel.setMemberLevelId(2L);
        currentLevel.setLevelCode("PLUS");
        currentLevel.setLevelName("健康PLUS会员");

        MemberFeatureEntity dailyQuotaFeature = new MemberFeatureEntity();
        dailyQuotaFeature.setMemberFeatureId(11L);
        dailyQuotaFeature.setFeatureCode("AI_REPORT_PARSE_QUOTA");
        dailyQuotaFeature.setFeatureName("AI报告解析次数");
        dailyQuotaFeature.setFeatureType("QUOTA");
        dailyQuotaFeature.setQuotaPeriodType(MemberFeatureQuotaPeriodEnum.DAY.name());
        dailyQuotaFeature.setFreeEnabled(YesOrNoEnum.YES.getValue());
        dailyQuotaFeature.setFreeLimitValue(3);
        dailyQuotaFeature.setFeatureSort(1);

        MemberFeatureEntity monthlyQuotaFeature = new MemberFeatureEntity();
        monthlyQuotaFeature.setMemberFeatureId(12L);
        monthlyQuotaFeature.setFeatureCode("AI_REPORT_SUMMARY_QUOTA");
        monthlyQuotaFeature.setFeatureName("AI报告总结次数");
        monthlyQuotaFeature.setFeatureType("QUOTA");
        monthlyQuotaFeature.setQuotaPeriodType(MemberFeatureQuotaPeriodEnum.MONTH.name());
        monthlyQuotaFeature.setFreeEnabled(YesOrNoEnum.YES.getValue());
        monthlyQuotaFeature.setFreeLimitValue(10);
        monthlyQuotaFeature.setFeatureSort(2);

        MemberFeatureQuotaUsageEntity dailyUsage = new MemberFeatureQuotaUsageEntity();
        dailyUsage.setUserId(1L);
        dailyUsage.setMemberFeatureId(11L);
        dailyUsage.setPeriodKey(memberEntitlementApplicationService.resolvePeriodKey(
            MemberFeatureQuotaPeriodEnum.DAY.name(), new Date()));
        dailyUsage.setUsedCount(1);

        MemberFeatureQuotaUsageEntity monthlyUsage = new MemberFeatureQuotaUsageEntity();
        monthlyUsage.setUserId(1L);
        monthlyUsage.setMemberFeatureId(12L);
        monthlyUsage.setPeriodKey(memberEntitlementApplicationService.resolvePeriodKey(
            MemberFeatureQuotaPeriodEnum.MONTH.name(), new Date()));
        monthlyUsage.setUsedCount(4);

        when(userMemberService.lambdaQuery()).thenReturn(userMemberQuery);
        doReturn(userMemberQuery).when(userMemberQuery).eq(any(), any());
        when(userMemberQuery.one()).thenReturn(userMemberEntity);

        when(memberLevelService.getById(2L)).thenReturn(currentLevel);

        when(memberLevelFeatureService.lambdaQuery()).thenReturn(levelFeatureQuery);
        doReturn(levelFeatureQuery).when(levelFeatureQuery).eq(any(), any());
        when(levelFeatureQuery.list()).thenReturn(List.of());

        when(memberFeatureService.lambdaQuery()).thenReturn(featureQuery);
        doReturn(featureQuery).when(featureQuery).in(anyBoolean(), any(), any(java.util.Collection.class));
        /*
         * MyBatis Plus 的 orderByAsc(...) 是接口默认方法，Mockito 的 RETURNS_SELF 不会像 eq/in 这类
         * 真实 mock 方法一样自动返回当前 wrapper。
         *
         * 这次批量优化把权益定义查询改成了 “in(...) + orderByAsc(...) + list()” 的链式写法，
         * 如果这里不显式 stub，测试会在默认方法返回 null 后提前 NPE，导致我们误以为是主逻辑有问题。
         *
         * 因此这里明确把排序链路也回到同一个 mock wrapper，确保测试验证的是：
         * 1. 权益快照是否会批量预热 QUOTA 使用量；
         * 2. 而不是被 Mockito 对默认方法的处理差异干扰。
         */
        doReturn(featureQuery).when(featureQuery).orderByAsc(org.mockito.ArgumentMatchers.<SFunction<MemberFeatureEntity, ?>>any());
        when(featureQuery.list()).thenReturn(List.of(dailyQuotaFeature, monthlyQuotaFeature));

        when(memberFeatureQuotaUsageService.lambdaQuery()).thenReturn(quotaUsageQuery);
        doReturn(quotaUsageQuery).when(quotaUsageQuery).eq(any(), any());
        doReturn(quotaUsageQuery).when(quotaUsageQuery).in(any(), any(java.util.Collection.class));
        when(quotaUsageQuery.list()).thenReturn(List.of(dailyUsage, monthlyUsage));

        AppMemberEntitlementDTO entitlements = memberEntitlementApplicationService.getCurrentEntitlements(1L);

        assertNotNull(entitlements);
        assertEquals(2, entitlements.getItems().size());
        AppMemberEntitlementItemDTO firstItem = entitlements.getItems().get(0);
        AppMemberEntitlementItemDTO secondItem = entitlements.getItems().get(1);
        assertEquals(Integer.valueOf(1), firstItem.getUsedCount());
        assertEquals(Integer.valueOf(2), firstItem.getRemainingCount());
        assertEquals(Integer.valueOf(4), secondItem.getUsedCount());
        assertEquals(Integer.valueOf(6), secondItem.getRemainingCount());

        verify(memberFeatureQuotaUsageService, times(1)).lambdaQuery();
        verify(quotaUsageQuery, times(1)).list();
        verify(quotaUsageQuery, never()).one();
    }
}

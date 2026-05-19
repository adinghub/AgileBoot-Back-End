package com.healthtrail.domain.system.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementItemDTO;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MemberAccessGateServiceTest {

    private final MemberEntitlementApplicationService memberEntitlementApplicationService = mock(MemberEntitlementApplicationService.class);
    private final MemberLevelService memberLevelService = mock(MemberLevelService.class);
    private final MemberAccessGateService memberAccessGateService =
        new MemberAccessGateService(memberEntitlementApplicationService, memberLevelService);

    @Test
    void shouldRejectWhenRequiredLevelIsHigherThanCurrentLevel() {
        when(memberEntitlementApplicationService.getCurrentEntitlements(eq(1L),
            org.mockito.ArgumentMatchers.<Collection<String>>any()))
            .thenReturn(buildFreeEntitlements());
        when(memberLevelService.list()).thenReturn(List.of(buildLevel(1L, "FREE", "免费版", 10), buildLevel(2L, "PLUS", "健康PLUS会员", 20)));

        MemberAccessRequirement requirement = new MemberAccessRequirement();
        requirement.setRequiredLevelCode(MemberLevelCodeConstants.PLUS);

        MemberAccessResult result = memberAccessGateService.checkAccess(1L, requirement);

        assertFalse(result.isAllowed());
        assertEquals(MemberAccessDenyReasonEnum.REQUIRED_LEVEL_NOT_MET.name(), result.getDenyReasonCode());
        assertEquals(MemberLevelCodeConstants.FREE, result.getCurrentLevelCode());
    }

    @Test
    void shouldRejectWhenQuotaFeatureHasNoRemainingCount() {
        AppMemberEntitlementDTO entitlements = buildFreeEntitlements();
        AppMemberEntitlementItemDTO item = new AppMemberEntitlementItemDTO();
        item.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_PARSE_QUOTA);
        item.setFeatureName("AI报告解析次数");
        item.setFeatureType("QUOTA");
        item.setEnabled(YesOrNoEnum.YES.getValue());
        item.setLimitValue(3);
        item.setUsedCount(3);
        item.setRemainingCount(0);
        item.setMessage("当前周期可用次数已用完");
        entitlements.setItems(List.of(item));

        when(memberEntitlementApplicationService.getCurrentEntitlements(eq(1L),
            org.mockito.ArgumentMatchers.<Collection<String>>any())).thenReturn(entitlements);
        when(memberLevelService.list()).thenReturn(List.of(buildLevel(1L, "FREE", "免费版", 10)));

        MemberAccessRequirement requirement = new MemberAccessRequirement();
        requirement.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_PARSE_QUOTA);

        MemberAccessResult result = memberAccessGateService.checkAccess(1L, requirement);

        assertFalse(result.isAllowed());
        assertEquals(MemberAccessDenyReasonEnum.QUOTA_NOT_ENOUGH.name(), result.getDenyReasonCode());
    }

    @Test
    void shouldAllowWhenCurrentLevelAndFeatureBothMeetRequirement() {
        AppMemberEntitlementDTO entitlements = new AppMemberEntitlementDTO();
        entitlements.setUserId(1L);
        entitlements.setHasActiveMember(true);
        entitlements.setCurrentMemberLevelId(2L);
        entitlements.setCurrentLevelCode("PLUS");
        entitlements.setCurrentLevelName("健康PLUS会员");

        AppMemberEntitlementItemDTO item = new AppMemberEntitlementItemDTO();
        item.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_SUMMARY);
        item.setFeatureName("AI报告总结");
        item.setFeatureType("SWITCH");
        item.setEnabled(YesOrNoEnum.YES.getValue());
        item.setMessage("当前会员已开放");
        entitlements.setItems(List.of(item));

        when(memberEntitlementApplicationService.getCurrentEntitlements(eq(1L),
            org.mockito.ArgumentMatchers.<Collection<String>>any())).thenReturn(entitlements);
        when(memberLevelService.list()).thenReturn(List.of(
            buildLevel(1L, "FREE", "免费版", 10),
            buildLevel(2L, "PLUS", "健康PLUS会员", 20),
            buildLevel(3L, "PRO", "健康PRO会员", 30)
        ));

        MemberAccessRequirement requirement = new MemberAccessRequirement();
        requirement.setRequiredLevelCode(MemberLevelCodeConstants.PLUS);
        requirement.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_SUMMARY);

        MemberAccessResult result = memberAccessGateService.checkAccess(1L, requirement);

        assertTrue(result.isAllowed());
        assertEquals("会员准入校验通过", result.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBatchCheckAccessWithSharedEntitlementContext() {
        AppMemberEntitlementDTO entitlements = new AppMemberEntitlementDTO();
        entitlements.setUserId(1L);
        entitlements.setHasActiveMember(true);
        entitlements.setCurrentMemberLevelId(2L);
        entitlements.setCurrentLevelCode("PLUS");
        entitlements.setCurrentLevelName("健康PLUS会员");

        AppMemberEntitlementItemDTO item = new AppMemberEntitlementItemDTO();
        item.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_SUMMARY);
        item.setFeatureName("AI报告总结");
        item.setFeatureType("SWITCH");
        item.setEnabled(YesOrNoEnum.YES.getValue());
        item.setMessage("当前会员已开放");
        entitlements.setItems(List.of(item));

        when(memberEntitlementApplicationService.getCurrentEntitlements(eq(1L),
            org.mockito.ArgumentMatchers.<Collection<String>>any())).thenReturn(entitlements);
        when(memberLevelService.list()).thenReturn(List.of(
            buildLevel(1L, "FREE", "免费版", 10),
            buildLevel(2L, "PLUS", "健康PLUS会员", 20)
        ));

        MemberAccessRequirement firstRequirement = new MemberAccessRequirement();
        firstRequirement.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_SUMMARY);
        MemberAccessRequirement secondRequirement = new MemberAccessRequirement();
        secondRequirement.setFeatureCode(MemberFeatureCodeConstants.AI_REPORT_SUMMARY);

        List<MemberAccessResult> results = memberAccessGateService.checkAccessList(1L, List.of(firstRequirement, secondRequirement));

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(MemberAccessResult::isAllowed));

        /*
         * Java 的泛型在运行时会被擦除，Mockito 这里无法直接拿到 `Collection<String>.class`。
         * 因此测试需要在 `Collection.class` 上做一次受控的泛型桥接，并通过方法级 suppress
         * 明确告诉后续维护者：这里的未检查转换是测试框架限制，不是业务代码类型设计问题。
         */
        ArgumentCaptor<Collection<String>> featureCodesCaptor =
            ArgumentCaptor.forClass((Class<Collection<String>>) (Class<?>) Collection.class);
        verify(memberEntitlementApplicationService, times(1)).getCurrentEntitlements(eq(1L), featureCodesCaptor.capture());
        verify(memberLevelService, times(1)).list();
        assertEquals(new LinkedHashSet<>(List.of(MemberFeatureCodeConstants.AI_REPORT_SUMMARY)),
            new LinkedHashSet<>(featureCodesCaptor.getValue()));
    }

    private AppMemberEntitlementDTO buildFreeEntitlements() {
        AppMemberEntitlementDTO entitlements = new AppMemberEntitlementDTO();
        entitlements.setUserId(1L);
        entitlements.setHasActiveMember(false);
        entitlements.setItems(List.of());
        return entitlements;
    }

    private MemberLevelEntity buildLevel(Long id, String code, String name, int sort) {
        MemberLevelEntity entity = new MemberLevelEntity();
        entity.setMemberLevelId(id);
        entity.setLevelCode(code);
        entity.setLevelName(name);
        entity.setLevelSort(sort);
        entity.setStatus(StatusEnum.ENABLE.getValue());
        return entity;
    }
}

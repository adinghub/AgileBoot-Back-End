package com.healthtrail.domain.system.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.domain.system.member.db.MemberFeatureService;
import com.healthtrail.domain.system.member.db.MemberGateEntity;
import com.healthtrail.domain.system.member.db.MemberGateRuleEntity;
import com.healthtrail.domain.system.member.db.MemberGateRuleService;
import com.healthtrail.domain.system.member.db.MemberGateService;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.dto.AppMemberGateAccessDTO;
import java.util.List;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MemberGateApplicationServiceTest {

    private final MemberGateService memberGateService = mock(MemberGateService.class);
    private final MemberGateRuleService memberGateRuleService = mock(MemberGateRuleService.class);
    private final MemberFeatureService memberFeatureService = mock(MemberFeatureService.class);
    private final MemberLevelService memberLevelService = mock(MemberLevelService.class);
    private final MemberAccessGateService memberAccessGateService = mock(MemberAccessGateService.class);
    private final MemberEntitlementApplicationService memberEntitlementApplicationService = mock(MemberEntitlementApplicationService.class);

    private final MemberGateApplicationService memberGateApplicationService = new MemberGateApplicationService(
        memberGateService,
        memberGateRuleService,
        memberFeatureService,
        memberLevelService,
        memberAccessGateService,
        memberEntitlementApplicationService
    );

    @Test
    void shouldAllowByDefaultWhenGateIsNotRegistered() {
        when(memberGateService.list(any())).thenReturn(Collections.emptyList());

        AppMemberGateAccessDTO result = memberGateApplicationService.checkCurrentUserGate(1L, "BOOK.CREATE");

        assertTrue(Boolean.TRUE.equals(result.getAllowed()));
        assertEquals(MemberGateAccessStatusConstants.ALLOW_BY_DEFAULT, result.getAccessStatus());
        assertEquals("BOOK.CREATE", result.getGateCode());
    }

    @Test
    void shouldMapFeatureQuotaDenyToGateQuotaStatus() {
        MemberGateEntity gateEntity = new MemberGateEntity();
        gateEntity.setMemberGateId(10L);
        gateEntity.setGateCode("AI.REPORT.EXPORT");
        gateEntity.setGateName("AI报告导出");
        gateEntity.setStatus(StatusEnum.ENABLE.getValue());

        MemberGateRuleEntity ruleEntity = new MemberGateRuleEntity();
        ruleEntity.setMemberGateRuleId(20L);
        ruleEntity.setMemberGateId(10L);
        ruleEntity.setPolicyType(MemberGatePolicyTypeConstants.FEATURE);
        ruleEntity.setFeatureCode("AI_REPORT_SUMMARY_QUOTA");
        ruleEntity.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
        ruleEntity.setGuideMemberPage(YesOrNoEnum.YES.getValue());
        ruleEntity.setStatus(StatusEnum.ENABLE.getValue());

        MemberAccessResult accessResult = new MemberAccessResult();
        accessResult.setAllowed(false);
        accessResult.setDenyReasonCode(MemberAccessDenyReasonEnum.QUOTA_NOT_ENOUGH.name());
        accessResult.setMessage("当前周期可用次数已用完");
        accessResult.setFeatureCode("AI_REPORT_SUMMARY_QUOTA");
        accessResult.setFeatureName("AI报告总结次数");
        accessResult.setCurrentLevelCode("FREE");
        accessResult.setCurrentLevelName("免费版");
        accessResult.setLimitValue(3);
        accessResult.setUsedCount(3);
        accessResult.setRemainingCount(0);

        when(memberGateService.list(any())).thenReturn(List.of(gateEntity));
        when(memberGateRuleService.list(any())).thenReturn(List.of(ruleEntity));
        when(memberAccessGateService.checkAccessList(eq(1L), any())).thenReturn(List.of(accessResult));

        AppMemberGateAccessDTO result = memberGateApplicationService.checkCurrentUserGate(1L, "AI.REPORT.EXPORT");

        assertEquals(Boolean.FALSE, result.getAllowed());
        assertEquals(MemberGateAccessStatusConstants.QUOTA_REACHED, result.getAccessStatus());
        assertEquals("当前周期可用次数已用完", result.getDenyMessage());
        assertEquals("AI报告总结次数", result.getFeatureName());
        assertEquals(Integer.valueOf(0), result.getRemainingValue());
    }

    @Test
    void shouldAllowLevelOnlyRuleWhenCurrentLevelInWhitelist() {
        MemberGateEntity gateEntity = new MemberGateEntity();
        gateEntity.setMemberGateId(30L);
        gateEntity.setGateCode("REPORT.EXPORT");
        gateEntity.setGateName("报告导出");
        gateEntity.setStatus(StatusEnum.ENABLE.getValue());

        MemberGateRuleEntity ruleEntity = new MemberGateRuleEntity();
        ruleEntity.setMemberGateRuleId(40L);
        ruleEntity.setMemberGateId(30L);
        ruleEntity.setPolicyType(MemberGatePolicyTypeConstants.FEATURE);
        ruleEntity.setAllowedLevelCodesJson("[\"PLUS\",\"PRO\"]");
        ruleEntity.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
        ruleEntity.setGuideMemberPage(YesOrNoEnum.YES.getValue());
        ruleEntity.setStatus(StatusEnum.ENABLE.getValue());

        com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO entitlements =
            new com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO();
        entitlements.setHasActiveMember(true);
        entitlements.setCurrentLevelCode("PLUS");
        entitlements.setCurrentLevelName("健康PLUS会员");

        when(memberGateService.list(any())).thenReturn(List.of(gateEntity));
        when(memberGateRuleService.list(any())).thenReturn(List.of(ruleEntity));
        when(memberEntitlementApplicationService.getCurrentEntitlements(1L)).thenReturn(entitlements);
        when(memberLevelService.list()).thenReturn(Collections.emptyList());

        AppMemberGateAccessDTO result = memberGateApplicationService.checkCurrentUserGate(1L, "REPORT.EXPORT");

        assertTrue(Boolean.TRUE.equals(result.getAllowed()));
        assertEquals(MemberGateAccessStatusConstants.ALLOW, result.getAccessStatus());
        assertEquals("PLUS", result.getLevelCode());
    }

    @Test
    void shouldBatchCheckCurrentUserGatesWithoutSingleGateQueries() {
        MemberGateEntity firstGate = new MemberGateEntity();
        firstGate.setMemberGateId(100L);
        firstGate.setGateCode("AI.REPORT.SUMMARY");
        firstGate.setGateName("AI报告总结");
        firstGate.setStatus(StatusEnum.ENABLE.getValue());

        MemberGateEntity secondGate = new MemberGateEntity();
        secondGate.setMemberGateId(101L);
        secondGate.setGateCode("AI.REPORT.EXPORT");
        secondGate.setGateName("AI报告导出");
        secondGate.setStatus(StatusEnum.ENABLE.getValue());

        MemberGateRuleEntity firstRule = new MemberGateRuleEntity();
        firstRule.setMemberGateId(100L);
        firstRule.setPolicyType(MemberGatePolicyTypeConstants.FEATURE);
        firstRule.setFeatureCode("AI_REPORT_SUMMARY");
        firstRule.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
        firstRule.setGuideMemberPage(YesOrNoEnum.YES.getValue());
        firstRule.setStatus(StatusEnum.ENABLE.getValue());

        MemberGateRuleEntity secondRule = new MemberGateRuleEntity();
        secondRule.setMemberGateId(101L);
        secondRule.setPolicyType(MemberGatePolicyTypeConstants.FEATURE);
        secondRule.setFeatureCode("AI_REPORT_EXPORT");
        secondRule.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
        secondRule.setGuideMemberPage(YesOrNoEnum.YES.getValue());
        secondRule.setStatus(StatusEnum.ENABLE.getValue());

        MemberAccessResult firstAccessResult = new MemberAccessResult();
        firstAccessResult.setAllowed(true);
        firstAccessResult.setFeatureCode("AI_REPORT_SUMMARY");
        firstAccessResult.setFeatureName("AI报告总结");
        firstAccessResult.setCurrentLevelCode("PLUS");
        firstAccessResult.setCurrentLevelName("健康PLUS会员");
        firstAccessResult.setMessage("会员准入校验通过");

        MemberAccessResult secondAccessResult = new MemberAccessResult();
        secondAccessResult.setAllowed(false);
        secondAccessResult.setFeatureCode("AI_REPORT_EXPORT");
        secondAccessResult.setFeatureName("AI报告导出");
        secondAccessResult.setCurrentLevelCode("PLUS");
        secondAccessResult.setCurrentLevelName("健康PLUS会员");
        secondAccessResult.setDenyReasonCode(MemberAccessDenyReasonEnum.FEATURE_NOT_ENABLED.name());
        secondAccessResult.setMessage("当前会员等级未开放");

        when(memberGateService.list(any())).thenReturn(List.of(firstGate, secondGate));
        when(memberGateRuleService.list(any())).thenReturn(List.of(firstRule, secondRule));
        when(memberAccessGateService.checkAccessList(eq(1L), any())).thenReturn(List.of(firstAccessResult, secondAccessResult));

        List<AppMemberGateAccessDTO> results = memberGateApplicationService.checkCurrentUserGates(1L,
            List.of("AI.REPORT.SUMMARY", "AI.REPORT.EXPORT"));

        assertEquals(2, results.size());
        assertEquals("AI.REPORT.SUMMARY", results.get(0).getGateCode());
        assertEquals(Boolean.TRUE, results.get(0).getAllowed());
        assertEquals("AI.REPORT.EXPORT", results.get(1).getGateCode());
        assertEquals(Boolean.FALSE, results.get(1).getAllowed());

        verify(memberGateService, times(1)).list(any());
        verify(memberGateRuleService, times(1)).list(any());
        verify(memberAccessGateService, times(1)).checkAccessList(eq(1L), any());
        verify(memberGateService, never()).getOne(any());
        verify(memberGateRuleService, never()).getOne(any());
        verify(memberAccessGateService, never()).checkAccess(any(Long.class), any(MemberAccessRequirement.class));
    }
}

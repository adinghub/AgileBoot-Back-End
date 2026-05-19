package com.healthtrail.domain.system.member;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.system.member.command.AddMemberGateCommand;
import com.healthtrail.domain.system.member.command.SaveMemberGateRuleCommand;
import com.healthtrail.domain.system.member.command.UpdateMemberGateCommand;
import com.healthtrail.domain.system.member.db.MemberFeatureEntity;
import com.healthtrail.domain.system.member.db.MemberFeatureService;
import com.healthtrail.domain.system.member.db.MemberGateEntity;
import com.healthtrail.domain.system.member.db.MemberGateRuleEntity;
import com.healthtrail.domain.system.member.db.MemberGateRuleService;
import com.healthtrail.domain.system.member.db.MemberGateService;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO;
import com.healthtrail.domain.system.member.dto.AppMemberGateAccessDTO;
import com.healthtrail.domain.system.member.dto.MemberGateDTO;
import com.healthtrail.domain.system.member.dto.MemberGateRuleDTO;
import com.healthtrail.domain.system.member.query.MemberGateQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员功能门禁应用服务。
 *
 * <p>这层服务把“业务功能入口 gateCode”和“会员权益 featureCode”解耦开来：
 * 1. 业务代码只认稳定的 gateCode；
 * 2. 后台通过规则表决定 gateCode 当前绑定什么会员策略；
 * 3. 真正的会员放行 / 拒绝逻辑仍继续复用现有会员能力。
 *
 * <p>因此后续大多数策略调整都不再需要修改业务代码，只需要改门禁配置即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberGateApplicationService {

    /** 会员门禁数据库服务 */
    private final MemberGateService memberGateService;
    /** 会员门禁规则数据库服务 */
    private final MemberGateRuleService memberGateRuleService;
    /** 会员权益数据库服务 */
    private final MemberFeatureService memberFeatureService;
    /** 会员等级数据库服务 */
    private final MemberLevelService memberLevelService;
    /** 会员门禁访问判定服务 */
    private final MemberAccessGateService memberAccessGateService;
    /** 会员权益快照应用服务 */
    private final MemberEntitlementApplicationService memberEntitlementApplicationService;

    public PageDTO<MemberGateDTO> getMemberGatePage(MemberGateQuery query) {
        Page<MemberGateEntity> page = memberGateService.page(query.toPage(), query.toQueryWrapper());
        List<MemberGateEntity> gateEntities = page.getRecords();
        Map<Long, MemberGateRuleEntity> ruleMap = listRuleMapByGateIds(gateEntities.stream()
            .map(MemberGateEntity::getMemberGateId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));
        Map<String, MemberFeatureEntity> featureMap = listFeatureCodeMap(ruleMap.values().stream()
            .map(MemberGateRuleEntity::getFeatureCode)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet()));

        List<MemberGateDTO> records = gateEntities.stream()
            .map(entity -> buildMemberGateDTO(entity, ruleMap.get(entity.getMemberGateId()), featureMap))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public MemberGateDTO getMemberGateInfo(Long memberGateId) {
        MemberGateEntity gateEntity = loadMemberGate(memberGateId);
        MemberGateRuleEntity ruleEntity = getRuleByGateId(memberGateId);
        Map<String, MemberFeatureEntity> featureMap = ruleEntity == null || StrUtil.isBlank(ruleEntity.getFeatureCode())
            ? Collections.emptyMap()
            : listFeatureCodeMap(Collections.singleton(normalizeCode(ruleEntity.getFeatureCode())));
        return buildMemberGateDTO(gateEntity, ruleEntity, featureMap);
    }

    /**
     * 门禁规则列表页本质上是“门禁点列表 + 当前规则快照”。
     *
     * <p>因此这里仍然以门禁点表作为主表分页，再把当前页涉及到的规则批量回填：
     * 1. 避免为了管理页再单独写一套复杂 join；
     * 2. 让“尚未配置规则的门禁点”也能自然出现在列表里；
     * 3. 前端可以在同一张表里直接看到“门禁点元数据 + 当前策略”。
     */
    public PageDTO<MemberGateRuleDTO> getMemberGateRulePage(MemberGateQuery query) {
        Page<MemberGateEntity> page = memberGateService.page(query.toPage(), query.toQueryWrapper());
        List<MemberGateEntity> gateEntities = page.getRecords();
        Map<Long, MemberGateRuleEntity> ruleMap = listRuleMapByGateIds(gateEntities.stream()
            .map(MemberGateEntity::getMemberGateId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));
        Map<String, MemberFeatureEntity> featureMap = listFeatureCodeMap(ruleMap.values().stream()
            .map(MemberGateRuleEntity::getFeatureCode)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet()));

        List<MemberGateRuleDTO> records = gateEntities.stream()
            .map(entity -> buildMemberGateRuleDTO(entity, ruleMap.get(entity.getMemberGateId()), featureMap))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public MemberGateRuleDTO getMemberGateRule(Long memberGateId) {
        MemberGateEntity gateEntity = loadMemberGate(memberGateId);
        MemberGateRuleEntity ruleEntity = getRuleByGateId(memberGateId);
        Map<String, MemberFeatureEntity> featureMap = ruleEntity == null || StrUtil.isBlank(ruleEntity.getFeatureCode())
            ? Collections.emptyMap()
            : listFeatureCodeMap(Collections.singleton(normalizeCode(ruleEntity.getFeatureCode())));
        return buildMemberGateRuleDTO(gateEntity, ruleEntity, featureMap);
    }

    public void addMemberGate(AddMemberGateCommand command) {
        validateMemberGateCodeDuplicated(command.getGateCode(), null);
        MemberGateEntity entity = new MemberGateEntity();
        BeanUtil.copyProperties(command, entity, "memberGateId");
        fillMemberGateDefaultFields(entity);
        memberGateService.save(entity);
    }

    public void updateMemberGate(UpdateMemberGateCommand command) {
        MemberGateEntity entity = loadMemberGate(command.getMemberGateId());
        validateMemberGateCodeDuplicated(command.getGateCode(), entity.getMemberGateId());
        BeanUtil.copyProperties(command, entity, "memberGateId");
        fillMemberGateDefaultFields(entity);
        memberGateService.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMemberGates(List<Long> memberGateIds) {
        if (memberGateIds == null || memberGateIds.isEmpty()) {
            return;
        }
        List<MemberGateEntity> gateEntities = memberGateService.listByIds(memberGateIds);
        for (MemberGateEntity gateEntity : gateEntities) {
            if (gateEntity != null && Objects.equals(gateEntity.getIsBuiltin(), YesOrNoEnum.YES.getValue())) {
                throw new ApiException(ErrorCode.Business.MEMBER_GATE_BUILTIN_NOT_ALLOW_DELETE);
            }
        }
        QueryWrapper<MemberGateRuleEntity> ruleRemoveWrapper = new QueryWrapper<>();
        ruleRemoveWrapper.in("member_gate_id", memberGateIds);
        memberGateRuleService.remove(ruleRemoveWrapper);
        memberGateService.removeByIds(memberGateIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveMemberGateRule(Long memberGateId, SaveMemberGateRuleCommand command) {
        MemberGateEntity gateEntity = loadMemberGate(memberGateId);
        command.setMemberGateId(memberGateId);
        MemberGateRuleEntity ruleEntity = getRuleByGateId(memberGateId);
        if (ruleEntity == null) {
            ruleEntity = new MemberGateRuleEntity();
            ruleEntity.setMemberGateId(memberGateId);
            ruleEntity.setVersionNo(0);
        }

        String policyType = normalizePolicyType(command.getPolicyType());
        List<String> allowedLevelCodes = normalizeAllowedLevelCodes(command.getAllowedLevelCodes());
        String featureCode = normalizeCode(command.getFeatureCode());

        validateRuleFeatureBinding(policyType, featureCode, allowedLevelCodes);
        validateAllowedLevelCodesExist(allowedLevelCodes);
        if (StrUtil.isNotBlank(featureCode) && getFeatureByCode(featureCode) == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_FEATURE_NOT_FOUND);
        }

        ruleEntity.setPolicyType(policyType);
        ruleEntity.setFeatureCode(featureCode);
        ruleEntity.setAllowedLevelCodesJson(allowedLevelCodes.isEmpty() ? null : JacksonUtil.to(allowedLevelCodes));
        ruleEntity.setDenyClientMode(normalizeDenyClientMode(command.getDenyClientMode()));
        ruleEntity.setDenyTitle(StrUtil.blankToDefault(StrUtil.trim(command.getDenyTitle()), null));
        ruleEntity.setDenyMessage(StrUtil.blankToDefault(StrUtil.trim(command.getDenyMessage()), null));
        ruleEntity.setGuideMemberPage(command.getGuideMemberPage() == null ? YesOrNoEnum.YES.getValue() : command.getGuideMemberPage());
        ruleEntity.setPriority(command.getPriority() == null ? 0 : command.getPriority());
        ruleEntity.setStatus(command.getStatus() == null ? StatusEnum.ENABLE.getValue() : command.getStatus());
        ruleEntity.setVersionNo((ruleEntity.getVersionNo() == null ? 0 : ruleEntity.getVersionNo()) + 1);
        ruleEntity.setRemark(command.getRemark());

        if (ruleEntity.getMemberGateRuleId() == null) {
            memberGateRuleService.save(ruleEntity);
        } else {
            memberGateRuleService.updateById(ruleEntity);
        }

        log.info("member gate rule saved, gateCode={}, policyType={}, featureCode={}, allowedLevelCodes={}",
            gateEntity.getGateCode(), ruleEntity.getPolicyType(), ruleEntity.getFeatureCode(), allowedLevelCodes);
    }

    /**
     * 查询当前用户单个门禁点的可用结果。
     *
     * <p>这里不会把“门禁未注册 / 规则未配置”当成错误抛出，而是按默认放行处理并打印日志。
     * 原因是当前阶段更重视线上业务可用性，避免因为后台配置缺失直接误伤主流程。
     */
    public AppMemberGateAccessDTO checkCurrentUserGate(Long userId, String gateCode) {
        if (userId == null) {
            throw new ApiException(ErrorCode.Business.APP_USER_NON_EXIST);
        }
        List<AppMemberGateAccessDTO> results = checkCurrentUserGates(userId, Collections.singletonList(gateCode));
        if (results.isEmpty()) {
            return buildDefaultAllowResult(gateCode, MemberGateAccessStatusConstants.ALLOW_BY_DEFAULT,
                "门禁点编码为空，按默认放行处理");
        }
        return results.get(0);
    }

    public List<AppMemberGateAccessDTO> checkCurrentUserGates(Long userId, List<String> gateCodes) {
        if (userId == null) {
            throw new ApiException(ErrorCode.Business.APP_USER_NON_EXIST);
        }
        if (gateCodes == null || gateCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<GateAccessRequestContext> requestContexts = new ArrayList<>(gateCodes.size());
        Set<String> normalizedGateCodes = new LinkedHashSet<>();
        for (String gateCode : gateCodes) {
            String normalizedGateCode = normalizeCode(gateCode);
            requestContexts.add(new GateAccessRequestContext(gateCode, normalizedGateCode));
            if (StrUtil.isNotBlank(normalizedGateCode)) {
                normalizedGateCodes.add(normalizedGateCode);
            }
        }
        if (normalizedGateCodes.isEmpty()) {
            return requestContexts.stream()
                .map(context -> buildDefaultAllowResult(context.getDisplayGateCode(),
                    MemberGateAccessStatusConstants.ALLOW_BY_DEFAULT, "门禁点编码为空，按默认放行处理"))
                .collect(Collectors.toList());
        }

        Map<String, MemberGateEntity> gateCodeMap = listGateCodeMap(normalizedGateCodes);
        Map<Long, MemberGateRuleEntity> ruleMap = listRuleMapByGateIds(gateCodeMap.values().stream()
            .map(MemberGateEntity::getMemberGateId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new)));

        AppMemberGateAccessDTO[] orderedResults = new AppMemberGateAccessDTO[requestContexts.size()];
        List<PendingFeatureGateContext> pendingFeatureGateContexts = new ArrayList<>();
        List<PendingLevelOnlyGateContext> pendingLevelOnlyGateContexts = new ArrayList<>();

        for (int index = 0; index < requestContexts.size(); index++) {
            GateAccessRequestContext requestContext = requestContexts.get(index);
            if (StrUtil.isBlank(requestContext.getNormalizedGateCode())) {
                orderedResults[index] = buildDefaultAllowResult(requestContext.getDisplayGateCode(),
                    MemberGateAccessStatusConstants.ALLOW_BY_DEFAULT, "门禁点编码为空，按默认放行处理");
                continue;
            }

            MemberGateEntity gateEntity = gateCodeMap.get(requestContext.getNormalizedGateCode());
            if (gateEntity == null) {
                log.warn("member gate not registered, gateCode={}, fallback=allow", requestContext.getNormalizedGateCode());
                orderedResults[index] = buildDefaultAllowResult(requestContext.getNormalizedGateCode(),
                    MemberGateAccessStatusConstants.ALLOW_BY_DEFAULT, "门禁点未注册，按默认放行处理");
                continue;
            }
            if (!Objects.equals(gateEntity.getStatus(), StatusEnum.ENABLE.getValue())) {
                orderedResults[index] = buildAllowResult(gateEntity, MemberGateAccessStatusConstants.GATE_DISABLED_ALLOW,
                    "门禁点已停用，按默认放行处理");
                continue;
            }

            MemberGateRuleEntity ruleEntity = ruleMap.get(gateEntity.getMemberGateId());
            if (ruleEntity == null) {
                log.warn("member gate rule not configured, gateCode={}, fallback=allow", requestContext.getNormalizedGateCode());
                orderedResults[index] = buildAllowResult(gateEntity, MemberGateAccessStatusConstants.ALLOW_BY_DEFAULT,
                    "门禁规则未配置，按默认放行处理");
                continue;
            }
            if (!Objects.equals(ruleEntity.getStatus(), StatusEnum.ENABLE.getValue())) {
                orderedResults[index] = buildAllowResult(gateEntity, MemberGateAccessStatusConstants.RULE_DISABLED_ALLOW,
                    "门禁规则已停用，按默认放行处理");
                continue;
            }

            String policyType = normalizePolicyType(ruleEntity.getPolicyType());
            if (MemberGatePolicyTypeConstants.ALLOW.equals(policyType)) {
                orderedResults[index] = buildAllowResult(gateEntity, MemberGateAccessStatusConstants.ALLOW, "当前门禁点未做会员限制");
                continue;
            }

            String featureCode = normalizeCode(ruleEntity.getFeatureCode());
            List<String> allowedLevelCodes = parseAllowedLevelCodes(ruleEntity.getAllowedLevelCodesJson());
            if (StrUtil.isBlank(featureCode)) {
                pendingLevelOnlyGateContexts.add(new PendingLevelOnlyGateContext(index, gateEntity, ruleEntity, allowedLevelCodes));
                continue;
            }

            MemberAccessRequirement requirement = new MemberAccessRequirement();
            requirement.setFeatureCode(featureCode);
            pendingFeatureGateContexts.add(new PendingFeatureGateContext(index, gateEntity, ruleEntity, allowedLevelCodes, requirement));
        }

        CurrentMemberSnapshot currentMemberSnapshot = null;
        if (!pendingFeatureGateContexts.isEmpty()) {
            List<MemberAccessResult> accessResults = memberAccessGateService.checkAccessList(userId, pendingFeatureGateContexts.stream()
                .map(PendingFeatureGateContext::getRequirement)
                .collect(Collectors.toList()));
            for (int i = 0; i < pendingFeatureGateContexts.size(); i++) {
                PendingFeatureGateContext pendingContext = pendingFeatureGateContexts.get(i);
                MemberAccessResult accessResult = accessResults.get(i);
                orderedResults[pendingContext.getOriginalIndex()] =
                    buildFeatureGateAccessResult(pendingContext.getGateEntity(), pendingContext.getRuleEntity(),
                        pendingContext.getAllowedLevelCodes(), accessResult);
            }
            if (!pendingLevelOnlyGateContexts.isEmpty()) {
                MemberAccessResult sampleAccessResult = accessResults.get(0);
                currentMemberSnapshot = new CurrentMemberSnapshot(
                    normalizeCode(sampleAccessResult.getCurrentLevelCode()),
                    StrUtil.blankToDefault(sampleAccessResult.getCurrentLevelName(), "免费版")
                );
            }
        }
        if (!pendingLevelOnlyGateContexts.isEmpty()) {
            CurrentMemberSnapshot resolvedSnapshot = currentMemberSnapshot == null
                ? resolveCurrentMemberSnapshot(userId)
                : currentMemberSnapshot;
            for (PendingLevelOnlyGateContext pendingContext : pendingLevelOnlyGateContexts) {
                orderedResults[pendingContext.getOriginalIndex()] = buildLevelOnlyAccessResult(
                    pendingContext.getGateEntity(), pendingContext.getRuleEntity(),
                    pendingContext.getAllowedLevelCodes(), resolvedSnapshot);
            }
        }
        List<AppMemberGateAccessDTO> results = new ArrayList<>(orderedResults.length);
        for (AppMemberGateAccessDTO orderedResult : orderedResults) {
            results.add(orderedResult);
        }
        return results;
    }

    public void ensureCurrentUserGateAllowed(Long userId, String gateCode) {
        AppMemberGateAccessDTO result = checkCurrentUserGate(userId, gateCode);
        if (Boolean.TRUE.equals(result.getAllowed())) {
            return;
        }
        throw new ApiException(ErrorCode.Business.MEMBER_ACCESS_DENIED, result.getDenyMessage());
    }

    private AppMemberGateAccessDTO buildFeaturePolicyAccessResult(Long userId, MemberGateEntity gateEntity,
        MemberGateRuleEntity ruleEntity) {
        AppMemberGateAccessDTO result;
        String featureCode = normalizeCode(ruleEntity.getFeatureCode());
        List<String> allowedLevelCodes = parseAllowedLevelCodes(ruleEntity.getAllowedLevelCodesJson());

        if (StrUtil.isBlank(featureCode)) {
            result = buildLevelOnlyAccessResult(userId, gateEntity, allowedLevelCodes);
        } else {
            MemberAccessRequirement requirement = new MemberAccessRequirement();
            requirement.setFeatureCode(featureCode);
            MemberAccessResult accessResult = memberAccessGateService.checkAccess(userId, requirement);
            result = buildGateResultFromAccessResult(gateEntity, ruleEntity, accessResult);
            if (Boolean.TRUE.equals(result.getAllowed()) && !allowedLevelCodes.isEmpty()
                && !allowedLevelCodes.contains(normalizeCode(result.getLevelCode()))) {
                result.setAllowed(false);
                result.setAccessStatus(MemberGateAccessStatusConstants.LEVEL_REQUIRED);
                result.setDenyMessage("当前会员等级不在允许范围内");
            }
        }

        if (!Boolean.TRUE.equals(result.getAllowed()) && StrUtil.isNotBlank(ruleEntity.getDenyTitle())) {
            result.setDenyTitle(ruleEntity.getDenyTitle().trim());
        }
        if (!Boolean.TRUE.equals(result.getAllowed()) && StrUtil.isNotBlank(ruleEntity.getDenyMessage())) {
            result.setDenyMessage(ruleEntity.getDenyMessage().trim());
        }
        result.setPolicyType(ruleEntity.getPolicyType());
        result.setDenyClientMode(normalizeDenyClientMode(ruleEntity.getDenyClientMode()));
        result.setGuideMemberPage(Objects.equals(ruleEntity.getGuideMemberPage(), YesOrNoEnum.YES.getValue()));
        result.setAllowedLevelCodes(allowedLevelCodes);
        return result;
    }

    private AppMemberGateAccessDTO buildLevelOnlyAccessResult(Long userId, MemberGateEntity gateEntity,
        List<String> allowedLevelCodes) {
        return buildLevelOnlyAccessResult(gateEntity, null, allowedLevelCodes, resolveCurrentMemberSnapshot(userId));
    }

    /**
     * 基于已经解析好的当前等级快照构建“仅等级白名单”门禁结果。
     *
     * <p>批量门禁场景下，如果每个 level-only gate 都自己去查一次当前会员快照，
     * 就会把用户会员关系和等级定义的查询成本按 gate 数量重复放大。
     * 因此这里改成接收预先解析好的 snapshot，让一批 level-only gate 复用同一个结果。
     */
    private AppMemberGateAccessDTO buildLevelOnlyAccessResult(MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
        List<String> allowedLevelCodes, CurrentMemberSnapshot snapshot) {
        AppMemberGateAccessDTO dto = new AppMemberGateAccessDTO();
        dto.setGateCode(gateEntity.getGateCode());
        dto.setGateName(gateEntity.getGateName());
        dto.setPolicyType(MemberGatePolicyTypeConstants.FEATURE);
        dto.setLevelCode(snapshot.getCurrentLevelCode());
        dto.setLevelName(snapshot.getCurrentLevelName());
        if (allowedLevelCodes.isEmpty() || allowedLevelCodes.contains(snapshot.getCurrentLevelCode())) {
            dto.setAllowed(true);
            dto.setAccessStatus(MemberGateAccessStatusConstants.ALLOW);
            dto.setDenyMessage("当前等级满足门禁要求");
        } else {
            dto.setAllowed(false);
            dto.setAccessStatus(MemberGateAccessStatusConstants.LEVEL_REQUIRED);
            dto.setDenyMessage("当前会员等级不在允许范围内");
        }
        if (ruleEntity != null) {
            dto.setDenyClientMode(normalizeDenyClientMode(ruleEntity.getDenyClientMode()));
            dto.setGuideMemberPage(Objects.equals(ruleEntity.getGuideMemberPage(), YesOrNoEnum.YES.getValue()));
            dto.setAllowedLevelCodes(allowedLevelCodes);
            if (!Boolean.TRUE.equals(dto.getAllowed()) && StrUtil.isNotBlank(ruleEntity.getDenyTitle())) {
                dto.setDenyTitle(ruleEntity.getDenyTitle().trim());
            }
            if (!Boolean.TRUE.equals(dto.getAllowed()) && StrUtil.isNotBlank(ruleEntity.getDenyMessage())) {
                dto.setDenyMessage(ruleEntity.getDenyMessage().trim());
            }
        } else {
            dto.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
            dto.setGuideMemberPage(false);
            dto.setAllowedLevelCodes(allowedLevelCodes == null ? Collections.emptyList() : allowedLevelCodes);
        }
        return dto;
    }

    /**
     * 把批量会员准入结果映射回门禁结果，并追加门禁规则层的等级白名单与展示配置。
     */
    private AppMemberGateAccessDTO buildFeatureGateAccessResult(MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
        List<String> allowedLevelCodes, MemberAccessResult accessResult) {
        AppMemberGateAccessDTO result = buildGateResultFromAccessResult(gateEntity, ruleEntity, accessResult);
        if (Boolean.TRUE.equals(result.getAllowed()) && allowedLevelCodes != null && !allowedLevelCodes.isEmpty()
            && !allowedLevelCodes.contains(normalizeCode(result.getLevelCode()))) {
            result.setAllowed(false);
            result.setAccessStatus(MemberGateAccessStatusConstants.LEVEL_REQUIRED);
            result.setDenyMessage("当前会员等级不在允许范围内");
        }
        if (!Boolean.TRUE.equals(result.getAllowed()) && StrUtil.isNotBlank(ruleEntity.getDenyTitle())) {
            result.setDenyTitle(ruleEntity.getDenyTitle().trim());
        }
        if (!Boolean.TRUE.equals(result.getAllowed()) && StrUtil.isNotBlank(ruleEntity.getDenyMessage())) {
            result.setDenyMessage(ruleEntity.getDenyMessage().trim());
        }
        result.setPolicyType(ruleEntity.getPolicyType());
        result.setDenyClientMode(normalizeDenyClientMode(ruleEntity.getDenyClientMode()));
        result.setGuideMemberPage(Objects.equals(ruleEntity.getGuideMemberPage(), YesOrNoEnum.YES.getValue()));
        result.setAllowedLevelCodes(allowedLevelCodes == null ? Collections.emptyList() : allowedLevelCodes);
        return result;
    }

    private AppMemberGateAccessDTO buildGateResultFromAccessResult(MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
        MemberAccessResult accessResult) {
        AppMemberGateAccessDTO dto = new AppMemberGateAccessDTO();
        dto.setGateCode(gateEntity.getGateCode());
        dto.setGateName(gateEntity.getGateName());
        dto.setAllowed(accessResult.isAllowed());
        dto.setPolicyType(ruleEntity.getPolicyType());
        dto.setFeatureCode(accessResult.getFeatureCode());
        dto.setFeatureName(accessResult.getFeatureName());
        dto.setLimitValue(accessResult.getLimitValue());
        dto.setUsedValue(accessResult.getUsedCount());
        dto.setRemainingValue(accessResult.getRemainingCount());
        dto.setLevelCode(accessResult.getCurrentLevelCode());
        dto.setLevelName(accessResult.getCurrentLevelName());
        dto.setAccessStatus(mapAccessStatus(accessResult));
        dto.setDenyMessage(accessResult.getMessage());
        return dto;
    }

    private String mapAccessStatus(MemberAccessResult accessResult) {
        if (accessResult == null || accessResult.isAllowed()) {
            return MemberGateAccessStatusConstants.ALLOW;
        }
        String denyReasonCode = accessResult.getDenyReasonCode();
        if (MemberAccessDenyReasonEnum.REQUIRED_LEVEL_NOT_MET.name().equals(denyReasonCode)) {
            return MemberGateAccessStatusConstants.LEVEL_REQUIRED;
        }
        if (MemberAccessDenyReasonEnum.FEATURE_NOT_ENABLED.name().equals(denyReasonCode)) {
            return MemberGateAccessStatusConstants.FEATURE_DISABLED;
        }
        if (MemberAccessDenyReasonEnum.LIMIT_NOT_ENOUGH.name().equals(denyReasonCode)) {
            return MemberGateAccessStatusConstants.LIMIT_REACHED;
        }
        if (MemberAccessDenyReasonEnum.QUOTA_NOT_ENOUGH.name().equals(denyReasonCode)) {
            return MemberGateAccessStatusConstants.QUOTA_REACHED;
        }
        return MemberGateAccessStatusConstants.MEMBER_DENIED;
    }

    private AppMemberGateAccessDTO buildDefaultAllowResult(String gateCode, String status, String message) {
        AppMemberGateAccessDTO dto = new AppMemberGateAccessDTO();
        dto.setGateCode(gateCode);
        dto.setAllowed(true);
        dto.setAccessStatus(status);
        dto.setPolicyType(MemberGatePolicyTypeConstants.ALLOW);
        dto.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
        dto.setGuideMemberPage(false);
        dto.setDenyMessage(message);
        dto.setAllowedLevelCodes(Collections.emptyList());
        return dto;
    }

    private AppMemberGateAccessDTO buildAllowResult(MemberGateEntity gateEntity, String status, String message) {
        AppMemberGateAccessDTO dto = new AppMemberGateAccessDTO();
        dto.setGateCode(gateEntity.getGateCode());
        dto.setGateName(gateEntity.getGateName());
        dto.setAllowed(true);
        dto.setAccessStatus(status);
        dto.setPolicyType(MemberGatePolicyTypeConstants.ALLOW);
        dto.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
        dto.setGuideMemberPage(false);
        dto.setDenyMessage(message);
        dto.setAllowedLevelCodes(Collections.emptyList());
        return dto;
    }

    private MemberGateDTO buildMemberGateDTO(MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
        Map<String, MemberFeatureEntity> featureMap) {
        MemberGateDTO dto = new MemberGateDTO(gateEntity);
        if (ruleEntity != null) {
            dto.setCurrentPolicyType(ruleEntity.getPolicyType());
            dto.setCurrentFeatureCode(ruleEntity.getFeatureCode());
            MemberFeatureEntity featureEntity = featureMap.get(normalizeCode(ruleEntity.getFeatureCode()));
            dto.setCurrentFeatureName(featureEntity == null ? null : featureEntity.getFeatureName());
            dto.setCurrentRuleStatus(ruleEntity.getStatus());
            dto.setCurrentRuleVersionNo(ruleEntity.getVersionNo());
        }
        return dto;
    }

    /**
     * 统一构造门禁规则管理页所需 DTO。
     *
     * <p>这里刻意把“未配置规则”也转成一份可直接编辑的默认 DTO，原因是：
     * 1. 前端规则弹窗打开后可以直接回显默认策略，不需要自己猜默认值；
     * 2. 列表页可以明确看出某个点位当前仍是默认放行；
     * 3. 后续如果默认值调整，只改这一处即可。
     */
    private MemberGateRuleDTO buildMemberGateRuleDTO(MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
        Map<String, MemberFeatureEntity> featureMap) {
        MemberGateRuleDTO dto = ruleEntity == null ? new MemberGateRuleDTO() : new MemberGateRuleDTO(ruleEntity);
        dto.setMemberGateId(gateEntity.getMemberGateId());
        dto.setGateCode(gateEntity.getGateCode());
        dto.setGateName(gateEntity.getGateName());
        dto.setGateScope(gateEntity.getGateScope());
        dto.setBizModule(gateEntity.getBizModule());
        dto.setTerminalType(gateEntity.getTerminalType());
        dto.setGateStatus(gateEntity.getStatus());
        dto.setIsBuiltin(gateEntity.getIsBuiltin());
        if (ruleEntity == null) {
            dto.setPolicyType(MemberGatePolicyTypeConstants.ALLOW);
            dto.setAllowedLevelCodes(Collections.emptyList());
            dto.setDenyClientMode(MemberGateDenyClientModeConstants.DIALOG);
            dto.setGuideMemberPage(YesOrNoEnum.YES.getValue());
            dto.setStatus(StatusEnum.ENABLE.getValue());
            dto.setVersionNo(0);
        }
        if (ruleEntity != null && StrUtil.isNotBlank(ruleEntity.getFeatureCode())) {
            MemberFeatureEntity featureEntity = featureMap.get(normalizeCode(ruleEntity.getFeatureCode()));
            dto.setFeatureName(featureEntity == null ? null : featureEntity.getFeatureName());
        }
        return dto;
    }

    private Map<Long, MemberGateRuleEntity> listRuleMapByGateIds(Set<Long> gateIds) {
        if (gateIds == null || gateIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<MemberGateRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("member_gate_id", gateIds);
        return memberGateRuleService.list(queryWrapper).stream()
            .collect(Collectors.toMap(MemberGateRuleEntity::getMemberGateId, entity -> entity, (left, right) -> left));
    }

    private Map<String, MemberFeatureEntity> listFeatureCodeMap(Set<String> featureCodes) {
        if (featureCodes == null || featureCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<MemberFeatureEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("feature_code", featureCodes);
        return memberFeatureService.list(queryWrapper).stream()
            .filter(entity -> StrUtil.isNotBlank(entity.getFeatureCode()))
            .collect(Collectors.toMap(entity -> normalizeCode(entity.getFeatureCode()), entity -> entity, (left, right) -> left));
    }

    private void fillMemberGateDefaultFields(MemberGateEntity entity) {
        entity.setGateCode(normalizeCode(entity.getGateCode()));
        entity.setGateName(StrUtil.trim(entity.getGateName()));
        entity.setGateScope(StrUtil.blankToDefault(StrUtil.trim(entity.getGateScope()), null));
        entity.setBizModule(normalizeCode(entity.getBizModule()));
        entity.setTerminalType(normalizeCode(entity.getTerminalType()));
        entity.setDefaultPolicyType(normalizePolicyType(entity.getDefaultPolicyType()));
        entity.setStatus(entity.getStatus() == null ? StatusEnum.ENABLE.getValue() : entity.getStatus());
        entity.setIsBuiltin(entity.getIsBuiltin() == null ? YesOrNoEnum.NO.getValue() : entity.getIsBuiltin());
        entity.setRemark(StrUtil.blankToDefault(StrUtil.trim(entity.getRemark()), null));
    }

    private void validateRuleFeatureBinding(String policyType, String featureCode, List<String> allowedLevelCodes) {
        if (!MemberGatePolicyTypeConstants.FEATURE.equals(policyType)) {
            return;
        }
        if (StrUtil.isBlank(featureCode) && (allowedLevelCodes == null || allowedLevelCodes.isEmpty())) {
            throw new ApiException(ErrorCode.Business.MEMBER_GATE_RULE_REQUIRE_FEATURE_OR_LEVEL);
        }
    }

    private void validateAllowedLevelCodesExist(List<String> allowedLevelCodes) {
        if (allowedLevelCodes == null || allowedLevelCodes.isEmpty()) {
            return;
        }
        Set<String> existingLevelCodes = memberLevelService.list().stream()
            .filter(Objects::nonNull)
            .map(MemberLevelEntity::getLevelCode)
            .filter(StrUtil::isNotBlank)
            .map(this::normalizeCode)
            .collect(Collectors.toSet());
        for (String allowedLevelCode : allowedLevelCodes) {
            if (!existingLevelCodes.contains(allowedLevelCode)) {
                throw new ApiException(ErrorCode.Business.MEMBER_GATE_ALLOWED_LEVEL_NOT_FOUND, allowedLevelCode);
            }
        }
    }

    private void validateMemberGateCodeDuplicated(String gateCode, Long excludeId) {
        if (StrUtil.isBlank(gateCode)) {
            return;
        }
        QueryWrapper<MemberGateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gate_code", normalizeCode(gateCode));
        queryWrapper.ne(excludeId != null, "member_gate_id", excludeId);
        if (memberGateService.count(queryWrapper) > 0) {
            throw new ApiException(ErrorCode.Business.MEMBER_GATE_CODE_IS_NOT_UNIQUE);
        }
    }

    private MemberGateEntity loadMemberGate(Long memberGateId) {
        MemberGateEntity entity = memberGateService.getById(memberGateId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_GATE_NOT_FOUND);
        }
        return entity;
    }

    private MemberGateEntity getGateByCode(String gateCode) {
        QueryWrapper<MemberGateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("gate_code", gateCode);
        return memberGateService.getOne(queryWrapper);
    }

    /**
     * 批量加载门禁点编码映射。
     */
    private Map<String, MemberGateEntity> listGateCodeMap(Set<String> gateCodes) {
        if (gateCodes == null || gateCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<MemberGateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("gate_code", gateCodes);
        return memberGateService.list(queryWrapper).stream()
            .filter(Objects::nonNull)
            .filter(entity -> StrUtil.isNotBlank(entity.getGateCode()))
            .collect(Collectors.toMap(entity -> normalizeCode(entity.getGateCode()), entity -> entity, (left, right) -> left));
    }

    private MemberGateRuleEntity getRuleByGateId(Long memberGateId) {
        QueryWrapper<MemberGateRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_gate_id", memberGateId);
        return memberGateRuleService.getOne(queryWrapper);
    }

    private MemberFeatureEntity getFeatureByCode(String featureCode) {
        if (StrUtil.isBlank(featureCode)) {
            return null;
        }
        QueryWrapper<MemberFeatureEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("feature_code", normalizeCode(featureCode));
        return memberFeatureService.getOne(queryWrapper);
    }

    private String normalizeCode(String rawCode) {
        String normalizedCode = StrUtil.trim(rawCode);
        if (StrUtil.isBlank(normalizedCode)) {
            return null;
        }
        return normalizedCode.toUpperCase();
    }

    private String normalizePolicyType(String rawPolicyType) {
        String policyType = normalizeCode(rawPolicyType);
        return policyType == null ? MemberGatePolicyTypeConstants.ALLOW : policyType;
    }

    private String normalizeDenyClientMode(String rawDenyClientMode) {
        String denyClientMode = normalizeCode(rawDenyClientMode);
        return denyClientMode == null ? MemberGateDenyClientModeConstants.DIALOG : denyClientMode;
    }

    private List<String> normalizeAllowedLevelCodes(List<String> rawLevelCodes) {
        if (rawLevelCodes == null || rawLevelCodes.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> normalizedCodes = new LinkedHashSet<>();
        for (String rawLevelCode : rawLevelCodes) {
            String normalizedCode = normalizeCode(rawLevelCode);
            if (StrUtil.isNotBlank(normalizedCode)) {
                normalizedCodes.add(normalizedCode);
            }
        }
        return new ArrayList<>(normalizedCodes);
    }

    private List<String> parseAllowedLevelCodes(String allowedLevelCodesJson) {
        if (StrUtil.isBlank(allowedLevelCodesJson)) {
            return Collections.emptyList();
        }
        return JacksonUtil.fromList(allowedLevelCodesJson, String.class).stream()
            .map(this::normalizeCode)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toList());
    }

    private CurrentMemberSnapshot resolveCurrentMemberSnapshot(Long userId) {
        AppMemberEntitlementDTO entitlements = memberEntitlementApplicationService.getCurrentEntitlements(userId);
        Map<String, MemberLevelEntity> levelMap = new LinkedHashMap<>();
        for (MemberLevelEntity levelEntity : memberLevelService.list()) {
            if (levelEntity != null && StrUtil.isNotBlank(levelEntity.getLevelCode())) {
                levelMap.put(normalizeCode(levelEntity.getLevelCode()), levelEntity);
            }
        }
        boolean hasActiveMember = Boolean.TRUE.equals(entitlements.getHasActiveMember());
        String currentLevelCode = hasActiveMember ? normalizeCode(entitlements.getCurrentLevelCode()) : MemberLevelCodeConstants.FREE;
        if (StrUtil.isBlank(currentLevelCode)) {
            currentLevelCode = MemberLevelCodeConstants.FREE;
        }
        MemberLevelEntity currentLevel = levelMap.get(currentLevelCode);
        String currentLevelName = currentLevel == null ? null : currentLevel.getLevelName();
        if (StrUtil.isBlank(currentLevelName)) {
            currentLevelName = hasActiveMember ? entitlements.getCurrentLevelName() : "免费版";
        }
        return new CurrentMemberSnapshot(currentLevelCode, StrUtil.blankToDefault(currentLevelName, "免费版"));
    }

    private static final class CurrentMemberSnapshot {

        private final String currentLevelCode;
        private final String currentLevelName;

        private CurrentMemberSnapshot(String currentLevelCode, String currentLevelName) {
            this.currentLevelCode = currentLevelCode;
            this.currentLevelName = currentLevelName;
        }

        private String getCurrentLevelCode() {
            return currentLevelCode;
        }

        private String getCurrentLevelName() {
            return currentLevelName;
        }
    }

    /**
     * 门禁批量检查时保留原始入参顺序与展示编码的请求上下文。
     */
    private static final class GateAccessRequestContext {

        private final String rawGateCode;

        private final String normalizedGateCode;

        private GateAccessRequestContext(String rawGateCode, String normalizedGateCode) {
            this.rawGateCode = rawGateCode;
            this.normalizedGateCode = normalizedGateCode;
        }

        private String getDisplayGateCode() {
            return StrUtil.isNotBlank(normalizedGateCode) ? normalizedGateCode : rawGateCode;
        }

        private String getNormalizedGateCode() {
            return normalizedGateCode;
        }
    }

    /**
     * 需要走会员权益批量判定的待处理门禁上下文。
     */
    private static final class PendingFeatureGateContext {

        private final int originalIndex;

        private final MemberGateEntity gateEntity;

        private final MemberGateRuleEntity ruleEntity;

        private final List<String> allowedLevelCodes;

        private final MemberAccessRequirement requirement;

        private PendingFeatureGateContext(int originalIndex, MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
            List<String> allowedLevelCodes, MemberAccessRequirement requirement) {
            this.originalIndex = originalIndex;
            this.gateEntity = gateEntity;
            this.ruleEntity = ruleEntity;
            this.allowedLevelCodes = allowedLevelCodes == null ? Collections.emptyList() : allowedLevelCodes;
            this.requirement = requirement;
        }

        private int getOriginalIndex() {
            return originalIndex;
        }

        private MemberGateEntity getGateEntity() {
            return gateEntity;
        }

        private MemberGateRuleEntity getRuleEntity() {
            return ruleEntity;
        }

        private List<String> getAllowedLevelCodes() {
            return allowedLevelCodes;
        }

        private MemberAccessRequirement getRequirement() {
            return requirement;
        }
    }

    /**
     * 只依赖等级白名单的待处理门禁上下文。
     */
    private static final class PendingLevelOnlyGateContext {

        private final int originalIndex;

        private final MemberGateEntity gateEntity;

        private final MemberGateRuleEntity ruleEntity;

        private final List<String> allowedLevelCodes;

        private PendingLevelOnlyGateContext(int originalIndex, MemberGateEntity gateEntity, MemberGateRuleEntity ruleEntity,
            List<String> allowedLevelCodes) {
            this.originalIndex = originalIndex;
            this.gateEntity = gateEntity;
            this.ruleEntity = ruleEntity;
            this.allowedLevelCodes = allowedLevelCodes == null ? Collections.emptyList() : allowedLevelCodes;
        }

        private int getOriginalIndex() {
            return originalIndex;
        }

        private MemberGateEntity getGateEntity() {
            return gateEntity;
        }

        private MemberGateRuleEntity getRuleEntity() {
            return ruleEntity;
        }

        private List<String> getAllowedLevelCodes() {
            return allowedLevelCodes;
        }
    }
}

package com.healthtrail.domain.system.member;

import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementDTO;
import com.healthtrail.domain.system.member.dto.AppMemberEntitlementItemDTO;
import com.healthtrail.domain.system.member.enums.MemberFeatureTypeEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 会员准入统一入口。
 *
 * <p>后续任意业务功能只要想加“会员才能用”的限制，都应该优先接这一层，而不是在业务里自行写：
 * 1. 当前等级是否达标；
 * 2. 当前权益是否启用；
 * 3. 当前次数是否用完；
 * 4. 当前额度是否不足。
 *
 * <p>这样做的目的，是把会员门槛判断从“散落在各功能里的 if/else”收口成一个稳定服务，
 * 以后会员规则新增或调整时，只需要改这一处。
 */
@Service
@RequiredArgsConstructor
public class MemberAccessGateService {

    /** 会员权益快照应用服务 */
    private final MemberEntitlementApplicationService memberEntitlementApplicationService;
    /** 会员等级数据库服务 */
    private final MemberLevelService memberLevelService;

    /**
     * 执行会员准入校验，并把判断细节完整返回给调用方。
     */
    public MemberAccessResult checkAccess(Long userId, MemberAccessRequirement requirement) {
        List<MemberAccessResult> results = checkAccessList(userId, Collections.singletonList(requirement));
        return results.isEmpty() ? deny(new MemberAccessResult(),
            MemberAccessDenyReasonEnum.INVALID_REQUIREMENT, "会员准入校验参数为空") : results.get(0);
    }

    /**
     * 批量执行会员准入校验。
     *
     * <p>该方法是对单点准入的真正批量化实现：
     * 1. 当前用户会员关系与等级快照只解析一次；
     * 2. 当前批次涉及的权益快照只加载一次；
     * 3. 等级定义只查询一次；
     * 4. 最终仍按原顺序返回逐条准入结果。
     *
     * <p>这样会员门禁批量检查、页面多按钮显隐等场景就不会再因为循环调用单点准入而重复查库。
     */
    public List<MemberAccessResult> checkAccessList(Long userId, List<MemberAccessRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return Collections.emptyList();
        }
        MemberAccessBatchContext batchContext = buildBatchContext(userId, requirements);
        List<MemberAccessResult> results = new ArrayList<>(requirements.size());
        for (MemberAccessRequirement requirement : requirements) {
            results.add(checkAccess(batchContext, requirement));
        }
        return results;
    }

    private MemberAccessResult checkAccess(MemberAccessBatchContext batchContext, MemberAccessRequirement requirement) {
        MemberAccessResult result = new MemberAccessResult();
        result.setAllowed(false);
        result.setUserId(batchContext.getUserId());

        if (batchContext.getUserId() == null) {
            return deny(result, MemberAccessDenyReasonEnum.INVALID_REQUIREMENT, "会员准入校验缺少用户ID");
        }
        if (isRequirementEmpty(requirement)) {
            return deny(result, MemberAccessDenyReasonEnum.INVALID_REQUIREMENT,
                "会员准入校验至少需要 featureCode 或 requiredLevelCode");
        }

        String normalizedFeatureCode = normalizeCode(requirement.getFeatureCode());
        String normalizedRequiredLevelCode = normalizeCode(requirement.getRequiredLevelCode());

        result.setHasActiveMember(batchContext.isHasActiveMember());
        result.setCurrentMemberLevelId(batchContext.getCurrentMemberLevelId());
        result.setCurrentLevelCode(batchContext.getCurrentLevelCode());
        result.setCurrentLevelName(batchContext.getCurrentLevelName());
        result.setCurrentLevelSort(batchContext.getCurrentLevelSort());

        if (StrUtil.isNotBlank(normalizedRequiredLevelCode)) {
            MemberLevelEntity requiredLevel = batchContext.getLevelCodeMap().get(normalizedRequiredLevelCode);
            if (requiredLevel == null) {
                result.setRequiredLevelCode(normalizedRequiredLevelCode);
                return deny(result, MemberAccessDenyReasonEnum.INVALID_REQUIRED_LEVEL,
                    "会员准入校验引用了不存在或未启用的等级编码: " + normalizedRequiredLevelCode);
            }
            result.setRequiredLevelCode(requiredLevel.getLevelCode());
            result.setRequiredLevelName(requiredLevel.getLevelName());
            result.setRequiredLevelSort(resolveLevelSort(requiredLevel));
            if (batchContext.getCurrentLevelSort() < resolveLevelSort(requiredLevel)) {
                return deny(
                    result,
                    MemberAccessDenyReasonEnum.REQUIRED_LEVEL_NOT_MET,
                    "当前功能要求会员等级至少为 " + requiredLevel.getLevelName()
                        + "，当前等级为 " + result.getCurrentLevelName()
                );
            }
        }

        if (StrUtil.isNotBlank(normalizedFeatureCode)) {
            AppMemberEntitlementItemDTO entitlementItem = batchContext.getEntitlementItemMap().get(normalizedFeatureCode);
            result.setFeatureCode(normalizedFeatureCode);
            if (entitlementItem == null) {
                return deny(result, MemberAccessDenyReasonEnum.FEATURE_NOT_FOUND,
                    "会员权益 " + normalizedFeatureCode + " 未配置，当前无法完成会员准入判断");
            }
            fillEntitlementFields(result, entitlementItem);

            if (!Objects.equals(entitlementItem.getEnabled(), YesOrNoEnum.YES.getValue())) {
                return deny(result, MemberAccessDenyReasonEnum.FEATURE_NOT_ENABLED,
                    StrUtil.blankToDefault(entitlementItem.getMessage(), "当前会员等级未开放该功能"));
            }

            if (MemberFeatureTypeEnum.QUOTA.name().equals(entitlementItem.getFeatureType())) {
                int requiredRemainingQuota = requirement.getRequiredRemainingQuota() == null
                    ? 1
                    : requirement.getRequiredRemainingQuota();
                Integer remainingCount = entitlementItem.getRemainingCount();
                if (remainingCount != null && remainingCount < requiredRemainingQuota) {
                    return deny(result, MemberAccessDenyReasonEnum.QUOTA_NOT_ENOUGH,
                        StrUtil.blankToDefault(entitlementItem.getMessage(), "当前周期可用次数不足"));
                }
            }

            if (requirement.getRequiredLimitValue() != null
                && MemberFeatureTypeEnum.LIMIT.name().equals(entitlementItem.getFeatureType())) {
                Integer currentLimitValue = entitlementItem.getLimitValue();
                if (currentLimitValue != null && currentLimitValue < requirement.getRequiredLimitValue()) {
                    return deny(result, MemberAccessDenyReasonEnum.LIMIT_NOT_ENOUGH,
                        "当前权益额度不足，至少需要 " + requirement.getRequiredLimitValue()
                            + "，当前仅支持 " + currentLimitValue);
                }
            }
        }

        result.setAllowed(true);
        result.setMessage("会员准入校验通过");
        return result;
    }

    /**
     * 执行会员准入校验；若不通过则直接抛业务异常。
     *
     * <p>适合后端真正的业务入口，例如：
     * 1. 某个 AI 能力发起前；
     * 2. 某个只对高等级会员开放的功能保存前；
     * 3. 某个次数型会员权益真正扣减前。
     */
    public void checkAccessOrThrow(Long userId, MemberAccessRequirement requirement) {
        MemberAccessResult result = checkAccess(userId, requirement);
        if (result.isAllowed()) {
            return;
        }
        if (isConfigurationError(result.getDenyReasonCode())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, result.getMessage());
        }
        throw new ApiException(ErrorCode.Business.MEMBER_ACCESS_DENIED, result.getMessage());
    }

    private boolean isRequirementEmpty(MemberAccessRequirement requirement) {
        return requirement == null
            || (StrUtil.isBlank(requirement.getFeatureCode()) && StrUtil.isBlank(requirement.getRequiredLevelCode()));
    }

    /**
     * 批量预热当前批次准入校验要用到的上下文。
     *
     * <p>这里把“用户权益快照”和“等级定义”拆开预热，原因是：
     * 1. 权益快照只需要本批次真正涉及的 featureCode 子集；
     * 2. 等级定义要完整加载，才能支持 requiredLevelCode 比较；
     * 3. 预热完成后，后续逐条 requirement 判断只做内存映射。
     */
    private MemberAccessBatchContext buildBatchContext(Long userId, List<MemberAccessRequirement> requirements) {
        Map<String, MemberLevelEntity> levelCodeMap = listEnabledLevelCodeMap();
        if (userId == null) {
            return new MemberAccessBatchContext(userId, null, levelCodeMap);
        }
        Set<String> featureCodes = requirements.stream()
            .filter(Objects::nonNull)
            .map(MemberAccessRequirement::getFeatureCode)
            .map(this::normalizeCode)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        AppMemberEntitlementDTO entitlements = memberEntitlementApplicationService.getCurrentEntitlements(userId, featureCodes);
        return new MemberAccessBatchContext(userId, entitlements, levelCodeMap);
    }

    /**
     * 只把启用中的等级放入比较集合。
     *
     * <p>会员门槛应该只依赖当前系统仍然有效的等级定义，
     * 这样某个等级被后台停用后，新的功能门槛也不会继续误引用旧配置。
     */
    private Map<String, MemberLevelEntity> listEnabledLevelCodeMap() {
        List<MemberLevelEntity> levelEntities = memberLevelService.list();
        if (levelEntities == null || levelEntities.isEmpty()) {
            return Collections.emptyMap();
        }
        return levelEntities.stream()
            .filter(Objects::nonNull)
            .filter(entity -> Objects.equals(entity.getStatus(), StatusEnum.ENABLE.getValue()))
            .filter(entity -> StrUtil.isNotBlank(entity.getLevelCode()))
            .collect(Collectors.toMap(
                entity -> normalizeCode(entity.getLevelCode()),
                entity -> entity,
                (left, right) -> left
            ));
    }

    private Map<String, AppMemberEntitlementItemDTO> toEntitlementMap(AppMemberEntitlementDTO entitlements) {
        if (entitlements == null || entitlements.getItems() == null || entitlements.getItems().isEmpty()) {
            return Collections.emptyMap();
        }
        return entitlements.getItems().stream()
            .filter(Objects::nonNull)
            .filter(item -> StrUtil.isNotBlank(item.getFeatureCode()))
            .collect(Collectors.toMap(
                item -> normalizeCode(item.getFeatureCode()),
                item -> item,
                (left, right) -> left
            ));
    }

    private void fillEntitlementFields(MemberAccessResult result, AppMemberEntitlementItemDTO entitlementItem) {
        result.setFeatureCode(normalizeCode(entitlementItem.getFeatureCode()));
        result.setFeatureName(entitlementItem.getFeatureName());
        result.setFeatureType(entitlementItem.getFeatureType());
        result.setFeatureEnabled(entitlementItem.getEnabled());
        result.setLimitValue(entitlementItem.getLimitValue());
        result.setUsedCount(entitlementItem.getUsedCount());
        result.setRemainingCount(entitlementItem.getRemainingCount());
    }

    private MemberAccessResult deny(MemberAccessResult result, MemberAccessDenyReasonEnum denyReason, String message) {
        result.setAllowed(false);
        result.setDenyReasonCode(denyReason.name());
        result.setMessage(message);
        return result;
    }

    private boolean isConfigurationError(String denyReasonCode) {
        return MemberAccessDenyReasonEnum.INVALID_REQUIREMENT.name().equals(denyReasonCode)
            || MemberAccessDenyReasonEnum.INVALID_REQUIRED_LEVEL.name().equals(denyReasonCode)
            || MemberAccessDenyReasonEnum.FEATURE_NOT_FOUND.name().equals(denyReasonCode);
    }

    private String normalizeCode(String rawCode) {
        String normalizedCode = StrUtil.trim(rawCode);
        if (StrUtil.isBlank(normalizedCode)) {
            return null;
        }
        return normalizedCode.toUpperCase();
    }

    private int resolveLevelSort(MemberLevelEntity levelEntity) {
        return levelEntity == null || levelEntity.getLevelSort() == null ? 0 : levelEntity.getLevelSort();
    }

    /**
     * 如果当前没有有效会员，则仍然返回“免费版”作为对外展示名称。
     *
     * <p>这是为了让调用方在展示拒绝提示时拿到一套稳定文案，
     * 不需要自己再写“没有会员时显示免费版”的补丁逻辑。
     */
    private String resolveCurrentLevelName(MemberLevelEntity currentLevel, AppMemberEntitlementDTO entitlements) {
        if (currentLevel != null && StrUtil.isNotBlank(currentLevel.getLevelName())) {
            return currentLevel.getLevelName();
        }
        if (Boolean.TRUE.equals(entitlements.getHasActiveMember()) && StrUtil.isNotBlank(entitlements.getCurrentLevelName())) {
            return entitlements.getCurrentLevelName();
        }
        return "免费版";
    }

    /**
     * 批量会员准入运行时上下文。
     *
     * <p>它不做跨请求缓存，只在当前批次校验过程中复用，
     * 目的是避免同一批门禁或同一页多按钮检查时重复解析：
     * 1. 当前会员快照
     * 2. 当前等级快照
     * 3. featureCode -> entitlementItem 映射
     * 4. levelCode -> levelEntity 映射
     */
    private final class MemberAccessBatchContext {

        private final Long userId;

        private final AppMemberEntitlementDTO entitlements;

        private final Map<String, MemberLevelEntity> levelCodeMap;

        private final Map<String, AppMemberEntitlementItemDTO> entitlementItemMap;

        private final boolean hasActiveMember;

        private final Long currentMemberLevelId;

        private final String currentLevelCode;

        private final String currentLevelName;

        private final Integer currentLevelSort;

        private MemberAccessBatchContext(Long userId, AppMemberEntitlementDTO entitlements,
            Map<String, MemberLevelEntity> levelCodeMap) {
            this.userId = userId;
            this.entitlements = entitlements;
            this.levelCodeMap = levelCodeMap == null ? Collections.emptyMap() : levelCodeMap;
            this.entitlementItemMap = toEntitlementMap(entitlements);

            boolean activeMember = entitlements != null && Boolean.TRUE.equals(entitlements.getHasActiveMember());
            String resolvedCurrentLevelCode = activeMember
                ? normalizeCode(entitlements.getCurrentLevelCode())
                : MemberLevelCodeConstants.FREE;
            if (StrUtil.isBlank(resolvedCurrentLevelCode)) {
                resolvedCurrentLevelCode = MemberLevelCodeConstants.FREE;
            }
            MemberLevelEntity currentLevel = this.levelCodeMap.get(resolvedCurrentLevelCode);

            this.hasActiveMember = activeMember;
            this.currentMemberLevelId = activeMember && entitlements != null ? entitlements.getCurrentMemberLevelId() : null;
            this.currentLevelCode = resolvedCurrentLevelCode;
            this.currentLevelName = resolveCurrentLevelName(currentLevel, entitlements == null
                ? new AppMemberEntitlementDTO() : entitlements);
            this.currentLevelSort = resolveLevelSort(currentLevel);
        }

        private Long getUserId() {
            return userId;
        }

        private Map<String, MemberLevelEntity> getLevelCodeMap() {
            return levelCodeMap;
        }

        private Map<String, AppMemberEntitlementItemDTO> getEntitlementItemMap() {
            return entitlementItemMap;
        }

        private boolean isHasActiveMember() {
            return hasActiveMember;
        }

        private Long getCurrentMemberLevelId() {
            return currentMemberLevelId;
        }

        private String getCurrentLevelCode() {
            return currentLevelCode;
        }

        private String getCurrentLevelName() {
            return currentLevelName;
        }

        private Integer getCurrentLevelSort() {
            return currentLevelSort;
        }
    }
}

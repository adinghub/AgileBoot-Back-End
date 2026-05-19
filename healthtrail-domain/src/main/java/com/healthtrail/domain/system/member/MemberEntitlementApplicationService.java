package com.healthtrail.domain.system.member;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
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
import com.healthtrail.domain.system.member.enums.MemberFeatureTypeEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 会员权益快照应用服务。
 *
 * <p>App 端不自己推会员规则，而是只消费这层输出的快照结果。
 * 这样后续调整会员配置时，App 无需重新发版。
 */
@Service
@RequiredArgsConstructor
public class MemberEntitlementApplicationService {

    /** 会员权益数据库服务 */
    private final MemberFeatureService memberFeatureService;
    /** 会员等级数据库服务 */
    private final MemberLevelService memberLevelService;
    /** 会员等级权益关联数据库服务 */
    private final MemberLevelFeatureService memberLevelFeatureService;
    /** 会员权益配额使用记录数据库服务 */
    private final MemberFeatureQuotaUsageService memberFeatureQuotaUsageService;
    /** 用户会员关系数据库服务 */
    private final UserMemberService userMemberService;

    public AppMemberEntitlementDTO getCurrentEntitlements(Long userId) {
        return getCurrentEntitlements(userId, null);
    }

    /**
     * 查询当前用户权益快照，并允许调用方按 featureCode 子集预热。
     *
     * <p>这个重载方法主要服务于会员准入与门禁批量检查场景：
     * 1. 如果调用方只关心少数几个权益，就只加载这些权益定义；
     * 2. 当前会员关系、当前等级、等级权益矩阵、QUOTA 使用量都只解析一次；
     * 3. 既保留原有快照结构，又避免批量门禁时重复拉取整套权益。
     *
     * <p>对外仍然保持“一个 DTO 描述当前用户权益快照”的语义不变，
     * 只是把内部实现从逐条现查改成了批量预热后复用。
     */
    AppMemberEntitlementDTO getCurrentEntitlements(Long userId, Collection<String> featureCodes) {
        MemberEntitlementRuntimeContext runtimeContext = buildRuntimeContext(userId, featureCodes, new Date());

        AppMemberEntitlementDTO dto = new AppMemberEntitlementDTO();
        dto.setUserId(userId);
        dto.setHasActiveMember(runtimeContext.isHasActiveMember());
        dto.setCurrentMemberLevelId(runtimeContext.getCurrentLevel() == null
            ? null : runtimeContext.getCurrentLevel().getMemberLevelId());
        dto.setCurrentLevelCode(runtimeContext.getCurrentLevel() == null
            ? null : runtimeContext.getCurrentLevel().getLevelCode());
        dto.setCurrentLevelName(runtimeContext.getCurrentLevel() == null
            ? null : runtimeContext.getCurrentLevel().getLevelName());
        dto.setEffectiveStartTime(runtimeContext.isHasActiveMember() && runtimeContext.getCurrentUserMember() != null
            ? runtimeContext.getCurrentUserMember().getEffectiveStartTime() : null);
        dto.setEffectiveEndTime(runtimeContext.isHasActiveMember() && runtimeContext.getCurrentUserMember() != null
            ? runtimeContext.getCurrentUserMember().getEffectiveEndTime() : null);
        dto.setItems(runtimeContext.getFeatureEntities().stream()
            .map(featureEntity -> buildEntitlementItem(runtimeContext,
                runtimeContext.getConfiguredMap().get(featureEntity.getMemberFeatureId()),
                featureEntity))
            .collect(Collectors.toList()));
        return dto;
    }

    private MemberEntitlementRuntimeContext buildRuntimeContext(Long userId, Collection<String> featureCodes, Date now) {
        UserMemberEntity currentUserMember = userMemberService.lambdaQuery()
            .eq(UserMemberEntity::getUserId, userId)
            .one();
        boolean hasActiveMember = MemberActiveStatusSupport.isMemberCurrentlyActive(currentUserMember, now);

        /*
         * 权益快照只应该基于“当前仍有效”的会员关系来展开。
         *
         * 如果用户历史上开通过会员，但现在已经过期，而这里仍继续把旧等级矩阵展开给 App，
         * 页面和通用会员准入工具就会把过期会员误判成仍然可用。
         *
         * 因此这里明确要求：只有有效会员关系才能拿到等级权益矩阵；否则退回免费默认权益。
         */
        MemberLevelEntity currentLevel = hasActiveMember && currentUserMember != null
            ? memberLevelService.getById(currentUserMember.getMemberLevelId())
            : null;
        Map<Long, MemberLevelFeatureEntity> configuredMap = currentLevel == null
            ? Collections.emptyMap()
            : memberLevelFeatureService.lambdaQuery()
                .eq(MemberLevelFeatureEntity::getMemberLevelId, currentLevel.getMemberLevelId())
                .list()
                .stream()
                .collect(Collectors.toMap(MemberLevelFeatureEntity::getMemberFeatureId, entity -> entity, (left, right) -> left));

        Set<String> normalizedFeatureCodes = normalizeFeatureCodes(featureCodes);
        List<MemberFeatureEntity> featureEntities = listFeatureEntities(normalizedFeatureCodes);
        Map<String, Integer> quotaUsageMap = preloadQuotaUsageMap(userId, featureEntities, now);
        return new MemberEntitlementRuntimeContext(
            userId,
            now,
            currentUserMember,
            hasActiveMember,
            currentLevel,
            configuredMap,
            featureEntities,
            quotaUsageMap
        );
    }

    private AppMemberEntitlementItemDTO buildEntitlementItem(MemberEntitlementRuntimeContext runtimeContext,
        MemberLevelFeatureEntity configuredEntity, MemberFeatureEntity featureEntity) {
        AppMemberEntitlementItemDTO dto = new AppMemberEntitlementItemDTO();
        dto.setFeatureCode(featureEntity.getFeatureCode());
        dto.setFeatureName(featureEntity.getFeatureName());
        dto.setFeatureType(featureEntity.getFeatureType());
        dto.setQuotaPeriodType(featureEntity.getQuotaPeriodType());
        dto.setEntitlementSource(configuredEntity == null ? "FREE_DEFAULT" : "MEMBER_LEVEL");
        dto.setEnabled(configuredEntity == null ? featureEntity.getFreeEnabled() : configuredEntity.getEnabled());
        dto.setLimitValue(configuredEntity == null ? featureEntity.getFreeLimitValue() : configuredEntity.getLimitValue());

        if (MemberFeatureTypeEnum.QUOTA.name().equals(featureEntity.getFeatureType())
            && Objects.equals(dto.getEnabled(), YesOrNoEnum.YES.getValue())) {
            String periodKey = resolvePeriodKey(featureEntity.getQuotaPeriodType(), runtimeContext.getNow());
            dto.setPeriodKey(periodKey);
            int usedCount = runtimeContext.getQuotaUsedCount(featureEntity.getMemberFeatureId(), periodKey);
            dto.setUsedCount(usedCount);
            if (dto.getLimitValue() == null) {
                dto.setRemainingCount(null);
                dto.setMessage("当前周期不限次数");
            } else {
                dto.setRemainingCount(Math.max(dto.getLimitValue() - usedCount, 0));
                dto.setMessage(dto.getRemainingCount() > 0
                    ? "当前周期仍有可用次数"
                    : "当前周期可用次数已用完");
            }
        } else {
            dto.setUsedCount(null);
            dto.setRemainingCount(dto.getLimitValue());
            dto.setMessage(buildNonQuotaMessage(runtimeContext.getCurrentLevel(), dto.getEnabled(),
                dto.getLimitValue(), featureEntity.getFeatureType()));
        }
        return dto;
    }

    public String resolvePeriodKey(String quotaPeriodType, Date now) {
        if (MemberFeatureQuotaPeriodEnum.DAY.name().equals(quotaPeriodType)) {
            return DateUtil.format(now, "yyyyMMdd");
        }
        return DateUtil.format(now, "yyyyMM");
    }

    private String buildNonQuotaMessage(MemberLevelEntity currentLevel, Integer enabled, Integer limitValue, String featureType) {
        if (!Objects.equals(enabled, YesOrNoEnum.YES.getValue())) {
            return currentLevel == null ? "当前免费版不可用" : "当前会员等级未开放";
        }
        if (MemberFeatureTypeEnum.LIMIT.name().equals(featureType)) {
            return limitValue == null ? "当前会员不限制数量" : "当前可用上限为 " + limitValue;
        }
        return currentLevel == null ? "当前免费版可用" : "当前会员已开放";
    }

    /**
     * 统一归一权益编码集合。
     *
     * <p>批量会员准入时，同一批命令里可能多次引用同一个 featureCode，
     * 这里先做大小写归一与去重，后续查询只需要按唯一 featureCode 集合加载即可。
     */
    private Set<String> normalizeFeatureCodes(Collection<String> featureCodes) {
        if (featureCodes == null) {
            return null;
        }
        if (featureCodes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> normalizedCodes = new LinkedHashSet<>();
        for (String featureCode : featureCodes) {
            String normalizedCode = normalizeCode(featureCode);
            if (StrUtil.isNotBlank(normalizedCode)) {
                normalizedCodes.add(normalizedCode);
            }
        }
        return normalizedCodes;
    }

    /**
     * 按是否指定 featureCode 子集加载权益定义。
     */
    private List<MemberFeatureEntity> listFeatureEntities(Set<String> normalizedFeatureCodes) {
        if (normalizedFeatureCodes != null && normalizedFeatureCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return memberFeatureService.lambdaQuery()
            .in(normalizedFeatureCodes != null && !normalizedFeatureCodes.isEmpty(),
                MemberFeatureEntity::getFeatureCode, normalizedFeatureCodes)
            .orderByAsc(MemberFeatureEntity::getFeatureSort)
            .list();
    }

    /**
     * 批量预热次数型权益当前周期已用次数。
     *
     * <p>旧实现是每个 QUOTA feature 单独 `one()` 一次使用记录。
     * 本次改成：
     * 1. 先按当前要构建的权益列表筛出所有 QUOTA feature；
     * 2. 再一次性按 `userId + memberFeatureId 集合 + periodKey 集合` 查询；
     * 3. 最后把结果放入内存映射，逐条组装 DTO 时直接读取。
     */
    private Map<String, Integer> preloadQuotaUsageMap(Long userId, List<MemberFeatureEntity> featureEntities, Date now) {
        if (userId == null || featureEntities == null || featureEntities.isEmpty()) {
            return Collections.emptyMap();
        }
        List<MemberFeatureEntity> quotaFeatures = featureEntities.stream()
            .filter(Objects::nonNull)
            .filter(featureEntity -> MemberFeatureTypeEnum.QUOTA.name().equals(featureEntity.getFeatureType()))
            .filter(featureEntity -> featureEntity.getMemberFeatureId() != null)
            .collect(Collectors.toList());
        if (quotaFeatures.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> memberFeatureIds = quotaFeatures.stream()
            .map(MemberFeatureEntity::getMemberFeatureId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> periodKeys = quotaFeatures.stream()
            .map(featureEntity -> resolvePeriodKey(featureEntity.getQuotaPeriodType(), now))
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (memberFeatureIds.isEmpty() || periodKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        return memberFeatureQuotaUsageService.lambdaQuery()
            .eq(MemberFeatureQuotaUsageEntity::getUserId, userId)
            .in(MemberFeatureQuotaUsageEntity::getMemberFeatureId, memberFeatureIds)
            .in(MemberFeatureQuotaUsageEntity::getPeriodKey, periodKeys)
            .list()
            .stream()
            .filter(Objects::nonNull)
            .filter(entity -> entity.getMemberFeatureId() != null && StrUtil.isNotBlank(entity.getPeriodKey()))
            .collect(Collectors.toMap(
                entity -> buildQuotaUsageKey(entity.getMemberFeatureId(), entity.getPeriodKey()),
                entity -> entity.getUsedCount() == null ? 0 : entity.getUsedCount(),
                (left, right) -> right
            ));
    }

    private String buildQuotaUsageKey(Long memberFeatureId, String periodKey) {
        return memberFeatureId + "#" + periodKey;
    }

    private String normalizeCode(String rawCode) {
        String normalizedCode = StrUtil.trim(rawCode);
        if (StrUtil.isBlank(normalizedCode)) {
            return null;
        }
        return normalizedCode.toUpperCase();
    }

    /**
     * 当前请求内的会员权益运行时上下文。
     *
     * <p>它不是跨请求缓存，只在单次服务调用内使用，
     * 目的是把“当前用户会员关系 / 当前等级 / 等级权益矩阵 / QUOTA 使用量”统一预热后复用，
     * 避免在一批 feature 或一批门禁判定里重复查库。
     */
    private static final class MemberEntitlementRuntimeContext {

        private final Long userId;

        private final Date now;

        private final UserMemberEntity currentUserMember;

        private final boolean hasActiveMember;

        private final MemberLevelEntity currentLevel;

        private final Map<Long, MemberLevelFeatureEntity> configuredMap;

        private final List<MemberFeatureEntity> featureEntities;

        private final Map<String, Integer> quotaUsageMap;

        private MemberEntitlementRuntimeContext(Long userId, Date now, UserMemberEntity currentUserMember,
            boolean hasActiveMember, MemberLevelEntity currentLevel,
            Map<Long, MemberLevelFeatureEntity> configuredMap,
            List<MemberFeatureEntity> featureEntities,
            Map<String, Integer> quotaUsageMap) {
            this.userId = userId;
            this.now = now;
            this.currentUserMember = currentUserMember;
            this.hasActiveMember = hasActiveMember;
            this.currentLevel = currentLevel;
            this.configuredMap = configuredMap == null ? Collections.emptyMap() : configuredMap;
            this.featureEntities = featureEntities == null ? Collections.emptyList() : new ArrayList<>(featureEntities);
            this.quotaUsageMap = quotaUsageMap == null ? Collections.emptyMap() : quotaUsageMap;
        }

        private Date getNow() {
            return now;
        }

        private UserMemberEntity getCurrentUserMember() {
            return currentUserMember;
        }

        private boolean isHasActiveMember() {
            return hasActiveMember;
        }

        private MemberLevelEntity getCurrentLevel() {
            return currentLevel;
        }

        private Map<Long, MemberLevelFeatureEntity> getConfiguredMap() {
            return configuredMap;
        }

        private List<MemberFeatureEntity> getFeatureEntities() {
            return featureEntities;
        }

        private int getQuotaUsedCount(Long memberFeatureId, String periodKey) {
            if (memberFeatureId == null || StrUtil.isBlank(periodKey)) {
                return 0;
            }
            return quotaUsageMap.getOrDefault(memberFeatureId + "#" + periodKey, 0);
        }
    }
}

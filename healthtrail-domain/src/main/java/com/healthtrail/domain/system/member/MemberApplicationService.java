package com.healthtrail.domain.system.member;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.healthtrail.domain.system.member.command.AddMemberFeatureCommand;
import com.healthtrail.domain.system.member.command.AddMemberLevelCommand;
import com.healthtrail.domain.system.member.command.AssignUserMemberCommand;
import com.healthtrail.domain.system.member.command.ClearUserMemberCommand;
import com.healthtrail.domain.system.member.command.GenerateMemberRedeemCodeCommand;
import com.healthtrail.domain.system.member.command.SaveMemberLevelFeatureCommand;
import com.healthtrail.domain.system.member.command.UpdateMemberFeatureCommand;
import com.healthtrail.domain.system.member.command.UpdateMemberLevelCommand;
import com.healthtrail.domain.system.member.db.MemberFeatureEntity;
import com.healthtrail.domain.system.member.db.MemberFeatureService;
import com.healthtrail.domain.system.member.db.MemberLevelEntity;
import com.healthtrail.domain.system.member.db.MemberLevelFeatureEntity;
import com.healthtrail.domain.system.member.db.MemberLevelFeatureService;
import com.healthtrail.domain.system.member.db.MemberLevelService;
import com.healthtrail.domain.system.member.db.MemberRedeemCodeEntity;
import com.healthtrail.domain.system.member.db.MemberRedeemCodeService;
import com.healthtrail.domain.system.member.db.UserMemberEntity;
import com.healthtrail.domain.system.member.db.UserMemberOrderEntity;
import com.healthtrail.domain.system.member.db.UserMemberOrderService;
import com.healthtrail.domain.system.member.db.UserMemberService;
import com.healthtrail.domain.system.member.db.UserMemberSubscriptionEntity;
import com.healthtrail.domain.system.member.db.UserMemberSubscriptionService;
import com.healthtrail.domain.system.member.dto.MemberFeatureDTO;
import com.healthtrail.domain.system.member.dto.MemberLevelDTO;
import com.healthtrail.domain.system.member.dto.MemberLevelFeatureDTO;
import com.healthtrail.domain.system.member.dto.MemberRedeemCodeBatchDTO;
import com.healthtrail.domain.system.member.dto.MemberRedeemCodeDTO;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionOrderDTO;
import com.healthtrail.domain.system.member.dto.UserMemberDTO;
import com.healthtrail.domain.system.member.enums.MemberFeatureQuotaPeriodEnum;
import com.healthtrail.domain.system.member.enums.MemberFeatureTypeEnum;
import com.healthtrail.domain.system.member.enums.MemberRedeemCodeStatusEnum;
import com.healthtrail.domain.system.member.enums.UserMemberOrderSourceTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberOrderStatusEnum;
import com.healthtrail.domain.system.member.enums.UserMemberOrderTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberSourceTypeEnum;
import com.healthtrail.domain.system.member.enums.UserMemberStatusEnum;
import com.healthtrail.domain.system.member.enums.UserMemberSubscriptionStatusEnum;
import com.healthtrail.domain.system.member.query.MemberFeatureQuery;
import com.healthtrail.domain.system.member.query.MemberLevelQuery;
import com.healthtrail.domain.system.member.query.MemberRedeemCodeQuery;
import com.healthtrail.domain.system.member.query.UserMemberQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员后台应用服务。
 *
 * <p>这层服务统一承接后台 5 类会员管理能力：
 * 1. 会员权益定义
 * 2. 会员等级管理
 * 3. 等级权益矩阵
 * 4. 用户会员关系
 * 5. 会员兑换码
 *
 * <p>先把核心业务收口到一处，后续即使会员规则继续扩展，也不需要把判断逻辑散落到多个 Controller。
 */
@Service
@RequiredArgsConstructor
public class MemberApplicationService {

    /** 会员权益数据库服务 */
    private final MemberFeatureService memberFeatureService;
    /** 会员等级数据库服务 */
    private final MemberLevelService memberLevelService;
    /** 会员等级权益关联数据库服务 */
    private final MemberLevelFeatureService memberLevelFeatureService;
    /** 用户会员关系数据库服务 */
    private final UserMemberService userMemberService;
    /** 会员兑换码数据库服务 */
    private final MemberRedeemCodeService memberRedeemCodeService;
    /** 用户会员订单数据库服务 */
    private final UserMemberOrderService userMemberOrderService;
    /** 用户会员订阅数据库服务 */
    private final UserMemberSubscriptionService userMemberSubscriptionService;
    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;

    public PageDTO<MemberFeatureDTO> getMemberFeaturePage(MemberFeatureQuery query) {
        Page<MemberFeatureEntity> page = memberFeatureService.page(query.toPage(), query.toQueryWrapper());
        List<MemberFeatureDTO> records = page.getRecords().stream().map(MemberFeatureDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public MemberFeatureDTO getMemberFeatureInfo(Long memberFeatureId) {
        return new MemberFeatureDTO(loadMemberFeature(memberFeatureId));
    }

    public void addMemberFeature(AddMemberFeatureCommand command) {
        validateMemberFeatureCodeDuplicated(command.getFeatureCode(), null);
        MemberFeatureEntity entity = new MemberFeatureEntity();
        BeanUtil.copyProperties(command, entity, "memberFeatureId");
        fillMemberFeatureDefaultFields(entity);
        validateMemberFeatureFields(entity);
        memberFeatureService.save(entity);
    }

    public void updateMemberFeature(UpdateMemberFeatureCommand command) {
        MemberFeatureEntity entity = loadMemberFeature(command.getMemberFeatureId());
        validateMemberFeatureCodeDuplicated(command.getFeatureCode(), entity.getMemberFeatureId());
        BeanUtil.copyProperties(command, entity, "memberFeatureId");
        fillMemberFeatureDefaultFields(entity);
        validateMemberFeatureFields(entity);
        memberFeatureService.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMemberFeatures(List<Long> memberFeatureIds) {
        if (memberFeatureIds == null || memberFeatureIds.isEmpty()) {
            return;
        }
        List<MemberFeatureEntity> featureEntities = memberFeatureService.listByIds(memberFeatureIds);
        for (MemberFeatureEntity featureEntity : featureEntities) {
            if (featureEntity != null && Objects.equals(featureEntity.getIsBuiltin(), YesOrNoEnum.YES.getValue())) {
                throw new ApiException(ErrorCode.Business.MEMBER_FEATURE_BUILTIN_NOT_ALLOW_DELETE);
            }
        }
        memberFeatureService.removeByIds(memberFeatureIds);
    }

    public PageDTO<MemberLevelDTO> getMemberLevelPage(MemberLevelQuery query) {
        Page<MemberLevelEntity> page = memberLevelService.page(query.toPage(), query.toQueryWrapper());
        List<MemberLevelDTO> records = page.getRecords().stream().map(MemberLevelDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public List<MemberLevelDTO> getEnabledMemberLevels() {
        return memberLevelService.lambdaQuery()
            .eq(MemberLevelEntity::getStatus, StatusEnum.ENABLE.getValue())
            .orderByAsc(MemberLevelEntity::getLevelSort)
            .list()
            .stream()
            .map(MemberLevelDTO::new)
            .collect(Collectors.toList());
    }

    public MemberLevelDTO getMemberLevelInfo(Long memberLevelId) {
        return new MemberLevelDTO(loadMemberLevel(memberLevelId));
    }

    public void addMemberLevel(AddMemberLevelCommand command) {
        validateMemberLevelCodeDuplicated(command.getLevelCode(), null);
        MemberLevelEntity entity = new MemberLevelEntity();
        BeanUtil.copyProperties(command, entity, "memberLevelId");
        fillMemberLevelDefaultFields(entity);
        memberLevelService.save(entity);
    }

    public void updateMemberLevel(UpdateMemberLevelCommand command) {
        MemberLevelEntity entity = loadMemberLevel(command.getMemberLevelId());
        validateMemberLevelCodeDuplicated(command.getLevelCode(), entity.getMemberLevelId());
        BeanUtil.copyProperties(command, entity, "memberLevelId");
        fillMemberLevelDefaultFields(entity);
        memberLevelService.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMemberLevels(List<Long> memberLevelIds) {
        if (memberLevelIds == null || memberLevelIds.isEmpty()) {
            return;
        }
        memberLevelService.removeByIds(memberLevelIds);
    }

    public List<MemberLevelFeatureDTO> getMemberLevelFeatureMatrix(Long memberLevelId) {
        loadMemberLevel(memberLevelId);
        List<MemberFeatureEntity> featureEntities = memberFeatureService.lambdaQuery()
            .orderByAsc(MemberFeatureEntity::getFeatureSort)
            .list();
        Map<Long, MemberLevelFeatureEntity> configuredMap = memberLevelFeatureService.lambdaQuery()
            .eq(MemberLevelFeatureEntity::getMemberLevelId, memberLevelId)
            .list()
            .stream()
            .collect(Collectors.toMap(MemberLevelFeatureEntity::getMemberFeatureId, entity -> entity, (left, right) -> left));

        List<MemberLevelFeatureDTO> dtos = new ArrayList<>();
        for (MemberFeatureEntity featureEntity : featureEntities) {
            MemberLevelFeatureDTO dto = new MemberLevelFeatureDTO();
            dto.setMemberFeatureId(featureEntity.getMemberFeatureId());
            dto.setFeatureCode(featureEntity.getFeatureCode());
            dto.setFeatureName(featureEntity.getFeatureName());
            dto.setFeatureType(featureEntity.getFeatureType());
            dto.setQuotaPeriodType(featureEntity.getQuotaPeriodType());
            dto.setFreeEnabled(featureEntity.getFreeEnabled());
            dto.setFreeLimitValue(featureEntity.getFreeLimitValue());
            MemberLevelFeatureEntity configuredEntity = configuredMap.get(featureEntity.getMemberFeatureId());
            dto.setConfigured(configuredEntity != null);
            dto.setEnabled(configuredEntity == null ? featureEntity.getFreeEnabled() : configuredEntity.getEnabled());
            dto.setLimitValue(configuredEntity == null ? featureEntity.getFreeLimitValue() : configuredEntity.getLimitValue());
            dto.setRemark(configuredEntity == null ? null : configuredEntity.getRemark());
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveMemberLevelFeatures(Long memberLevelId, SaveMemberLevelFeatureCommand command) {
        loadMemberLevel(memberLevelId);
        if (command == null || command.getItems() == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_LEVEL_FEATURE_SAVE_EMPTY);
        }
        QueryWrapper<MemberLevelFeatureEntity> removeWrapper = new QueryWrapper<>();
        removeWrapper.eq("member_level_id", memberLevelId);
        memberLevelFeatureService.remove(removeWrapper);

        List<MemberLevelFeatureEntity> saveEntities = new ArrayList<>();
        for (SaveMemberLevelFeatureCommand.Item item : command.getItems()) {
            if (item == null || item.getMemberFeatureId() == null) {
                continue;
            }
            MemberFeatureEntity featureEntity = loadMemberFeature(item.getMemberFeatureId());
            MemberLevelFeatureEntity saveEntity = new MemberLevelFeatureEntity();
            saveEntity.setMemberLevelId(memberLevelId);
            saveEntity.setMemberFeatureId(item.getMemberFeatureId());
            saveEntity.setEnabled(item.getEnabled() == null ? featureEntity.getFreeEnabled() : item.getEnabled());
            saveEntity.setLimitValue(normalizeLimitValue(featureEntity.getFeatureType(), saveEntity.getEnabled(), item.getLimitValue()));
            saveEntity.setRemark(item.getRemark());
            saveEntities.add(saveEntity);
        }
        if (!saveEntities.isEmpty()) {
            memberLevelFeatureService.saveBatch(saveEntities);
        }
    }

    public PageDTO<UserMemberDTO> getUserMemberPage(UserMemberQuery query) {
        Page<UserMemberEntity> page = userMemberService.page(query.toPage(), query.toQueryWrapper());
        Map<Long, MemberLevelEntity> memberLevelMap = listLevelMap(page.getRecords().stream()
            .map(UserMemberEntity::getMemberLevelId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, HealthAppUserEntity> userMap = listAppUserMap(page.getRecords().stream()
            .map(UserMemberEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
        List<UserMemberDTO> records = page.getRecords().stream()
            .map(entity -> buildUserMemberDTO(entity, memberLevelMap.get(entity.getMemberLevelId()), userMap.get(entity.getUserId())))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public UserMemberDTO getUserMemberInfo(Long userId) {
        UserMemberEntity userMemberEntity = userMemberService.lambdaQuery()
            .eq(UserMemberEntity::getUserId, userId)
            .one();
        if (userMemberEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, userId, "用户会员");
        }
        HealthAppUserEntity appUserEntity = loadAppUser(userId);
        MemberLevelEntity memberLevelEntity = loadMemberLevel(userMemberEntity.getMemberLevelId());
        return buildUserMemberDTO(userMemberEntity, memberLevelEntity, appUserEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignUserMember(AssignUserMemberCommand command) {
        HealthAppUserEntity appUserEntity = loadAppUser(command.getUserId());
        MemberLevelEntity memberLevelEntity = loadEnabledMemberLevel(command.getMemberLevelId());
        Date now = new Date();
        UserMemberEntity currentEntity = getCurrentUserMember(command.getUserId());
        Date nextStartTime = now;
        Date nextEndTime = command.getDurationDays() == null
            ? resolveEndTime(now, memberLevelEntity.getDurationDays())
            : resolveEndTime(now, command.getDurationDays());

        if (currentEntity == null) {
            UserMemberEntity saveEntity = new UserMemberEntity();
            saveEntity.setUserId(appUserEntity.getUserId());
            saveEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            saveEntity.setEffectiveStartTime(nextStartTime);
            saveEntity.setEffectiveEndTime(nextEndTime);
            saveEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
            saveEntity.setSourceType(UserMemberSourceTypeEnum.ADMIN_GRANT.name());
            saveEntity.setRemark(command.getRemark());
            userMemberService.save(saveEntity);
        } else {
            currentEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            currentEntity.setEffectiveStartTime(nextStartTime);
            currentEntity.setEffectiveEndTime(nextEndTime);
            currentEntity.setStatus(UserMemberStatusEnum.ACTIVE.name());
            currentEntity.setSourceType(UserMemberSourceTypeEnum.ADMIN_GRANT.name());
            currentEntity.setRemark(command.getRemark());
            userMemberService.updateById(currentEntity);
        }

        createMemberOrder(command.getUserId(), memberLevelEntity, UserMemberOrderTypeEnum.GRANT.name(),
            UserMemberOrderSourceTypeEnum.ADMIN.name(), BigDecimal.ZERO, nextStartTime, nextEndTime, command.getRemark(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearUserMember(ClearUserMemberCommand command) {
        UserMemberEntity currentEntity = getCurrentUserMember(command.getUserId());
        if (currentEntity == null) {
            return;
        }
        currentEntity.setStatus(UserMemberStatusEnum.DISABLED.name());
        currentEntity.setEffectiveEndTime(new Date());
        currentEntity.setRemark(command.getRemark());
        userMemberService.updateById(currentEntity);

        UserMemberSubscriptionEntity subscriptionEntity = userMemberSubscriptionService.lambdaQuery()
            .eq(UserMemberSubscriptionEntity::getUserId, command.getUserId())
            .one();
        if (subscriptionEntity != null) {
            subscriptionEntity.setSubscriptionStatus(UserMemberSubscriptionStatusEnum.CANCELED.name());
            subscriptionEntity.setAutoRenew(YesOrNoEnum.NO.getValue());
            subscriptionEntity.setCancelTime(new Date());
            subscriptionEntity.setCancelReason(StrUtil.blankToDefault(command.getRemark(), "后台清空会员"));
            userMemberSubscriptionService.updateById(subscriptionEntity);
        }
    }

    public PageDTO<MemberRedeemCodeDTO> getMemberRedeemCodePage(MemberRedeemCodeQuery query) {
        Page<MemberRedeemCodeEntity> page = memberRedeemCodeService.page(query.toPage(), query.toQueryWrapper());
        List<MemberRedeemCodeDTO> records = page.getRecords().stream().map(MemberRedeemCodeDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberRedeemCodeBatchDTO generateRedeemCodes(GenerateMemberRedeemCodeCommand command) {
        if (command.getGenerateCount() == null || command.getGenerateCount() < 1 || command.getGenerateCount() > 500) {
            throw new ApiException(ErrorCode.Business.MEMBER_REDEEM_CODE_GENERATE_COUNT_INVALID);
        }
        MemberLevelEntity memberLevelEntity = loadEnabledMemberLevel(command.getMemberLevelId());
        String batchNo = "BATCH" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
        List<MemberRedeemCodeEntity> saveEntities = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        for (int index = 0; index < command.getGenerateCount(); index++) {
            String code = RandomUtil.randomStringUpper(12);
            codes.add(code);
            MemberRedeemCodeEntity entity = new MemberRedeemCodeEntity();
            entity.setBatchNo(batchNo);
            entity.setRedeemCode(code);
            entity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
            entity.setLevelCodeSnapshot(memberLevelEntity.getLevelCode());
            entity.setLevelNameSnapshot(memberLevelEntity.getLevelName());
            entity.setPriceSnapshot(Objects.requireNonNullElse(memberLevelEntity.getPrice(), BigDecimal.ZERO));
            entity.setDurationDaysSnapshot(memberLevelEntity.getDurationDays());
            entity.setCodeStatus(MemberRedeemCodeStatusEnum.AVAILABLE.name());
            entity.setExpireTime(command.getExpireTime());
            entity.setRemark(command.getRemark());
            saveEntities.add(entity);
        }
        memberRedeemCodeService.saveBatch(saveEntities);

        MemberRedeemCodeBatchDTO dto = new MemberRedeemCodeBatchDTO();
        dto.setBatchNo(batchNo);
        dto.setMemberLevelId(memberLevelEntity.getMemberLevelId());
        dto.setLevelCode(memberLevelEntity.getLevelCode());
        dto.setLevelName(memberLevelEntity.getLevelName());
        dto.setGeneratedCount(saveEntities.size());
        dto.setExpireTime(command.getExpireTime());
        dto.setCodes(codes);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMemberRedeemCodes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<MemberRedeemCodeEntity> entities = memberRedeemCodeService.listByIds(ids);
        for (MemberRedeemCodeEntity entity : entities) {
            if (entity == null) {
                continue;
            }
            if (!Objects.equals(entity.getCodeStatus(), MemberRedeemCodeStatusEnum.AVAILABLE.name())) {
                throw new ApiException(ErrorCode.Business.MEMBER_REDEEM_CODE_STATUS_INVALID);
            }
        }
        memberRedeemCodeService.removeByIds(ids);
    }

    public List<MemberSubscriptionOrderDTO> getRecentUserMemberOrders(Long userId) {
        Map<Long, MemberLevelEntity> levelMap = listLevelMap(Collections.emptySet());
        return userMemberOrderService.lambdaQuery()
            .eq(UserMemberOrderEntity::getUserId, userId)
            .orderByDesc(UserMemberOrderEntity::getUserMemberOrderId)
            .page(new Page<>(1, 10))
            .getRecords()
            .stream()
            .map(entity -> buildOrderDTO(entity, levelMap.get(entity.getMemberLevelId())))
            .collect(Collectors.toList());
    }

    private MemberFeatureEntity loadMemberFeature(Long memberFeatureId) {
        MemberFeatureEntity entity = memberFeatureService.getById(memberFeatureId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.MEMBER_FEATURE_NOT_FOUND);
        }
        return entity;
    }

    private MemberLevelEntity loadMemberLevel(Long memberLevelId) {
        MemberLevelEntity entity = memberLevelService.getById(memberLevelId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberLevelId, "会员等级");
        }
        return entity;
    }

    private MemberLevelEntity loadEnabledMemberLevel(Long memberLevelId) {
        MemberLevelEntity entity = loadMemberLevel(memberLevelId);
        if (!Objects.equals(entity.getStatus(), StatusEnum.ENABLE.getValue())) {
            throw new ApiException(ErrorCode.Business.MEMBER_LEVEL_DISABLED);
        }
        return entity;
    }

    private HealthAppUserEntity loadAppUser(Long userId) {
        HealthAppUserEntity entity = healthAppUserService.getById(userId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.APP_USER_NON_EXIST);
        }
        return entity;
    }

    private void validateMemberFeatureCodeDuplicated(String featureCode, Long excludeId) {
        if (StrUtil.isBlank(featureCode)) {
            return;
        }
        boolean duplicated = memberFeatureService.lambdaQuery()
            .eq(MemberFeatureEntity::getFeatureCode, featureCode.trim().toUpperCase())
            .ne(excludeId != null, MemberFeatureEntity::getMemberFeatureId, excludeId)
            .exists();
        if (duplicated) {
            throw new ApiException(ErrorCode.Business.MEMBER_FEATURE_CODE_IS_NOT_UNIQUE);
        }
    }

    private void validateMemberLevelCodeDuplicated(String levelCode, Long excludeId) {
        if (StrUtil.isBlank(levelCode)) {
            return;
        }
        boolean duplicated = memberLevelService.lambdaQuery()
            .eq(MemberLevelEntity::getLevelCode, levelCode.trim().toUpperCase())
            .ne(excludeId != null, MemberLevelEntity::getMemberLevelId, excludeId)
            .exists();
        if (duplicated) {
            throw new ApiException(ErrorCode.Business.MEMBER_LEVEL_CODE_IS_NOT_UNIQUE);
        }
    }

    private void fillMemberFeatureDefaultFields(MemberFeatureEntity entity) {
        entity.setFeatureCode(StrUtil.blankToDefault(StrUtil.upperFirst(StrUtil.trim(entity.getFeatureCode())), entity.getFeatureCode()));
        entity.setFeatureCode(StrUtil.blankToDefault(entity.getFeatureCode(), null));
        if (entity.getFeatureCode() != null) {
            entity.setFeatureCode(entity.getFeatureCode().trim().toUpperCase());
        }
        entity.setFeatureSort(entity.getFeatureSort() == null ? 0 : entity.getFeatureSort());
        entity.setFreeEnabled(entity.getFreeEnabled() == null ? YesOrNoEnum.NO.getValue() : entity.getFreeEnabled());
        entity.setStatus(entity.getStatus() == null ? StatusEnum.ENABLE.getValue() : entity.getStatus());
        entity.setIsBuiltin(entity.getIsBuiltin() == null ? YesOrNoEnum.NO.getValue() : entity.getIsBuiltin());
        entity.setFreeLimitValue(normalizeLimitValue(entity.getFeatureType(), entity.getFreeEnabled(), entity.getFreeLimitValue()));
    }

    private void validateMemberFeatureFields(MemberFeatureEntity entity) {
        if (MemberFeatureTypeEnum.QUOTA.name().equals(entity.getFeatureType())
            && StrUtil.isBlank(entity.getQuotaPeriodType())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "次数型权益必须填写周期类型");
        }
    }

    private void fillMemberLevelDefaultFields(MemberLevelEntity entity) {
        if (entity.getLevelCode() != null) {
            entity.setLevelCode(entity.getLevelCode().trim().toUpperCase());
        }
        entity.setLevelSort(entity.getLevelSort() == null ? 0 : entity.getLevelSort());
        entity.setPrice(entity.getPrice() == null ? BigDecimal.ZERO : entity.getPrice());
        entity.setStatus(entity.getStatus() == null ? StatusEnum.ENABLE.getValue() : entity.getStatus());
    }

    private Integer normalizeLimitValue(String featureType, Integer enabled, Integer rawLimitValue) {
        if (!Objects.equals(enabled, YesOrNoEnum.YES.getValue())) {
            return null;
        }
        if (MemberFeatureTypeEnum.SWITCH.name().equals(featureType)) {
            return null;
        }
        return rawLimitValue;
    }

    private Map<Long, MemberLevelEntity> listLevelMap(Set<Long> levelIds) {
        List<MemberLevelEntity> levelEntities = levelIds == null || levelIds.isEmpty()
            ? memberLevelService.list()
            : memberLevelService.listByIds(levelIds);
        return levelEntities.stream().collect(Collectors.toMap(MemberLevelEntity::getMemberLevelId, entity -> entity, (left, right) -> left));
    }

    private Map<Long, HealthAppUserEntity> listAppUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return healthAppUserService.listByIds(userIds).stream()
            .collect(Collectors.toMap(HealthAppUserEntity::getUserId, entity -> entity, (left, right) -> left));
    }

    private UserMemberDTO buildUserMemberDTO(UserMemberEntity userMemberEntity, MemberLevelEntity memberLevelEntity,
        HealthAppUserEntity appUserEntity) {
        UserMemberDTO dto = new UserMemberDTO();
        dto.setUserMemberId(userMemberEntity.getUserMemberId());
        dto.setUserId(userMemberEntity.getUserId());
        dto.setMobile(appUserEntity == null ? null : appUserEntity.getMobile());
        dto.setNickname(appUserEntity == null ? null : appUserEntity.getNickname());
        dto.setMemberLevelId(userMemberEntity.getMemberLevelId());
        dto.setLevelCode(memberLevelEntity == null ? null : memberLevelEntity.getLevelCode());
        dto.setLevelName(memberLevelEntity == null ? null : memberLevelEntity.getLevelName());
        dto.setEffectiveStartTime(userMemberEntity.getEffectiveStartTime());
        dto.setEffectiveEndTime(userMemberEntity.getEffectiveEndTime());
        dto.setStatus(userMemberEntity.getStatus());
        dto.setSourceType(userMemberEntity.getSourceType());
        dto.setSourceId(userMemberEntity.getSourceId());
        dto.setRemark(userMemberEntity.getRemark());
        return dto;
    }

    private MemberSubscriptionOrderDTO buildOrderDTO(UserMemberOrderEntity entity, MemberLevelEntity memberLevelEntity) {
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
    }

    private UserMemberEntity getCurrentUserMember(Long userId) {
        return userMemberService.lambdaQuery().eq(UserMemberEntity::getUserId, userId).one();
    }

    public Date resolveEndTime(Date startTime, Integer durationDays) {
        if (startTime == null || durationDays == null || durationDays <= 0) {
            return null;
        }
        return DateUtil.offsetDay(startTime, durationDays);
    }

    public UserMemberOrderEntity createMemberOrder(Long userId, MemberLevelEntity memberLevelEntity, String orderType,
        String sourceType, BigDecimal amount, Date effectiveStartTime, Date effectiveEndTime, String remark, Long subscriptionId) {
        UserMemberOrderEntity orderEntity = new UserMemberOrderEntity();
        orderEntity.setOrderNo("MEM" + IdUtil.getSnowflakeNextIdStr());
        orderEntity.setSubscriptionId(subscriptionId);
        orderEntity.setUserId(userId);
        orderEntity.setMemberLevelId(memberLevelEntity.getMemberLevelId());
        orderEntity.setOrderType(orderType);
        orderEntity.setOrderStatus(UserMemberOrderStatusEnum.SUCCESS.name());
        orderEntity.setSourceType(sourceType);
        orderEntity.setOrderAmount(amount == null ? BigDecimal.ZERO : amount);
        orderEntity.setPayTime(new Date());
        orderEntity.setEffectiveStartTime(effectiveStartTime);
        orderEntity.setEffectiveEndTime(effectiveEndTime);
        orderEntity.setRemark(remark);
        userMemberOrderService.save(orderEntity);
        return orderEntity;
    }
}

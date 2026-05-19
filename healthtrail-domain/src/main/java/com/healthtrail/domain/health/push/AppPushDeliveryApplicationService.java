package com.healthtrail.domain.health.push;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.health.AppPushPlatformEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.healthtrail.domain.health.message.db.HealthAppMessageService;
import com.healthtrail.domain.health.push.db.HealthAppPushDeliveryLogEntity;
import com.healthtrail.domain.health.push.db.HealthAppPushDeliveryLogService;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryAdminDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryAdminDetailDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryPlatformStatisticsDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryStatisticsDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushStatisticsBucketDTO;
import com.healthtrail.domain.health.push.query.HealthAppPushDeliveryAdminQuery;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * App Push 设备级派发审计应用服务。
 *
 * <p>该服务同时承接两件事：
 * 1. 在真实/模拟发送结束后把设备级结果落库；
 * 2. 为后台提供设备级推送审计的列表、详情、统计查询。
 *
 * <p>这样“写入侧”和“读取侧”共用同一套口径：
 * 1. 失败原因分类；
 * 2. 重试编号规则；
 * 3. 场景名称与状态名称转换；
 *
 * <p>避免发送链路和后台审计展示各自维护一套解释逻辑。
 */
@Service
public class AppPushDeliveryApplicationService {

    private static final int DEFAULT_RECENT_DEVICE_DELIVERY_LIMIT = 20;

    /** Push派发日志数据库服务 */
    private final HealthAppPushDeliveryLogService deliveryLogService;

    /** App消息数据库服务 */
    private final HealthAppMessageService healthAppMessageService;

    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;

    public AppPushDeliveryApplicationService(HealthAppPushDeliveryLogService deliveryLogService,
        HealthAppMessageService healthAppMessageService, HealthAppUserService healthAppUserService) {
        this.deliveryLogService = deliveryLogService;
        this.healthAppMessageService = healthAppMessageService;
        this.healthAppUserService = healthAppUserService;
    }

    /**
     * 按本次命中的目标设备批量写入设备级派发审计。
     *
     * <p>这里采用“一次批量发送 -> 多条设备级快照”的策略：
     * 1. 同一消息如果命中 3 台设备，就写 3 条审计；
     * 2. 如果网关只有批量结果，没有逐设备回执，也先把批量结果回填到每台目标设备；
     * 3. 如果后续网关升级为逐设备回执，这个方法可以继续扩展而不影响后台查询接口。
     */
    public void recordBatchDelivery(Long messageId, Long ownerUserId, Long memberId, String businessScene, Long businessId,
        List<HealthAppDeviceEntity> targetDevices, String sendChannel, boolean success, String resultMessage,
        String vendorCode, String vendorMessage) {
        if (targetDevices == null || targetDevices.isEmpty()) {
            return;
        }

        Date sendTime = new Date();
        Integer retryNo = resolveNextRetryNo(messageId);
        String failureReasonCategory = AppPushAuditSupport.resolveFailureReasonCategory(success, resultMessage);
        List<HealthAppPushDeliveryLogEntity> entities = new ArrayList<>(targetDevices.size());
        for (HealthAppDeviceEntity device : targetDevices) {
            HealthAppPushDeliveryLogEntity entity = new HealthAppPushDeliveryLogEntity();
            entity.setMessageId(messageId);
            entity.setOwnerUserId(ownerUserId);
            entity.setMemberId(memberId);
            entity.setBusinessScene(AppPushAuditSupport.limitLength(businessScene, 40));
            entity.setBusinessId(businessId);
            entity.setDeviceId(device.getDeviceId());
            entity.setDeviceCode(AppPushAuditSupport.limitLength(device.getDeviceCode(), 64));
            entity.setPushPlatform(AppPushAuditSupport.limitLength(device.getPushPlatform(), 20));
            entity.setDeviceStatusSnapshot(device.getStatus());
            entity.setDeviceTokenMasked(AppPushAuditSupport.limitLength(
                AppPushAuditSupport.maskDeviceToken(device.getDeviceToken()), 40));
            entity.setSendChannel(AppPushAuditSupport.limitLength(sendChannel, 30));
            entity.setSendStatus(success
                ? HealthAppMessageSendStatusEnum.SUCCESS.getValue()
                : HealthAppMessageSendStatusEnum.FAILED.getValue());
            entity.setFailureReasonCategory(AppPushAuditSupport.limitLength(failureReasonCategory, 30));
            entity.setFailureReasonMessage(AppPushAuditSupport.limitLength(resultMessage, 255));
            entity.setVendorCode(AppPushAuditSupport.limitLength(vendorCode, 50));
            entity.setVendorMessage(AppPushAuditSupport.limitLength(
                StrUtil.blankToDefault(vendorMessage, resultMessage), 255));
            entity.setRetryNo(retryNo);
            entity.setSendTime(sendTime);
            entities.add(entity);
        }
        deliveryLogService.saveBatch(entities);
    }

    /**
     * 后台分页查询设备级派发审计列表。
     */
    public PageDTO<HealthAppPushDeliveryAdminDTO> getDeliveryList(HealthAppPushDeliveryAdminQuery query) {
        QueryWrapper<HealthAppPushDeliveryLogEntity> queryWrapper = buildDeliveryQueryWrapper(query);
        if (queryWrapper == null) {
            return new PageDTO<>(Collections.emptyList(), 0L);
        }

        Page<HealthAppPushDeliveryLogEntity> page = deliveryLogService.page(query.toPage(), queryWrapper);
        Map<Long, HealthAppMessageEntity> messageMap = loadMessageMap(page.getRecords());
        Map<Long, HealthAppUserEntity> userMap = loadUserMapByOwnerUserIds(
            page.getRecords().stream().map(HealthAppPushDeliveryLogEntity::getOwnerUserId).collect(Collectors.toSet()));
        List<HealthAppPushDeliveryAdminDTO> records = page.getRecords().stream()
            .map(entity -> buildDeliveryDTO(entity, messageMap.get(entity.getMessageId()), userMap.get(entity.getOwnerUserId())))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 后台查询设备级派发审计详情。
     */
    public HealthAppPushDeliveryAdminDetailDTO getDeliveryDetail(Long deliveryId) {
        HealthAppPushDeliveryLogEntity entity = deliveryLogService.getById(deliveryId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, deliveryId, "Push派发审计");
        }

        HealthAppMessageEntity messageEntity = entity.getMessageId() == null
            ? null
            : healthAppMessageService.getById(entity.getMessageId());
        HealthAppUserEntity userEntity = entity.getOwnerUserId() == null
            ? null
            : healthAppUserService.getById(entity.getOwnerUserId());

        HealthAppPushDeliveryAdminDetailDTO detailDTO = new HealthAppPushDeliveryAdminDetailDTO();
        copyToDetailDTO(detailDTO, buildDeliveryDTO(entity, messageEntity, userEntity));
        detailDTO.setMessageTitle(messageEntity == null ? null : messageEntity.getMessageTitle());
        detailDTO.setMessageContent(messageEntity == null ? null : messageEntity.getMessageContent());
        detailDTO.setPayloadJson(messageEntity == null ? null : messageEntity.getPayloadJson());
        detailDTO.setSameBatchDeliveries(loadSameBatchDeliveries(entity));
        return detailDTO;
    }

    /**
     * 后台统计设备级派发审计。
     */
    public HealthAppPushDeliveryStatisticsDTO getDeliveryStatistics(HealthAppPushDeliveryAdminQuery query) {
        QueryWrapper<HealthAppPushDeliveryLogEntity> queryWrapper = buildDeliveryQueryWrapper(query);
        List<HealthAppPushDeliveryLogEntity> deliveryLogs = queryWrapper == null
            ? Collections.emptyList()
            : deliveryLogService.list(queryWrapper);

        HealthAppPushDeliveryStatisticsDTO dto = new HealthAppPushDeliveryStatisticsDTO();
        dto.setTotalDeliveryCount((long) deliveryLogs.size());
        dto.setSuccessDeliveryCount(countBySendStatus(deliveryLogs, HealthAppMessageSendStatusEnum.SUCCESS.getValue()));
        dto.setFailedDeliveryCount(countBySendStatus(deliveryLogs, HealthAppMessageSendStatusEnum.FAILED.getValue()));
        dto.setUniqueMessageCount(deliveryLogs.stream()
            .map(HealthAppPushDeliveryLogEntity::getMessageId)
            .filter(Objects::nonNull)
            .distinct()
            .count());
        dto.setTodayDeliveryCount(countTodayDeliveries(deliveryLogs));
        dto.setTodayDeviceSuccessCount(countTodayDeliveriesByStatus(deliveryLogs, HealthAppMessageSendStatusEnum.SUCCESS.getValue()));
        dto.setTodayDeviceFailedCount(countTodayDeliveriesByStatus(deliveryLogs, HealthAppMessageSendStatusEnum.FAILED.getValue()));
        dto.setTodayMessageSuccessCount(countTodayMessageSuccess(query));
        dto.setNoActiveDeviceFailedCount(countNoActiveDeviceFailures(query));
        dto.setFailureReasonDistributions(buildFailureReasonDistributions(deliveryLogs));
        dto.setPlatformStatistics(buildPlatformStatistics(deliveryLogs));
        return dto;
    }

    /**
     * 查询某台设备最近的推送明细。
     *
     * <p>该方法主要给设备详情页复用，避免设备管理服务再重复写一套 DTO 组装逻辑。
     */
    public List<HealthAppPushDeliveryAdminDTO> getRecentDeliveriesByDeviceId(Long deviceId, Integer limit) {
        if (deviceId == null) {
            return Collections.emptyList();
        }
        int safeLimit = limit == null || limit <= 0 ? DEFAULT_RECENT_DEVICE_DELIVERY_LIMIT : limit;
        List<HealthAppPushDeliveryLogEntity> entities = deliveryLogService.lambdaQuery()
            .eq(HealthAppPushDeliveryLogEntity::getDeviceId, deviceId)
            .orderByDesc(HealthAppPushDeliveryLogEntity::getSendTime)
            .orderByDesc(HealthAppPushDeliveryLogEntity::getDeliveryId)
            .last("limit " + safeLimit)
            .list();
        Map<Long, HealthAppMessageEntity> messageMap = loadMessageMap(entities);
        Map<Long, HealthAppUserEntity> userMap = loadUserMapByOwnerUserIds(
            entities.stream().map(HealthAppPushDeliveryLogEntity::getOwnerUserId).collect(Collectors.toSet()));
        return entities.stream()
            .map(entity -> buildDeliveryDTO(entity, messageMap.get(entity.getMessageId()), userMap.get(entity.getOwnerUserId())))
            .collect(Collectors.toList());
    }

    private Integer resolveNextRetryNo(Long messageId) {
        if (messageId == null) {
            return 1;
        }
        return deliveryLogService.lambdaQuery()
            .eq(HealthAppPushDeliveryLogEntity::getMessageId, messageId)
            .list()
            .stream()
            .map(HealthAppPushDeliveryLogEntity::getRetryNo)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .map(maxRetryNo -> maxRetryNo + 1)
            .orElse(1);
    }

    private QueryWrapper<HealthAppPushDeliveryLogEntity> buildDeliveryQueryWrapper(HealthAppPushDeliveryAdminQuery query) {
        QueryWrapper<HealthAppPushDeliveryLogEntity> queryWrapper = query.toQueryWrapper();

        Set<Long> ownerUserIds = resolveOwnerUserIds(query.getOwnerUserId(), query.getMobile());
        if (ownerUserIds != null) {
            if (ownerUserIds.isEmpty()) {
                return null;
            }
            queryWrapper.in("owner_user_id", ownerUserIds);
        }

        if (query.getMessageSendStatus() != null) {
            Set<Long> messageIds = healthAppMessageService.lambdaQuery()
                .eq(HealthAppMessageEntity::getSendStatus, query.getMessageSendStatus())
                .list()
                .stream()
                .map(HealthAppMessageEntity::getMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            if (messageIds.isEmpty()) {
                return null;
            }
            queryWrapper.in("message_id", messageIds);
        }
        return queryWrapper;
    }

    private Set<Long> resolveOwnerUserIds(Long ownerUserId, String mobile) {
        boolean hasUserFilter = ownerUserId != null || StrUtil.isNotBlank(mobile);
        if (!hasUserFilter) {
            return null;
        }
        return healthAppUserService.lambdaQuery()
            .eq(ownerUserId != null, HealthAppUserEntity::getUserId, ownerUserId)
            .like(StrUtil.isNotBlank(mobile), HealthAppUserEntity::getMobile, mobile)
            .list()
            .stream()
            .map(HealthAppUserEntity::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, HealthAppMessageEntity> loadMessageMap(Collection<HealthAppPushDeliveryLogEntity> entities) {
        Set<Long> messageIds = entities == null
            ? Collections.emptySet()
            : entities.stream()
                .map(HealthAppPushDeliveryLogEntity::getMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (messageIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return healthAppMessageService.listByIds(messageIds).stream()
            .collect(Collectors.toMap(HealthAppMessageEntity::getMessageId, item -> item, (left, right) -> left));
    }

    private Map<Long, HealthAppUserEntity> loadUserMapByOwnerUserIds(Collection<Long> ownerUserIds) {
        if (ownerUserIds == null || ownerUserIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return healthAppUserService.listByIds(ownerUserIds).stream()
            .collect(Collectors.toMap(HealthAppUserEntity::getUserId, item -> item, (left, right) -> left));
    }

    private HealthAppPushDeliveryAdminDTO buildDeliveryDTO(HealthAppPushDeliveryLogEntity entity,
        HealthAppMessageEntity messageEntity, HealthAppUserEntity userEntity) {
        HealthAppPushDeliveryAdminDTO dto = new HealthAppPushDeliveryAdminDTO();
        dto.setDeliveryId(entity.getDeliveryId());
        dto.setMessageId(entity.getMessageId());
        dto.setBusinessScene(entity.getBusinessScene());
        dto.setBusinessSceneName(resolveSceneName(entity.getBusinessScene()));
        dto.setBusinessId(entity.getBusinessId());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setOwnerUserMobile(userEntity == null ? null : userEntity.getMobile());
        dto.setOwnerUserNickname(userEntity == null ? null : userEntity.getNickname());
        dto.setMemberId(entity.getMemberId());
        dto.setMemberName(messageEntity == null ? null : messageEntity.getMemberNameSnapshot());
        dto.setDeviceId(entity.getDeviceId());
        dto.setDeviceCode(entity.getDeviceCode());
        dto.setPushPlatform(entity.getPushPlatform());
        dto.setDeviceStatusSnapshot(entity.getDeviceStatusSnapshot());
        dto.setDeviceStatusSnapshotName(AppPushAuditSupport.resolveDeviceStatusName(entity.getDeviceStatusSnapshot()));
        dto.setDeviceTokenMasked(entity.getDeviceTokenMasked());
        dto.setSendChannel(entity.getSendChannel());
        dto.setSendStatus(entity.getSendStatus());
        dto.setSendStatusName(resolveSendStatusName(entity.getSendStatus()));
        dto.setFailureReasonCategory(entity.getFailureReasonCategory());
        dto.setFailureReasonCategoryName(AppPushAuditSupport.resolveFailureReasonCategoryName(entity.getFailureReasonCategory()));
        dto.setFailureReasonMessage(entity.getFailureReasonMessage());
        dto.setVendorCode(entity.getVendorCode());
        dto.setVendorMessage(entity.getVendorMessage());
        dto.setRetryNo(entity.getRetryNo());
        dto.setSendTime(entity.getSendTime());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    private void copyToDetailDTO(HealthAppPushDeliveryAdminDetailDTO detailDTO, HealthAppPushDeliveryAdminDTO source) {
        detailDTO.setDeliveryId(source.getDeliveryId());
        detailDTO.setMessageId(source.getMessageId());
        detailDTO.setBusinessScene(source.getBusinessScene());
        detailDTO.setBusinessSceneName(source.getBusinessSceneName());
        detailDTO.setBusinessId(source.getBusinessId());
        detailDTO.setOwnerUserId(source.getOwnerUserId());
        detailDTO.setOwnerUserMobile(source.getOwnerUserMobile());
        detailDTO.setOwnerUserNickname(source.getOwnerUserNickname());
        detailDTO.setMemberId(source.getMemberId());
        detailDTO.setMemberName(source.getMemberName());
        detailDTO.setDeviceId(source.getDeviceId());
        detailDTO.setDeviceCode(source.getDeviceCode());
        detailDTO.setPushPlatform(source.getPushPlatform());
        detailDTO.setDeviceStatusSnapshot(source.getDeviceStatusSnapshot());
        detailDTO.setDeviceStatusSnapshotName(source.getDeviceStatusSnapshotName());
        detailDTO.setDeviceTokenMasked(source.getDeviceTokenMasked());
        detailDTO.setSendChannel(source.getSendChannel());
        detailDTO.setSendStatus(source.getSendStatus());
        detailDTO.setSendStatusName(source.getSendStatusName());
        detailDTO.setFailureReasonCategory(source.getFailureReasonCategory());
        detailDTO.setFailureReasonCategoryName(source.getFailureReasonCategoryName());
        detailDTO.setFailureReasonMessage(source.getFailureReasonMessage());
        detailDTO.setVendorCode(source.getVendorCode());
        detailDTO.setVendorMessage(source.getVendorMessage());
        detailDTO.setRetryNo(source.getRetryNo());
        detailDTO.setSendTime(source.getSendTime());
        detailDTO.setCreateTime(source.getCreateTime());
    }

    private List<HealthAppPushDeliveryAdminDTO> loadSameBatchDeliveries(HealthAppPushDeliveryLogEntity entity) {
        if (entity.getMessageId() == null || entity.getRetryNo() == null) {
            return Collections.emptyList();
        }
        List<HealthAppPushDeliveryLogEntity> entities = deliveryLogService.lambdaQuery()
            .eq(HealthAppPushDeliveryLogEntity::getMessageId, entity.getMessageId())
            .eq(HealthAppPushDeliveryLogEntity::getRetryNo, entity.getRetryNo())
            .orderByDesc(HealthAppPushDeliveryLogEntity::getDeliveryId)
            .list();
        Map<Long, HealthAppMessageEntity> messageMap = loadMessageMap(entities);
        Map<Long, HealthAppUserEntity> userMap = loadUserMapByOwnerUserIds(
            entities.stream().map(HealthAppPushDeliveryLogEntity::getOwnerUserId).collect(Collectors.toSet()));
        return entities.stream()
            .filter(item -> !Objects.equals(item.getDeliveryId(), entity.getDeliveryId()))
            .map(item -> buildDeliveryDTO(item, messageMap.get(item.getMessageId()), userMap.get(item.getOwnerUserId())))
            .collect(Collectors.toList());
    }

    private Long countBySendStatus(List<HealthAppPushDeliveryLogEntity> deliveryLogs, Integer sendStatus) {
        return deliveryLogs.stream()
            .filter(item -> Objects.equals(item.getSendStatus(), sendStatus))
            .count();
    }

    private Long countTodayDeliveries(List<HealthAppPushDeliveryLogEntity> deliveryLogs) {
        Date todayBegin = DateUtil.beginOfDay(new Date());
        return deliveryLogs.stream()
            .filter(item -> item.getSendTime() != null && !item.getSendTime().before(todayBegin))
            .count();
    }

    private Long countTodayDeliveriesByStatus(List<HealthAppPushDeliveryLogEntity> deliveryLogs, Integer sendStatus) {
        Date todayBegin = DateUtil.beginOfDay(new Date());
        return deliveryLogs.stream()
            .filter(item -> item.getSendTime() != null && !item.getSendTime().before(todayBegin))
            .filter(item -> Objects.equals(item.getSendStatus(), sendStatus))
            .count();
    }

    private Long countTodayMessageSuccess(HealthAppPushDeliveryAdminQuery query) {
        QueryWrapper<HealthAppMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("send_status", HealthAppMessageSendStatusEnum.SUCCESS.getValue())
            .ge("send_time", DateUtil.beginOfDay(new Date()));

        Set<Long> ownerUserIds = resolveOwnerUserIds(query.getOwnerUserId(), query.getMobile());
        if (ownerUserIds != null) {
            if (ownerUserIds.isEmpty()) {
                return 0L;
            }
            queryWrapper.in("owner_user_id", ownerUserIds);
        }
        queryWrapper.eq(StrUtil.isNotBlank(query.getBusinessScene()), "business_scene", query.getBusinessScene())
            .eq(query.getBusinessId() != null, "business_id", query.getBusinessId())
            .eq(query.getMessageId() != null, "message_id", query.getMessageId());
        return healthAppMessageService.count(queryWrapper);
    }

    private Long countNoActiveDeviceFailures(HealthAppPushDeliveryAdminQuery query) {
        if (query.getDeviceId() != null || StrUtil.isNotBlank(query.getDeviceCode())
            || StrUtil.isNotBlank(query.getPushPlatform()) || query.getDeviceSendStatus() != null
            || StrUtil.isNotBlank(query.getFailureReasonCategory())) {
            return 0L;
        }

        QueryWrapper<HealthAppMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("send_status", HealthAppMessageSendStatusEnum.FAILED.getValue())
            .eq("send_result_message", AppPushAuditSupport.NO_ACTIVE_DEVICE_TOKEN_MESSAGE);

        Set<Long> ownerUserIds = resolveOwnerUserIds(query.getOwnerUserId(), query.getMobile());
        if (ownerUserIds != null) {
            if (ownerUserIds.isEmpty()) {
                return 0L;
            }
            queryWrapper.in("owner_user_id", ownerUserIds);
        }
        queryWrapper.eq(StrUtil.isNotBlank(query.getBusinessScene()), "business_scene", query.getBusinessScene())
            .eq(query.getBusinessId() != null, "business_id", query.getBusinessId())
            .eq(query.getMessageId() != null, "message_id", query.getMessageId());
        if (query.getBeginTime() != null) {
            queryWrapper.ge("send_time", DateUtil.beginOfDay(query.getBeginTime()));
        }
        if (query.getEndTime() != null) {
            queryWrapper.le("send_time", DateUtil.endOfDay(query.getEndTime()));
        }
        return healthAppMessageService.count(queryWrapper);
    }

    private List<HealthAppPushStatisticsBucketDTO> buildFailureReasonDistributions(List<HealthAppPushDeliveryLogEntity> deliveryLogs) {
        Map<String, Long> grouped = deliveryLogs.stream()
            .filter(item -> StrUtil.isNotBlank(item.getFailureReasonCategory()))
            .collect(Collectors.groupingBy(HealthAppPushDeliveryLogEntity::getFailureReasonCategory,
                LinkedHashMap::new, Collectors.counting()));
        List<HealthAppPushStatisticsBucketDTO> buckets = new ArrayList<>();
        grouped.forEach((code, count) -> {
            HealthAppPushStatisticsBucketDTO bucketDTO = new HealthAppPushStatisticsBucketDTO();
            bucketDTO.setCode(code);
            bucketDTO.setName(AppPushAuditSupport.resolveFailureReasonCategoryName(code));
            bucketDTO.setCount(count);
            buckets.add(bucketDTO);
        });
        buckets.sort(Comparator.comparing(HealthAppPushStatisticsBucketDTO::getCount, Comparator.nullsLast(Long::compareTo)).reversed());
        return buckets;
    }

    private List<HealthAppPushDeliveryPlatformStatisticsDTO> buildPlatformStatistics(List<HealthAppPushDeliveryLogEntity> deliveryLogs) {
        Map<String, List<HealthAppPushDeliveryLogEntity>> grouped = deliveryLogs.stream()
            .collect(Collectors.groupingBy(item -> StrUtil.blankToDefault(item.getPushPlatform(), "UNKNOWN"),
                LinkedHashMap::new, Collectors.toList()));
        List<HealthAppPushDeliveryPlatformStatisticsDTO> results = new ArrayList<>();
        grouped.forEach((platform, items) -> {
            HealthAppPushDeliveryPlatformStatisticsDTO dto = new HealthAppPushDeliveryPlatformStatisticsDTO();
            dto.setPushPlatform(platform);
            dto.setPushPlatformName(resolvePlatformName(platform));
            dto.setSuccessCount(items.stream()
                .filter(item -> Objects.equals(item.getSendStatus(), HealthAppMessageSendStatusEnum.SUCCESS.getValue()))
                .count());
            dto.setFailedCount(items.stream()
                .filter(item -> Objects.equals(item.getSendStatus(), HealthAppMessageSendStatusEnum.FAILED.getValue()))
                .count());
            dto.setTotalCount((long) items.size());
            results.add(dto);
        });
        results.sort(Comparator.comparing(HealthAppPushDeliveryPlatformStatisticsDTO::getTotalCount,
            Comparator.nullsLast(Long::compareTo)).reversed());
        return results;
    }

    private String resolveSceneName(String businessScene) {
        HealthAppMessageSceneEnum sceneEnum = HealthAppMessageSceneEnum.fromValue(businessScene);
        return sceneEnum == null ? businessScene : sceneEnum.getDescription();
    }

    private String resolveSendStatusName(Integer sendStatus) {
        if (sendStatus == null) {
            return null;
        }
        for (HealthAppMessageSendStatusEnum item : HealthAppMessageSendStatusEnum.values()) {
            if (Objects.equals(item.getValue(), sendStatus)) {
                return item.description();
            }
        }
        return String.valueOf(sendStatus);
    }

    private String resolvePlatformName(String pushPlatform) {
        if (StrUtil.isBlank(pushPlatform)) {
            return pushPlatform;
        }
        for (AppPushPlatformEnum item : AppPushPlatformEnum.values()) {
            if (Objects.equals(item.getValue(), pushPlatform)) {
                return item.description();
            }
        }
        return pushPlatform;
    }
}

package com.healthtrail.domain.health.push;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.AppPushPlatformEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import com.healthtrail.domain.health.device.db.HealthAppDeviceService;
import com.healthtrail.domain.health.push.db.HealthAppPushDeliveryLogEntity;
import com.healthtrail.domain.health.push.db.HealthAppPushDeliveryLogService;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeliveryAdminDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeviceAdminDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeviceAdminDetailDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushDeviceStatisticsDTO;
import com.healthtrail.domain.health.push.dto.HealthAppPushStatisticsBucketDTO;
import com.healthtrail.domain.health.push.query.HealthAppPushDeviceAdminQuery;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * 后台 App 设备管理应用服务。
 *
 * <p>该服务聚焦“当前有哪些设备可推、最近是否活跃、最近推送表现如何”，
 * 是后台排查“用户为什么收不到提醒”时的第一站。
 */
@Service
public class AppPushDeviceAdminApplicationService {

    private static final int DEFAULT_RECENT_DEVICE_DELIVERY_LIMIT = 20;

    /** App设备数据库服务 */
    private final HealthAppDeviceService healthAppDeviceService;

    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;

    /** Push派发日志数据库服务 */
    private final HealthAppPushDeliveryLogService deliveryLogService;

    /** Push派发应用服务 */
    private final AppPushDeliveryApplicationService appPushDeliveryApplicationService;

    public AppPushDeviceAdminApplicationService(HealthAppDeviceService healthAppDeviceService,
        HealthAppUserService healthAppUserService, HealthAppPushDeliveryLogService deliveryLogService,
        AppPushDeliveryApplicationService appPushDeliveryApplicationService) {
        this.healthAppDeviceService = healthAppDeviceService;
        this.healthAppUserService = healthAppUserService;
        this.deliveryLogService = deliveryLogService;
        this.appPushDeliveryApplicationService = appPushDeliveryApplicationService;
    }

    /**
     * 后台分页查询设备管理列表。
     */
    public PageDTO<HealthAppPushDeviceAdminDTO> getDeviceList(HealthAppPushDeviceAdminQuery query) {
        QueryWrapper<HealthAppDeviceEntity> queryWrapper = buildDeviceQueryWrapper(query);
        if (queryWrapper == null) {
            return new PageDTO<>(Collections.emptyList(), 0L);
        }

        Page<HealthAppDeviceEntity> page = healthAppDeviceService.page(query.toPage(), queryWrapper);
        Map<Long, HealthAppUserEntity> userMap = loadUserMap(page.getRecords());
        Map<Long, List<HealthAppPushDeliveryLogEntity>> deliveryMap = loadDeliveryGroupMap(page.getRecords());
        Date recentSevenDays = DateUtil.offsetDay(new Date(), -7);

        List<HealthAppPushDeviceAdminDTO> records = page.getRecords().stream()
            .map(device -> buildDeviceDTO(device, userMap.get(device.getOwnerUserId()),
                deliveryMap.getOrDefault(device.getDeviceId(), Collections.emptyList()), recentSevenDays))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 后台查询设备统计概览。
     */
    public HealthAppPushDeviceStatisticsDTO getDeviceStatistics(HealthAppPushDeviceAdminQuery query) {
        QueryWrapper<HealthAppDeviceEntity> queryWrapper = buildDeviceQueryWrapper(query);
        List<HealthAppDeviceEntity> devices = queryWrapper == null
            ? Collections.emptyList()
            : healthAppDeviceService.list(queryWrapper);

        Date todayBegin = DateUtil.beginOfDay(new Date());
        Date sevenDaysAgo = DateUtil.offsetDay(new Date(), -7);
        Date thirtyDaysAgo = DateUtil.offsetDay(new Date(), -30);

        HealthAppPushDeviceStatisticsDTO dto = new HealthAppPushDeviceStatisticsDTO();
        dto.setEnabledDeviceCount(devices.stream()
            .filter(item -> Objects.equals(item.getStatus(), StatusEnum.ENABLE.getValue()))
            .count());
        dto.setTodayActiveDeviceCount(devices.stream()
            .filter(item -> item.getLastActiveTime() != null && !item.getLastActiveTime().before(todayBegin))
            .count());
        dto.setRecent7DayActiveDeviceCount(devices.stream()
            .filter(item -> item.getLastActiveTime() != null && !item.getLastActiveTime().before(sevenDaysAgo))
            .count());
        dto.setStale30DayDeviceCount(devices.stream()
            .filter(item -> Objects.equals(item.getStatus(), StatusEnum.ENABLE.getValue()))
            .filter(item -> item.getLastActiveTime() == null || item.getLastActiveTime().before(thirtyDaysAgo))
            .count());
        dto.setDisabledDeviceCount(devices.stream()
            .filter(item -> Objects.equals(item.getStatus(), StatusEnum.DISABLE.getValue()))
            .count());
        dto.setPlatformDistributions(buildPlatformDistributions(devices));
        return dto;
    }

    /**
     * 后台查询设备详情。
     */
    public HealthAppPushDeviceAdminDetailDTO getDeviceDetail(Long deviceId) {
        HealthAppDeviceEntity deviceEntity = healthAppDeviceService.getById(deviceId);
        if (deviceEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, deviceId, "App设备");
        }

        List<HealthAppPushDeliveryLogEntity> deliveryLogs = deliveryLogService.lambdaQuery()
            .eq(HealthAppPushDeliveryLogEntity::getDeviceId, deviceId)
            .orderByDesc(HealthAppPushDeliveryLogEntity::getSendTime)
            .orderByDesc(HealthAppPushDeliveryLogEntity::getDeliveryId)
            .list();
        Date recentSevenDays = DateUtil.offsetDay(new Date(), -7);
        HealthAppPushDeviceAdminDTO baseDTO = buildDeviceDTO(deviceEntity,
            deviceEntity.getOwnerUserId() == null ? null : healthAppUserService.getById(deviceEntity.getOwnerUserId()),
            deliveryLogs, recentSevenDays);

        HealthAppPushDeviceAdminDetailDTO detailDTO = new HealthAppPushDeviceAdminDetailDTO();
        copyToDetailDTO(detailDTO, baseDTO);
        detailDTO.setLastFailureReasonMessage(baseDTO.getLastPushFailureReasonMessage());
        detailDTO.setRecent7DayPushTotalCount(
            Objects.requireNonNullElse(baseDTO.getRecent7DayPushSuccessCount(), 0L)
                + Objects.requireNonNullElse(baseDTO.getRecent7DayPushFailedCount(), 0L));
        detailDTO.setRecent7DayPushSuccessRate(buildSuccessRate(
            baseDTO.getRecent7DayPushSuccessCount(), detailDTO.getRecent7DayPushTotalCount()));
        detailDTO.setRecentDeliveries(appPushDeliveryApplicationService.getRecentDeliveriesByDeviceId(
            deviceId, DEFAULT_RECENT_DEVICE_DELIVERY_LIMIT));
        return detailDTO;
    }

    /**
     * 后台手动停用设备。
     *
     * <p>一期只开放停用，不开放启用，是为了优先保证“坏设备不再继续占用推送资源”。
     * 如果后续需要恢复设备，可在二期增加更严格的校验与操作日志。
     */
    public void disableDevice(Long deviceId) {
        HealthAppDeviceEntity deviceEntity = healthAppDeviceService.getById(deviceId);
        if (deviceEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, deviceId, "App设备");
        }
        if (Objects.equals(deviceEntity.getStatus(), StatusEnum.DISABLE.getValue())) {
            return;
        }
        deviceEntity.setStatus(StatusEnum.DISABLE.getValue());
        healthAppDeviceService.updateById(deviceEntity);
    }

    private QueryWrapper<HealthAppDeviceEntity> buildDeviceQueryWrapper(HealthAppPushDeviceAdminQuery query) {
        QueryWrapper<HealthAppDeviceEntity> queryWrapper = query.toQueryWrapper();

        Set<Long> ownerUserIds = resolveOwnerUserIds(query.getOwnerUserId(), query.getMobile(), query.getNickname());
        if (ownerUserIds != null) {
            if (ownerUserIds.isEmpty()) {
                return null;
            }
            queryWrapper.in("owner_user_id", ownerUserIds);
        }

        if (StrUtil.isNotBlank(query.getKeyword())) {
            Set<Long> keywordUserIds = resolveOwnerUserIdsByKeyword(query.getKeyword());
            queryWrapper.and(wrapper -> {
                boolean hasUserMatches = keywordUserIds != null && !keywordUserIds.isEmpty();
                if (hasUserMatches) {
                    wrapper.in("owner_user_id", keywordUserIds).or();
                }
                wrapper.like("device_code", query.getKeyword())
                    .or()
                    .like("device_model", query.getKeyword())
                    .or()
                    .like("manufacturer", query.getKeyword());
            });
        }
        return queryWrapper;
    }

    private Set<Long> resolveOwnerUserIds(Long ownerUserId, String mobile, String nickname) {
        boolean hasUserFilter = ownerUserId != null || StrUtil.isNotBlank(mobile) || StrUtil.isNotBlank(nickname);
        if (!hasUserFilter) {
            return null;
        }
        return healthAppUserService.lambdaQuery()
            .eq(ownerUserId != null, HealthAppUserEntity::getUserId, ownerUserId)
            .like(StrUtil.isNotBlank(mobile), HealthAppUserEntity::getMobile, mobile)
            .like(StrUtil.isNotBlank(nickname), HealthAppUserEntity::getNickname, nickname)
            .list()
            .stream()
            .map(HealthAppUserEntity::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> resolveOwnerUserIdsByKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return null;
        }
        return healthAppUserService.lambdaQuery()
            .and(wrapper -> wrapper.like(HealthAppUserEntity::getMobile, keyword)
                .or()
                .like(HealthAppUserEntity::getNickname, keyword))
            .list()
            .stream()
            .map(HealthAppUserEntity::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, HealthAppUserEntity> loadUserMap(Collection<HealthAppDeviceEntity> devices) {
        Set<Long> ownerUserIds = devices == null
            ? Collections.emptySet()
            : devices.stream()
                .map(HealthAppDeviceEntity::getOwnerUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ownerUserIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return healthAppUserService.listByIds(ownerUserIds).stream()
            .collect(Collectors.toMap(HealthAppUserEntity::getUserId, item -> item, (left, right) -> left));
    }

    private Map<Long, List<HealthAppPushDeliveryLogEntity>> loadDeliveryGroupMap(Collection<HealthAppDeviceEntity> devices) {
        Set<Long> deviceIds = devices == null
            ? Collections.emptySet()
            : devices.stream()
                .map(HealthAppDeviceEntity::getDeviceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (deviceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HealthAppPushDeliveryLogEntity> deliveryLogs = deliveryLogService.lambdaQuery()
            .in(HealthAppPushDeliveryLogEntity::getDeviceId, deviceIds)
            .orderByDesc(HealthAppPushDeliveryLogEntity::getSendTime)
            .orderByDesc(HealthAppPushDeliveryLogEntity::getDeliveryId)
            .list();
        return deliveryLogs.stream()
            .collect(Collectors.groupingBy(HealthAppPushDeliveryLogEntity::getDeviceId,
                LinkedHashMap::new, Collectors.toList()));
    }

    private HealthAppPushDeviceAdminDTO buildDeviceDTO(HealthAppDeviceEntity deviceEntity, HealthAppUserEntity userEntity,
        List<HealthAppPushDeliveryLogEntity> deliveryLogs, Date recentSevenDays) {
        List<HealthAppPushDeliveryLogEntity> safeLogs = deliveryLogs == null ? Collections.emptyList() : deliveryLogs;
        HealthAppPushDeliveryLogEntity latestDelivery = safeLogs.stream()
            .sorted(Comparator.comparing(HealthAppPushDeliveryLogEntity::getSendTime,
                Comparator.nullsLast(Date::compareTo)).reversed()
                .thenComparing(HealthAppPushDeliveryLogEntity::getDeliveryId, Comparator.nullsLast(Long::compareTo)).reversed())
            .findFirst()
            .orElse(null);

        long recentSuccessCount = safeLogs.stream()
            .filter(item -> item.getSendTime() != null && !item.getSendTime().before(recentSevenDays))
            .filter(item -> Objects.equals(item.getSendStatus(), HealthAppMessageSendStatusEnum.SUCCESS.getValue()))
            .count();
        long recentFailedCount = safeLogs.stream()
            .filter(item -> item.getSendTime() != null && !item.getSendTime().before(recentSevenDays))
            .filter(item -> Objects.equals(item.getSendStatus(), HealthAppMessageSendStatusEnum.FAILED.getValue()))
            .count();

        HealthAppPushDeviceAdminDTO dto = new HealthAppPushDeviceAdminDTO();
        dto.setDeviceId(deviceEntity.getDeviceId());
        dto.setOwnerUserId(deviceEntity.getOwnerUserId());
        dto.setOwnerUserMobile(userEntity == null ? null : userEntity.getMobile());
        dto.setOwnerUserNickname(userEntity == null ? null : userEntity.getNickname());
        dto.setDeviceCode(deviceEntity.getDeviceCode());
        dto.setPushPlatform(deviceEntity.getPushPlatform());
        dto.setDeviceModel(deviceEntity.getDeviceModel());
        dto.setManufacturer(deviceEntity.getManufacturer());
        dto.setOsVersion(deviceEntity.getOsVersion());
        dto.setAppVersion(deviceEntity.getAppVersion());
        dto.setStatus(deviceEntity.getStatus());
        dto.setStatusName(AppPushAuditSupport.resolveDeviceStatusName(deviceEntity.getStatus()));
        String activeStatus = AppPushAuditSupport.resolveDeviceActiveStatus(deviceEntity.getStatus(), deviceEntity.getLastActiveTime());
        dto.setActiveStatus(activeStatus);
        dto.setActiveStatusName(AppPushAuditSupport.resolveDeviceActiveStatusName(activeStatus));
        dto.setLastActiveTime(deviceEntity.getLastActiveTime());
        dto.setLastPushTime(latestDelivery == null ? null : latestDelivery.getSendTime());
        dto.setLastPushSendStatus(latestDelivery == null ? null : latestDelivery.getSendStatus());
        dto.setLastPushSendStatusName(latestDelivery == null ? null : resolveSendStatusName(latestDelivery.getSendStatus()));
        dto.setLastPushFailureReasonCategory(latestDelivery == null ? null : latestDelivery.getFailureReasonCategory());
        dto.setLastPushFailureReasonCategoryName(latestDelivery == null
            ? null
            : AppPushAuditSupport.resolveFailureReasonCategoryName(latestDelivery.getFailureReasonCategory()));
        dto.setLastPushFailureReasonMessage(latestDelivery == null ? null : latestDelivery.getFailureReasonMessage());
        dto.setRecent7DayPushSuccessCount(recentSuccessCount);
        dto.setRecent7DayPushFailedCount(recentFailedCount);
        return dto;
    }

    private void copyToDetailDTO(HealthAppPushDeviceAdminDetailDTO detailDTO, HealthAppPushDeviceAdminDTO source) {
        detailDTO.setDeviceId(source.getDeviceId());
        detailDTO.setOwnerUserId(source.getOwnerUserId());
        detailDTO.setOwnerUserMobile(source.getOwnerUserMobile());
        detailDTO.setOwnerUserNickname(source.getOwnerUserNickname());
        detailDTO.setDeviceCode(source.getDeviceCode());
        detailDTO.setPushPlatform(source.getPushPlatform());
        detailDTO.setDeviceModel(source.getDeviceModel());
        detailDTO.setManufacturer(source.getManufacturer());
        detailDTO.setOsVersion(source.getOsVersion());
        detailDTO.setAppVersion(source.getAppVersion());
        detailDTO.setStatus(source.getStatus());
        detailDTO.setStatusName(source.getStatusName());
        detailDTO.setActiveStatus(source.getActiveStatus());
        detailDTO.setActiveStatusName(source.getActiveStatusName());
        detailDTO.setLastActiveTime(source.getLastActiveTime());
        detailDTO.setLastPushTime(source.getLastPushTime());
        detailDTO.setLastPushSendStatus(source.getLastPushSendStatus());
        detailDTO.setLastPushSendStatusName(source.getLastPushSendStatusName());
        detailDTO.setLastPushFailureReasonCategory(source.getLastPushFailureReasonCategory());
        detailDTO.setLastPushFailureReasonCategoryName(source.getLastPushFailureReasonCategoryName());
        detailDTO.setLastPushFailureReasonMessage(source.getLastPushFailureReasonMessage());
        detailDTO.setRecent7DayPushSuccessCount(source.getRecent7DayPushSuccessCount());
        detailDTO.setRecent7DayPushFailedCount(source.getRecent7DayPushFailedCount());
    }

    private String buildSuccessRate(Long successCount, Long totalCount) {
        long safeTotalCount = Objects.requireNonNullElse(totalCount, 0L);
        if (safeTotalCount <= 0L) {
            return "0%";
        }
        BigDecimal rate = BigDecimal.valueOf(Objects.requireNonNullElse(successCount, 0L))
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(safeTotalCount), 2, RoundingMode.HALF_UP);
        return rate.stripTrailingZeros().toPlainString() + "%";
    }

    private List<HealthAppPushStatisticsBucketDTO> buildPlatformDistributions(List<HealthAppDeviceEntity> devices) {
        Map<String, Long> grouped = devices.stream()
            .collect(Collectors.groupingBy(item -> StrUtil.blankToDefault(item.getPushPlatform(), "UNKNOWN"),
                LinkedHashMap::new, Collectors.counting()));
        List<HealthAppPushStatisticsBucketDTO> results = new ArrayList<>();
        grouped.forEach((code, count) -> {
            HealthAppPushStatisticsBucketDTO dto = new HealthAppPushStatisticsBucketDTO();
            dto.setCode(code);
            dto.setName(resolvePlatformName(code));
            dto.setCount(count);
            results.add(dto);
        });
        results.sort(Comparator.comparing(HealthAppPushStatisticsBucketDTO::getCount,
            Comparator.nullsLast(Long::compareTo)).reversed());
        return results;
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
}

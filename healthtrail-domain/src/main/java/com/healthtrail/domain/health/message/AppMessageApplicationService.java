package com.healthtrail.domain.health.message;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.health.HealthAppMessageReadStatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.common.utils.jackson.JacksonUtil;
import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import com.healthtrail.domain.health.message.command.BatchResendHealthAppMessageCommand;
import com.healthtrail.domain.health.message.query.HealthAppMessageAdminQuery;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.healthtrail.domain.health.message.db.HealthAppMessageService;
import com.healthtrail.domain.health.message.dto.HealthAppMessageBatchResendItemDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageBatchResendResultDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageAdminDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageAdminDetailDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageCreateRequest;
import com.healthtrail.domain.health.message.dto.HealthAppMessageDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageDetailDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageResendResultDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageStatisticsBucketDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageStatisticsDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageStatisticsOverviewDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageStatisticsTrendDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageUnreadCountDTO;
import com.healthtrail.domain.health.message.notify.HealthAppMessageNotice;
import com.healthtrail.domain.health.message.notify.HealthAppMessageNotifyResult;
import com.healthtrail.domain.health.message.notify.HealthAppMessageNotifier;
import com.healthtrail.domain.health.message.query.HealthAppMessageStatisticsQuery;
import com.healthtrail.domain.health.message.query.HealthAppMessageQuery;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * App 消息中心应用服务。
 *
 * <p>该服务一方面为 App 暴露消息列表、详情、已读等接口，
 * 另一方面也作为各业务模块统一的“消息落库入口”。
 *
 * <p>这样用药提醒、报告建议等业务只需要关心：
 * 1. 我要给谁发什么消息
 * 2. 这条消息的业务载荷是什么
 *
 * <p>至于去重、落库、已读、发送结果回写，则统一由这里承接。
 */
@Service
@RequiredArgsConstructor
public class AppMessageApplicationService {

    /** App消息数据库服务 */
    private final HealthAppMessageService healthAppMessageService;

    /** App用户数据库服务 */
    private final HealthAppUserService healthAppUserService;

    /** App消息通知发送器 */
    private final HealthAppMessageNotifier healthAppMessageNotifier;

    /**
     * 分页查询当前用户的消息列表。
     */
    public PageDTO<HealthAppMessageDTO> getMessageList(HealthAppMessageQuery query) {
        Page<HealthAppMessageEntity> page = healthAppMessageService.page(query.toPage(), query.toQueryWrapper());
        List<HealthAppMessageDTO> records = page.getRecords().stream()
            .map(this::buildMessageDTO)
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 查询消息详情。
     */
    public HealthAppMessageDetailDTO getMessageDetail(Long messageId, Long ownerUserId) {
        HealthAppMessageEntity messageEntity = getOwnedMessage(messageId, ownerUserId);
        HealthAppPushPayloadDTO payloadDTO = parsePayload(messageEntity.getPayloadJson());

        HealthAppMessageDetailDTO detailDTO = new HealthAppMessageDetailDTO();
        fillBaseMessageDTO(detailDTO, messageEntity, payloadDTO);
        detailDTO.setPayload(payloadDTO);
        return detailDTO;
    }

    /**
     * 查询当前用户未读消息数量。
     */
    public HealthAppMessageUnreadCountDTO getUnreadCount(Long ownerUserId) {
        long unreadCount = healthAppMessageService.lambdaQuery()
            .eq(HealthAppMessageEntity::getOwnerUserId, ownerUserId)
            .eq(HealthAppMessageEntity::getReadStatus, HealthAppMessageReadStatusEnum.UNREAD.getValue())
            .count();

        HealthAppMessageUnreadCountDTO dto = new HealthAppMessageUnreadCountDTO();
        dto.setUnreadCount(unreadCount);
        return dto;
    }

    /**
     * 后台消息统计看板。
     *
     * <p>当前统计统一基于消息中心表实时计算，
     * 这样后台看到的统计口径会和消息审计列表保持一致。
     */
    public HealthAppMessageStatisticsDTO getMessageStatistics(HealthAppMessageStatisticsQuery query) {
        List<HealthAppMessageEntity> messageEntities = healthAppMessageService.list(query.toQueryWrapper());

        HealthAppMessageStatisticsDTO statisticsDTO = new HealthAppMessageStatisticsDTO();
        statisticsDTO.setOverview(buildStatisticsOverview(messageEntities));
        statisticsDTO.setSceneDistributions(buildSceneDistributions(messageEntities));
        statisticsDTO.setSendStatusDistributions(buildSendStatusDistributions(messageEntities));
        statisticsDTO.setRecentTrends(buildRecentTrends(messageEntities, query));
        statisticsDTO.setFailureReasonDistributions(buildFailureReasonDistributions(messageEntities, query));
        return statisticsDTO;
    }

    /**
     * 后台分页查询消息审计列表。
     *
     * <p>这里复用消息中心同一张表作为后台审计数据源，
     * 避免再维护额外冗余审计表。
     */
    public PageDTO<HealthAppMessageAdminDTO> getAdminMessageList(HealthAppMessageAdminQuery query) {
        Page<HealthAppMessageEntity> page = healthAppMessageService.page(query.toPage(), query.toQueryWrapper());
        Map<Long, HealthAppUserEntity> userMap = loadOwnerUserMap(page.getRecords());
        List<HealthAppMessageAdminDTO> records = page.getRecords().stream()
            .map(messageEntity -> buildAdminMessageDTO(messageEntity, parsePayload(messageEntity.getPayloadJson()),
                userMap.get(messageEntity.getOwnerUserId())))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 后台查询消息审计详情。
     */
    public HealthAppMessageAdminDetailDTO getAdminMessageDetail(Long messageId) {
        HealthAppMessageEntity messageEntity = getMessageById(messageId);

        HealthAppPushPayloadDTO payloadDTO = parsePayload(messageEntity.getPayloadJson());
        HealthAppUserEntity appUserEntity = messageEntity.getOwnerUserId() == null
            ? null
            : healthAppUserService.getById(messageEntity.getOwnerUserId());

        HealthAppMessageAdminDetailDTO detailDTO = new HealthAppMessageAdminDetailDTO();
        fillAdminMessageDTO(detailDTO, messageEntity, payloadDTO, appUserEntity);
        detailDTO.setPayloadJson(messageEntity.getPayloadJson());
        detailDTO.setPayload(payloadDTO);
        return detailDTO;
    }

    /**
     * 后台人工重发指定消息。
     *
     * <p>这里复用消息表里已经保存好的标题、正文和透传载荷，
     * 直接对当前消息执行一次重新派发，而不是再回到原始业务模块重新构建通知。
     */
    public HealthAppMessageResendResultDTO resendMessage(Long messageId) {
        HealthAppMessageEntity messageEntity = getMessageById(messageId);
        HealthAppMessageNotice notice = buildMessageNotice(messageEntity);

        HealthAppMessageNotifyResult notifyResult;
        try {
            notifyResult = healthAppMessageNotifier.notify(notice);
        } catch (Exception ex) {
            markMessageSendFailed(messageId, null, ex.getMessage());
            return buildResendResult(messageId, false, null, ex.getMessage());
        }

        if (notifyResult != null && notifyResult.isSuccess()) {
            markMessageSendSuccess(messageId, notifyResult.getChannel(), notifyResult.getMessage());
            return buildResendResult(messageId, true, notifyResult.getChannel(), notifyResult.getMessage());
        }

        String failMessage = notifyResult == null ? "消息发送器未返回结果" : notifyResult.getMessage();
        markMessageSendFailed(messageId, notifyResult == null ? null : notifyResult.getChannel(), failMessage);
        return buildResendResult(messageId, false, notifyResult == null ? null : notifyResult.getChannel(), failMessage);
    }

    /**
     * 后台批量重发消息。
     *
     * <p>这里采用“逐条执行、逐条汇总”的方式，而不是一条失败就整批回滚，
     * 原因是后台批量补发更强调可恢复性：
     * 1. 某一条消息数据异常，不应该阻塞其他正常消息补发
     * 2. 页面需要知道每条消息各自的结果，方便继续二次处理
     */
    public HealthAppMessageBatchResendResultDTO batchResendMessages(BatchResendHealthAppMessageCommand command) {
        List<Long> requestMessageIds = command == null ? null : command.getMessageIds();
        LinkedHashSet<Long> uniqueMessageIds = requestMessageIds == null
            ? new LinkedHashSet<>()
            : requestMessageIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<HealthAppMessageBatchResendItemDTO> results = uniqueMessageIds.stream()
            .map(this::batchResendSingleMessage)
            .collect(Collectors.toList());

        int successCount = (int) results.stream()
            .filter(item -> Boolean.TRUE.equals(item.getSuccess()))
            .count();

        HealthAppMessageBatchResendResultDTO resultDTO = new HealthAppMessageBatchResendResultDTO();
        resultDTO.setRequestCount(requestMessageIds == null ? 0 : requestMessageIds.size());
        resultDTO.setActualCount(uniqueMessageIds.size());
        resultDTO.setSuccessCount(successCount);
        resultDTO.setFailedCount(results.size() - successCount);
        resultDTO.setResults(results);
        return resultDTO;
    }

    /**
     * 标记指定消息为已读。
     *
     * <p>这里做幂等处理：
     * 如果消息已经是已读状态，则直接返回，避免前端重复点击产生额外负担。
     */
    public void markMessageRead(Long messageId, Long ownerUserId) {
        HealthAppMessageEntity messageEntity = getOwnedMessage(messageId, ownerUserId);
        if (Objects.equals(messageEntity.getReadStatus(), HealthAppMessageReadStatusEnum.READ.getValue())) {
            return;
        }

        UpdateWrapper<HealthAppMessageEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("message_id", messageId)
            .eq("owner_user_id", ownerUserId)
            .set("read_status", HealthAppMessageReadStatusEnum.READ.getValue())
            .set("read_time", new Date());
        healthAppMessageService.update(updateWrapper);
    }

    /**
     * 一键标记当前用户全部未读消息为已读。
     */
    public void markAllRead(Long ownerUserId) {
        UpdateWrapper<HealthAppMessageEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("owner_user_id", ownerUserId)
            .eq("read_status", HealthAppMessageReadStatusEnum.UNREAD.getValue())
            .set("read_status", HealthAppMessageReadStatusEnum.READ.getValue())
            .set("read_time", new Date());
        healthAppMessageService.update(updateWrapper);
    }

    /**
     * 创建消息记录，如果去重键已存在则直接复用原消息。
     *
     * <p>这样可以同时满足两类场景：
     * 1. 用药提醒重试发送时，不重复新增消息，只更新同一条消息的发送结果
     * 2. 报告建议重复保存同一批异常结果时，不重复创建消息与 Push
     */
    public HealthAppMessageEntity createOrReuseMessage(HealthAppMessageCreateRequest createRequest) {
        validateCreateRequest(createRequest);

        if (StrUtil.isNotBlank(createRequest.getDedupKey())) {
            HealthAppMessageEntity existingMessage = getByOwnerUserIdAndDedupKey(
                createRequest.getOwnerUserId(), createRequest.getDedupKey());
            if (existingMessage != null) {
                return existingMessage;
            }
        }

        HealthAppMessageEntity messageEntity = new HealthAppMessageEntity();
        messageEntity.setOwnerUserId(createRequest.getOwnerUserId());
        messageEntity.setMemberId(createRequest.getMemberId());
        messageEntity.setMemberNameSnapshot(limitLength(createRequest.getMemberName(), 50));
        messageEntity.setBusinessScene(createRequest.getBusinessScene());
        messageEntity.setBusinessId(createRequest.getBusinessId());
        messageEntity.setMessageTitle(limitLength(createRequest.getMessageTitle(), 100));
        messageEntity.setMessageContent(limitLength(createRequest.getMessageContent(), 500));
        messageEntity.setPayloadJson(createRequest.getPayload() == null ? null : JacksonUtil.to(createRequest.getPayload()));
        messageEntity.setDedupKey(limitLength(createRequest.getDedupKey(), 100));
        messageEntity.setReadStatus(HealthAppMessageReadStatusEnum.UNREAD.getValue());
        messageEntity.setSendStatus(HealthAppMessageSendStatusEnum.PENDING.getValue());
        messageEntity.setSendRetryCount(0);

        try {
            healthAppMessageService.save(messageEntity);
            return messageEntity;
        } catch (DuplicateKeyException ex) {
            if (StrUtil.isNotBlank(createRequest.getDedupKey())) {
                HealthAppMessageEntity existingMessage = getByOwnerUserIdAndDedupKey(
                    createRequest.getOwnerUserId(), createRequest.getDedupKey());
                if (existingMessage != null) {
                    return existingMessage;
                }
            }
            throw ex;
        }
    }

    /**
     * 回写消息发送成功状态。
     */
    public void markMessageSendSuccess(Long messageId, String channel, String resultMessage) {
        HealthAppMessageEntity messageEntity = healthAppMessageService.getById(messageId);
        if (messageEntity == null) {
            return;
        }

        messageEntity.setSendStatus(HealthAppMessageSendStatusEnum.SUCCESS.getValue());
        messageEntity.setSendTime(new Date());
        messageEntity.setSendChannel(limitLength(channel, 30));
        messageEntity.setSendResultMessage(limitLength(StrUtil.blankToDefault(resultMessage, "消息发送成功"), 255));
        healthAppMessageService.updateById(messageEntity);
    }

    /**
     * 回写消息发送失败状态。
     *
     * <p>每失败一次都会累计重试次数，
     * 方便后续如果需要做消息中心维度的发送审计，可以直接读取该字段。
     */
    public void markMessageSendFailed(Long messageId, String channel, String resultMessage) {
        HealthAppMessageEntity messageEntity = healthAppMessageService.getById(messageId);
        if (messageEntity == null) {
            return;
        }

        messageEntity.setSendStatus(HealthAppMessageSendStatusEnum.FAILED.getValue());
        messageEntity.setSendTime(new Date());
        messageEntity.setSendChannel(limitLength(channel, 30));
        messageEntity.setSendRetryCount(Objects.requireNonNullElse(messageEntity.getSendRetryCount(), 0) + 1);
        messageEntity.setSendResultMessage(limitLength(StrUtil.blankToDefault(resultMessage, "消息发送失败"), 255));
        healthAppMessageService.updateById(messageEntity);
    }

    /**
     * 读取指定用户与去重键对应的消息。
     */
    public HealthAppMessageEntity getByOwnerUserIdAndDedupKey(Long ownerUserId, String dedupKey) {
        if (ownerUserId == null || StrUtil.isBlank(dedupKey)) {
            return null;
        }
        return healthAppMessageService.lambdaQuery()
            .eq(HealthAppMessageEntity::getOwnerUserId, ownerUserId)
            .eq(HealthAppMessageEntity::getDedupKey, dedupKey)
            .one();
    }

    /**
     * 读取指定业务对象最近一条消息。
     *
     * <p>这个入口主要服务于“同一个业务对象允许多轮消息，但失败时希望原地重试”的场景。
     * 例如低库存提醒：
     * 1. 同一轮低库存周期里，如果上一次 Push 失败，希望继续复用同一条消息补发
     * 2. 当库存恢复安全区、进入下一轮低库存时，又允许重新创建一条新消息
     *
     * <p>因此这里返回“最近一条”而不是全量列表，交由业务结合自己的周期状态字段判断是否复用。
     */
    public HealthAppMessageEntity getLatestMessageByBusiness(Long ownerUserId, String businessScene, Long businessId) {
        if (ownerUserId == null || StrUtil.isBlank(businessScene) || businessId == null) {
            return null;
        }
        return healthAppMessageService.lambdaQuery()
            .eq(HealthAppMessageEntity::getOwnerUserId, ownerUserId)
            .eq(HealthAppMessageEntity::getBusinessScene, businessScene)
            .eq(HealthAppMessageEntity::getBusinessId, businessId)
            .orderByDesc(HealthAppMessageEntity::getMessageId)
            .last("limit 1")
            .one();
    }

    /**
     * 刷新已有消息的快照内容。
     *
     * <p>当业务决定复用旧消息ID进行补发时，仍然要把标题、正文和业务载荷刷新成最新值，
     * 否则会出现“本次 Push 内容已经变了，但消息中心和后台审计里看到的仍是旧快照”的口径偏差。
     */
    public HealthAppMessageEntity refreshMessageSnapshot(Long messageId, HealthAppMessageCreateRequest createRequest) {
        validateCreateRequest(createRequest);
        if (messageId == null) {
            return null;
        }

        HealthAppMessageEntity messageEntity = healthAppMessageService.getById(messageId);
        if (messageEntity == null) {
            return null;
        }
        messageEntity.setMessageTitle(limitLength(createRequest.getMessageTitle(), 100));
        messageEntity.setMessageContent(limitLength(createRequest.getMessageContent(), 500));
        messageEntity.setPayloadJson(createRequest.getPayload() == null ? null : JacksonUtil.to(createRequest.getPayload()));
        healthAppMessageService.updateById(messageEntity);
        return messageEntity;
    }

    /**
     * 构建消息列表 DTO。
     */
    private HealthAppMessageDTO buildMessageDTO(HealthAppMessageEntity messageEntity) {
        HealthAppPushPayloadDTO payloadDTO = parsePayload(messageEntity.getPayloadJson());
        HealthAppMessageDTO dto = new HealthAppMessageDTO();
        fillBaseMessageDTO(dto, messageEntity, payloadDTO);
        return dto;
    }

    /**
     * 构建后台消息审计 DTO。
     */
    private HealthAppMessageAdminDTO buildAdminMessageDTO(HealthAppMessageEntity messageEntity,
        HealthAppPushPayloadDTO payloadDTO, HealthAppUserEntity appUserEntity) {
        HealthAppMessageAdminDTO dto = new HealthAppMessageAdminDTO();
        fillAdminMessageDTO(dto, messageEntity, payloadDTO, appUserEntity);
        return dto;
    }

    /**
     * 给消息 DTO 回填公共字段。
     *
     * <p>列表和详情大部分字段完全一致，
     * 因此统一在这里处理，避免两处字段解释出现偏差。
     */
    private void fillBaseMessageDTO(HealthAppMessageDTO dto, HealthAppMessageEntity messageEntity,
        HealthAppPushPayloadDTO payloadDTO) {
        dto.setMessageId(messageEntity.getMessageId());
        dto.setMemberId(messageEntity.getMemberId());
        dto.setMemberName(messageEntity.getMemberNameSnapshot());
        dto.setBusinessScene(messageEntity.getBusinessScene());
        dto.setBusinessSceneName(resolveSceneName(messageEntity.getBusinessScene()));
        dto.setBusinessId(messageEntity.getBusinessId());
        dto.setMessageTitle(messageEntity.getMessageTitle());
        dto.setMessageContent(messageEntity.getMessageContent());
        dto.setReadStatus(messageEntity.getReadStatus());
        dto.setReadTime(messageEntity.getReadTime());
        dto.setSendStatus(messageEntity.getSendStatus());
        dto.setSendTime(messageEntity.getSendTime());
        dto.setSendRetryCount(messageEntity.getSendRetryCount());
        dto.setSendChannel(messageEntity.getSendChannel());
        dto.setSendResultMessage(messageEntity.getSendResultMessage());
        dto.setCreateTime(messageEntity.getCreateTime());
        dto.setNavigation(payloadDTO == null ? null : payloadDTO.getNavigation());
        dto.setRecommendedAction(payloadDTO == null ? null : payloadDTO.getRecommendedAction());
    }

    /**
     * 回填后台消息审计 DTO 公共字段。
     */
    private void fillAdminMessageDTO(HealthAppMessageAdminDTO dto, HealthAppMessageEntity messageEntity,
        HealthAppPushPayloadDTO payloadDTO, HealthAppUserEntity appUserEntity) {
        dto.setMessageId(messageEntity.getMessageId());
        dto.setOwnerUserId(messageEntity.getOwnerUserId());
        dto.setOwnerUserMobile(appUserEntity == null ? null : appUserEntity.getMobile());
        dto.setOwnerUserNickname(appUserEntity == null ? HealthAppI18n.deletedUserName() : appUserEntity.getNickname());
        dto.setMemberId(messageEntity.getMemberId());
        dto.setMemberName(messageEntity.getMemberNameSnapshot());
        dto.setBusinessScene(messageEntity.getBusinessScene());
        dto.setBusinessSceneName(resolveSceneName(messageEntity.getBusinessScene()));
        dto.setBusinessId(messageEntity.getBusinessId());
        dto.setMessageTitle(messageEntity.getMessageTitle());
        dto.setMessageContent(messageEntity.getMessageContent());
        dto.setReadStatus(messageEntity.getReadStatus());
        dto.setReadTime(messageEntity.getReadTime());
        dto.setSendStatus(messageEntity.getSendStatus());
        dto.setSendTime(messageEntity.getSendTime());
        dto.setSendChannel(messageEntity.getSendChannel());
        dto.setSendRetryCount(messageEntity.getSendRetryCount());
        dto.setSendResultMessage(messageEntity.getSendResultMessage());
        dto.setDedupKey(messageEntity.getDedupKey());
        dto.setCreateTime(messageEntity.getCreateTime());
        dto.setNavigation(payloadDTO == null ? null : payloadDTO.getNavigation());
    }

    /**
     * 校验消息创建请求必要字段。
     */
    private void validateCreateRequest(HealthAppMessageCreateRequest createRequest) {
        if (createRequest == null || createRequest.getOwnerUserId() == null
            || StrUtil.isBlank(createRequest.getBusinessScene())
            || createRequest.getBusinessId() == null
            || StrUtil.isBlank(createRequest.getMessageTitle())) {
            throw new ApiException(ErrorCode.Internal.INVALID_PARAMETER, "App消息创建请求缺少必要字段");
        }
    }

    /**
     * 查询当前用户拥有的消息。
     */
    private HealthAppMessageEntity getOwnedMessage(Long messageId, Long ownerUserId) {
        HealthAppMessageEntity messageEntity = healthAppMessageService.lambdaQuery()
            .eq(HealthAppMessageEntity::getMessageId, messageId)
            .eq(HealthAppMessageEntity::getOwnerUserId, ownerUserId)
            .one();
        if (messageEntity == null) {
            throw new ApiException(ErrorCode.Business.APP_MESSAGE_NOT_FOUND);
        }
        return messageEntity;
    }

    /**
     * 按消息ID查询消息。
     */
    private HealthAppMessageEntity getMessageById(Long messageId) {
        HealthAppMessageEntity messageEntity = healthAppMessageService.getById(messageId);
        if (messageEntity == null) {
            throw new ApiException(ErrorCode.Business.APP_MESSAGE_NOT_FOUND);
        }
        return messageEntity;
    }

    /**
     * 把消息记录转换成发送器可直接消费的通知载荷。
     */
    private HealthAppMessageNotice buildMessageNotice(HealthAppMessageEntity messageEntity) {
        if (messageEntity == null || messageEntity.getOwnerUserId() == null
            || (StrUtil.isBlank(messageEntity.getMessageTitle()) && StrUtil.isBlank(messageEntity.getMessageContent()))) {
            throw new ApiException(ErrorCode.Business.APP_MESSAGE_RESEND_DATA_INVALID);
        }

        return HealthAppMessageNotice.builder()
            .messageId(messageEntity.getMessageId())
            .ownerUserId(messageEntity.getOwnerUserId())
            .memberId(messageEntity.getMemberId())
            .businessScene(messageEntity.getBusinessScene())
            .businessId(messageEntity.getBusinessId())
            .messageTitle(messageEntity.getMessageTitle())
            .messageContent(messageEntity.getMessageContent())
            .payload(parsePayload(messageEntity.getPayloadJson()))
            .build();
    }

    /**
     * 组装后台人工重发结果。
     */
    private HealthAppMessageResendResultDTO buildResendResult(Long messageId, boolean success, String channel, String message) {
        HealthAppMessageEntity latestMessage = getMessageById(messageId);
        HealthAppMessageResendResultDTO resultDTO = new HealthAppMessageResendResultDTO();
        resultDTO.setMessageId(messageId);
        resultDTO.setSuccess(success);
        resultDTO.setChannel(channel);
        resultDTO.setMessage(message);
        resultDTO.setSendStatus(latestMessage.getSendStatus());
        resultDTO.setSendTime(latestMessage.getSendTime());
        resultDTO.setSendRetryCount(latestMessage.getSendRetryCount());
        return resultDTO;
    }

    /**
     * 批量重发模式下执行单条消息补发。
     *
     * <p>这里单独做一层包装，是为了把异常转成“单条失败结果”，
     * 避免批量处理中途直接抛错中断后续消息。
     */
    private HealthAppMessageBatchResendItemDTO batchResendSingleMessage(Long messageId) {
        try {
            HealthAppMessageResendResultDTO resendResultDTO = resendMessage(messageId);
            HealthAppMessageBatchResendItemDTO itemDTO = new HealthAppMessageBatchResendItemDTO();
            itemDTO.setMessageId(messageId);
            itemDTO.setSuccess(resendResultDTO.getSuccess());
            itemDTO.setChannel(resendResultDTO.getChannel());
            itemDTO.setMessage(resendResultDTO.getMessage());
            itemDTO.setSendStatus(resendResultDTO.getSendStatus());
            itemDTO.setSendTime(resendResultDTO.getSendTime());
            itemDTO.setSendRetryCount(resendResultDTO.getSendRetryCount());
            return itemDTO;
        } catch (ApiException ex) {
            return buildBatchFailedItem(messageId, ex.getMessage());
        } catch (Exception ex) {
            return buildBatchFailedItem(messageId, StrUtil.blankToDefault(ex.getMessage(), "批量重发执行异常"));
        }
    }

    /**
     * 构建批量重发失败项结果。
     */
    private HealthAppMessageBatchResendItemDTO buildBatchFailedItem(Long messageId, String message) {
        HealthAppMessageEntity messageEntity = healthAppMessageService.getById(messageId);
        HealthAppMessageBatchResendItemDTO itemDTO = new HealthAppMessageBatchResendItemDTO();
        itemDTO.setMessageId(messageId);
        itemDTO.setSuccess(false);
        itemDTO.setChannel(messageEntity == null ? null : messageEntity.getSendChannel());
        itemDTO.setMessage(message);
        itemDTO.setSendStatus(messageEntity == null ? null : messageEntity.getSendStatus());
        itemDTO.setSendTime(messageEntity == null ? null : messageEntity.getSendTime());
        itemDTO.setSendRetryCount(messageEntity == null ? null : messageEntity.getSendRetryCount());
        return itemDTO;
    }

    /**
     * 构建消息统计总览。
     */
    private HealthAppMessageStatisticsOverviewDTO buildStatisticsOverview(List<HealthAppMessageEntity> messageEntities) {
        List<HealthAppMessageEntity> safeMessages = messageEntities == null ? Collections.emptyList() : messageEntities;
        Date todayBegin = DateUtil.beginOfDay(new Date());

        HealthAppMessageStatisticsOverviewDTO overviewDTO = new HealthAppMessageStatisticsOverviewDTO();
        overviewDTO.setTotalMessageCount((long) safeMessages.size());
        overviewDTO.setUnreadMessageCount(countByReadStatus(safeMessages, HealthAppMessageReadStatusEnum.UNREAD.getValue()));
        overviewDTO.setSendSuccessCount(countBySendStatus(safeMessages, HealthAppMessageSendStatusEnum.SUCCESS.getValue()));
        overviewDTO.setSendFailedCount(countBySendStatus(safeMessages, HealthAppMessageSendStatusEnum.FAILED.getValue()));
        overviewDTO.setPendingSendCount(countBySendStatus(safeMessages, HealthAppMessageSendStatusEnum.PENDING.getValue()));
        overviewDTO.setOwnerUserCount(safeMessages.stream()
            .map(HealthAppMessageEntity::getOwnerUserId)
            .filter(Objects::nonNull)
            .distinct()
            .count());
        overviewDTO.setTodayCreatedCount(safeMessages.stream()
            .filter(message -> message.getCreateTime() != null && !message.getCreateTime().before(todayBegin))
            .count());
        overviewDTO.setTodayFailedCount(safeMessages.stream()
            .filter(message -> message.getCreateTime() != null && !message.getCreateTime().before(todayBegin))
            .filter(message -> Objects.equals(message.getSendStatus(), HealthAppMessageSendStatusEnum.FAILED.getValue()))
            .count());
        return overviewDTO;
    }

    /**
     * 构建业务场景分布。
     */
    private List<HealthAppMessageStatisticsBucketDTO> buildSceneDistributions(List<HealthAppMessageEntity> messageEntities) {
        Map<String, Long> countMap = (messageEntities == null ? Collections.<HealthAppMessageEntity>emptyList() : messageEntities).stream()
            .collect(Collectors.groupingBy(message -> StrUtil.blankToDefault(message.getBusinessScene(), "UNKNOWN"),
                Collectors.counting()));

        return countMap.entrySet().stream()
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .map(entry -> buildBucket(entry.getKey(), resolveSceneName(entry.getKey()), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * 构建发送状态分布。
     */
    private List<HealthAppMessageStatisticsBucketDTO> buildSendStatusDistributions(List<HealthAppMessageEntity> messageEntities) {
        Map<String, Long> countMap = (messageEntities == null ? Collections.<HealthAppMessageEntity>emptyList() : messageEntities).stream()
            .collect(Collectors.groupingBy(message -> Objects.toString(message.getSendStatus(), "UNKNOWN"),
                Collectors.counting()));

        return countMap.entrySet().stream()
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .map(entry -> buildBucket(entry.getKey(), resolveSendStatusName(entry.getKey()), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * 构建最近趋势。
     *
     * <p>趋势统计默认返回最近 7 天，
     * 若前端显式传入 `trendDays`，则按该值返回，最大 30 天。
     */
    private List<HealthAppMessageStatisticsTrendDTO> buildRecentTrends(List<HealthAppMessageEntity> messageEntities,
        HealthAppMessageStatisticsQuery query) {
        List<HealthAppMessageEntity> safeMessages = messageEntities == null ? Collections.emptyList() : messageEntities;
        int safeTrendDays = query == null || query.getTrendDays() == null
            ? 7
            : Math.max(1, Math.min(query.getTrendDays(), 30));
        Date today = DateUtil.beginOfDay(new Date());
        Date trendBegin = DateUtil.beginOfDay(DateUtil.offsetDay(today, 1 - safeTrendDays));

        Map<String, List<HealthAppMessageEntity>> messageMap = safeMessages.stream()
            .filter(message -> message.getCreateTime() != null && !message.getCreateTime().before(trendBegin))
            .collect(Collectors.groupingBy(message -> DateUtil.formatDate(message.getCreateTime())));

        return IntStream.range(0, safeTrendDays)
            .mapToObj(index -> DateUtil.offsetDay(trendBegin, index))
            .map(statDate -> buildTrendItem(DateUtil.formatDate(statDate),
                messageMap.getOrDefault(DateUtil.formatDate(statDate), Collections.emptyList())))
            .collect(Collectors.toList());
    }

    /**
     * 构建失败原因排行。
     *
     * <p>这里主要读取消息表里最近一次发送结果说明，
     * 并仅统计当前处于“发送失败”状态的消息。
     * 这样后台看到的是“当前仍未成功送达的失败原因分布”，
     * 更适合做即时排障。
     */
    private List<HealthAppMessageStatisticsBucketDTO> buildFailureReasonDistributions(
        List<HealthAppMessageEntity> messageEntities, HealthAppMessageStatisticsQuery query) {
        int safeTopLimit = query == null || query.getFailureReasonTopLimit() == null
            ? 5
            : Math.max(1, Math.min(query.getFailureReasonTopLimit(), 20));

        Map<String, Long> countMap = (messageEntities == null ? Collections.<HealthAppMessageEntity>emptyList() : messageEntities)
            .stream()
            .filter(message -> Objects.equals(message.getSendStatus(), HealthAppMessageSendStatusEnum.FAILED.getValue()))
            .collect(Collectors.groupingBy(message -> normalizeFailureReason(message.getSendResultMessage()),
                Collectors.counting()));

        return countMap.entrySet().stream()
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .limit(safeTopLimit)
            .map(entry -> buildBucket(entry.getKey(), entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * 构建单个趋势点。
     */
    private HealthAppMessageStatisticsTrendDTO buildTrendItem(String statDate, List<HealthAppMessageEntity> dayMessages) {
        List<HealthAppMessageEntity> safeMessages = dayMessages == null ? Collections.emptyList() : dayMessages;
        HealthAppMessageStatisticsTrendDTO trendDTO = new HealthAppMessageStatisticsTrendDTO();
        trendDTO.setStatDate(statDate);
        trendDTO.setCreatedCount((long) safeMessages.size());
        trendDTO.setSuccessCount(countBySendStatus(safeMessages, HealthAppMessageSendStatusEnum.SUCCESS.getValue()));
        trendDTO.setFailedCount(countBySendStatus(safeMessages, HealthAppMessageSendStatusEnum.FAILED.getValue()));
        return trendDTO;
    }

    /**
     * 构建分布桶 DTO。
     */
    private HealthAppMessageStatisticsBucketDTO buildBucket(String code, String name, Long count) {
        HealthAppMessageStatisticsBucketDTO bucketDTO = new HealthAppMessageStatisticsBucketDTO();
        bucketDTO.setCode(code);
        bucketDTO.setName(name);
        bucketDTO.setCount(count);
        return bucketDTO;
    }

    /**
     * 统计已读状态数量。
     */
    private Long countByReadStatus(List<HealthAppMessageEntity> messageEntities, Integer readStatus) {
        return (messageEntities == null ? Collections.<HealthAppMessageEntity>emptyList() : messageEntities).stream()
            .filter(message -> Objects.equals(message.getReadStatus(), readStatus))
            .count();
    }

    /**
     * 统计发送状态数量。
     */
    private Long countBySendStatus(List<HealthAppMessageEntity> messageEntities, Integer sendStatus) {
        return (messageEntities == null ? Collections.<HealthAppMessageEntity>emptyList() : messageEntities).stream()
            .filter(message -> Objects.equals(message.getSendStatus(), sendStatus))
            .count();
    }

    /**
     * 归一化失败原因文案。
     *
     * <p>当前先采用轻量规则：
     * 1. 空值统一收敛为“未知失败原因”
     * 2. 去掉首尾空格
     * 3. 超长文案做长度保护，避免统计图例过长
     */
    private String normalizeFailureReason(String failureReason) {
        String normalizedReason = StrUtil.trimToEmpty(failureReason);
        if (StrUtil.isBlank(normalizedReason)) {
            return "未知失败原因";
        }
        return limitLength(normalizedReason, 60);
    }

    /**
     * 解析消息透传载荷。
     *
     * <p>这里采用宽松策略：
     * 如果历史数据为空或 JSON 异常，消息列表仍然应该可以展示基础标题正文，
     * 不因为某一条老消息载荷异常而整体失败。
     */
    private HealthAppPushPayloadDTO parsePayload(String payloadJson) {
        if (StrUtil.isBlank(payloadJson)) {
            return null;
        }
        try {
            return JacksonUtil.from(payloadJson, HealthAppPushPayloadDTO.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 批量加载列表中消息归属的 App 用户信息。
     *
     * <p>后台审计列表通常需要同时看到“用户ID + 手机号 + 昵称”，
     * 这里统一预加载，避免按行逐条查询。
     */
    private Map<Long, HealthAppUserEntity> loadOwnerUserMap(List<HealthAppMessageEntity> messageEntities) {
        if (messageEntities == null || messageEntities.isEmpty()) {
            return new HashMap<>();
        }

        Set<Long> ownerUserIds = messageEntities.stream()
            .map(HealthAppMessageEntity::getOwnerUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (ownerUserIds.isEmpty()) {
            return new HashMap<>();
        }

        return healthAppUserService.listByIds(ownerUserIds).stream()
            .collect(Collectors.toMap(HealthAppUserEntity::getUserId, user -> user, (left, right) -> left));
    }

    /**
     * 按业务场景编码解析场景名称。
     */
    private String resolveSceneName(String businessScene) {
        return HealthAppI18n.appMessageSceneName(businessScene);
    }

    /**
     * 按发送状态编码解析状态名称。
     */
    private String resolveSendStatusName(String sendStatusCode) {
        Integer sendStatus = null;
        if (StrUtil.isNotBlank(sendStatusCode) && StrUtil.isNumeric(sendStatusCode)) {
            sendStatus = Integer.valueOf(sendStatusCode);
        }
        if (sendStatus == null) {
            return sendStatusCode;
        }
        return HealthAppI18n.appMessageSendStatusName(sendStatus);
    }

    /**
     * 对字符串长度做统一保护，避免数据库字段超长。
     */
    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}

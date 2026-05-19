package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.health.message.AppMessageApplicationService;
import com.healthtrail.domain.health.message.dto.HealthAppMessageDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageDetailDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageUnreadCountDTO;
import com.healthtrail.domain.health.message.query.HealthAppMessageQuery;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 消息中心控制器。
 *
 * <p>该控制器提供 App 端统一消息入口，
 * 用于承接用药提醒、报告建议等各类系统消息。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/messages")
@Tag(name = "App消息中心API", description = "App端消息中心接口")
public class HealthAppMessageController extends BaseController {

    private final AppMessageApplicationService appMessageApplicationService;

    /**
     * 分页查询消息列表。
     */
    @Operation(summary = "消息中心列表")
    @GetMapping
    public ResponseDTO<PageDTO<HealthAppMessageDTO>> list(HealthAppMessageQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(appMessageApplicationService.getMessageList(query));
    }

    /**
     * 查询消息详情。
     */
    @Operation(summary = "消息详情")
    @GetMapping("/{messageId}")
    public ResponseDTO<HealthAppMessageDetailDTO> detail(@PathVariable Long messageId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(appMessageApplicationService.getMessageDetail(messageId, currentUserId));
    }

    /**
     * 查询未读消息数。
     */
    @Operation(summary = "未读消息数")
    @GetMapping("/unread-count")
    public ResponseDTO<HealthAppMessageUnreadCountDTO> unreadCount() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(appMessageApplicationService.getUnreadCount(currentUserId));
    }

    /**
     * 标记单条消息为已读。
     */
    @Operation(summary = "标记单条消息已读")
    @PostMapping("/{messageId}/read")
    public ResponseDTO<Void> markRead(@PathVariable Long messageId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        appMessageApplicationService.markMessageRead(messageId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 一键标记全部消息已读。
     */
    @Operation(summary = "全部标记已读")
    @PostMapping("/read-all")
    public ResponseDTO<Void> markAllRead() {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        appMessageApplicationService.markAllRead(currentUserId);
        return ResponseDTO.ok();
    }
}

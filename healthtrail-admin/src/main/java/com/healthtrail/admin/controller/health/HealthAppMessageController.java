package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.health.message.AppMessageApplicationService;
import com.healthtrail.domain.health.message.command.BatchResendHealthAppMessageCommand;
import com.healthtrail.domain.health.message.dto.HealthAppMessageAdminDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageBatchResendResultDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageAdminDetailDTO;
import com.healthtrail.domain.health.message.dto.HealthAppMessageResendResultDTO;
import com.healthtrail.domain.health.message.query.HealthAppMessageAdminQuery;
import com.healthtrail.domain.health.message.dto.HealthAppMessageStatisticsDTO;
import com.healthtrail.domain.health.message.query.HealthAppMessageStatisticsQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 App 消息审计控制器。
 *
 * <p>该控制器当前提供：
 * 1. 查询消息审计列表
 * 2. 查询消息审计详情
 * 3. 对单条消息执行后台人工重发
 * 4. 对多条消息执行后台批量重发
 * 5. 查看消息统计看板
 *
 * <p>当前仍然不开放删除等更重操作，
 * 先把“看得到、能单条补救、能小批量补救”这条链路做稳。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/messages")
@Tag(name = "后台消息审计API", description = "后台 App 消息中心与Push发送审计接口")
public class HealthAppMessageController extends BaseController {

    private final AppMessageApplicationService appMessageApplicationService;

    /**
     * 分页查询消息审计列表。
     */
    @Operation(summary = "消息审计列表")
    @PreAuthorize("@permission.has('health:message:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthAppMessageAdminDTO>> list(HealthAppMessageAdminQuery query) {
        if (query.getTimeRangeColumn() == null) {
            query.setTimeRangeColumn("createTime");
        }
        return ResponseDTO.ok(appMessageApplicationService.getAdminMessageList(query));
    }

    /**
     * 查询消息统计看板。
     *
     * <p>该接口主要服务于后台消息运营页顶部统计卡片、分布图和趋势图。
     */
    @Operation(summary = "消息统计看板")
    @PreAuthorize("@permission.has('health:message:list')")
    @GetMapping("/statistics")
    public ResponseDTO<HealthAppMessageStatisticsDTO> statistics(HealthAppMessageStatisticsQuery query) {
        if (query.getTimeRangeColumn() == null) {
            query.setTimeRangeColumn("createTime");
        }
        return ResponseDTO.ok(appMessageApplicationService.getMessageStatistics(query));
    }

    /**
     * 查询消息审计详情。
     */
    @Operation(summary = "消息审计详情")
    @PreAuthorize("@permission.has('health:message:query')")
    @GetMapping("/{messageId}")
    public ResponseDTO<HealthAppMessageAdminDetailDTO> getInfo(@PathVariable @NotNull @Positive Long messageId) {
        return ResponseDTO.ok(appMessageApplicationService.getAdminMessageDetail(messageId));
    }

    /**
     * 后台人工重发指定消息。
     *
     * <p>该接口适用于以下场景：
     * 1. 设备注册晚于首次派发
     * 2. 首次发送失败后需要人工补发
     * 3. 运营或客服在排查过程中确认需要重新触达用户
     */
    @Operation(summary = "人工重发消息")
    @PreAuthorize("@permission.has('health:message:resend')")
    @AccessLog(title = "App消息重发", businessType = BusinessTypeEnum.OTHER)
    @PostMapping("/{messageId}/resend")
    public ResponseDTO<HealthAppMessageResendResultDTO> resend(@PathVariable @NotNull @Positive Long messageId) {
        return ResponseDTO.ok(appMessageApplicationService.resendMessage(messageId));
    }

    /**
     * 后台批量重发消息。
     *
     * <p>该接口适用于后台在列表页勾选多条消息后统一补发。
     * 当前采用“逐条执行、返回明细结果”的模式，
     * 即使其中某几条失败，也不会阻塞其他消息继续重发。
     */
    @Operation(summary = "批量重发消息")
    @PreAuthorize("@permission.has('health:message:resend')")
    @AccessLog(title = "App消息批量重发", businessType = BusinessTypeEnum.OTHER)
    @PostMapping("/batch-resend")
    public ResponseDTO<HealthAppMessageBatchResendResultDTO> batchResend(
        @Valid @RequestBody BatchResendHealthAppMessageCommand command) {
        return ResponseDTO.ok(appMessageApplicationService.batchResendMessages(command));
    }
}

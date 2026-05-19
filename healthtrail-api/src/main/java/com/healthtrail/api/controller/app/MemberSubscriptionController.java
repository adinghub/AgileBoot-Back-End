package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.system.member.MemberSubscriptionApplicationService;
import com.healthtrail.domain.system.member.command.ChangeMemberSubscriptionAutoRenewCommand;
import com.healthtrail.domain.system.member.command.SubscribeMemberCommand;
import com.healthtrail.domain.system.member.dto.MemberLevelDTO;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionDTO;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionOrderDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App端会员订阅控制器，提供会员开通、续费管理和订单查询能力。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/member-subscription")
@Tag(name = "App会员订阅API", description = "App端会员订阅接口")
public class MemberSubscriptionController extends BaseController {

    private final MemberSubscriptionApplicationService memberSubscriptionApplicationService;

    @Operation(summary = "可开通等级列表")
    @GetMapping("/levels")
    public ResponseDTO<List<MemberLevelDTO>> levels() {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberSubscriptionApplicationService.getAvailableLevels(userId));
    }

    @Operation(summary = "当前会员状态")
    @GetMapping("/current")
    public ResponseDTO<MemberSubscriptionDTO> current() {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberSubscriptionApplicationService.getCurrentSubscription(userId));
    }

    @Operation(summary = "最近会员订单")
    @GetMapping("/orders")
    public ResponseDTO<List<MemberSubscriptionOrderDTO>> orders() {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberSubscriptionApplicationService.getSubscriptionOrders(userId));
    }

    @Operation(summary = "开通会员")
    @PostMapping("/subscribe")
    public ResponseDTO<MemberSubscriptionDTO> subscribe(@Valid @RequestBody SubscribeMemberCommand command) {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberSubscriptionApplicationService.subscribe(userId, command));
    }

    @Operation(summary = "自动续费开关")
    @PostMapping("/auto-renew")
    public ResponseDTO<MemberSubscriptionDTO> autoRenew(
        @Valid @RequestBody ChangeMemberSubscriptionAutoRenewCommand command) {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberSubscriptionApplicationService.changeAutoRenew(userId, command));
    }
}

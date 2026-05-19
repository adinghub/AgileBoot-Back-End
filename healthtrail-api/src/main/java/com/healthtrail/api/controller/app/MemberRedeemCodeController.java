package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.system.member.MemberRedeemCodeApplicationService;
import com.healthtrail.domain.system.member.command.RedeemMemberCodeCommand;
import com.healthtrail.domain.system.member.dto.MemberSubscriptionDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App端会员兑换码控制器，提供兑换码核销能力。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/member-redeem-codes")
@Tag(name = "App会员兑换码API", description = "App端会员兑换码接口")
public class MemberRedeemCodeController extends BaseController {

    private final MemberRedeemCodeApplicationService memberRedeemCodeApplicationService;

    @Operation(summary = "兑换会员兑换码")
    @PostMapping("/redeem")
    public ResponseDTO<MemberSubscriptionDTO> redeem(@Valid @RequestBody RedeemMemberCodeCommand command) {
        Long userId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(memberRedeemCodeApplicationService.redeemCode(userId, command));
    }
}

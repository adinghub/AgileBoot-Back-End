package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberApplicationService;
import com.healthtrail.domain.system.member.command.GenerateMemberRedeemCodeCommand;
import com.healthtrail.domain.system.member.dto.MemberRedeemCodeBatchDTO;
import com.healthtrail.domain.system.member.dto.MemberRedeemCodeDTO;
import com.healthtrail.domain.system.member.query.MemberRedeemCodeQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台会员兑换码管理接口，提供兑换码的分页查询、批量生成和删除能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/member-redeem-codes")
@Tag(name = "后台会员兑换码API", description = "后台会员兑换码管理接口")
public class SysMemberRedeemCodeController extends BaseController {

    private final MemberApplicationService memberApplicationService;

    @Operation(summary = "会员兑换码列表")
    @PreAuthorize("@permission.has('system:memberRedeemCode:list')")
    @GetMapping
    public ResponseDTO<PageDTO<MemberRedeemCodeDTO>> list(MemberRedeemCodeQuery query) {
        return ResponseDTO.ok(memberApplicationService.getMemberRedeemCodePage(query));
    }

    @Operation(summary = "批量生成会员兑换码")
    @PreAuthorize("@permission.has('system:memberRedeemCode:generate')")
    @AccessLog(title = "会员兑换码", businessType = BusinessTypeEnum.ADD)
    @PostMapping("/generate")
    public ResponseDTO<MemberRedeemCodeBatchDTO> generate(@Valid @RequestBody GenerateMemberRedeemCodeCommand command) {
        return ResponseDTO.ok(memberApplicationService.generateRedeemCodes(command));
    }

    @Operation(summary = "删除会员兑换码")
    @PreAuthorize("@permission.has('system:memberRedeemCode:remove')")
    @AccessLog(title = "会员兑换码", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{memberRedeemCodeIds}")
    public ResponseDTO<Void> remove(@PathVariable String memberRedeemCodeIds) {
        List<Long> ids = Arrays.stream(memberRedeemCodeIds.split(","))
            .map(String::trim).filter(value -> !value.isEmpty()).map(Long::valueOf).collect(Collectors.toList());
        memberApplicationService.removeMemberRedeemCodes(ids);
        return ResponseDTO.ok();
    }
}

package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberGateApplicationService;
import com.healthtrail.domain.system.member.command.SaveMemberGateRuleCommand;
import com.healthtrail.domain.system.member.dto.MemberGateRuleDTO;
import com.healthtrail.domain.system.member.query.MemberGateQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台会员门禁规则管理接口，提供每个门禁点规则矩阵的查询和保存能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/member-gate-rules")
@Tag(name = "后台会员门禁规则API", description = "后台会员功能门禁规则管理接口")
public class SysMemberGateRuleController extends BaseController {

    private final MemberGateApplicationService memberGateApplicationService;

    @Operation(summary = "会员门禁规则列表")
    @PreAuthorize("@permission.has('system:memberGateRule:list')")
    @GetMapping
    public ResponseDTO<PageDTO<MemberGateRuleDTO>> list(MemberGateQuery query) {
        return ResponseDTO.ok(memberGateApplicationService.getMemberGateRulePage(query));
    }

    @Operation(summary = "会员门禁规则详情")
    @PreAuthorize("@permission.has('system:memberGateRule:query')")
    @GetMapping("/{memberGateId}")
    public ResponseDTO<MemberGateRuleDTO> getInfo(@PathVariable Long memberGateId) {
        return ResponseDTO.ok(memberGateApplicationService.getMemberGateRule(memberGateId));
    }

    @Operation(summary = "保存会员门禁规则")
    @PreAuthorize("@permission.has('system:memberGateRule:edit')")
    @AccessLog(title = "会员门禁规则", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{memberGateId}")
    public ResponseDTO<Void> save(@PathVariable Long memberGateId, @Valid @RequestBody SaveMemberGateRuleCommand command) {
        memberGateApplicationService.saveMemberGateRule(memberGateId, command);
        return ResponseDTO.ok();
    }
}

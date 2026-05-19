package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberApplicationService;
import com.healthtrail.domain.system.member.command.SaveMemberLevelFeatureCommand;
import com.healthtrail.domain.system.member.dto.MemberLevelFeatureDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
 * 后台会员等级权益矩阵管理接口，提供等级与权益绑定关系的查询和保存能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/member-level-features")
@Tag(name = "后台会员等级权益API", description = "后台会员等级权益矩阵接口")
public class SysMemberLevelFeatureController extends BaseController {

    private final MemberApplicationService memberApplicationService;

    @Operation(summary = "查询会员等级完整权益矩阵")
    @PreAuthorize("@permission.has('system:memberLevelFeature:query')")
    @GetMapping("/{memberLevelId}")
    public ResponseDTO<List<MemberLevelFeatureDTO>> getMatrix(@PathVariable Long memberLevelId) {
        return ResponseDTO.ok(memberApplicationService.getMemberLevelFeatureMatrix(memberLevelId));
    }

    @Operation(summary = "整等级全量保存权益矩阵")
    @PreAuthorize("@permission.has('system:memberLevelFeature:edit')")
    @AccessLog(title = "会员等级权益矩阵", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{memberLevelId}")
    public ResponseDTO<Void> save(@PathVariable Long memberLevelId,
        @Valid @RequestBody SaveMemberLevelFeatureCommand command) {
        command.setMemberLevelId(memberLevelId);
        memberApplicationService.saveMemberLevelFeatures(memberLevelId, command);
        return ResponseDTO.ok();
    }
}

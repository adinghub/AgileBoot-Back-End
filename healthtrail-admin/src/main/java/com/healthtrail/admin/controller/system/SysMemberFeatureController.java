package com.healthtrail.admin.controller.system;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.domain.system.member.MemberApplicationService;
import com.healthtrail.domain.system.member.command.AddMemberFeatureCommand;
import com.healthtrail.domain.system.member.command.UpdateMemberFeatureCommand;
import com.healthtrail.domain.system.member.dto.MemberFeatureDTO;
import com.healthtrail.domain.system.member.query.MemberFeatureQuery;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台会员权益管理接口，提供会员权益定义的增删改查能力。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/system/member-features")
@Tag(name = "后台会员权益API", description = "后台会员权益定义管理接口")
public class SysMemberFeatureController extends BaseController {

    private final MemberApplicationService memberApplicationService;

    @Operation(summary = "会员权益列表")
    @PreAuthorize("@permission.has('system:memberFeature:list')")
    @GetMapping
    public ResponseDTO<PageDTO<MemberFeatureDTO>> list(MemberFeatureQuery query) {
        return ResponseDTO.ok(memberApplicationService.getMemberFeaturePage(query));
    }

    @Operation(summary = "会员权益详情")
    @PreAuthorize("@permission.has('system:memberFeature:query')")
    @GetMapping("/{memberFeatureId}")
    public ResponseDTO<MemberFeatureDTO> getInfo(@PathVariable Long memberFeatureId) {
        return ResponseDTO.ok(memberApplicationService.getMemberFeatureInfo(memberFeatureId));
    }

    @Operation(summary = "新增会员权益")
    @PreAuthorize("@permission.has('system:memberFeature:add')")
    @AccessLog(title = "会员权益", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody AddMemberFeatureCommand command) {
        memberApplicationService.addMemberFeature(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改会员权益")
    @PreAuthorize("@permission.has('system:memberFeature:edit')")
    @AccessLog(title = "会员权益", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{memberFeatureId}")
    public ResponseDTO<Void> edit(@PathVariable Long memberFeatureId,
        @Valid @RequestBody UpdateMemberFeatureCommand command) {
        command.setMemberFeatureId(memberFeatureId);
        memberApplicationService.updateMemberFeature(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除会员权益")
    @PreAuthorize("@permission.has('system:memberFeature:remove')")
    @AccessLog(title = "会员权益", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{memberFeatureIds}")
    public ResponseDTO<Void> remove(@PathVariable String memberFeatureIds) {
        List<Long> ids = Arrays.stream(memberFeatureIds.split(","))
            .map(String::trim).filter(value -> !value.isEmpty()).map(Long::valueOf).collect(Collectors.toList());
        memberApplicationService.removeMemberFeatures(ids);
        return ResponseDTO.ok();
    }
}

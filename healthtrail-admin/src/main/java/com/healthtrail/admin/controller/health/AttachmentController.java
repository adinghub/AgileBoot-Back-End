package com.healthtrail.admin.controller.health;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.attachment.AttachmentApplicationService;
import com.healthtrail.domain.health.attachment.dto.AttachmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台附件控制器。
 *
 * <p>后台药品管理、药品单位管理都需要上传图片资源，
 * 因此这里提供一个统一的上传入口，减少页面侧重复接不同上传接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/health/attachments")
@Tag(name = "后台附件API", description = "后台药品图片与药品单位图标上传接口")
public class AttachmentController extends BaseController {

    private final AttachmentApplicationService attachmentApplicationService;

    /**
     * 上传附件。
     */
    @Operation(summary = "上传附件")
    @PreAuthorize("@permission.has('health:drug:add') or @permission.has('health:drug:edit') "
        + "or @permission.has('health:drugUnit:add') or @permission.has('health:drugUnit:edit')")
    @PostMapping("/upload")
    public ResponseDTO<AttachmentDTO> upload(@RequestParam("file") MultipartFile file,
        @RequestParam(value = "attachmentType", required = false) String attachmentType) {
        return ResponseDTO.ok(attachmentApplicationService.uploadAttachment(file, attachmentType, null));
    }
}

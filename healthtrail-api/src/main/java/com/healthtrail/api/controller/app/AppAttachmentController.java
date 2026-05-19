package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.domain.health.attachment.AttachmentApplicationService;
import com.healthtrail.domain.health.attachment.dto.AttachmentDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * App 端附件控制器。
 *
 * <p>个人药品图片由 App 用户自己上传，
 * 上传成功后前端只需要把返回的 `attachmentId` 再回填到新增/编辑药品接口即可。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/attachments")
@Tag(name = "App附件API", description = "App端药品图片上传接口")
public class AppAttachmentController extends BaseController {

    private final AttachmentApplicationService attachmentApplicationService;

    /**
     * 上传附件。
     */
    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public ResponseDTO<AttachmentDTO> upload(@RequestParam("file") MultipartFile file,
        @RequestParam(value = "attachmentType", required = false) String attachmentType) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(attachmentApplicationService.uploadAttachment(file, attachmentType, currentUserId));
    }
}

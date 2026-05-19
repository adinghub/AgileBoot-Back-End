package com.healthtrail.domain.health.attachment;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.constant.Constants.UploadSubDir;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.domain.health.attachment.db.AttachmentEntity;
import com.healthtrail.domain.health.attachment.db.AttachmentService;
import com.healthtrail.domain.health.attachment.dto.AttachmentDTO;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件应用服务。
 *
 * <p>这里统一承接药品图片、药品单位图标等上传动作，
 * 避免 Controller 里既写文件落盘逻辑，又写附件元数据入库逻辑。
 */
@Service
@RequiredArgsConstructor
public class AttachmentApplicationService {

    /** 附件数据库服务 */
    private final AttachmentService attachmentService;

    /**
     * 上传附件并写入统一附件表。
     *
     * @param file 上传文件
     * @param attachmentType 附件类型
     * @param ownerUserId 归属App用户ID，后台上传允许为空
     * @return 附件元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public AttachmentDTO uploadAttachment(MultipartFile file, String attachmentType, Long ownerUserId) {
        if (file == null) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_IS_EMPTY);
        }

        String normalizedAttachmentType = normalizeAttachmentType(attachmentType);
        String fileUrl = FileUploadUtils.upload(resolveUploadSubDir(normalizedAttachmentType), file);

        AttachmentEntity entity = new AttachmentEntity();
        entity.setOwnerUserId(ownerUserId);
        entity.setAttachmentType(normalizedAttachmentType);
        // storageProvider 直接记录当前运行时的真实存储实现，
        // 后续做排障、迁移或数据巡检时，可以一眼看出这条附件究竟在本地盘还是 MinIO。
        entity.setStorageProvider(FileUploadUtils.getStorageType());
        entity.setFileUrl(fileUrl);
        entity.setStoredFileName(FileNameUtil.getName(fileUrl));
        entity.setOriginalFileName(file.getOriginalFilename());
        entity.setFileSize(file.getSize());
        entity.setFileExtension(resolveFileExtension(file.getOriginalFilename(), fileUrl));
        entity.setContentType(file.getContentType());
        entity.setStatus(StatusEnum.ENABLE.getValue());
        entity.setDeleted(0);
        attachmentService.save(entity);
        return new AttachmentDTO(entity);
    }

    /**
     * 统一规范附件类型，避免表中出现大小写和空白不一致的问题。
     */
    private String normalizeAttachmentType(String attachmentType) {
        String normalizedAttachmentType = StrUtil.blankToDefault(
            StrUtil.trim(attachmentType), AttachmentTypeConstants.DRUG_IMAGE).toUpperCase(Locale.ROOT);
        if (!AttachmentTypeConstants.isSupported(normalizedAttachmentType)) {
            throw new ApiException(ErrorCode.Business.HEALTH_ATTACHMENT_TYPE_INVALID);
        }
        return normalizedAttachmentType;
    }

    /**
     * 根据附件类型选择实际上传目录。
     *
     * <p>当前虽然都走本地文件系统，
     * 但仍然按业务类型拆目录，便于后续做清理和资源盘点。
     */
    private String resolveUploadSubDir(String attachmentType) {
        if (Objects.equals(attachmentType, AttachmentTypeConstants.DRUG_UNIT_ICON)) {
            return UploadSubDir.DRUG_UNIT_ICON_PATH;
        }
        return UploadSubDir.DRUG_IMAGE_PATH;
    }

    /**
     * 提取文件后缀。
     *
     * <p>优先使用原始文件名后缀，
     * 原始文件名缺失时再兜底使用落盘后的存储文件名后缀。
     */
    private String resolveFileExtension(String originalFileName, String fileUrl) {
        String fileExtension = FileNameUtil.extName(StrUtil.blankToDefault(originalFileName, ""));
        if (StrUtil.isBlank(fileExtension)) {
            fileExtension = FileNameUtil.extName(StrUtil.blankToDefault(fileUrl, ""));
        }
        return StrUtil.blankToDefault(fileExtension, null);
    }
}

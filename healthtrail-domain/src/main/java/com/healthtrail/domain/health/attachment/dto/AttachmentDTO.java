package com.healthtrail.domain.health.attachment.dto;

import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.domain.health.attachment.db.AttachmentEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 附件返回对象。
 */
@Data
@NoArgsConstructor
public class AttachmentDTO {

    /** 附件主键ID。 */
    private Long attachmentId;

    /** 归属用户ID，后台上传时允许为空。 */
    private Long ownerUserId;

    /** 附件类型（如药品图片、单位图标）。 */
    private String attachmentType;

    /** 存储提供方（LOCAL/MINIO/OSS）。 */
    private String storageProvider;

    /** 文件访问地址（DTO层已转为完整可访问URL）。 */
    private String fileUrl;

    /** 存储端文件名。 */
    private String storedFileName;

    /** 用户上传时的原始文件名。 */
    private String originalFileName;

    /** 文件大小（字节）。 */
    private Long fileSize;

    /** 文件扩展名（不含点）。 */
    private String fileExtension;

    /** 文件MIME内容类型。 */
    private String contentType;

    /** 状态（0停用 1正常）。 */
    private Integer status;

    public AttachmentDTO(AttachmentEntity entity) {
        if (entity != null) {
            this.attachmentId = entity.getAttachmentId();
            this.ownerUserId = entity.getOwnerUserId();
            this.attachmentType = entity.getAttachmentType();
            this.storageProvider = entity.getStorageProvider();
            // DTO 层统一回填最终可访问地址，
            // 这样前端不需要关心数据库里存的是本地相对路径还是 MinIO object key。
            this.fileUrl = FileUploadUtils.getAccessUrl(entity.getFileUrl());
            this.storedFileName = entity.getStoredFileName();
            this.originalFileName = entity.getOriginalFileName();
            this.fileSize = entity.getFileSize();
            this.fileExtension = entity.getFileExtension();
            this.contentType = entity.getContentType();
            this.status = entity.getStatus();
        }
    }
}

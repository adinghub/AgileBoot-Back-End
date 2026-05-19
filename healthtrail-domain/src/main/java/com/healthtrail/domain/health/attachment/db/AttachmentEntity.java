package com.healthtrail.domain.health.attachment.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.healthtrail.common.core.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一附件表，存储文件元数据，业务表通过附件ID引用
 *
 * <p>当前附件表采用"独立元数据 + 业务表引用附件ID"的方式设计，
 * 好处是药品、药品单位等业务主表都能保持简洁：
 * 1. 主表只保存附件ID，不直接耦合文件路径细节
 * 2. 后续从本地存储切换到 OSS / MinIO 时，只需要调整附件上传与解析链路
 * 3. 如果未来某个业务需要多图，也可以在不改附件表的前提下继续扩展关系表
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("attachment")
@ApiModel(value = "AttachmentEntity对象", description = "统一附件表")
public class AttachmentEntity extends BaseEntity<AttachmentEntity> {

    private static final long serialVersionUID = 1L;

    /** 附件主键ID */
    @ApiModelProperty("附件ID")
    @TableId(value = "attachment_id", type = IdType.AUTO)
    private Long attachmentId;

    /** 附件的归属用户ID，后台上传时允许为空 */
    @ApiModelProperty("归属App用户ID，后台上传时允许为空")
    @TableField(value = "owner_user_id", updateStrategy = FieldStrategy.IGNORED)
    private Long ownerUserId;

    /** 附件类型，如药品图片DRUG_IMAGE、单位图标DRUG_UNIT_ICON */
    @ApiModelProperty("附件类型，例如 DRUG_IMAGE、DRUG_UNIT_ICON")
    @TableField("attachment_type")
    private String attachmentType;

    /** 存储提供方，如LOCAL、OSS、MinIO */
    @ApiModelProperty("存储提供方，当前固定为 LOCAL")
    @TableField("storage_provider")
    private String storageProvider;

    /** 文件访问的相对路径或URL */
    @ApiModelProperty("文件访问地址，当前保存相对路径")
    @TableField("file_url")
    private String fileUrl;

    /** 存储系统中的文件名 */
    @ApiModelProperty("存储文件名")
    @TableField("stored_file_name")
    private String storedFileName;

    /** 用户上传时的原始文件名 */
    @ApiModelProperty("原始文件名")
    @TableField(value = "original_file_name", updateStrategy = FieldStrategy.IGNORED)
    private String originalFileName;

    /** 文件大小，单位字节 */
    @ApiModelProperty("文件大小")
    @TableField(value = "file_size", updateStrategy = FieldStrategy.IGNORED)
    private Long fileSize;

    /** 文件扩展名，如jpg、png */
    @ApiModelProperty("文件后缀")
    @TableField(value = "file_extension", updateStrategy = FieldStrategy.IGNORED)
    private String fileExtension;

    /** 文件的MIME内容类型 */
    @ApiModelProperty("内容类型")
    @TableField(value = "content_type", updateStrategy = FieldStrategy.IGNORED)
    private String contentType;

    /** 附件状态，1正常 0停用 */
    @ApiModelProperty("状态（1正常 0停用）")
    @TableField("status")
    private Integer status;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.attachmentId;
    }
}

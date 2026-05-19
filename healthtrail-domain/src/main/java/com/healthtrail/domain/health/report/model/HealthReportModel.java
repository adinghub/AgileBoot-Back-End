package com.healthtrail.domain.health.report.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.health.HealthProcessingStatusEnum;
import com.healthtrail.common.enums.health.HealthReportParseStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.report.command.AddHealthReportCommand;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 体检报告领域模型。
 *
 * <p>报告上传虽然流程不复杂，但仍有几条必须统一的规则：
 * 1. 报告必须归属于当前用户自己的成员
 * 2. 文件上传完成后要补齐文件元信息
 * 3. 刚上传的报告默认进入待解析状态
 * 4. 未传报告名称时自动使用原始文件名兜底
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthReportModel extends HealthReportEntity {

    public HealthReportModel(HealthReportEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 装载新增命令和上传后的文件信息。
     */
    public void loadAddCommand(AddHealthReportCommand command, Long ownerUserId, String fileUrl, String storedFileName,
        String originalFileName, Long fileSize, String fileExtension) {
        if (command == null) {
            return;
        }

        BeanUtil.copyProperties(command, this, "reportId");
        this.setOwnerUserId(ownerUserId);
        this.setFileUrl(fileUrl);
        this.setStoredFileName(storedFileName);
        this.setOriginalFileName(originalFileName);
        this.setFileSize(fileSize);
        this.setFileExtension(fileExtension);
        this.setParseStatus(HealthReportParseStatusEnum.WAIT_PARSE.getValue());
        // 新上传的报告还没有形成可复用的结构化结果，
        // 因此这里显式把“AI解析缓存标记”初始化为 0。
        //
        // 后续只有在后台真正完成了一次可复用的结构化解析后，
        // 才会把它改成 1，用来控制“再次解析时是否直接复用已有结果、跳过大模型”。
        this.setAiParseCached(0);
        this.setOcrStatus(HealthProcessingStatusEnum.PENDING.getValue());
        this.setAiSummaryStatus(HealthProcessingStatusEnum.PENDING.getValue());
        this.setAnalysisSummary(null);

        if (StrUtil.isBlank(this.getReportName())) {
            this.setReportName(FileNameUtil.mainName(StrUtil.blankToDefault(originalFileName, storedFileName)));
        }
    }

    /**
     * 校验报告是否属于指定 App 用户。
     */
    public void checkOwnedByUser(Long ownerUserId) {
        if (getReportId() == null || ownerUserId == null || !ownerUserId.equals(getOwnerUserId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, getReportId(), "体检报告");
        }
    }

    /**
     * 校验报告字段。
     */
    public void checkFields() {
        if (StrUtil.isBlank(getReportName())) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_FAILED, "报告名称为空");
        }
        if (getReportName().length() > 100) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_NAME_TOO_LONG);
        }
        if (getParseStatus() == null) {
            setParseStatus(HealthReportParseStatusEnum.WAIT_PARSE.getValue());
        }
        if (getDeleted() == null) {
            setDeleted(0);
        }
    }
}

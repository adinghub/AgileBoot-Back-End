package com.healthtrail.domain.health.report.dto;

import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 体检报告返回对象。
 */
@Data
@NoArgsConstructor
public class HealthReportDTO {

    /** 报告ID。 */
    private Long reportId;

    /** 成员ID。 */
    private Long memberId;

    /**
     * 成员编码。
     *
     * <p>报告链路补齐该字段后，前端与 App 在成员姓名缺失时
     * 就可以统一退回到业务编码，而不是继续暴露内部主键。
     */
    private String memberCode;

    /** 成员姓名。 */
    private String memberName;

    /** 报告名称。 */
    private String reportName;

    /** 报告类型。 */
    private String reportType;

    /**
     * 后台结构化解析识别出的报告细分类型。
     *
     * <p>例如用户手填“血液检查”，而模型进一步识别出更准确的“血常规”。
     * App 端可以利用这个字段做更友好的展示拼接，而不是直接覆盖用户原始输入。
     */
    private String recognizedReportType;

    /** 医院名称。 */
    private String hospitalName;

    /** 报告日期。 */
    private Date reportDate;

    /** 文件地址。 */
    private String fileUrl;

    /** 原始文件名。 */
    private String originalFileName;

    /** 文件大小。 */
    private Long fileSize;

    /** 文件扩展名。 */
    private String fileExtension;

    /** 报告解析状态（待处理/解析中/已完成/失败）。 */
    private Integer parseStatus;

    /** OCR状态。 */
    private Integer ocrStatus;

    /** OCR文本快照。 */
    private String ocrTextSnapshot;

    /** OCR时间。 */
    private java.util.Date ocrTime;

    /** AI摘要状态。 */
    private Integer aiSummaryStatus;

    /** AI摘要内容。 */
    private String aiSummaryContent;

    /** AI摘要时间。 */
    private java.util.Date aiSummaryTime;

    /** 分析摘要。 */
    private String analysisSummary;

    /** 结果解读。 */
    private String resultInterpretation;

    /** 备注。 */
    private String remark;

    /**
     * 当前登录账号访问该报告所属成员的来源。
     */
    private String accessSource;

    /**
     * 访问来源名称。
     */
    private String accessSourceName;

    /**
     * 当前登录账号对该报告所属成员的访问角色。
     */
    private String accessRole;

    /**
     * 访问角色名称。
     */
    private String accessRoleName;

    /**
     * 是否为主账号。
     */
    private Boolean isOwner;

    /**
     * 是否允许编辑报告。
     */
    private Boolean canEdit;

    /**
     * 是否允许管理报告协同。
     *
     * <p>当前规则与“是否为主账号”保持一致，
     * 这样 App 端无需再次推断权限口径。
     */
    private Boolean canShare;

    /** 创建时间。 */
    private Date createTime;

    public HealthReportDTO(HealthReportEntity entity) {
        if (entity != null) {
            this.reportId = entity.getReportId();
            this.memberId = entity.getMemberId();
            this.reportName = entity.getReportName();
            this.reportType = entity.getReportType();
            this.recognizedReportType = entity.getRecognizedReportType();
            this.hospitalName = entity.getHospitalName();
            this.reportDate = entity.getReportDate();
            // 报告原文件地址统一在 DTO 层转成可直接访问的 URL。
            // 这样 App 端点开报告时，不需要知道后端当前到底走本地静态资源还是 MinIO。
            this.fileUrl = FileUploadUtils.getAccessUrl(entity.getFileUrl());
            this.originalFileName = entity.getOriginalFileName();
            this.fileSize = entity.getFileSize();
            this.fileExtension = entity.getFileExtension();
            this.parseStatus = entity.getParseStatus();
            this.ocrStatus = entity.getOcrStatus();
            this.ocrTextSnapshot = entity.getOcrTextSnapshot();
            this.ocrTime = entity.getOcrTime();
            this.aiSummaryStatus = entity.getAiSummaryStatus();
            this.aiSummaryContent = entity.getAiSummaryContent();
            this.aiSummaryTime = entity.getAiSummaryTime();
            this.analysisSummary = entity.getAnalysisSummary();
            this.resultInterpretation = entity.getResultInterpretation();
            this.remark = entity.getRemark();
            this.createTime = entity.getCreateTime();
        }
    }
}

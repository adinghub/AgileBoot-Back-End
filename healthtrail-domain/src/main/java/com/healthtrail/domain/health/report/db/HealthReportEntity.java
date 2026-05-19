package com.healthtrail.domain.health.report.db;

import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 体检报告表，记录报告元信息、文件及AI解析状态
 *
 * <p>当前阶段的报告模块先聚焦"文件归档 + 元信息管理"：
 * 1. 保存文件地址与原始文件信息
 * 2. 绑定成员和报告日期
 * 3. 预留解析状态与摘要字段，方便后续接规则分析或 AI 解析
 *
 * @author valarchie
 */
@Getter
@Setter
@TableName("report")
@ApiModel(value = "HealthReportEntity对象", description = "健康系统体检报告表")
public class HealthReportEntity extends BaseEntity<HealthReportEntity> {

    private static final long serialVersionUID = 1L;

    /** 报告主键ID */
    @ApiModelProperty("报告ID")
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /** 报告归属用户ID */
    @ApiModelProperty("归属App用户ID")
    @TableField("owner_user_id")
    private Long ownerUserId;

    /** 报告所属的家庭成员ID */
    @ApiModelProperty("家庭成员ID")
    @TableField("member_id")
    private Long memberId;

    /** 报告显示名称 */
    @ApiModelProperty("报告名称")
    @TableField("report_name")
    private String reportName;

    /** 报告类型，如体检报告、化验单等 */
    @ApiModelProperty("报告类型")
    @TableField("report_type")
    private String reportType;

    /** 后台自动识别出的报告细分类型 */
    @ApiModelProperty("后台识别出的报告细分类型")
    @TableField("recognized_report_type")
    private String recognizedReportType;

    /** 出具报告的医院名称 */
    @ApiModelProperty("医院名称")
    @TableField("hospital_name")
    private String hospitalName;

    /** 报告的检查日期 */
    @ApiModelProperty("报告日期")
    @TableField("report_date")
    private Date reportDate;

    /** 报告文件的访问URL */
    @ApiModelProperty("文件访问地址")
    @TableField("file_url")
    private String fileUrl;

    /** 存储系统中的文件名 */
    @ApiModelProperty("存储文件名")
    @TableField("stored_file_name")
    private String storedFileName;

    /** 用户上传时的原始文件名 */
    @ApiModelProperty("原始文件名")
    @TableField("original_file_name")
    private String originalFileName;

    /** 文件大小，单位字节 */
    @ApiModelProperty("文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    /** 文件扩展名 */
    @ApiModelProperty("文件后缀")
    @TableField("file_extension")
    private String fileExtension;

    /** 报告的解析处理状态 */
    @ApiModelProperty("解析状态")
    @TableField("parse_status")
    private Integer parseStatus;

    /** AI解析结果是否已缓存 */
    @ApiModelProperty("AI解析结果缓存标记")
    @TableField("ai_parse_cached")
    private Integer aiParseCached;

    /** OCR文字识别处理状态 */
    @ApiModelProperty("OCR占位处理状态")
    @TableField("ocr_status")
    private Integer ocrStatus;

    /** OCR识别结果的文本快照 */
    @ApiModelProperty("OCR占位文本快照")
    @TableField("ocr_text_snapshot")
    private String ocrTextSnapshot;

    /** OCR处理完成时间 */
    @ApiModelProperty("OCR占位处理时间")
    @TableField("ocr_time")
    private Date ocrTime;

    /** AI智能总结处理状态 */
    @ApiModelProperty("AI总结占位状态")
    @TableField("ai_summary_status")
    private Integer aiSummaryStatus;

    /** AI智能总结的内容结果 */
    @ApiModelProperty("AI总结占位内容")
    @TableField("ai_summary_content")
    private String aiSummaryContent;

    /** AI总结处理完成时间 */
    @ApiModelProperty("AI总结占位处理时间")
    @TableField("ai_summary_time")
    private Date aiSummaryTime;

    /** 报告分析的摘要结果 */
    @ApiModelProperty("解析摘要")
    @TableField("analysis_summary")
    private String analysisSummary;

    /** 报告结果的解读说明 */
    @ApiModelProperty("结果解读")
    @TableField("result_interpretation")
    private String resultInterpretation;

    /** 备注说明 */
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.reportId;
    }
}

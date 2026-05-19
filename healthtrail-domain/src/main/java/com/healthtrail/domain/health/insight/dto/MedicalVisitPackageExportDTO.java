package com.healthtrail.domain.health.insight.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 就医资料包导出数据。
 *
 * <p>该 DTO 返回的是结构化导出内容，不直接生成 PDF/图片文件。
 * 这样 App 可以先做预览、复制、系统分享或本地渲染，后端也能统一控制资料包口径。
 */
@Data
public class MedicalVisitPackageExportDTO {

    /** 生成时间 */
    private Date generatedTime;

    /** 资料包标题 */
    private String packageTitle;

    /** 资料包摘要 */
    private String summary;

    /** 成员信息 */
    private MemberSectionDTO member = new MemberSectionDTO();

    /** 慢病专项列表 */
    private List<ChronicProfileSectionDTO> chronicProfiles = new ArrayList<>();

    /** 近期报告列表 */
    private List<ReportSectionDTO> recentReports = new ArrayList<>();

    /** 用药计划列表 */
    private List<MedicationPlanSectionDTO> medicationPlans = new ArrayList<>();

    /** 健康问题列表 */
    private List<ProblemSectionDTO> healthProblems = new ArrayList<>();

    /** 导出清单列表 */
    private List<String> checklist = new ArrayList<>();

    @Data
    public static class MemberSectionDTO {
        /** 成员ID */
        private Long memberId;
        /** 成员编码 */
        private String memberCode;
        /** 成员姓名 */
        private String memberName;
        /** 性别 */
        private Integer gender;
        /** 出生日期 */
        private Date birthday;
        /** 与用户关系 */
        private String relationType;
        /** 身高 */
        private BigDecimal height;
        /** 体重 */
        private BigDecimal weight;
        /** 血型 */
        private String bloodType;
        /** 过敏史 */
        private String allergyHistory;
        /** 慢病史 */
        private String chronicHistory;
        /** 备注 */
        private String remark;
    }

    @Data
    public static class ChronicProfileSectionDTO {
        /** 慢病专项档案ID */
        private Long profileId;
        /** 病种编码 */
        private String diseaseCode;
        /** 病种名称 */
        private String diseaseName;
        /** 专项档案状态 */
        private Integer profileStatus;
        /** 风险等级 */
        private String riskLevel;
        /** 确诊日期 */
        private Date diagnosedDate;
        /** 管理目标 */
        private String targetSummary;
        /** 当前情况摘要 */
        private String currentSummary;
        /** 最近复盘日期 */
        private Date lastReviewDate;
    }

    @Data
    public static class ReportSectionDTO {
        /** 报告ID */
        private Long reportId;
        /** 报告名称 */
        private String reportName;
        /** 报告类型 */
        private String reportType;
        /** 医院名称 */
        private String hospitalName;
        /** 报告日期 */
        private Date reportDate;
        /** 分析摘要 */
        private String analysisSummary;
        /** 异常指标列表 */
        private List<ReportItemSectionDTO> abnormalItems = new ArrayList<>();
    }

    @Data
    public static class ReportItemSectionDTO {
        /** 指标项ID */
        private Long itemId;
        /** 指标项名称 */
        private String itemName;
        /** 结果值 */
        private String resultValue;
        /** 结果单位 */
        private String resultUnit;
        /** 参考范围文本 */
        private String referenceText;
        /** 异常标记 */
        private Integer abnormalFlag;
        /** 指标解读 */
        private String itemInterpretation;
    }

    @Data
    public static class MedicationPlanSectionDTO {
        /** 用药计划ID */
        private Long planId;
        /** 计划编码 */
        private String planCode;
        /** 药品ID */
        private Long drugId;
        /** 药品名称 */
        private String drugName;
        /** 开始日期 */
        private Date startDate;
        /** 结束日期 */
        private Date endDate;
        /** 剂量 */
        private BigDecimal doseAmount;
        /** 剂量单位 */
        private String doseUnit;
        /** 用药频次 */
        private String frequencyType;
        /** 就餐时间类型 */
        private String mealTiming;
        /** 备注 */
        private String remark;
    }

    @Data
    public static class ProblemSectionDTO {
        /** 问题ID */
        private Long problemId;
        /** 问题名称 */
        private String problemName;
        /** 问题类型 */
        private String problemType;
        /** 问题状态 */
        private Integer problemStatus;
        /** 问题状态名称 */
        private String problemStatusName;
        /** 风险等级 */
        private Integer riskLevel;
        /** 风险等级名称 */
        private String riskLevelName;
        /** 首次发现日期 */
        private Date firstFoundDate;
        /** 最近随访日期 */
        private Date lastFollowDate;
        /** 问题摘要 */
        private String summary;
    }
}

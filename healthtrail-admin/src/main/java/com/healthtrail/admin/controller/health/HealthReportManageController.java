package com.healthtrail.admin.controller.health;

import com.healthtrail.admin.customize.aop.accessLog.AccessLog;
import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.report.HealthReportApplicationService;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportService;
import com.healthtrail.domain.health.report.dto.HealthReportAiSummaryDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAnalysisDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceDTO;
import com.healthtrail.domain.health.report.dto.HealthReportDTO;
import com.healthtrail.domain.health.report.dto.HealthReportItemDTO;
import com.healthtrail.domain.health.report.dto.HealthReportTrendDTO;
import com.healthtrail.domain.health.report.query.HealthReportQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台体检报告管理控制器。
 *
 * <p>后台管理这里尽量直接复用 App 端已经稳定跑通的报告分析能力，
 * 只在“列表查询”层面补一个全局视角入口，避免后台与 App 两边出现两套分析逻辑。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/health/reports")
@Tag(name = "后台体检报告API", description = "后台体检报告管理接口")
public class HealthReportManageController extends BaseController {

    private final HealthReportService reportService;
    private final HealthFamilyMemberService familyMemberService;
    private final HealthReportApplicationService healthReportApplicationService;

    @Operation(summary = "后台体检报告列表")
    @PreAuthorize("@permission.has('health:report:list')")
    @GetMapping
    public ResponseDTO<PageDTO<HealthReportDTO>> list(HealthReportQuery query) {
        Page<HealthReportEntity> page = reportService.page(query.toPage(), query.addQueryCondition());
        List<HealthReportDTO> records = page.getRecords().stream()
            .map(this::buildReportDTO)
            .collect(Collectors.toList());
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "后台体检报告详情")
    @PreAuthorize("@permission.has('health:report:query')")
    @GetMapping("/{reportId}")
    public ResponseDTO<HealthReportDTO> getInfo(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        return ResponseDTO.ok(healthReportApplicationService.getReportInfo(reportId, reportEntity.getOwnerUserId()));
    }

    @Operation(summary = "后台体检报告指标列表")
    @PreAuthorize("@permission.has('health:report:query')")
    @GetMapping("/{reportId}/items")
    public ResponseDTO<List<HealthReportItemDTO>> items(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        return ResponseDTO.ok(healthReportApplicationService.getReportItems(reportId, reportEntity.getOwnerUserId()));
    }

    @Operation(summary = "后台体检报告分析摘要")
    @PreAuthorize("@permission.has('health:report:query')")
    @GetMapping("/{reportId}/analysis")
    public ResponseDTO<HealthReportAnalysisDTO> analysis(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        return ResponseDTO.ok(healthReportApplicationService.getReportAnalysis(reportId, reportEntity.getOwnerUserId()));
    }

    @Operation(summary = "后台体检报告异常建议")
    @PreAuthorize("@permission.has('health:report:query')")
    @GetMapping("/{reportId}/advice")
    public ResponseDTO<HealthReportAdviceDTO> advice(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        return ResponseDTO.ok(healthReportApplicationService.getReportAdvice(reportId, reportEntity.getOwnerUserId()));
    }

    @Operation(summary = "后台体检报告趋势对比")
    @PreAuthorize("@permission.has('health:report:query')")
    @GetMapping("/{reportId}/trends")
    public ResponseDTO<HealthReportTrendDTO> trends(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        return ResponseDTO.ok(healthReportApplicationService.getReportTrend(reportId, reportEntity.getOwnerUserId()));
    }

    @Operation(summary = "后台重新生成AI智能总结")
    @PreAuthorize("@permission.has('health:report:ai')")
    @AccessLog(title = "体检报告AI总结", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{reportId}/ai-summary")
    public ResponseDTO<HealthReportAiSummaryDTO> generateAiSummary(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        return ResponseDTO.ok(healthReportApplicationService.generateAiSummary(reportId, reportEntity.getOwnerUserId()));
    }

    @Operation(summary = "后台删除体检报告")
    @PreAuthorize("@permission.has('health:report:remove')")
    @AccessLog(title = "体检报告", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{reportId}")
    public ResponseDTO<Void> remove(@PathVariable Long reportId) {
        HealthReportEntity reportEntity = getRequiredReport(reportId);
        healthReportApplicationService.removeReport(reportId, reportEntity.getOwnerUserId());
        return ResponseDTO.ok();
    }

    private HealthReportDTO buildReportDTO(HealthReportEntity entity) {
        HealthReportDTO dto = new HealthReportDTO(entity);
        if (entity.getMemberId() != null) {
            HealthFamilyMemberEntity memberEntity = familyMemberService.getById(entity.getMemberId());
            // 后台报告管理也和 App 端保持同一口径：
            // 姓名缺失时由前端统一回退成员编码，不再展示内部 ID 风格的旧文案。
            dto.setMemberName(memberEntity == null ? "已删除成员" : memberEntity.getMemberName());
            dto.setMemberCode(memberEntity == null ? null : memberEntity.getMemberCode());
        }
        return dto;
    }

    private HealthReportEntity getRequiredReport(Long reportId) {
        HealthReportEntity reportEntity = reportService.getById(reportId);
        if (reportEntity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, reportId, "体检报告");
        }
        return reportEntity;
    }
}

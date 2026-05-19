package com.healthtrail.api.controller.app;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.dto.ResponseDTO;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.health.report.HealthReportApplicationService;
import com.healthtrail.domain.health.family.command.CreateFamilyShareInviteCommand;
import com.healthtrail.domain.health.family.dto.FamilyMemberShareDTO;
import com.healthtrail.domain.health.family.dto.FamilyShareInviteDTO;
import com.healthtrail.domain.health.report.command.AddHealthReportCommand;
import com.healthtrail.domain.health.report.command.HealthReportExportDataCommand;
import com.healthtrail.domain.health.report.command.SaveHealthReportItemsCommand;
import com.healthtrail.domain.health.report.dto.HealthReportAiSummaryDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAnalysisDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceDTO;
import com.healthtrail.domain.health.report.dto.HealthReportExportDataDTO;
import com.healthtrail.domain.health.report.dto.HealthReportDTO;
import com.healthtrail.domain.health.report.dto.HealthReportItemDTO;
import com.healthtrail.domain.health.report.dto.HealthReportTrendDTO;
import com.healthtrail.domain.health.problem.dto.HealthProblemDTO;
import com.healthtrail.domain.health.report.query.HealthReportQuery;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * App 端体检报告控制器。
 *
 * <p>当前阶段已经覆盖两层能力：
 * 1. 上传报告文件
 * 2. 查看列表和详情
 * 3. 维护报告指标结果
 * 4. 查看报告分析摘要
 * 5. 删除自己的报告
 * 6. 手动重新解析报告文件
 *
 * <p>控制器层不再暴露 OCR 接口。报告图片解析统一通过 AI 视觉能力或人工补录完成，
 * 避免 App 误认为服务端仍提供云 OCR / 本地 OCR 识别。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/app/reports")
@Tag(name = "体检报告API", description = "App端体检报告上传与管理接口")
public class HealthReportController extends BaseController {

    private final HealthReportApplicationService healthReportApplicationService;

    /**
     * 分页查询当前用户的体检报告列表。
     */
    @Operation(summary = "体检报告列表")
    @GetMapping
    public ResponseDTO<PageDTO<HealthReportDTO>> list(HealthReportQuery query) {
        query.setOwnerUserId(AuthenticationUtils.getAppLoginUser().getUserId());
        return ResponseDTO.ok(healthReportApplicationService.getReportList(query));
    }

    /**
     * 查询体检报告详情。
     */
    @Operation(summary = "体检报告详情")
    @GetMapping("/{reportId}")
    public ResponseDTO<HealthReportDTO> getInfo(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportInfo(reportId, currentUserId));
    }

    /**
     * 查询当前报告对应成员的协同账号列表。
     *
     * <p>这里虽然底层仍然是成员共享关系，
     * 但按 `reportId` 暴露能让报告详情页直接完成协同管理，不需要前端额外再拼成员接口。
     */
    @Operation(summary = "报告协同账号列表")
    @GetMapping("/{reportId}/collaborators")
    public ResponseDTO<List<FamilyMemberShareDTO>> collaborators(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportCollaboratorList(reportId, currentUserId));
    }

    /**
     * 查询当前报告对应成员的共享邀请列表。
     */
    @Operation(summary = "报告共享邀请列表")
    @GetMapping("/{reportId}/share-invites")
    public ResponseDTO<List<FamilyShareInviteDTO>> shareInvites(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportShareInviteList(reportId, currentUserId));
    }

    /**
     * 为当前报告对应成员创建共享邀请。
     */
    @Operation(summary = "创建报告共享邀请")
    @PostMapping("/{reportId}/share-invites")
    public ResponseDTO<FamilyShareInviteDTO> createShareInvite(@PathVariable Long reportId,
        @Valid @RequestBody CreateFamilyShareInviteCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.createReportShareInvite(reportId, command, currentUserId));
    }

    /**
     * 取消当前报告对应成员的一条共享邀请。
     */
    @Operation(summary = "取消报告共享邀请")
    @DeleteMapping("/{reportId}/share-invites/{inviteId}")
    public ResponseDTO<Void> cancelShareInvite(@PathVariable Long reportId, @PathVariable Long inviteId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthReportApplicationService.cancelReportShareInvite(reportId, inviteId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 移除当前报告对应成员的一位协同账号。
     */
    @Operation(summary = "移除报告协同账号")
    @DeleteMapping("/{reportId}/collaborators/{shareId}")
    public ResponseDTO<Void> removeCollaborator(@PathVariable Long reportId, @PathVariable Long shareId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthReportApplicationService.removeReportCollaborator(reportId, shareId, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 上传体检报告。
     *
     * <p>这里采用 multipart/form-data 方式：
     * 1. `file` 传真实文件
     * 2. 其他字段走表单参数绑定到命令对象
     */
    @Operation(summary = "上传体检报告")
    @PostMapping
    public ResponseDTO<HealthReportDTO> upload(@Valid @ModelAttribute AddHealthReportCommand addCommand,
        @RequestParam("file") MultipartFile file) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(
            healthReportApplicationService.uploadReport(addCommand, file, currentUserId),
            "报告上传成功，解析任务已提交队列，请稍后查看。");
    }

    /**
     * 查询指定报告下的指标结果列表。
     *
     * <p>当前阶段的“结构化结果”采用报告维度挂载的方式：
     * 一份报告可以维护多条指标，例如血糖、血脂、白细胞等。
     */
    @Operation(summary = "体检报告指标列表")
    @GetMapping("/{reportId}/items")
    public ResponseDTO<List<HealthReportItemDTO>> getReportItems(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportItems(reportId, currentUserId));
    }

    /**
     * 覆盖保存指定报告下的指标结果。
     *
     * <p>这里采用“整份覆盖保存”而不是单条零散增改删，主要是因为：
     * 1. 前端通常一次性录入整张报告
     * 2. 这样可以减少前端对比与多次提交复杂度
     * 3. 后端也能在一次事务里统一重算异常项和摘要
     */
    @Operation(summary = "保存体检报告指标")
    @PutMapping("/{reportId}/items")
    public ResponseDTO<Void> saveReportItems(@PathVariable Long reportId,
        @Valid @RequestBody SaveHealthReportItemsCommand saveCommand) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthReportApplicationService.saveReportItems(reportId, saveCommand, currentUserId);
        return ResponseDTO.ok();
    }

    /**
     * 查询报告分析摘要。
     *
     * <p>分析摘要并不是 AI 诊断，而是基于已录入指标做的规则化统计结果，
     * 用于前端先快速展示“总项数、异常项数、异常明细摘要”。
     */
    @Operation(summary = "体检报告分析摘要")
    @GetMapping("/{reportId}/analysis")
    public ResponseDTO<HealthReportAnalysisDTO> getReportAnalysis(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportAnalysis(reportId, currentUserId));
    }

    /**
     * 查询报告异常建议。
     *
     * <p>当前建议是“规则化业务建议”，
     * 目的是帮助 App 在报告详情页给出后续动作引导，
     * 例如复查、持续观察、查看现有用药计划等。
     */
    @Operation(summary = "体检报告异常建议")
    @GetMapping("/{reportId}/advice")
    public ResponseDTO<HealthReportAdviceDTO> getReportAdvice(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportAdvice(reportId, currentUserId));
    }

    /**
     * 查询报告趋势对比。
     */
    @Operation(summary = "体检报告趋势对比")
    @GetMapping("/{reportId}/trends")
    public ResponseDTO<HealthReportTrendDTO> getReportTrends(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportTrend(reportId, currentUserId));
    }

    /**
     * 查询报告关联健康问题。
     */
    @Operation(summary = "检查报告关联健康问题")
    @GetMapping("/{reportId}/health-problems")
    public ResponseDTO<List<HealthProblemDTO>> getReportHealthProblems(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.getReportHealthProblems(reportId, currentUserId));
    }

    /**
     * 手动触发报告文件重新解析。
     *
     * <p>该接口替代历史 OCR 占位入口，语义更贴近当前实现：
     * 1. 文本型 PDF / 文本文件走服务端文本抽取和结构化；
     * 2. 图片文件仅在 AI 视觉能力可用时解析；
     * 3. 不再调用任何 OCR 供应商或本地 OCR 命令。
     *
     * <p>这里刻意只返回空成功响应，不直接回传详情对象，原因是：
     * 1. 重新解析本质上是“触发动作”，不是“查询详情”；
     * 2. App 端已经具备独立拉取详情的能力，动作成功后再刷新即可；
     * 3. 这样可以把动作链路和详情序列化链路彻底拆开，降低复杂对象回传时的异常风险。
     */
    @Operation(summary = "体检报告文件重新解析")
    @PostMapping("/{reportId}/parse")
    public ResponseDTO<Void> triggerParse(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthReportApplicationService.triggerReportParse(reportId, currentUserId);
        return ResponseDTO.ok("报告解析任务已提交队列，请稍后查看。");
    }

    /**
     * 触发 AI 智能总结生成。
     */
    @Operation(summary = "体检报告AI智能总结生成")
    @PostMapping("/{reportId}/ai-summary")
    public ResponseDTO<HealthReportAiSummaryDTO> generateAiSummary(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.generateAiSummary(reportId, currentUserId));
    }

    /**
     * 生成报告导出数据。
     *
     * <p>这里返回的是“最终可导出的结构化内容”，而不是直接推文件流，原因是：
     * 1. App 端当前仍然需要本地生成图片预览；
     * 2. 系统分享、保存图片、复制摘要三条链路都能复用同一份服务端结果；
     * 3. 会员门禁和导出口径可以在这里统一由后端裁决。
     */
    @Operation(summary = "体检报告导出数据")
    @PostMapping("/{reportId}/export-data")
    public ResponseDTO<HealthReportExportDataDTO> exportData(@PathVariable Long reportId,
        @RequestBody(required = false) HealthReportExportDataCommand command) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        return ResponseDTO.ok(healthReportApplicationService.buildReportExportData(reportId, command, currentUserId));
    }

    /**
     * 删除体检报告。
     */
    @Operation(summary = "删除体检报告")
    @DeleteMapping("/{reportId}")
    public ResponseDTO<Void> remove(@PathVariable Long reportId) {
        Long currentUserId = AuthenticationUtils.getAppLoginUser().getUserId();
        healthReportApplicationService.removeReport(reportId, currentUserId);
        return ResponseDTO.ok();
    }
}

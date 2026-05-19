package com.healthtrail.domain.health.report;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.healthtrail.common.constant.Constants.UploadSubDir;
import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSceneEnum;
import com.healthtrail.common.enums.health.HealthAppMessageSendStatusEnum;
import com.healthtrail.common.enums.health.HealthFollowUpRiskLevelEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetAnchorEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTargetPageEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskTypeEnum;
import com.healthtrail.common.enums.health.HealthProcessingStatusEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.enums.health.HealthReportParseStatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.common.utils.i18n.HealthAppI18n;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpNavigationDTO;
import com.healthtrail.domain.health.dashboard.dto.HealthFollowUpRecommendedActionDTO;
import com.healthtrail.domain.health.device.dto.HealthAppPushPayloadDTO;
import com.healthtrail.domain.health.family.FamilyMemberAccessService;
import com.healthtrail.domain.health.family.FamilyMemberApplicationService;
import com.healthtrail.domain.health.family.command.CreateFamilyShareInviteCommand;
import com.healthtrail.domain.health.family.dto.FamilyMemberAccessContextDTO;
import com.healthtrail.domain.health.family.dto.FamilyMemberShareDTO;
import com.healthtrail.domain.health.family.dto.FamilyShareInviteDTO;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberEntity;
import com.healthtrail.domain.health.family.db.HealthFamilyMemberService;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanEntity;
import com.healthtrail.domain.health.medication.db.HealthMedicationPlanService;
import com.healthtrail.domain.health.message.AppMessageApplicationService;
import com.healthtrail.domain.health.message.db.HealthAppMessageEntity;
import com.healthtrail.domain.health.message.dto.HealthAppMessageCreateRequest;
import com.healthtrail.domain.health.problem.db.HealthProblemEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemEvidenceEntity;
import com.healthtrail.domain.health.problem.db.HealthProblemEvidenceService;
import com.healthtrail.domain.health.problem.db.HealthProblemService;
import com.healthtrail.domain.health.problem.dto.HealthProblemDTO;
import com.healthtrail.domain.health.report.command.AddHealthReportCommand;
import com.healthtrail.domain.health.report.command.HealthReportExportDataCommand;
import com.healthtrail.domain.health.report.command.HealthReportItemCommand;
import com.healthtrail.domain.health.report.command.SaveHealthReportItemsCommand;
import com.healthtrail.domain.health.report.config.HealthReportExternalLlmProperties;
import com.healthtrail.domain.health.report.config.HealthReportParserProperties;
import com.healthtrail.domain.health.report.db.HealthIndicatorTemplateEntity;
import com.healthtrail.domain.health.report.db.HealthIndicatorTemplateService;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import com.healthtrail.domain.health.report.db.HealthReportItemService;
import com.healthtrail.domain.health.report.db.HealthReportService;
import com.healthtrail.domain.health.report.dto.HealthReportAiSummaryDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAnalysisDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceDTO;
import com.healthtrail.domain.health.report.dto.HealthReportAdviceItemDTO;
import com.healthtrail.domain.health.report.dto.HealthReportDTO;
import com.healthtrail.domain.health.report.dto.HealthReportExportDataDTO;
import com.healthtrail.domain.health.report.dto.HealthReportItemDTO;
import com.healthtrail.domain.health.report.dto.HealthReportTrendDTO;
import com.healthtrail.domain.health.report.dto.HealthReportTrendItemDTO;
import com.healthtrail.domain.health.report.dto.HealthReportTrendPointDTO;
import com.healthtrail.domain.health.report.model.HealthReportItemModel;
import com.healthtrail.domain.health.report.model.HealthReportModel;
import com.healthtrail.domain.health.report.model.HealthReportModelFactory;
import com.healthtrail.domain.health.report.llm.HealthReportExternalLlmClient;
import com.healthtrail.domain.health.report.notify.HealthReportAdviceNotice;
import com.healthtrail.domain.health.report.notify.HealthReportAdviceNotifyResult;
import com.healthtrail.domain.health.report.notify.HealthReportAdviceNotifier;
import com.healthtrail.domain.health.report.parser.HealthReportFileParser;
import com.healthtrail.domain.health.report.parser.HealthReportItemInterpretationGenerator;
import com.healthtrail.domain.health.report.parser.HealthReportRetryableParseException;
import com.healthtrail.domain.health.report.parser.HealthReportResultInterpretationGenerator;
import com.healthtrail.domain.health.report.parser.HealthReportStandardIndicatorResolver;
import com.healthtrail.domain.health.report.parser.ParsedReportItem;
import com.healthtrail.domain.health.report.parser.ParsedReportResult;
import com.healthtrail.domain.health.report.query.HealthReportQuery;
import com.healthtrail.domain.system.member.MemberGateApplicationService;
import com.healthtrail.domain.system.member.MemberGateCodeConstants;
import com.healthtrail.infrastructure.thread.ThreadPoolManager;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 体检报告应用服务。
 *
 * <p>该服务负责组织 App 报告主流程：
 * 1. 上传文件并保存元信息
 * 2. 查询报告列表与详情
 * 3. 维护报告结构化指标结果
 * 4. 输出规则化分析摘要
 * 5. 生成基于结构化数据的智能总结
 * 6. 删除当前用户自己的报告
 *
 * <p>当前阶段不再提供 OCR 能力，而是把“文件归档 + 结构化结果 + 规则摘要 + 智能总结”主链路打通，
 * 并且支持通过 OpenAI 兼容协议扩展外部大模型生成更自然的报告总结。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportApplicationService {

    /** 会员门禁应用服务 */
    private final MemberGateApplicationService memberGateApplicationService;

    /**
     * App 端在”解析任务刚提交到后台队列”阶段看到的统一提示文案。
     *
     * <p>这里集中定义，避免上传成功、手动重新解析、详情页立即刷新这几个入口
     * 各自维护一套不一致的话术。
     */
    private static final String REPORT_PARSE_QUEUE_ANALYSIS_SUMMARY =
        “报告解析任务已提交队列，系统正在后台处理中，请稍后查看。”;

    private static final String REPORT_PARSE_QUEUE_RESULT_INTERPRETATION =
        “当前报告正在后台解析中，结构化结果与解读将在任务完成后自动更新。”;

    /**
     * 趋势比较时用于从结果文本中提取数值的正则。
     */
    private static final Pattern TREND_NUMBER_PATTERN = Pattern.compile(“[-+]?\\d+(\\.\\d+)?”);

    /** 报告主表数据库服务 */
    private final HealthReportService reportService;

    /** 报告指标项数据库服务 */
    private final HealthReportItemService reportItemService;

    /** 体检报告领域模型工厂 */
    private final HealthReportModelFactory reportModelFactory;

    /** 家庭成员数据库服务 */
    private final HealthFamilyMemberService familyMemberService;

    /** 家庭成员访问控制服务 */
    private final FamilyMemberAccessService familyMemberAccessService;

    /** 家庭成员应用服务 */
    private final FamilyMemberApplicationService familyMemberApplicationService;

    /** 用药计划数据库服务 */
    private final HealthMedicationPlanService medicationPlanService;

    /** 指标模板数据库服务 */
    private final HealthIndicatorTemplateService indicatorTemplateService;

    /** 报告建议通知发送器 */
    private final HealthReportAdviceNotifier healthReportAdviceNotifier;

    /** App消息应用服务 */
    private final AppMessageApplicationService appMessageApplicationService;

    /** 外部大模型客户端 */
    private final HealthReportExternalLlmClient healthReportExternalLlmClient;

    /** 外部大模型配置属性 */
    private final HealthReportExternalLlmProperties healthReportExternalLlmProperties;

    /** 报告文件解析配置属性 */
    private final HealthReportParserProperties healthReportParserProperties;

    /** 报告文件解析器 */
    private final HealthReportFileParser healthReportFileParser;

    /** 报告指标项解读文本生成器 */
    private final HealthReportItemInterpretationGenerator healthReportItemInterpretationGenerator;

    /** 报告结果解读文本生成器 */
    private final HealthReportResultInterpretationGenerator healthReportResultInterpretationGenerator;

    /** 标准指标解析器 */
    private final HealthReportStandardIndicatorResolver standardIndicatorResolver;

    /** 健康问题数据库服务 */
    private final HealthProblemService healthProblemService;

    /** 健康问题证据数据库服务 */
    private final HealthProblemEvidenceService healthProblemEvidenceService;

    /**
     * 分页查询当前用户的报告列表。
     */
    public PageDTO<HealthReportDTO> getReportList(HealthReportQuery query) {
        Set<Long> accessibleMemberIds = resolveAccessibleMemberIds(query.getOwnerUserId(), query.getMemberId());
        if (accessibleMemberIds.isEmpty()) {
            return new PageDTO<>(Collections.emptyList(), 0L);
        }

        Map<Long, FamilyMemberAccessContextDTO> accessContextMap =
            familyMemberAccessService.getAccessContextMap(accessibleMemberIds, query.getOwnerUserId());

        QueryWrapper<HealthReportEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("member_id", accessibleMemberIds)
            .eq(query.getMemberId() != null, "member_id", query.getMemberId())
            .eq(StrUtil.isNotBlank(query.getReportType()), "report_type", query.getReportType())
            .eq(query.getParseStatus() != null, "parse_status", query.getParseStatus())
            .and(StrUtil.isNotBlank(query.getKeyword()), wrapper -> wrapper
                .like("report_name", query.getKeyword())
                .or()
                .like("hospital_name", query.getKeyword())
                .or()
                .like("original_file_name", query.getKeyword()))
            .orderByDesc("report_date")
            .orderByDesc("report_id");

        Page<HealthReportEntity> page = reportService.page(query.toPage(), queryWrapper);
        // 报告列表是分页接口，但如果分页记录里的每条报告都单独查一次成员表，
        // 依然会形成分页内 N+1。
        // 这里先按当前页成员 ID 一次性加载成员快照，再统一构建 DTO。
        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(page.getRecords().stream()
            .map(HealthReportEntity::getMemberId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));
        List<HealthReportDTO> records = page.getRecords().stream()
            .map(entity -> buildReportDTO(entity, accessContextMap.get(entity.getMemberId()), memberMap))
            .collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    /**
     * 查询单个报告详情。
     */
    public HealthReportDTO getReportInfo(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);
        return buildReportDTO(reportModel, accessContext);
    }

    /**
     * 查询报告对应成员下的协同账号列表。
     *
     * <p>虽然真正的共享关系挂在“成员”上，而不是“报告”上，
     * 但从 App 报告详情视角出发，直接按 `reportId` 查询会更符合用户心智。
     * 因此这里做一层薄封装，把 reportId 转成 memberId 后委托家庭共享服务。
     */
    public List<FamilyMemberShareDTO> getReportCollaboratorList(Long reportId, Long currentUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        return familyMemberApplicationService.getCollaboratorList(reportModel.getMemberId(), currentUserId);
    }

    /**
     * 查询报告对应成员的共享邀请列表。
     */
    public List<FamilyShareInviteDTO> getReportShareInviteList(Long reportId, Long currentUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        return familyMemberApplicationService.getShareInviteList(reportModel.getMemberId(), currentUserId);
    }

    /**
     * 为报告对应成员创建共享邀请。
     */
    public FamilyShareInviteDTO createReportShareInvite(Long reportId, CreateFamilyShareInviteCommand command, Long currentUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        return familyMemberApplicationService.createShareInvite(reportModel.getMemberId(), command, currentUserId);
    }

    /**
     * 取消报告对应成员的共享邀请。
     */
    public void cancelReportShareInvite(Long reportId, Long inviteId, Long currentUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberApplicationService.cancelShareInvite(reportModel.getMemberId(), inviteId, currentUserId);
    }

    /**
     * 移除报告对应成员的协同账号。
     */
    public void removeReportCollaborator(Long reportId, Long shareId, Long currentUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberApplicationService.removeCollaborator(reportModel.getMemberId(), shareId, currentUserId);
    }

    /**
     * 查询某份报告下的指标结果列表。
     */
    public List<HealthReportItemDTO> getReportItems(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);

        QueryWrapper<HealthReportItemEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_id", reportId)
            .orderByAsc("sort")
            .orderByAsc("item_id");
        return reportItemService.list(queryWrapper).stream().map(HealthReportItemDTO::new).collect(Collectors.toList());
    }

    /**
     * 覆盖保存指定报告下的全部指标结果。
     *
     * <p>该方法放在单事务中执行，确保以下动作要么全部成功，要么全部回滚：
     * 1. 删除旧指标
     * 2. 保存新指标
     * 3. 重算报告解析状态与分析摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReportItems(Long reportId, SaveHealthReportItemsCommand saveCommand, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.checkCanEditMember(reportModel.getMemberId(), ownerUserId);

        QueryWrapper<HealthReportItemEntity> removeWrapper = new QueryWrapper<>();
        removeWrapper.eq("report_id", reportId);
        reportItemService.remove(removeWrapper);

        List<HealthReportItemEntity> reportItems = buildReportItems(saveCommand, reportModel);
        healthReportItemInterpretationGenerator.fillInterpretationsIfNecessary(reportItems);
        if (!reportItems.isEmpty()) {
            reportItemService.saveBatch(reportItems);
        }

        refreshReportAnalysisSnapshot(reportModel, reportItems);
        syncHealthProblems(reportModel, reportItems);
        dispatchReportAdviceNoticeQuietly(reportModel, reportItems, reportModel.getOwnerUserId());
    }

    /**
     * 获取体检报告分析摘要。
     *
     * <p>摘要数据以当前数据库中的指标结果实时计算为准，
     * 这样即使后续后台人工修正了指标，也能立刻得到最新摘要。
     */
    public HealthReportAnalysisDTO getReportAnalysis(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);
        return buildAnalysisDTO(reportModel, listReportItems(reportId));
    }

    /**
     * 获取体检报告异常建议。
     *
     * <p>该接口建立在报告分析摘要之上，但会继续往“用户下一步该做什么”推进一步。
     * 当前阶段输出的是规则化建议，而不是医学诊断意见。
     */
    public HealthReportAdviceDTO getReportAdvice(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);

        List<HealthReportItemEntity> reportItems = listReportItems(reportId);
        List<HealthReportItemEntity> abnormalItems = reportItems.stream()
            .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
            .collect(Collectors.toList());

        int activeMedicationPlanCount = countActiveMedicationPlans(reportModel.getOwnerUserId(), reportModel.getMemberId());
        return buildAdviceDTO(reportModel, abnormalItems, activeMedicationPlanCount);
    }

    /**
     * 批量构建报告建议结果。
     *
     * <p>这个方法主要给首页、消息中心这类“同一批报告要一起展示”的聚合场景复用，
     * 目的是把原来“每份报告各查一次指标、各查一次启用计划数”的串行调用，
     * 收敛为：
     * 1. 一次性加载这批报告的全部指标结果
     * 2. 一次性加载这些成员当前启用中的用药计划
     * 3. 在内存中逐报告组装建议 DTO
     *
     * <p>这样能显著减少首页任务流这类高频入口的 SQL 次数，
     * 同时仍然完全复用既有 `buildAdviceDTO(...)` 组装逻辑，避免不同入口各自维护一套建议摘要口径。
     *
     * @param reportEntities 已经完成归属过滤的报告集合，要求都属于同一 ownerUserId
     * @param ownerUserId 当前账号所有者 ID
     * @return 按 reportId 建立的建议结果映射
     */
    public Map<Long, HealthReportAdviceDTO> buildAdviceMapForOwner(List<HealthReportEntity> reportEntities, Long ownerUserId) {
        if (reportEntities == null || reportEntities.isEmpty() || ownerUserId == null) {
            return Collections.emptyMap();
        }

        List<HealthReportEntity> safeReports = reportEntities.stream()
            .filter(Objects::nonNull)
            .filter(report -> report.getReportId() != null)
            .filter(report -> Objects.equals(report.getOwnerUserId(), ownerUserId))
            .collect(Collectors.toList());
        if (safeReports.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> reportIds = safeReports.stream()
            .map(HealthReportEntity::getReportId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, List<HealthReportItemEntity>> reportItemMap = reportItemService.lambdaQuery()
            .in(HealthReportItemEntity::getReportId, reportIds)
            .orderByAsc(HealthReportItemEntity::getSort)
            .orderByAsc(HealthReportItemEntity::getItemId)
            .list()
            .stream()
            .collect(Collectors.groupingBy(HealthReportItemEntity::getReportId));

        List<Long> memberIds = safeReports.stream()
            .map(HealthReportEntity::getMemberId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, Integer> activeMedicationPlanCountMap = loadActiveMedicationPlanCountMap(ownerUserId, memberIds);

        Map<Long, HealthReportAdviceDTO> adviceMap = new HashMap<>();
        for (HealthReportEntity reportEntity : safeReports) {
            List<HealthReportItemEntity> reportItems =
                reportItemMap.getOrDefault(reportEntity.getReportId(), Collections.emptyList());
            List<HealthReportItemEntity> abnormalItems = reportItems.stream()
                .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
                .collect(Collectors.toList());
            int activeMedicationPlanCount =
                activeMedicationPlanCountMap.getOrDefault(reportEntity.getMemberId(), 0);
            adviceMap.put(reportEntity.getReportId(),
                buildAdviceDTO(reportEntity, abnormalItems, activeMedicationPlanCount));
        }
        return adviceMap;
    }

    /**
     * 上传体检报告。
     *
     * <p>上传动作由两部分组成：
     * 1. 文件保存到报告独立目录
     * 2. 元信息写入数据库
     */
    public HealthReportDTO uploadReport(AddHealthReportCommand addCommand, MultipartFile file, Long ownerUserId) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_IS_EMPTY);
        }

        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.checkCanEditMember(addCommand.getMemberId(), ownerUserId);

        String fileUrl = FileUploadUtils.upload(UploadSubDir.REPORT_PATH, file);
        HealthReportModel reportModel = reportModelFactory.create();
        reportModel.loadAddCommand(addCommand, accessContext.getOwnerUserId(), fileUrl, FileNameUtil.getName(fileUrl),
            file.getOriginalFilename(), file.getSize(), FileNameUtil.extName(file.getOriginalFilename()));
        reportModel.checkFields();
        reportService.save(reportModel);
        if (healthReportParserProperties.isAutoParseOnUpload()) {
            // 一旦确定上传后会自动解析，就立刻把主表状态切成“已入队处理中”。
            // 这样 App 在接口返回后立即查看详情时，也不会再看到“待解析”这种静止态误导。
            markReportQueuedForProcessing(reportModel);
            // 这里显式记录“上传完成 -> 已投递后台解析”的交接点，
            // 方便排查用户反馈“文件已经上传成功，但长时间看起来没有开始解析”这类问题。
            log.info("体检报告上传成功，准备触发后台解析，报告ID：{}，成员ID：{}，文件：{}",
                reportModel.getReportId(), reportModel.getMemberId(), reportModel.getOriginalFileName());
            triggerBackendParseAsync(reportModel.getReportId(), accessContext.getOwnerUserId());
        }
        // 上传完成后直接把刚落库的报告基础信息返回给调用方，
        // 让 App 可以拿到稳定的 reportId，继续主动触发解析、轮询状态或打开详情，
        // 不需要再通过“重新拉列表 + 猜最后一条记录”这种不稳定方式反查。
        return buildReportDTO(reportModel, accessContext);
    }

    /**
     * 删除体检报告。
     *
     * <p>当前仅做逻辑删除，不物理删除文件。
     * 这样后续即使补做解析任务、审计日志或文件恢复，也还有追溯空间。
     */
    public void removeReport(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.checkCanEditMember(reportModel.getMemberId(), ownerUserId);
        reportService.removeById(reportId);

        // 报告被逻辑删除后，它下面的结构化指标结果也一并逻辑删除，
        // 避免后续按 report_id 查询时残留“孤儿数据”。
        QueryWrapper<HealthReportItemEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_id", reportId);
        reportItemService.remove(queryWrapper);
    }

    /**
     * 查询报告趋势对比。
     *
     * <p>当前版本采用“以当前报告为基准，对比同成员同类型历史报告”的方式，
     * 这样 App 进入某份报告详情时，就能直接看到可复用的趋势结果。
     */
    public HealthReportTrendDTO getReportTrend(Long reportId, Long ownerUserId) {
        AuthorizedReportContext reportContext = loadAuthorizedReportContext(reportId, ownerUserId);
        HealthReportModel reportModel = reportContext.reportModel;
        List<HealthReportItemEntity> currentItems = listReportItems(reportId);
        LinkedHashSet<String> currentComparableKeys = currentItems.stream()
            .map(this::resolveComparableKey)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // 趋势链路先按“当前成员自己的历史报告”收敛候选范围，
        // 再统一加载这些候选报告对应的指标，避免像旧实现那样先扫描全库 report_item，
        // 最后才回头用 memberId 过滤。
        List<HealthReportEntity> candidateReports = reportService.lambdaQuery()
            .eq(HealthReportEntity::getMemberId, reportModel.getMemberId())
            .le(reportModel.getReportDate() != null, HealthReportEntity::getReportDate, reportModel.getReportDate())
            .orderByDesc(HealthReportEntity::getReportDate)
            .orderByDesc(HealthReportEntity::getReportId)
            .list();
        if (candidateReports.isEmpty()) {
            candidateReports = Collections.singletonList(reportModel);
        }

        Long currentReportId = reportModel.getReportId();
        List<Long> historyReportIds = candidateReports.stream()
            .map(HealthReportEntity::getReportId)
            .filter(Objects::nonNull)
            .filter(historyReportId -> !Objects.equals(historyReportId, currentReportId))
            .collect(Collectors.toList());
        Map<Long, Map<String, HealthReportItemEntity>> comparableItemMap = new HashMap<>();
        comparableItemMap.put(currentReportId, buildComparableItemLookup(currentItems, currentComparableKeys));
        if (!historyReportIds.isEmpty()) {
            List<HealthReportItemEntity> historyItems = reportItemService.lambdaQuery()
                .in(HealthReportItemEntity::getReportId, historyReportIds)
                .orderByAsc(HealthReportItemEntity::getReportId)
                .orderByAsc(HealthReportItemEntity::getSort)
                .orderByAsc(HealthReportItemEntity::getItemId)
                .list();
            mergeComparableItemLookup(comparableItemMap, historyItems, currentComparableKeys);
        }

        List<HealthReportEntity> historyReports = candidateReports.stream()
            .filter(historyReport -> Objects.equals(historyReport.getReportId(), currentReportId)
                || hasComparableTrendItem(comparableItemMap, historyReport.getReportId()))
            .limit(6)
            .collect(Collectors.toList());
        if (historyReports.isEmpty()) {
            historyReports = Collections.singletonList(reportModel);
        }
        final List<HealthReportEntity> finalHistoryReports = historyReports;

        List<HealthReportTrendItemDTO> trendItems = currentItems.stream()
            .map(item -> buildTrendItem(item, finalHistoryReports, comparableItemMap))
            .collect(Collectors.toList());

        HealthReportTrendDTO dto = new HealthReportTrendDTO();
        dto.setReportId(reportModel.getReportId());
        dto.setMemberId(reportModel.getMemberId());
        dto.setMemberName(resolveMemberName(reportModel.getMemberId(), reportContext.memberMap));
        dto.setReportType(reportModel.getReportType());
        dto.setComparedReportCount(finalHistoryReports.size());
        dto.setItems(trendItems);
        return dto;
    }

    /**
     * 查询报告关联健康问题。
     */
    public List<HealthProblemDTO> getReportHealthProblems(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);
        return listReportHealthProblems(reportId);
    }

    /**
     * 生成报告导出数据。
     *
     * <p>这里统一返回“导出最终会使用的数据”，而不是直接生成文件：
     * 1. App 端仍然可以沿用本地预览与图片渲染；
     * 2. 但导出文本、文件名、异常项截断口径等都改由后端统一决定；
     * 3. 会员门禁也在这里集中做最终兜底，避免客户端绕过规则。
     */
    public HealthReportExportDataDTO buildReportExportData(Long reportId, HealthReportExportDataCommand command, Long ownerUserId) {
        memberGateApplicationService.ensureCurrentUserGateAllowed(ownerUserId, MemberGateCodeConstants.REPORT_EXPORT);

        HealthReportExportDataCommand safeCommand = normalizeExportCommand(command);
        ReportExportRuntimeContext exportContext = loadReportExportRuntimeContext(reportId, ownerUserId);
        HealthReportDTO detail = exportContext.reportDetail;
        List<HealthReportItemDTO> items = exportContext.reportItems;
        HealthReportAnalysisDTO analysis = exportContext.analysis;
        HealthReportAdviceDTO advice = exportContext.advice;
        List<HealthProblemDTO> healthProblems = exportContext.healthProblems;

        List<HealthReportItemDTO> abnormalItems = collectShareAbnormalItems(items);
        int totalIndicatorCount = items.size();
        int abnormalCount = abnormalItems.size();
        int pendingIndicatorCount = countSharePendingItems(items);

        List<HealthReportItemDTO> selectedIndicatorItems =
            selectIndicatorItems(items, safeCommand.getFormat(), safeCommand.getIncludeIndicatorItems());
        List<HealthReportItemDTO> selectedAbnormalItems =
            selectAbnormalItems(abnormalItems, safeCommand.getFormat(), safeCommand.getIncludeAbnormalItems());
        List<HealthReportAdviceItemDTO> selectedAdviceItems =
            selectAdviceItems(advice, safeCommand.getFormat(), safeCommand.getIncludeAdvice());
        List<HealthProblemDTO> selectedHealthProblems =
            selectHealthProblems(healthProblems, safeCommand.getFormat(), safeCommand.getIncludeSupplementalInfo());

        String analysisSummary = Boolean.TRUE.equals(safeCommand.getIncludeAnalysisSummary())
            ? normalizeText(analysis.getSummary())
            : null;
        String resultInterpretation = Boolean.TRUE.equals(safeCommand.getIncludeResultInterpretation())
            ? normalizeText(detail.getResultInterpretation())
            : null;
        String aiSummary = Boolean.TRUE.equals(safeCommand.getIncludeAiSummary())
            ? normalizeText(detail.getAiSummaryContent())
            : null;
        String adviceSummary = Boolean.TRUE.equals(safeCommand.getIncludeAdvice())
            ? normalizeText(advice.getSummary())
            : null;

        // AI 报告导出门禁只在本次导出内容真的带上 AI 总结时才触发。
        // 这样用户仍然可以关闭 AI 总结开关后，继续导出普通报告摘要。
        if (StrUtil.isNotBlank(aiSummary)) {
            memberGateApplicationService.ensureCurrentUserGateAllowed(ownerUserId, MemberGateCodeConstants.AI_REPORT_EXPORT);
        }

        HealthReportExportDataDTO dto = new HealthReportExportDataDTO();
        dto.setFormat(safeCommand.getFormat());
        dto.setHidePersonalInfo(safeCommand.getHidePersonalInfo());
        dto.setReportTitle(buildDisplayReportName(detail));
        dto.setMemberLabel(Boolean.TRUE.equals(safeCommand.getHidePersonalInfo()) ? null : buildDisplayMemberLabel(detail));
        dto.setReportDateText(formatReportDate(detail.getReportDate()));
        dto.setReportTypeText(buildDisplayReportTypeText(detail));
        dto.setHospitalName(Boolean.TRUE.equals(safeCommand.getIncludeSupplementalInfo())
            ? normalizeText(detail.getHospitalName())
            : null);
        dto.setTotalIndicatorCount(totalIndicatorCount);
        dto.setAbnormalCount(abnormalCount);
        dto.setPendingIndicatorCount(pendingIndicatorCount);
        dto.setAnalysisSummary(analysisSummary);
        dto.setResultInterpretation(resultInterpretation);
        dto.setAiSummary(aiSummary);
        dto.setAdviceSummary(adviceSummary);
        dto.setIndicatorItems(selectedIndicatorItems);
        dto.setAbnormalItems(selectedAbnormalItems);
        dto.setAdviceItems(selectedAdviceItems);
        dto.setHealthProblems(selectedHealthProblems);
        dto.setShareFileName(buildReportShareFileName(detail, safeCommand.getFormat()));
        dto.setShareText(buildReportShareText(
            detail,
            safeCommand,
            analysisSummary,
            resultInterpretation,
            aiSummary,
            adviceSummary,
            totalIndicatorCount,
            abnormalCount,
            pendingIndicatorCount,
            selectedAbnormalItems,
            selectedAdviceItems,
            selectedHealthProblems
        ));
        return dto;
    }

    /**
     * 手动触发一次报告文件解析。
     *
     * <p>该方法替代历史 `/ocr` 占位入口，明确表达“重新解析报告文件”的业务含义：
     * 1. 文本型 PDF / 文本文件继续走文本抽取和规则结构化；
     * 2. 图片报告只在外部大模型支持视觉能力时直接走 AI 视觉结构化；
     * 3. 服务端不再调用云 OCR SDK，也不再执行本地 OCR 命令。
     */
    public void triggerReportParse(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.checkCanEditMember(reportModel.getMemberId(), ownerUserId);
        markReportQueuedForProcessing(reportModel);
        triggerBackendParseAsync(reportId, ownerUserId);
    }

    /**
     * 把检查报告解析任务放入应用内后台线程。
     *
     * <p>上传接口不等待 PDF 文本抽取、规则结构化或 AI 视觉解析完成，避免大文件拖慢用户上传体验。
     */
    private void triggerBackendParseAsync(Long reportId, Long ownerUserId) {
        enqueueReportParseTask(reportId, ownerUserId, 0, 0L);
    }

    /**
     * 在任务真正开始执行前，先把报告主表更新成“已提交后台解析队列”的可见状态。
     *
     * <p>异步任务从“接口返回”到“工作线程真正开始执行”之间天然会有一个小时间窗。
     * 如果这个时间窗里主表仍停留在 `WAIT_PARSE`，用户会误以为任务并没有提交成功。
     * 因此这里提前更新主表，让 App 可以立刻读到明确的排队提示。
     */
    private void markReportQueuedForProcessing(HealthReportEntity reportEntity) {
        if (reportEntity == null || reportEntity.getReportId() == null) {
            return;
        }
        reportEntity.setParseStatus(HealthReportParseStatusEnum.PROCESSING.getValue());
        reportEntity.setOcrStatus(HealthProcessingStatusEnum.PROCESSING.getValue());
        // 已经形成过可复用结构化结果的报告，再次手动解析时只需要让用户看到
        // “任务已开始 / 正在处理” 的状态流转，不需要把原有摘要和结果解读清空成排队占位文案。
        //
        // 这样做有两个直接好处：
        // 1. App 侧依然会通过 parseStatus 看到“正在解析”；
        // 2. 详情页已有的分析摘要、结果解读不会在这次“缓存成功路径”里被短暂覆盖掉。
        if (!isAiParseCached(reportEntity)) {
            reportEntity.setAnalysisSummary(REPORT_PARSE_QUEUE_ANALYSIS_SUMMARY);
            reportEntity.setResultInterpretation(REPORT_PARSE_QUEUE_RESULT_INTERPRETATION);
        }
        reportEntity.setOcrTime(new Date());
        reportService.updateById(reportEntity);
    }

    /**
     * 解析检查报告文件并覆盖写入结构化指标。
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseReportFile(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.checkCanEditMember(reportModel.getMemberId(), ownerUserId);

        // 解析开始时立刻把状态切到“处理中”，这样前端轮询或手动刷新时
        // 能第一时间看到任务已经真正启动，而不是继续停留在“待解析”。
        reportModel.setParseStatus(HealthReportParseStatusEnum.PROCESSING.getValue());
        reportModel.setOcrStatus(HealthProcessingStatusEnum.PROCESSING.getValue());
        reportService.updateById(reportModel);

        try {
            if (shouldReuseCachedAiParseResult(reportModel)) {
                completeParseByReusingCachedResult(reportModel);
                return;
            }
            log.info("开始解析体检报告文件，报告ID：{}，文件扩展名：{}，原文件名：{}",
                reportId, reportModel.getFileExtension(), reportModel.getOriginalFileName());
            ParsedReportResult parsedResult = healthReportFileParser.parse(reportModel);
            // 解析链路只负责提供“附件里识别出的日期候选值”，
            // 这里才是真正执行业务规则的地方：
            // 1. 用户上传时没填 reportDate -> 允许自动回填；
            // 2. 用户已经手填 reportDate -> 严禁解析结果覆盖。
            fillReportDateFromParsedResultIfMissing(reportModel, parsedResult);
            // 识别出的报告细分类型同样在这里统一回写。
            //
            // 这里故意单独存到 `recognizedReportType`，而不是直接覆盖用户手填的
            // `reportType`，原因是：
            // 1. 用户原始输入仍然有业务价值，不能被静默篡改；
            // 2. 模型识别出的类型通常更细，比如“血液检查” -> “血常规”；
            // 3. 前端展示时可以同时利用两者做更友好的补充提示。
            fillRecognizedReportTypeFromParsedResult(reportModel, parsedResult);
            List<HealthReportItemEntity> parsedItems = buildReportItemsFromParsedResult(parsedResult, reportModel);
            // 规则解析和视觉解析最终都会汇总成同一批 report_item 实体。
            // 这里统一在落库前补一次“指标解读”回填，确保：
            // 1. 不管前面命中的是规则解析还是 AI 结构化解析，最终都有机会生成逐项解释；
            // 2. 即使 AI 结构化结果里漏掉了 itemInterpretation，也能在这里补齐；
            // 3. 解读按行存进 report_item，前端详情页逐条展示时不必再额外触发二次生成。
            healthReportItemInterpretationGenerator.fillInterpretationsIfNecessary(parsedItems);
            if (!parsedItems.isEmpty()) {
                QueryWrapper<HealthReportItemEntity> removeWrapper = new QueryWrapper<>();
                removeWrapper.eq("report_id", reportId);
                reportItemService.remove(removeWrapper);
                reportItemService.saveBatch(parsedItems);
            }
            log.info("体检报告文件解析完成，报告ID：{}，提取文本长度：{}，结构化指标数：{}",
                reportId,
                parsedResult.getExtractedText() == null ? 0 : parsedResult.getExtractedText().length(),
                parsedItems.size());
            reportModel.setOcrStatus(HealthProcessingStatusEnum.COMPLETED.getValue());
            reportModel.setOcrTextSnapshot(limitLength(parsedResult.getExtractedText(), 2000));
            reportModel.setOcrTime(new Date());
            refreshReportAnalysisSnapshot(reportModel, parsedItems);
            syncHealthProblems(reportModel, parsedItems);
            // `ai_parse_cached` 只能在整次解析主流程成功收尾后才允许写成 1。
            //
            // 这里故意放在：
            // 1. 文件解析成功
            // 2. 结构化指标已落库
            // 3. 摘要/结果解读已刷新
            // 4. 健康问题同步也已完成
            // 之后，原因是只要前面任一步抛异常，这次解析在业务语义上就不能算“成功缓存”。
            //
            // 否则会出现：
            // 1. 用户这次解析实际上失败了；
            // 2. 但主表却提前被打上了缓存标记；
            // 3. 用户下一次再点“解析报告”时，系统直接跳过大模型；
            // 4. 最终反而失去真正重试修复的机会。
            reportModel.setAiParseCached(parsedItems.isEmpty() ? 0 : 1);
            reportService.updateById(reportModel);
            dispatchReportAdviceNoticeQuietly(reportModel, parsedItems, reportModel.getOwnerUserId());
            log.info("体检报告解析结果已落库，报告ID：{}，解析状态：{}，兼容处理状态：{}",
                reportId, reportModel.getParseStatus(), reportModel.getOcrStatus());
        } catch (RuntimeException ex) {
            if (ex instanceof HealthReportRetryableParseException retryableParseException) {
                // 这里不要把状态直接打成 FAILED。
                //
                // 原因是这类异常通常代表“外部视觉模型暂时超时 / 限流 / 网关抖动”，
                // 并不意味着这份报告文件本身不可解析。
                // 如果立刻标失败，前端会误以为任务已经终结，和后续的自动重试队列语义冲突。
                reportModel.setParseStatus(HealthReportParseStatusEnum.PROCESSING.getValue());
                reportModel.setOcrStatus(HealthProcessingStatusEnum.PROCESSING.getValue());
                reportModel.setOcrTextSnapshot(limitLength(retryableParseException.getMessage(), 2000));
                reportModel.setOcrTime(new Date());
                reportModel.setAnalysisSummary("报告解析任务正在等待外部 AI 服务恢复，已加入后台重试队列。");
                reportModel.setResultInterpretation("当前报告解析暂时受外部 AI 服务状态影响，系统会在后台自动重试，成功后再写入结构化结果。");
                reportService.updateById(reportModel);
                throw retryableParseException;
            }
            reportModel.setParseStatus(HealthReportParseStatusEnum.FAILED.getValue());
            reportModel.setOcrStatus(HealthProcessingStatusEnum.FAILED.getValue());
            reportModel.setOcrTextSnapshot(limitLength(ex.getMessage(), 2000));
            reportModel.setOcrTime(new Date());
            reportService.updateById(reportModel);
            throw ex;
        }
    }

    /**
     * 判断当前报告是否可以直接复用已有解析结果。
     *
     * <p>这里不只看主表标记，还额外确认 report_item 里仍然留有结构化结果，
     * 目的是防止以下脏数据场景把一次“真实重试”误判成缓存命中：
     * 1. 主表标记是 1，但结构化指标后来被人工清空；
     * 2. 历史脚本迁移不完整，只补了主表标记，没有保留明细结果；
     * 3. 线上异常中断导致主表和明细表状态不一致。
     */
    private boolean shouldReuseCachedAiParseResult(HealthReportEntity reportEntity) {
        if (!isAiParseCached(reportEntity) || reportEntity.getReportId() == null) {
            return false;
        }
        return reportItemService.lambdaQuery()
            .eq(HealthReportItemEntity::getReportId, reportEntity.getReportId())
            .count() > 0;
    }

    /**
     * 使用已有结构化结果完成一次“零大模型成本”的重新解析。
     *
     * <p>用户仍然会看到：
     * 1. 点击后立刻进入“处理中”；
     * 2. 随后回到“已解析成功”；
     *
     * 但这里不会再重复做：
     * 1. 文件视觉结构化；
     * 2. 指标解释批量生成；
     * 3. 报告级结果解读外部模型调用；
     *
     * 这样既保留原有交互心智，也能避免对同一份已成功解析过的报告反复消耗大模型费用。
     */
    private void completeParseByReusingCachedResult(HealthReportModel reportModel) {
        List<HealthReportItemEntity> cachedItems = listReportItems(reportModel.getReportId());
        HealthReportAnalysisDTO analysisDTO = buildAnalysisDTO(reportModel, cachedItems);
        reportModel.setParseStatus(HealthReportParseStatusEnum.COMPLETED.getValue());
        reportModel.setOcrStatus(HealthProcessingStatusEnum.COMPLETED.getValue());
        reportModel.setOcrTime(new Date());
        reportModel.setAiParseCached(1);
        if (!cachedItems.isEmpty()) {
            reportModel.setAnalysisSummary(limitLength(analysisDTO.getSummary(), 1000));
        }
        if (StrUtil.isBlank(reportModel.getResultInterpretation())) {
            reportModel.setResultInterpretation(healthReportResultInterpretationGenerator.generateFallbackOnly(
                reportModel, cachedItems, analysisDTO));
        }
        reportService.updateById(reportModel);
        log.info("体检报告重新解析命中 AI 解析缓存，已复用既有结构化结果并跳过大模型调用，报告ID：{}，结构化指标数：{}",
            reportModel.getReportId(), cachedItems.size());
    }

    /**
     * 判断报告是否已经具备可复用的 AI 解析缓存标记。
     */
    private boolean isAiParseCached(HealthReportEntity reportEntity) {
        return reportEntity != null && Objects.equals(reportEntity.getAiParseCached(), 1);
    }

    /**
     * 把报告解析任务投递到后台队列，并在需要时附带延迟重试能力。
     *
     * <p>这里不用“线程里 while 无限循环等成功”，而是采用“执行一次 -> 失败后按退避时间重新入队”的方式：
     * 1. 不会长期占住工作线程；
     * 2. 更适合处理外部模型偶发超时和限流；
     * 3. 每次重试前都会重新进入正常任务入口，日志与状态也更容易追踪。
     */
    private void enqueueReportParseTask(Long reportId, Long ownerUserId, int retryAttempt, long delayMs) {
        Runnable parseTask = () -> {
            try {
                log.info("体检报告后台解析任务开始执行，报告ID：{}，用户ID：{}，重试序号：{}",
                    reportId, ownerUserId, retryAttempt);
                parseReportFile(reportId, ownerUserId);
                log.info("体检报告后台解析任务执行完成，报告ID：{}，用户ID：{}，重试序号：{}",
                    reportId, ownerUserId, retryAttempt);
            } catch (HealthReportRetryableParseException retryableParseException) {
                handleRetryableParseFailure(reportId, ownerUserId, retryAttempt, retryableParseException);
            } catch (Exception ex) {
                log.warn("检查报告后台解析失败，报告ID：{}，重试序号：{}，异常：{}",
                    reportId, retryAttempt, ex.getMessage(), ex);
            }
        };

        if (delayMs <= 0L) {
            log.info("体检报告已加入后台解析队列，报告ID：{}，用户ID：{}，重试序号：{}", reportId, ownerUserId, retryAttempt);
            ThreadPoolManager.execute(parseTask);
            return;
        }
        log.info("体检报告已加入后台延迟重试队列，报告ID：{}，用户ID：{}，重试序号：{}，delayMs={}",
            reportId, ownerUserId, retryAttempt, delayMs);
        ThreadPoolManager.schedule(parseTask, delayMs);
    }

    /**
     * 处理“可重试”的报告解析失败。
     *
     * <p>当前只要失败原因更像第三方临时不可用，就不立刻终止任务，
     * 而是根据配置决定是否继续留在后台队列里等待下一次机会。
     */
    private void handleRetryableParseFailure(Long reportId, Long ownerUserId, int retryAttempt,
        HealthReportRetryableParseException retryableParseException) {
        int nextRetryAttempt = retryAttempt + 1;
        if (!healthReportParserProperties.isAsyncRetryEnabled()) {
            markReportParseRetryExhausted(reportId, retryableParseException.getMessage(), retryAttempt);
            log.warn("检查报告后台解析遇到可重试异常，但自动重试未启用，报告ID：{}，异常：{}",
                reportId, retryableParseException.getMessage(), retryableParseException);
            return;
        }
        if (!shouldContinueParseRetry(nextRetryAttempt)) {
            markReportParseRetryExhausted(reportId, retryableParseException.getMessage(), retryAttempt);
            log.warn("检查报告后台解析自动重试次数已耗尽，报告ID：{}，已执行重试次数：{}，异常：{}",
                reportId, retryAttempt, retryableParseException.getMessage(), retryableParseException);
            return;
        }

        long nextDelayMs = calculateParseRetryDelayMs(nextRetryAttempt);
        markReportQueuedForRetry(reportId, nextRetryAttempt, nextDelayMs, retryableParseException.getMessage());
        log.warn("检查报告后台解析触发自动重试，报告ID：{}，下次重试序号：{}，delayMs={}，原因：{}",
            reportId, nextRetryAttempt, nextDelayMs, retryableParseException.getMessage());
        enqueueReportParseTask(reportId, ownerUserId, nextRetryAttempt, nextDelayMs);
    }

    /**
     * 判断是否还允许继续自动重试。
     *
     * <p>这里支持两种策略：
     * 1. `maxAsyncRetryCount > 0`：最多重试固定次数；
     * 2. `maxAsyncRetryCount <= 0`：视为不限次数，持续在后台队列里等待成功。
     */
    private boolean shouldContinueParseRetry(int nextRetryAttempt) {
        int maxRetryCount = healthReportParserProperties.getMaxAsyncRetryCount();
        return maxRetryCount <= 0 || nextRetryAttempt <= maxRetryCount;
    }

    /**
     * 计算下一次自动重试的退避时间。
     *
     * <p>当前采用指数退避并带上限：
     * 1. 第一次失败后等待 initialDelay；
     * 2. 后续每次乘 2；
     * 3. 最终不超过 maxDelay。
     *
     * <p>这样既能避免第三方服务刚恢复时打得太急，也不会把等待时间无限拉长到不可控。
     */
    private long calculateParseRetryDelayMs(int nextRetryAttempt) {
        long initialDelayMs = Math.max(healthReportParserProperties.getAsyncRetryInitialDelayMs(), 1_000L);
        long maxDelayMs = Math.max(healthReportParserProperties.getAsyncRetryMaxDelayMs(), initialDelayMs);
        long multiplier = 1L << Math.max(nextRetryAttempt - 1, 0);
        long calculatedDelayMs;
        try {
            calculatedDelayMs = Math.multiplyExact(initialDelayMs, multiplier);
        } catch (ArithmeticException ex) {
            calculatedDelayMs = maxDelayMs;
        }
        return Math.min(calculatedDelayMs, maxDelayMs);
    }

    /**
     * 把报告状态更新成“已排队等待自动重试”。
     */
    private void markReportQueuedForRetry(Long reportId, int nextRetryAttempt, long nextDelayMs, String reason) {
        HealthReportEntity reportEntity = reportService.getById(reportId);
        if (reportEntity == null) {
            return;
        }
        reportEntity.setParseStatus(HealthReportParseStatusEnum.PROCESSING.getValue());
        reportEntity.setOcrStatus(HealthProcessingStatusEnum.PROCESSING.getValue());
        reportEntity.setOcrTextSnapshot(limitLength(reason, 2000));
        reportEntity.setOcrTime(new Date());
        reportEntity.setAnalysisSummary(StrUtil.format(
            "报告解析暂时受外部 AI 服务状态影响，已进入后台重试队列（第 {} 次重试预计在 {} 秒后执行）。",
            nextRetryAttempt, Math.max(nextDelayMs / 1000L, 1L)));
        reportEntity.setResultInterpretation("当前报告尚未生成新的结构化结果，系统会在后台继续重试，成功后再写入并更新解读。");
        reportService.updateById(reportEntity);
    }

    /**
     * 当自动重试次数耗尽后，把报告显式标成失败，避免任务一直处于“处理中”却没有后续动作。
     */
    private void markReportParseRetryExhausted(Long reportId, String reason, int retryAttempt) {
        HealthReportEntity reportEntity = reportService.getById(reportId);
        if (reportEntity == null) {
            return;
        }
        reportEntity.setParseStatus(HealthReportParseStatusEnum.FAILED.getValue());
        reportEntity.setOcrStatus(HealthProcessingStatusEnum.FAILED.getValue());
        reportEntity.setOcrTextSnapshot(limitLength(reason, 2000));
        reportEntity.setOcrTime(new Date());
        reportEntity.setAnalysisSummary(StrUtil.format(
            "报告解析因外部 AI 服务持续不可用而未完成，自动重试已结束（累计重试 {} 次），可稍后手动重试。",
            Math.max(retryAttempt, 0)));
        reportEntity.setResultInterpretation("当前报告暂未生成新的结构化结果解读，建议稍后重试解析或人工补录关键指标。");
        reportService.updateById(reportEntity);
    }

    /**
     * 仅在主表 reportDate 为空时，采用解析结果中的日期候选值补齐。
     *
     * <p>这个方法故意不做“更正用户输入”的事情，只做空值补全，
     * 这样可以稳定满足“用户手填优先”的产品规则。
     */
    private void fillReportDateFromParsedResultIfMissing(HealthReportModel reportModel, ParsedReportResult parsedResult) {
        if (reportModel == null || reportModel.getReportDate() != null || parsedResult == null
            || parsedResult.getReportDate() == null) {
            return;
        }
        reportModel.setReportDate(parsedResult.getReportDate());
        log.info("体检报告解析阶段自动回填报告日期，报告ID：{}，回填日期：{}",
            reportModel.getReportId(), DateUtil.formatDate(parsedResult.getReportDate()));
    }

    /**
     * 将解析得到的报告细分类型回写到主表。
     *
     * <p>这里不覆盖用户原始 `reportType`，而是单独落到 `recognizedReportType`：
     * 1. 用户输入代表上传时的主观归类；
     * 2. 识别类型代表系统对报告内容的客观判断；
     * 3. 两者分开存储后，前端才能安全地展示“用户填写值 + 识别补充值”。
     */
    private void fillRecognizedReportTypeFromParsedResult(HealthReportModel reportModel, ParsedReportResult parsedResult) {
        if (reportModel == null || parsedResult == null || StrUtil.isBlank(parsedResult.getRecognizedReportType())) {
            return;
        }
        reportModel.setRecognizedReportType(StrUtil.trim(parsedResult.getRecognizedReportType()));
        log.info("体检报告解析阶段识别出报告细分类型，报告ID：{}，识别类型：{}",
            reportModel.getReportId(), reportModel.getRecognizedReportType());
    }

    /**
     * 生成报告 AI 智能总结。
     *
     * <p>当前总结链路采用“双通道”策略：
     * 1. 优先尝试外部大模型，输出更自然的可读总结
     * 2. 如果外部模型未配置或调用失败，则自动回退到本地规则总结
     *
     * <p>这样既能满足本期“先支持外部模型扩展”的目标，
     * 也能保证在第三方服务波动时，App 端依然有稳定可展示的总结结果。
     */
    public HealthReportAiSummaryDTO generateAiSummary(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);

        // AI 总结属于明确的会员能力型入口。
        //
        // 这里先校验“当前账号对这份报告是否有访问权”，
        // 再校验会员门禁，原因是：
        // 1. 访问权是更基础的数据权限；
        // 2. 只有先确认这份报告本来就能看，会员提示才是有意义的后置限制；
        // 3. 这样也能避免无权限账号误收到“去升级会员”的错误引导。
        memberGateApplicationService.ensureCurrentUserGateAllowed(
            ownerUserId,
            MemberGateCodeConstants.AI_ASSISTANT_OPEN
        );

        reportModel.setAiSummaryStatus(HealthProcessingStatusEnum.PROCESSING.getValue());
        reportService.updateById(reportModel);

        try {
            List<HealthReportItemEntity> reportItems = listReportItems(reportId);
            List<HealthReportItemEntity> abnormalItems = reportItems.stream()
                .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
                .collect(Collectors.toList());
            int activeMedicationPlanCount = countActiveMedicationPlans(reportModel.getOwnerUserId(), reportModel.getMemberId());

            HealthReportAnalysisDTO analysisDTO = buildAnalysisDTO(reportModel, reportItems);
            HealthReportAdviceDTO adviceDTO = buildAdviceDTO(reportModel, abnormalItems, activeMedicationPlanCount);
            HealthReportTrendDTO trendDTO = reportItems.isEmpty() ? new HealthReportTrendDTO() : getReportTrend(reportId, ownerUserId);

            String fallbackSummary = buildAiSummaryContent(reportModel, reportItems, abnormalItems, analysisDTO, adviceDTO, trendDTO,
                activeMedicationPlanCount);
            String externalSummary = buildExternalAiSummary(reportModel, abnormalItems, analysisDTO, adviceDTO, trendDTO,
                activeMedicationPlanCount);
            boolean usedExternalLlm = StrUtil.isNotBlank(externalSummary);
            String summary = usedExternalLlm ? externalSummary : fallbackSummary;

            reportModel.setAiSummaryStatus(HealthProcessingStatusEnum.COMPLETED.getValue());
            reportModel.setAiSummaryContent(limitLength(summary, 2000));
            reportModel.setAiSummaryTime(new Date());
            reportService.updateById(reportModel);

            HealthReportAiSummaryDTO dto = new HealthReportAiSummaryDTO();
            dto.setReportId(reportId);
            dto.setAiSummaryStatus(reportModel.getAiSummaryStatus());
            dto.setSummary(reportModel.getAiSummaryContent());
            dto.setDisclaimer(buildAiSummaryDisclaimer(usedExternalLlm));
            dto.setProcessedTime(reportModel.getAiSummaryTime());
            return dto;
        } catch (RuntimeException ex) {
            reportModel.setAiSummaryStatus(HealthProcessingStatusEnum.FAILED.getValue());
            reportModel.setAiSummaryTime(new Date());
            reportService.updateById(reportModel);
            throw ex;
        }
    }

    /**
     * 构建报告 DTO。
     * 这里额外回填成员姓名，方便前端列表直接展示。
     */
    private HealthReportDTO buildReportDTO(HealthReportEntity entity, FamilyMemberAccessContextDTO accessContext) {
        HealthReportDTO dto = new HealthReportDTO(entity);
        if (entity.getMemberId() != null) {
            // 这里故意改成软查：即使成员后来被删除，历史报告也仍然应该能被正常查看。
            HealthFamilyMemberEntity memberEntity = familyMemberService.getById(entity.getMemberId());
            // 报告详情、列表和协同入口都依赖同一份成员展示信息，
            // 因此这里一次性把姓名与成员编码都补齐，避免各端再各自拼兜底文案。
            dto.setMemberName(memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName());
            dto.setMemberCode(memberEntity == null ? null : memberEntity.getMemberCode());
        }
        if (accessContext != null) {
            dto.setAccessSource(accessContext.getAccessSource());
            dto.setAccessSourceName(accessContext.getAccessSourceName());
            dto.setAccessRole(accessContext.getAccessRole());
            dto.setAccessRoleName(accessContext.getAccessRoleName());
            dto.setIsOwner(accessContext.isOwner());
            dto.setCanEdit(accessContext.isCanEdit());
            dto.setCanShare(accessContext.isOwner());
        }
        return dto;
    }

    /**
     * 用预加载成员快照构建报告 DTO。
     *
     * <p>这个重载方法专门给分页列表使用，
     * 这样可以在不改变详情页、分享页等单条查询行为的前提下，
     * 把“当前页内逐条查成员”收敛成一次批量查询。
     */
    private HealthReportDTO buildReportDTO(HealthReportEntity entity, FamilyMemberAccessContextDTO accessContext,
        Map<Long, HealthFamilyMemberEntity> memberMap) {
        HealthReportDTO dto = new HealthReportDTO(entity);
        if (entity.getMemberId() != null) {
            HealthFamilyMemberEntity memberEntity = memberMap == null ? null : memberMap.get(entity.getMemberId());
            dto.setMemberName(memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName());
            dto.setMemberCode(memberEntity == null ? null : memberEntity.getMemberCode());
        }
        if (accessContext != null) {
            dto.setAccessSource(accessContext.getAccessSource());
            dto.setAccessSourceName(accessContext.getAccessSourceName());
            dto.setAccessRole(accessContext.getAccessRole());
            dto.setAccessRoleName(accessContext.getAccessRoleName());
            dto.setIsOwner(accessContext.isOwner());
            dto.setCanEdit(accessContext.isCanEdit());
            dto.setCanShare(accessContext.isOwner());
        }
        return dto;
    }

    /**
     * 把前端提交的指标命令列表构造成可入库的实体列表。
     */
    private List<HealthReportItemEntity> buildReportItems(SaveHealthReportItemsCommand saveCommand, HealthReportEntity reportEntity) {
        if (saveCommand == null || saveCommand.getItems() == null || saveCommand.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        List<HealthReportItemEntity> reportItems = new ArrayList<>();
        int sortSeed = 1;
        for (HealthReportItemCommand itemCommand : saveCommand.getItems()) {
            HealthIndicatorTemplateEntity templateEntity =
                matchIndicatorTemplate(resolveEffectiveReportType(reportEntity), itemCommand.getItemCode(), itemCommand.getItemName());
            fillItemCommandByTemplate(itemCommand, templateEntity);
            if (StrUtil.isBlank(itemCommand.getStandardItemCode())) {
                itemCommand.setStandardItemCode(standardIndicatorResolver.resolve(itemCommand.getItemCode(), itemCommand.getItemName()));
            }
            HealthReportItemModel itemModel = new HealthReportItemModel();
            itemModel.loadCommand(itemCommand, reportEntity.getReportId(), reportEntity.getOwnerUserId(), sortSeed++);
            itemModel.checkFields();
            reportItems.add(itemModel);
        }
        return reportItems;
    }

    /**
     * 把后台解析结果转换成现有指标实体，复用同一套异常判定和字段校验逻辑。
     */
    private List<HealthReportItemEntity> buildReportItemsFromParsedResult(ParsedReportResult parsedResult,
        HealthReportEntity reportEntity) {
        if (parsedResult == null || parsedResult.getItems() == null || parsedResult.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        List<HealthReportItemEntity> reportItems = new ArrayList<>();
        int sortSeed = 1;
        for (ParsedReportItem parsedItem : parsedResult.getItems()) {
            HealthReportItemCommand command = new HealthReportItemCommand();
            command.setItemCode(parsedItem.getItemCode());
            command.setStandardItemCode(StrUtil.blankToDefault(parsedItem.getStandardItemCode(),
                standardIndicatorResolver.resolve(parsedItem.getItemCode(), parsedItem.getItemName())));
            command.setItemName(parsedItem.getItemName());
            command.setResultValue(parsedItem.getResultValue());
            command.setResultUnit(parsedItem.getResultUnit());
            command.setReferenceMin(parsedItem.getReferenceMin());
            command.setReferenceMax(parsedItem.getReferenceMax());
            command.setReferenceText(parsedItem.getReferenceText());
            command.setItemInterpretation(parsedItem.getItemInterpretation());
            command.setSort(parsedItem.getSort() == null ? sortSeed : parsedItem.getSort());
            command.setRemark(parsedItem.getRemark());
            HealthIndicatorTemplateEntity templateEntity =
                matchIndicatorTemplate(resolveEffectiveReportType(reportEntity), command.getItemCode(), command.getItemName());
            fillItemCommandByTemplate(command, templateEntity);
            HealthReportItemModel itemModel = new HealthReportItemModel();
            itemModel.loadCommand(command, reportEntity.getReportId(), reportEntity.getOwnerUserId(), sortSeed++);
            itemModel.checkFields();
            reportItems.add(itemModel);
        }
        return reportItems;
    }

    /**
     * 重新计算报告主表中的解析状态和摘要快照。
     *
     * <p>虽然分析摘要也能实时现算，但主表保留一份快照仍然有价值：
     * 1. 列表页可以直接展示摘要
     * 2. 后续首页健康看板可以快速读取
     * 3. 也为后台列表留出快速检索入口
     */
    private void refreshReportAnalysisSnapshot(HealthReportModel reportModel, List<HealthReportItemEntity> reportItems) {
        if (reportItems == null || reportItems.isEmpty()) {
            // 这里不再把“没有结构化指标”回写成“待解析”。
            //
            // 原先的逻辑会导致一个很迷惑的现象：
            // 文件其实已经完成 OCR / AI 识别，但只要没有成功抽出指标项，
            // 列表状态又会被打回“待解析”，前端看起来就像后台根本没开始干活。
            //
            // 现在改成：
            // 1. 解析流程只要正常跑完，就标记为“已解析”
            // 2. 是否产出指标，交给摘要文案和详情页去表达
            // 这样状态语义会更稳定，也更符合用户感知。
            reportModel.setParseStatus(HealthReportParseStatusEnum.COMPLETED.getValue());
            reportModel.setAnalysisSummary("报告文件已完成识别，但暂未提取到结构化指标，可人工补录或重试解析。");
            reportModel.setResultInterpretation("当前报告已完成解析，但暂未形成可用的结构化结果解读，建议人工补录关键指标后重新查看。");
            reportService.updateById(reportModel);
            return;
        }

        HealthReportAnalysisDTO analysisDTO = buildAnalysisDTO(reportModel, reportItems);
        reportModel.setParseStatus(HealthReportParseStatusEnum.COMPLETED.getValue());
        reportModel.setAnalysisSummary(limitLength(analysisDTO.getSummary(), 1000));
        reportModel.setResultInterpretation(limitLength(
            healthReportResultInterpretationGenerator.generate(reportModel, reportItems, analysisDTO), 2000));
        reportService.updateById(reportModel);
    }

    /**
     * 在报告保存完结构化结果后，尝试派发一条报告建议通知。
     *
     * <p>当前阶段选择“静默派发”而不是让通知失败阻塞主流程，原因是：
     * 1. 用户保存报告结果是主业务，通知只是增强体验
     * 2. 当前通知通道仍处于逐步建设阶段，不应影响报告主链路稳定性
     *
     * <p>当前版本已经接入消息中心落库，并在报告建议场景做基础去重：
     * 如果同一用户对同一报告重复保存出“相同异常集合”，
     * 则只保留一条消息并跳过重复 Push。
     */
    private void dispatchReportAdviceNoticeQuietly(HealthReportModel reportModel, List<HealthReportItemEntity> reportItems,
        Long ownerUserId) {
        List<HealthReportItemEntity> safeItems = reportItems == null ? Collections.emptyList() : reportItems;
        List<HealthReportItemEntity> abnormalItems = safeItems.stream()
            .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
            .collect(Collectors.toList());
        if (abnormalItems.isEmpty()) {
            return;
        }

        HealthAppMessageEntity messageEntity = null;
        try {
            int activeMedicationPlanCount = countActiveMedicationPlans(ownerUserId, reportModel.getMemberId());
            HealthReportAdviceDTO adviceDTO = buildAdviceDTO(reportModel, abnormalItems, activeMedicationPlanCount);
            HealthReportAdviceNotice notice = buildReportAdviceNotice(reportModel, adviceDTO, ownerUserId);
            if (notice == null) {
                return;
            }

            String dedupKey = buildReportAdviceMessageDedupKey(reportModel.getReportId(), abnormalItems);
            HealthAppMessageEntity existingMessage = appMessageApplicationService.getByOwnerUserIdAndDedupKey(ownerUserId, dedupKey);
            if (existingMessage != null
                && Objects.equals(existingMessage.getSendStatus(), HealthAppMessageSendStatusEnum.SUCCESS.getValue())) {
                // 这里仅在“同一条报告建议已经成功触达过”时才直接跳过，
                // 避免重复给用户发送完全相同的报告建议通知。
                //
                // 但如果历史上只是创建过消息、发送失败，或者状态仍停留在待发送，
                // 就不能直接 return。
                // 否则会出现：
                // 1. 首次解析时用户还没注册设备，Push 失败；
                // 2. 后续用户重新登录并上报了设备；
                // 3. 再次触发报告建议派发时，因为 dedupKey 已存在而被提前跳过；
                // 4. 最终用户只能在站内消息里看到记录，但永远收不到离线 Push。
                return;
            }

            messageEntity = existingMessage != null
                ? existingMessage
                : appMessageApplicationService.createOrReuseMessage(buildReportMessageCreateRequest(notice, dedupKey));
            notice.setMessageId(messageEntity.getMessageId());

            HealthReportAdviceNotifyResult notifyResult = healthReportAdviceNotifier.notify(notice);
            if (notifyResult != null && notifyResult.isSuccess()) {
                appMessageApplicationService.markMessageSendSuccess(messageEntity.getMessageId(),
                    notifyResult.getChannel(), notifyResult.getMessage());
                return;
            }

            appMessageApplicationService.markMessageSendFailed(messageEntity.getMessageId(),
                notifyResult == null ? null : notifyResult.getChannel(),
                notifyResult == null ? "报告建议发送器未返回结果" : notifyResult.getMessage());
            if (notifyResult == null || !notifyResult.isSuccess()) {
                log.warn("报告建议通知派发失败，报告ID：{}，用户ID：{}，结果：{}",
                    reportModel.getReportId(), ownerUserId, notifyResult == null ? "null" : notifyResult.getMessage());
            }
        } catch (Exception ex) {
            if (messageEntity != null && messageEntity.getMessageId() != null) {
                appMessageApplicationService.markMessageSendFailed(messageEntity.getMessageId(), null, ex.getMessage());
            }
            log.warn("报告建议通知派发异常，报告ID：{}，用户ID：{}，异常：{}",
                reportModel.getReportId(), ownerUserId, ex.getMessage(), ex);
        }
    }

    /**
     * 将异常指标同步为健康问题和问题证据。
     *
     * <p>这里故意只处理异常指标，且生成的是“健康问题/风险线索”，不是疾病诊断。
     * 同一成员同一标准指标编码只维护一个持续问题，后续多份报告会不断追加证据。
     */
    private void syncHealthProblems(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems) {
        List<HealthReportItemEntity> abnormalItems = reportItems == null
            ? Collections.emptyList()
            : reportItems.stream().filter(item -> isAbnormalFlag(item.getAbnormalFlag())).collect(Collectors.toList());
        if (abnormalItems.isEmpty()) {
            return;
        }

        for (HealthReportItemEntity item : abnormalItems) {
            String standardCode = StrUtil.blankToDefault(item.getStandardItemCode(),
                standardIndicatorResolver.resolve(item.getItemCode(), item.getItemName()));
            HealthProblemEntity problemEntity = findOrCreateHealthProblem(reportEntity, item, standardCode);
            upsertProblemEvidence(reportEntity, item, problemEntity);
        }
    }

    private HealthProblemEntity findOrCreateHealthProblem(HealthReportEntity reportEntity, HealthReportItemEntity item,
        String standardCode) {
        HealthProblemEntity existingProblem = healthProblemService.lambdaQuery()
            .eq(HealthProblemEntity::getOwnerUserId, reportEntity.getOwnerUserId())
            .eq(HealthProblemEntity::getMemberId, reportEntity.getMemberId())
            .eq(StrUtil.isNotBlank(standardCode), HealthProblemEntity::getStandardItemCode, standardCode)
            .one();
        String summary = StrUtil.format("{}{}，结果{}{}，来源报告：{}。",
            StrUtil.blankToDefault(item.getItemName(), "检查指标"),
            StrUtil.blankToDefault(resolveAbnormalFlagName(item.getAbnormalFlag()), "异常"),
            StrUtil.blankToDefault(item.getResultValue(), "-"),
            StrUtil.isBlank(item.getResultUnit()) ? "" : " " + item.getResultUnit(),
            StrUtil.blankToDefault(reportEntity.getReportName(), "检查报告"));
        if (existingProblem != null) {
            existingProblem.setRiskLevel(resolveProblemRiskLevel(item.getAbnormalFlag()));
            existingProblem.setLastFollowDate(reportEntity.getReportDate() == null ? new Date() : reportEntity.getReportDate());
            existingProblem.setSummary(limitLength(summary, 1000));
            healthProblemService.updateById(existingProblem);
            return existingProblem;
        }

        HealthProblemEntity problemEntity = new HealthProblemEntity();
        problemEntity.setOwnerUserId(reportEntity.getOwnerUserId());
        problemEntity.setMemberId(reportEntity.getMemberId());
        problemEntity.setProblemName(buildProblemName(item, standardCode));
        problemEntity.setProblemType("REPORT_ABNORMAL");
        problemEntity.setProblemStatus(1);
        problemEntity.setRiskLevel(resolveProblemRiskLevel(item.getAbnormalFlag()));
        problemEntity.setStandardItemCode(standardCode);
        problemEntity.setFirstFoundDate(reportEntity.getReportDate() == null ? new Date() : reportEntity.getReportDate());
        problemEntity.setLastFollowDate(problemEntity.getFirstFoundDate());
        problemEntity.setSummary(limitLength(summary, 1000));
            problemEntity.setDeleted(0);
        healthProblemService.save(problemEntity);
        return problemEntity;
    }

    private void upsertProblemEvidence(HealthReportEntity reportEntity, HealthReportItemEntity item,
        HealthProblemEntity problemEntity) {
        if (problemEntity == null || problemEntity.getProblemId() == null || item.getItemId() == null) {
            return;
        }
        HealthProblemEvidenceEntity existingEvidence = healthProblemEvidenceService.lambdaQuery()
            .eq(HealthProblemEvidenceEntity::getProblemId, problemEntity.getProblemId())
            .eq(HealthProblemEvidenceEntity::getReportItemId, item.getItemId())
            .one();
        if (existingEvidence != null) {
            return;
        }
        HealthProblemEvidenceEntity evidenceEntity = new HealthProblemEvidenceEntity();
        evidenceEntity.setProblemId(problemEntity.getProblemId());
        evidenceEntity.setOwnerUserId(reportEntity.getOwnerUserId());
        evidenceEntity.setMemberId(reportEntity.getMemberId());
        evidenceEntity.setEvidenceType("REPORT_ITEM");
        evidenceEntity.setReportId(reportEntity.getReportId());
        evidenceEntity.setReportItemId(item.getItemId());
        evidenceEntity.setEvidenceTitle(item.getItemName());
        evidenceEntity.setEvidenceSummary(StrUtil.format("{}{}，结果{}{}。",
            StrUtil.blankToDefault(item.getItemName(), "检查指标"),
            StrUtil.blankToDefault(resolveAbnormalFlagName(item.getAbnormalFlag()), "异常"),
            StrUtil.blankToDefault(item.getResultValue(), "-"),
            StrUtil.isBlank(item.getResultUnit()) ? "" : " " + item.getResultUnit()));
        evidenceEntity.setEvidenceDate(reportEntity.getReportDate() == null ? new Date() : reportEntity.getReportDate());
        evidenceEntity.setConfidenceLevel(2);
        evidenceEntity.setConfirmStatus(0);
            evidenceEntity.setDeleted(0);
        healthProblemEvidenceService.save(evidenceEntity);
    }

    private String buildProblemName(HealthReportItemEntity item, String standardCode) {
        String itemName = StrUtil.blankToDefault(item.getItemName(), StrUtil.blankToDefault(standardCode, "检查指标"));
        String abnormalName = StrUtil.blankToDefault(resolveAbnormalFlagName(item.getAbnormalFlag()), "异常");
        return limitLength(itemName + abnormalName + "管理", 100);
    }

    private Integer resolveProblemRiskLevel(Integer abnormalFlag) {
        if (abnormalFlag == null) {
            return 1;
        }
        if (HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue().equals(abnormalFlag)) {
            return 3;
        }
        if (HealthReportItemAbnormalFlagEnum.HIGH.getValue().equals(abnormalFlag)
            || HealthReportItemAbnormalFlagEnum.LOW.getValue().equals(abnormalFlag)) {
            return 2;
        }
        return 1;
    }

    /**
     * 查询当前报告下的全部指标结果。
     */
    private List<HealthReportItemEntity> listReportItems(Long reportId) {
        QueryWrapper<HealthReportItemEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_id", reportId)
            .orderByAsc("sort")
            .orderByAsc("item_id");
        return reportItemService.list(queryWrapper);
    }

    /**
     * 查询报告关联健康问题。
     *
     * <p>这里拆成“已授权后的纯数据装配方法”，
     * 让导出、详情扩展等聚合链路可以在完成一次成员权限校验后直接复用，
     * 不必再经过公共接口重复加载报告主表和重复做成员权限判断。
     */
    private List<HealthProblemDTO> listReportHealthProblems(Long reportId) {
        List<Long> problemIds = healthProblemEvidenceService.lambdaQuery()
            .eq(HealthProblemEvidenceEntity::getReportId, reportId)
            .list()
            .stream()
            .map(HealthProblemEvidenceEntity::getProblemId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (problemIds.isEmpty()) {
            return Collections.emptyList();
        }
        return healthProblemService.lambdaQuery()
            .in(HealthProblemEntity::getProblemId, problemIds)
            .orderByDesc(HealthProblemEntity::getRiskLevel)
            .orderByDesc(HealthProblemEntity::getLastFollowDate)
            .list()
            .stream()
            .map(HealthProblemDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * 统一加载“报告主表 + 成员访问上下文 + 成员展示快照”。
     *
     * <p>导出、趋势、详情扩展这类聚合链路都会同时依赖：
     * 1. 报告主表
     * 2. 当前账号对该成员的访问权限
     * 3. 成员名称/编码这类展示快照
     *
     * <p>把这三个动作收口后，可以避免同一请求里不同步骤各自再查一次报告或成员。
     */
    private AuthorizedReportContext loadAuthorizedReportContext(Long reportId, Long ownerUserId) {
        HealthReportModel reportModel = reportModelFactory.loadById(reportId);
        FamilyMemberAccessContextDTO accessContext =
            familyMemberAccessService.getRequiredAccessContext(reportModel.getMemberId(), ownerUserId);
        Set<Long> memberIds = reportModel.getMemberId() == null
            ? Collections.emptySet()
            : Collections.singleton(reportModel.getMemberId());
        Map<Long, HealthFamilyMemberEntity> memberMap = loadMemberMap(memberIds);
        return new AuthorizedReportContext(reportModel, accessContext, memberMap);
    }

    /**
     * 预热报告导出需要的全部运行时数据。
     *
     * <p>导出原先串行调用 5 个公开接口，每个接口又会各自：
     * 1. 重新加载 report 主表
     * 2. 重新做成员权限校验
     * 3. 重新查 report_item 或成员表
     *
     * <p>这里改成一次性预热上下文，让导出链路改回“同一次请求只查一遍该查的数据”。
     */
    private ReportExportRuntimeContext loadReportExportRuntimeContext(Long reportId, Long ownerUserId) {
        AuthorizedReportContext reportContext = loadAuthorizedReportContext(reportId, ownerUserId);
        HealthReportModel reportModel = reportContext.reportModel;
        List<HealthReportItemEntity> reportItems = listReportItems(reportId);
        List<HealthReportItemEntity> abnormalItems = reportItems.stream()
            .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
            .collect(Collectors.toList());
        List<HealthReportItemDTO> itemDTOs = reportItems.stream()
            .map(HealthReportItemDTO::new)
            .collect(Collectors.toList());
        String memberName = resolveMemberName(reportModel.getMemberId(), reportContext.memberMap);
        int activeMedicationPlanCount = countActiveMedicationPlans(reportModel.getOwnerUserId(), reportModel.getMemberId());
        HealthReportDTO reportDetail = buildReportDTO(reportModel, reportContext.accessContext, reportContext.memberMap);
        HealthReportAnalysisDTO analysis = buildAnalysisDTO(reportModel, reportItems);
        HealthReportAdviceDTO advice = buildAdviceDTO(reportModel, abnormalItems, activeMedicationPlanCount, memberName);
        List<HealthProblemDTO> healthProblems = listReportHealthProblems(reportId);
        return new ReportExportRuntimeContext(reportDetail, itemDTOs, analysis, advice, healthProblems);
    }

    /**
     * 统一构建报告分析摘要。
     */
    private HealthReportAnalysisDTO buildAnalysisDTO(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems) {
        List<HealthReportItemEntity> safeItems = Objects.requireNonNullElse(reportItems, Collections.emptyList());
        List<HealthReportItemDTO> abnormalItemDTOList = safeItems.stream()
            .filter(item -> isAbnormalFlag(item.getAbnormalFlag()))
            .map(HealthReportItemDTO::new)
            .collect(Collectors.toList());

        int normalCount = countByFlag(safeItems, HealthReportItemAbnormalFlagEnum.NORMAL.getValue());
        int lowCount = countByFlag(safeItems, HealthReportItemAbnormalFlagEnum.LOW.getValue());
        int highCount = countByFlag(safeItems, HealthReportItemAbnormalFlagEnum.HIGH.getValue());
        int abnormalCount = countByFlag(safeItems, HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue());
        int unknownCount = countByFlag(safeItems, HealthReportItemAbnormalFlagEnum.UNKNOWN.getValue());

        HealthReportAnalysisDTO dto = new HealthReportAnalysisDTO();
        dto.setReportId(reportEntity.getReportId());
        dto.setParseStatus(safeItems.isEmpty()
            ? HealthReportParseStatusEnum.WAIT_PARSE.getValue()
            : HealthReportParseStatusEnum.COMPLETED.getValue());
        dto.setTotalItemCount(safeItems.size());
        dto.setNormalItemCount(normalCount);
        dto.setLowItemCount(lowCount);
        dto.setHighItemCount(highCount);
        dto.setAbnormalItemCount(abnormalCount + lowCount + highCount);
        dto.setUnknownItemCount(unknownCount);
        dto.setSummary(buildAnalysisSummary(safeItems, abnormalItemDTOList, unknownCount));
        dto.setAbnormalItems(abnormalItemDTOList);
        return dto;
    }

    /**
     * 构建简短分析摘要，供报告列表和详情快速展示。
     */
    private String buildAnalysisSummary(List<HealthReportItemEntity> reportItems, List<HealthReportItemDTO> abnormalItems,
        int unknownCount) {
        if (reportItems == null || reportItems.isEmpty()) {
            return null;
        }

        if (abnormalItems == null || abnormalItems.isEmpty()) {
            StringBuilder summaryBuilder = new StringBuilder("共录入")
                .append(reportItems.size())
                .append("项指标，暂未发现异常指标");
            if (unknownCount > 0) {
                summaryBuilder.append("，另有").append(unknownCount).append("项待人工判断");
            }
            summaryBuilder.append("。");
            return summaryBuilder.toString();
        }

        String abnormalItemNames = abnormalItems.stream()
            .limit(5)
            .map(item -> StrUtil.format("{}{}", item.getItemName(),
                StrUtil.isBlank(item.getAbnormalFlagName()) ? "" : item.getAbnormalFlagName()))
            .collect(Collectors.joining("、"));

        StringBuilder summaryBuilder = new StringBuilder("共录入")
            .append(reportItems.size())
            .append("项指标，其中异常")
            .append(abnormalItems.size())
            .append("项：")
            .append(abnormalItemNames);
        if (abnormalItems.size() > 5) {
            summaryBuilder.append("等");
        }
        if (unknownCount > 0) {
            summaryBuilder.append("；另有").append(unknownCount).append("项待人工判断");
        }
        summaryBuilder.append("。");
        return summaryBuilder.toString();
    }

    /**
     * 解析当前账号可访问的成员范围。
     */
    private Set<Long> resolveAccessibleMemberIds(Long currentUserId, Long memberId) {
        Set<Long> accessibleMemberIds = familyMemberAccessService.getAccessibleMemberIds(currentUserId);
        if (memberId == null) {
            return accessibleMemberIds;
        }
        familyMemberAccessService.getRequiredAccessContext(memberId, currentUserId);
        return Collections.singleton(memberId);
    }

    /**
     * 基于模板补齐指标命令缺失字段。
     */
    private void fillItemCommandByTemplate(HealthReportItemCommand itemCommand, HealthIndicatorTemplateEntity templateEntity) {
        if (itemCommand == null || templateEntity == null) {
            return;
        }
        if (StrUtil.isBlank(itemCommand.getResultUnit())) {
            itemCommand.setResultUnit(templateEntity.getResultUnit());
        }
        if (itemCommand.getReferenceMin() == null) {
            itemCommand.setReferenceMin(templateEntity.getReferenceMin());
        }
        if (itemCommand.getReferenceMax() == null) {
            itemCommand.setReferenceMax(templateEntity.getReferenceMax());
        }
        if (StrUtil.isBlank(itemCommand.getReferenceText())) {
            itemCommand.setReferenceText(templateEntity.getReferenceText());
        }
    }

    /**
     * 匹配启用中的指标模板。
     */
    private HealthIndicatorTemplateEntity matchIndicatorTemplate(String reportType, String itemCode, String itemName) {
        if (StrUtil.isBlank(itemName)) {
            return null;
        }
        return indicatorTemplateService.matchEnabledTemplate(reportType, itemCode, itemName);
    }

    /**
     * 统一决定当前报告用于模板匹配的“有效报告类型”。
     *
     * <p>优先使用后台识别出的细分类型，原因是它通常比用户手填值更精确，
     * 例如用户填“血液检查”，模型识别出“血常规”后，
     * 指标模板命中率会明显高于继续使用泛类型。
     *
     * <p>如果识别类型为空，再退回用户原始 `reportType`，保持兼容。
     */
    private String resolveEffectiveReportType(HealthReportEntity reportEntity) {
        if (reportEntity == null) {
            return null;
        }
        return StrUtil.blankToDefault(
            StrUtil.trim(reportEntity.getRecognizedReportType()),
            StrUtil.trim(reportEntity.getReportType()));
    }

    /**
     * 构建单个指标的趋势对比结果。
     */
    private HealthReportTrendItemDTO buildTrendItem(HealthReportItemEntity currentItem, List<HealthReportEntity> historyReports,
        Map<Long, Map<String, HealthReportItemEntity>> comparableItemMap) {
        List<HealthReportTrendPointDTO> trendPoints = buildTrendPoints(currentItem, historyReports, comparableItemMap);
        HealthReportTrendPointDTO previousPoint = trendPoints.size() > 1 ? trendPoints.get(trendPoints.size() - 2) : null;
        HealthReportTrendPointDTO currentPoint = trendPoints.isEmpty() ? null : trendPoints.get(trendPoints.size() - 1);

        HealthReportTrendItemDTO dto = new HealthReportTrendItemDTO();
        dto.setItemCode(currentItem.getItemCode());
        dto.setStandardItemCode(resolveComparableKey(currentItem));
        dto.setItemName(currentItem.getItemName());
        dto.setResultUnit(currentItem.getResultUnit());
        dto.setCurrentResultValue(currentItem.getResultValue());
        dto.setPreviousResultValue(previousPoint == null ? null : previousPoint.getResultValue());
        dto.setChangeDirection(resolveChangeDirection(currentPoint, previousPoint));
        dto.setChangeSummary(resolveChangeSummary(currentPoint, previousPoint));
        dto.setAbnormalFlag(currentItem.getAbnormalFlag());
        dto.setAbnormalFlagName(resolveAbnormalFlagName(currentItem.getAbnormalFlag()));
        dto.setTrendPoints(trendPoints);
        return dto;
    }

    /**
     * 构建某个指标在多份报告中的趋势点列表。
     */
    private List<HealthReportTrendPointDTO> buildTrendPoints(HealthReportItemEntity currentItem, List<HealthReportEntity> historyReports,
        Map<Long, Map<String, HealthReportItemEntity>> comparableItemMap) {
        List<HealthReportTrendPointDTO> trendPoints = new ArrayList<>();
        String comparableKey = resolveComparableKey(currentItem);

        for (HealthReportEntity historyReport : historyReports) {
            Map<String, HealthReportItemEntity> historyItemMap =
                comparableItemMap.getOrDefault(historyReport.getReportId(), Collections.emptyMap());
            HealthReportItemEntity matchedItem = historyItemMap.get(comparableKey);
            if (matchedItem == null) {
                continue;
            }

            HealthReportTrendPointDTO trendPointDTO = new HealthReportTrendPointDTO();
            trendPointDTO.setReportId(historyReport.getReportId());
            trendPointDTO.setReportDate(historyReport.getReportDate());
            trendPointDTO.setResultValue(matchedItem.getResultValue());
            trendPointDTO.setResultUnit(matchedItem.getResultUnit());
            trendPointDTO.setAbnormalFlag(matchedItem.getAbnormalFlag());
            trendPointDTO.setAbnormalFlagName(resolveAbnormalFlagName(matchedItem.getAbnormalFlag()));
            trendPoints.add(trendPointDTO);
        }
        return trendPoints;
    }

    /**
     * 构建“reportId -> comparableKey -> item”的索引。
     *
     * <p>趋势组装最常见的访问模式不是“拿到一份报告后遍历所有指标”，
     * 而是“给我某份报告里某个 comparableKey 对应的指标”。
     * 因此这里提前把列表拍平成 O(1) 查找索引，避免后续每个趋势点都再走一次 stream filter。
     */
    private Map<String, HealthReportItemEntity> buildComparableItemLookup(List<HealthReportItemEntity> reportItems,
        Set<String> comparableKeys) {
        Map<String, HealthReportItemEntity> comparableItemLookup = new HashMap<>();
        if (reportItems == null || reportItems.isEmpty()) {
            return comparableItemLookup;
        }
        for (HealthReportItemEntity reportItem : reportItems) {
            String comparableKey = resolveComparableKey(reportItem);
            if (StrUtil.isBlank(comparableKey)) {
                continue;
            }
            if (comparableKeys != null && !comparableKeys.isEmpty() && !comparableKeys.contains(comparableKey)) {
                continue;
            }
            comparableItemLookup.putIfAbsent(comparableKey, reportItem);
        }
        return comparableItemLookup;
    }

    /**
     * 把多份历史报告的指标列表批量合并进趋势索引。
     *
     * <p>这里保留每个 reportId 下第一个命中的 comparableKey 结果，
     * 这样可以与原先按 `sort -> itemId` 顺序取首个匹配项的行为保持一致。
     */
    private void mergeComparableItemLookup(Map<Long, Map<String, HealthReportItemEntity>> comparableItemMap,
        List<HealthReportItemEntity> historyItems, Set<String> comparableKeys) {
        if (comparableItemMap == null || historyItems == null || historyItems.isEmpty()) {
            return;
        }
        for (HealthReportItemEntity historyItem : historyItems) {
            if (historyItem.getReportId() == null) {
                continue;
            }
            Map<String, HealthReportItemEntity> reportLookup =
                comparableItemMap.computeIfAbsent(historyItem.getReportId(), key -> new HashMap<>());
            String comparableKey = resolveComparableKey(historyItem);
            if (StrUtil.isBlank(comparableKey)) {
                continue;
            }
            if (comparableKeys != null && !comparableKeys.isEmpty() && !comparableKeys.contains(comparableKey)) {
                continue;
            }
            reportLookup.putIfAbsent(comparableKey, historyItem);
        }
    }

    /**
     * 判断某份历史报告是否至少包含一个可用于趋势对比的指标。
     */
    private boolean hasComparableTrendItem(Map<Long, Map<String, HealthReportItemEntity>> comparableItemMap, Long reportId) {
        if (comparableItemMap == null || reportId == null) {
            return false;
        }
        Map<String, HealthReportItemEntity> reportLookup = comparableItemMap.get(reportId);
        return reportLookup != null && !reportLookup.isEmpty();
    }

    /**
     * 解析趋势对比键。
     */
    private String resolveComparableKey(HealthReportItemEntity itemEntity) {
        if (itemEntity == null) {
            return null;
        }
        return StrUtil.blankToDefault(itemEntity.getStandardItemCode(),
            StrUtil.blankToDefault(itemEntity.getItemCode(), itemEntity.getItemName()));
    }

    /**
     * 解析趋势变化方向。
     */
    private String resolveChangeDirection(HealthReportTrendPointDTO currentPoint, HealthReportTrendPointDTO previousPoint) {
        Double currentValue = parseNumericValue(currentPoint == null ? null : currentPoint.getResultValue());
        Double previousValue = parseNumericValue(previousPoint == null ? null : previousPoint.getResultValue());
        if (currentValue == null || previousValue == null) {
            return previousPoint == null ? "NO_BASELINE" : "UNCHANGED";
        }
        if (Math.abs(currentValue - previousValue) < 0.000001D) {
            return "UNCHANGED";
        }
        return currentValue > previousValue ? "UP" : "DOWN";
    }

    /**
     * 构建趋势变化说明文案。
     */
    private String resolveChangeSummary(HealthReportTrendPointDTO currentPoint, HealthReportTrendPointDTO previousPoint) {
        if (previousPoint == null) {
            return "当前仅有本次报告数据，暂无法形成前后对比。";
        }
        String direction = resolveChangeDirection(currentPoint, previousPoint);
        if (Objects.equals(direction, "UP")) {
            return StrUtil.format("相较上次结果 {}，本次结果 {} 呈上升趋势。",
                previousPoint.getResultValue(), currentPoint == null ? null : currentPoint.getResultValue());
        }
        if (Objects.equals(direction, "DOWN")) {
            return StrUtil.format("相较上次结果 {}，本次结果 {} 呈下降趋势。",
                previousPoint.getResultValue(), currentPoint == null ? null : currentPoint.getResultValue());
        }
        return StrUtil.format("相较上次结果 {}，本次结果 {} 变化不明显或暂无法量化。",
            previousPoint.getResultValue(), currentPoint == null ? null : currentPoint.getResultValue());
    }

    /**
     * 解析结果值中的首个数值，供趋势对比使用。
     */
    private Double parseNumericValue(String resultValue) {
        if (StrUtil.isBlank(resultValue)) {
            return null;
        }
        Matcher matcher = TREND_NUMBER_PATTERN.matcher(resultValue);
        if (!matcher.find()) {
            return null;
        }
        return Double.valueOf(matcher.group());
    }

    /**
     * 判断是否属于需要重点提示给前端的异常标记。
     */
    private boolean isAbnormalFlag(Integer abnormalFlag) {
        return Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.LOW.getValue())
            || Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.HIGH.getValue())
            || Objects.equals(abnormalFlag, HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue());
    }

    /**
     * 统计指定异常标记的数量。
     */
    private int countByFlag(List<HealthReportItemEntity> reportItems, Integer abnormalFlag) {
        return (int) reportItems.stream()
            .filter(item -> Objects.equals(item.getAbnormalFlag(), abnormalFlag))
            .count();
    }

    /**
     * 统计当前成员启用中的用药计划数量。
     *
     * <p>这里的“启用中”先按业务状态判断，
     * 当前阶段不再额外限定日期窗口，目的是让用户能看到该成员当前是否已经进入持续用药管理流程。
     */
    private int countActiveMedicationPlans(Long ownerUserId, Long memberId) {
        if (ownerUserId == null || memberId == null) {
            return 0;
        }
        return medicationPlanService.lambdaQuery()
            .eq(HealthMedicationPlanEntity::getOwnerUserId, ownerUserId)
            .eq(HealthMedicationPlanEntity::getMemberId, memberId)
            .eq(HealthMedicationPlanEntity::getStatus, StatusEnum.ENABLE.getValue())
            .count().intValue();
    }

    /**
     * 批量统计成员当前启用中的用药计划数量。
     *
     * <p>这里返回 memberId -> count 的映射，专门服务“同一个 owner 下多份报告一起算建议”的场景。
     * 这样首页任务流就不用在循环里一条报告执行一次 `count()`。
     */
    private Map<Long, Integer> loadActiveMedicationPlanCountMap(Long ownerUserId, List<Long> memberIds) {
        if (ownerUserId == null || memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return medicationPlanService.lambdaQuery()
            .eq(HealthMedicationPlanEntity::getOwnerUserId, ownerUserId)
            .in(HealthMedicationPlanEntity::getMemberId, memberIds)
            .eq(HealthMedicationPlanEntity::getStatus, StatusEnum.ENABLE.getValue())
            .list()
            .stream()
            .filter(plan -> plan.getMemberId() != null)
            .collect(Collectors.toMap(HealthMedicationPlanEntity::getMemberId,
                plan -> 1,
                Integer::sum));
    }

    /**
     * 构建报告异常建议结果。
     */
    private HealthReportAdviceDTO buildAdviceDTO(HealthReportEntity reportEntity, List<HealthReportItemEntity> abnormalItems,
        int activeMedicationPlanCount) {
        return buildAdviceDTO(reportEntity, abnormalItems, activeMedicationPlanCount, resolveMemberName(reportEntity.getMemberId()));
    }

    /**
     * 构建报告异常建议结果。
     *
     * <p>这个重载允许调用方把成员展示名提前批量预热后再传进来，
     * 从而避免导出、批量建议等聚合链路在构建 DTO 时再次单条查成员表。
     */
    private HealthReportAdviceDTO buildAdviceDTO(HealthReportEntity reportEntity, List<HealthReportItemEntity> abnormalItems,
        int activeMedicationPlanCount, String memberName) {
        HealthReportAdviceDTO dto = new HealthReportAdviceDTO();
        dto.setReportId(reportEntity.getReportId());
        dto.setMemberId(reportEntity.getMemberId());
        dto.setMemberName(memberName);
        dto.setAbnormalItemCount(abnormalItems.size());
        dto.setHasActiveMedicationPlan(activeMedicationPlanCount > 0);
        dto.setActiveMedicationPlanCount(activeMedicationPlanCount);

        List<HealthReportAdviceItemDTO> adviceItems = buildAdviceItems(abnormalItems, activeMedicationPlanCount);
        dto.setAdviceItems(adviceItems);
        dto.setSummary(buildAdviceSummary(abnormalItems, activeMedicationPlanCount));
        return dto;
    }

    /**
     * 组装 AI 智能总结正文。
     *
     * <p>为了让前端展示结果更像“可读总结”而不是原始数据拼接，
     * 这里固定拆成四段：
     * 1. 本次报告概览
     * 2. 重点风险判断
     * 3. 趋势变化提示
     * 4. 后续跟进建议
     */
    private String buildAiSummaryContent(HealthReportEntity reportEntity, List<HealthReportItemEntity> reportItems,
        List<HealthReportItemEntity> abnormalItems, HealthReportAnalysisDTO analysisDTO, HealthReportAdviceDTO adviceDTO,
        HealthReportTrendDTO trendDTO, int activeMedicationPlanCount) {
        if (reportItems == null || reportItems.isEmpty()) {
            return buildAiSummaryWithoutItems(reportEntity);
        }

        List<String> paragraphs = new ArrayList<>();
        paragraphs.add(buildAiOverviewParagraph(reportEntity, analysisDTO));
        paragraphs.add(buildAiRiskParagraph(abnormalItems, adviceDTO));
        paragraphs.add(buildAiTrendParagraph(trendDTO));
        paragraphs.add(buildAiActionParagraph(adviceDTO, activeMedicationPlanCount));
        return paragraphs.stream()
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining("\n\n"));
    }

    /**
     * 没有结构化指标时，明确告诉用户当前总结能力受限的原因，
     * 并给出下一步动作，而不是直接抛异常让 App 端变成失败提示。
     */
    private String buildAiSummaryWithoutItems(HealthReportEntity reportEntity) {
        String memberName = StrUtil.blankToDefault(resolveMemberName(reportEntity.getMemberId()), "该成员");
        return StrUtil.format("{}的《{}》当前还没有录入结构化指标，因此暂时无法生成基于指标的智能总结。"
                + "建议先在报告详情中补充关键指标，系统随后会自动输出分析摘要、异常建议和趋势对比。",
            memberName,
            StrUtil.blankToDefault(reportEntity.getReportName(), "体检报告"));
    }

    /**
     * 生成报告概览段落。
     * 这一段主要回答“这份报告是什么、录入了多少项、总体结果如何”。
     */
    private String buildAiOverviewParagraph(HealthReportEntity reportEntity, HealthReportAnalysisDTO analysisDTO) {
        String memberName = StrUtil.blankToDefault(resolveMemberName(reportEntity.getMemberId()), "该成员");
        String reportDateText = reportEntity.getReportDate() == null ? "未填写日期" : DateUtil.formatDate(reportEntity.getReportDate());
        return StrUtil.format("{}的《{}》已完成智能归纳。报告日期为{}，当前共录入{}项指标，其中异常{}项、待人工判断{}项。{}",
            memberName,
            StrUtil.blankToDefault(reportEntity.getReportName(), "体检报告"),
            reportDateText,
            Objects.requireNonNullElse(analysisDTO.getTotalItemCount(), 0),
            Objects.requireNonNullElse(analysisDTO.getAbnormalItemCount(), 0),
            Objects.requireNonNullElse(analysisDTO.getUnknownItemCount(), 0),
            StrUtil.blankToDefault(analysisDTO.getSummary(), "当前暂无额外分析摘要。"));
    }

    /**
     * 生成风险判断段落。
     * 这一段把异常指标和建议摘要揉成自然语言，方便用户快速抓住重点。
     */
    private String buildAiRiskParagraph(List<HealthReportItemEntity> abnormalItems, HealthReportAdviceDTO adviceDTO) {
        if (abnormalItems == null || abnormalItems.isEmpty()) {
            return "重点风险判断：当前未发现需要优先干预的异常指标，可继续保持现有健康管理节奏，并按周期复查。";
        }

        String abnormalDigest = abnormalItems.stream()
            .limit(3)
            .map(item -> StrUtil.format("{}({}，{})",
                StrUtil.blankToDefault(item.getItemName(), "未命名指标"),
                StrUtil.blankToDefault(resolveAbnormalFlagName(item.getAbnormalFlag()), "异常"),
                StrUtil.blankToDefault(buildDisplayResult(item), "无结果值")))
            .collect(Collectors.joining("；"));

        StringBuilder paragraph = new StringBuilder("重点风险判断：当前优先关注 ")
            .append(abnormalDigest);
        if (abnormalItems.size() > 3) {
            paragraph.append(" 等指标。");
        } else {
            paragraph.append("。");
        }
        paragraph.append(StrUtil.blankToDefault(adviceDTO.getSummary(), "建议结合完整报告和线下意见继续跟进。"));
        return paragraph.toString();
    }

    /**
     * 生成趋势变化段落。
     *
     * <p>趋势总结优先挑选已经能量化出“上升 / 下降 / 变化不明显”的指标，
     * 这样用户看到的不是原始点位列表，而是直接可理解的变化描述。
     */
    private String buildAiTrendParagraph(HealthReportTrendDTO trendDTO) {
        if (trendDTO == null || trendDTO.getItems() == null || trendDTO.getItems().isEmpty()) {
            return "趋势变化：当前缺少可复用的历史同类报告，暂时无法判断长期变化趋势。";
        }

        List<String> trendSummaries = trendDTO.getItems().stream()
            .filter(Objects::nonNull)
            .filter(item -> StrUtil.isNotBlank(item.getChangeSummary()))
            .limit(3)
            .map(item -> StrUtil.format("{}：{}",
                StrUtil.blankToDefault(item.getItemName(), "指标"),
                item.getChangeSummary()))
            .collect(Collectors.toList());
        if (trendSummaries.isEmpty()) {
            return StrUtil.format("趋势变化：系统已对比{}份同类历史报告，但当前暂无可稳定量化的趋势结论。",
                Objects.requireNonNullElse(trendDTO.getComparedReportCount(), 0));
        }
        return StrUtil.format("趋势变化：系统已对比{}份同类历史报告。{}",
            Objects.requireNonNullElse(trendDTO.getComparedReportCount(), 0),
            StrUtil.join("；", trendSummaries));
    }

    /**
     * 生成后续跟进段落。
     *
     * <p>这里会把建议标题、当前用药计划状态一起带出来，
     * 让用户知道下一步应该去哪里继续操作。
     */
    private String buildAiActionParagraph(HealthReportAdviceDTO adviceDTO, int activeMedicationPlanCount) {
        List<String> actionTitles = adviceDTO.getAdviceItems() == null
            ? Collections.emptyList()
            : adviceDTO.getAdviceItems().stream()
                .filter(Objects::nonNull)
                .map(HealthReportAdviceItemDTO::getTitle)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .limit(2)
                .collect(Collectors.toList());

        StringBuilder paragraph = new StringBuilder("后续跟进建议：");
        if (actionTitles.isEmpty()) {
            paragraph.append("建议继续保留本次报告，并结合日常健康管理节奏安排后续观察。");
        } else {
            paragraph.append("优先处理 ")
                .append(StrUtil.join("、", actionTitles))
                .append("。");
        }

        if (activeMedicationPlanCount > 0) {
            paragraph.append("当前该成员已有")
                .append(activeMedicationPlanCount)
                .append("条启用中的用药计划，可直接联动今日提醒持续跟踪执行情况。");
        } else {
            paragraph.append("当前该成员暂无启用中的用药计划，如需持续观察，可补充用药提醒或复查计划。");
        }
        return paragraph.toString();
    }

    /**
     * 尝试使用外部大模型生成总结。
     *
     * <p>为了控制模型成本和稳定性，这里只把“已经清洗过的关键上下文”发给外部模型，
     * 不会把整份原始报告文本无限制透传出去。
     */
    private String buildExternalAiSummary(HealthReportEntity reportEntity, List<HealthReportItemEntity> abnormalItems,
        HealthReportAnalysisDTO analysisDTO, HealthReportAdviceDTO adviceDTO, HealthReportTrendDTO trendDTO,
        int activeMedicationPlanCount) {
        if (analysisDTO == null || Objects.requireNonNullElse(analysisDTO.getTotalItemCount(), 0) <= 0) {
            return null;
        }
        String systemPrompt = StrUtil.blankToDefault(healthReportExternalLlmProperties.getSystemPrompt(),
            "你是一名医疗健康管理总结助手。请基于输入的结构化体检报告数据，用中文输出一段简明、可信、克制的健康管理总结。"
                + "不要杜撰未提供的信息，不要给出确诊结论，不要代替医生开药。总结应覆盖总体情况、重点异常、趋势变化和后续建议，"
                + "对明确异常的重点指标，在医学上能够合理推断时，可补充“可能出现的常见症状/不适表现”提示，但必须使用“可能、如伴有、若近期存在”等风险提示语气，"
                + "不能把症状写成已经发生的事实。语言保持专业、准确、简短，便于普通用户理解，可提示结合医生诊断，不要输出 Markdown 标题或代码块。");
        String userPrompt = buildExternalAiSummaryUserPrompt(reportEntity, abnormalItems, analysisDTO, adviceDTO, trendDTO,
            activeMedicationPlanCount);
        return healthReportExternalLlmClient.generateSummary(systemPrompt, userPrompt);
    }

    /**
     * 构建外部大模型用户提示词。
     * 这里把上下文明确组织成 JSON，方便后续排查模型输入内容。
     */
    private String buildExternalAiSummaryUserPrompt(HealthReportEntity reportEntity, List<HealthReportItemEntity> abnormalItems,
        HealthReportAnalysisDTO analysisDTO, HealthReportAdviceDTO adviceDTO, HealthReportTrendDTO trendDTO,
        int activeMedicationPlanCount) {
        /**
         * 这里额外强调“异常指标可能导致什么症状”的原因：
         * 1. 用户已经不满足于只看“异常了什么”，还希望知道“这类异常通常会带来什么感受”；
         * 2. 但症状提示天然比结果值更容易被模型说得过头，所以必须把语气约束写死在 prompt 里；
         * 3. 因此同一处同时补两类约束：要求补充常见症状提示，以及禁止把症状写成已经发生的事实。
         */
        JSONObject contextJson = JSONUtil.createObj()
            .set("memberName", resolveMemberName(reportEntity.getMemberId()))
            .set("reportName", reportEntity.getReportName())
            .set("reportType", reportEntity.getReportType())
            .set("reportDate", reportEntity.getReportDate() == null ? null : DateUtil.formatDate(reportEntity.getReportDate()))
            .set("analysisSummary", analysisDTO == null ? null : analysisDTO.getSummary())
            .set("abnormalItemCount", adviceDTO == null ? 0 : adviceDTO.getAbnormalItemCount())
            .set("activeMedicationPlanCount", activeMedicationPlanCount)
            .set("abnormalItems", buildExternalAbnormalItemsJson(abnormalItems))
            .set("adviceItems", buildExternalAdviceItemsJson(adviceDTO))
            .set("trendItems", buildExternalTrendItemsJson(trendDTO));

        return "请根据下面的结构化体检报告数据生成中文智能总结。"
            + "输出要求："
            + "1. 直接输出自然语言正文，不要加 Markdown 标题；"
            + "2. 优先覆盖总体情况、重点异常、趋势变化、后续跟进建议；"
            + "3. 对重点异常尽量结合具体结果值说清楚，不要只做空泛提醒；"
            + "4. 对明确异常的重点指标，在医学上能够合理推断时，可补充1到2个常见的可能症状或不适表现；"
            + "5. 症状提示必须使用“可能出现、如伴有、若近期存在”等表述，不能把症状写成已经发生的事实，也不能编造用户未提供的主诉；"
            + "6. 如果无法从现有结构化数据稳定推断症状，就不要强行补症状；"
            + "7. 建议必须克制、专业、简短，不给诊疗结论，可提示结合医生诊断；"
            + "8. 结尾简短提醒这不是医学诊断；"
            + "9. 控制在 160 到 280 字之间。"
            + "\n\n结构化数据如下：\n"
            + JSONUtil.toJsonPrettyStr(contextJson);
    }

    private JSONArray buildExternalAbnormalItemsJson(List<HealthReportItemEntity> abnormalItems) {
        JSONArray abnormalArray = new JSONArray();
        if (abnormalItems == null || abnormalItems.isEmpty()) {
            return abnormalArray;
        }
        abnormalItems.stream()
            .limit(6)
            .forEach(item -> abnormalArray.add(JSONUtil.createObj()
                .set("itemName", item.getItemName())
                .set("resultValue", buildDisplayResult(item))
                .set("abnormalFlagName", resolveAbnormalFlagName(item.getAbnormalFlag()))));
        return abnormalArray;
    }

    private JSONArray buildExternalAdviceItemsJson(HealthReportAdviceDTO adviceDTO) {
        JSONArray adviceArray = new JSONArray();
        if (adviceDTO == null || adviceDTO.getAdviceItems() == null || adviceDTO.getAdviceItems().isEmpty()) {
            return adviceArray;
        }
        adviceDTO.getAdviceItems().stream()
            .filter(Objects::nonNull)
            .limit(5)
            .forEach(item -> adviceArray.add(JSONUtil.createObj()
                .set("title", item.getTitle())
                .set("content", item.getContent())
                .set("actionText", item.getActionText())));
        return adviceArray;
    }

    private JSONArray buildExternalTrendItemsJson(HealthReportTrendDTO trendDTO) {
        JSONArray trendArray = new JSONArray();
        if (trendDTO == null || trendDTO.getItems() == null || trendDTO.getItems().isEmpty()) {
            return trendArray;
        }
        trendDTO.getItems().stream()
            .filter(Objects::nonNull)
            .filter(item -> StrUtil.isNotBlank(item.getChangeSummary()))
            .limit(5)
            .forEach(item -> trendArray.add(JSONUtil.createObj()
                .set("itemName", item.getItemName())
                .set("currentResultValue", item.getCurrentResultValue())
                .set("previousResultValue", item.getPreviousResultValue())
                .set("changeSummary", item.getChangeSummary())));
        return trendArray;
    }

    /**
     * 根据总结来源回填不同的风险声明，确保前端和业务人员都能看懂当前结果来自哪里。
     */
    private String buildAiSummaryDisclaimer(boolean usedExternalLlm) {
        if (usedExternalLlm) {
            return StrUtil.format("该总结已结合外部大模型（{}）生成，并基于结构化指标、趋势变化与规则建议做增强归纳，"
                    + "仅用于健康管理提示，不构成医学诊断或处方建议。",
                StrUtil.blankToDefault(healthReportExternalLlmClient.getProviderName(), "OpenAI-Compatible"));
        }
        return "该总结基于结构化指标、趋势变化和规则建议自动生成，用于健康管理提示，不构成医学诊断或处方建议。";
    }

    /**
     * 构建报告建议通知标准载荷。
     */
    private HealthReportAdviceNotice buildReportAdviceNotice(HealthReportEntity reportEntity, HealthReportAdviceDTO adviceDTO,
        Long ownerUserId) {
        if (reportEntity == null || adviceDTO == null || adviceDTO.getAbnormalItemCount() == null
            || adviceDTO.getAbnormalItemCount() <= 0) {
            return null;
        }

        return HealthReportAdviceNotice.builder()
            .reportId(reportEntity.getReportId())
            .ownerUserId(ownerUserId)
            .memberId(reportEntity.getMemberId())
            .memberName(resolveMemberName(reportEntity.getMemberId()))
            .reportName(reportEntity.getReportName())
            .adviceSummary(adviceDTO.getSummary())
            .pushPayload(buildReportPushPayload(reportEntity, adviceDTO))
            .build();
    }

    /**
     * 构建报告建议消息落库请求。
     *
     * <p>报告建议消息不是简单按报告ID唯一，
     * 而是按“报告ID + 当前异常项签名”去重。
     * 这样同一份报告后续如果录入了新的异常结果，仍然可以生成新消息。
     */
    private HealthAppMessageCreateRequest buildReportMessageCreateRequest(HealthReportAdviceNotice notice, String dedupKey) {
        return HealthAppMessageCreateRequest.builder()
            .ownerUserId(notice.getOwnerUserId())
            .memberId(notice.getMemberId())
            .memberName(notice.getMemberName())
            .businessScene(HealthAppMessageSceneEnum.REPORT_FOLLOW_UP.getValue())
            .businessId(notice.getReportId())
            .messageTitle(notice.getPushPayload() == null ? null : notice.getPushPayload().getTitle())
            .messageContent(notice.getPushPayload() == null ? null : notice.getPushPayload().getContent())
            .payload(notice.getPushPayload())
            .dedupKey(dedupKey)
            .build();
    }

    /**
     * 构建报告建议消息去重键。
     *
     * <p>这里不直接使用指标结果表主键，
     * 因为保存报告指标时采用的是“先删后存”，主键会变化。
     * 因此改为对异常项的业务特征做归一化签名，再生成摘要哈希。
     */
    private String buildReportAdviceMessageDedupKey(Long reportId, List<HealthReportItemEntity> abnormalItems) {
        if (reportId == null || abnormalItems == null || abnormalItems.isEmpty()) {
            return null;
        }

        String abnormalSignature = abnormalItems.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing((HealthReportItemEntity item) -> StrUtil.blankToDefault(item.getItemName(), ""))
                .thenComparing(item -> Objects.toString(item.getAbnormalFlag(), ""))
                .thenComparing(item -> StrUtil.blankToDefault(item.getResultValue(), ""))
                .thenComparing(item -> StrUtil.blankToDefault(item.getResultUnit(), "")))
            .map(item -> StrUtil.join("|",
                StrUtil.blankToDefault(item.getItemName(), ""),
                Objects.toString(item.getAbnormalFlag(), ""),
                StrUtil.blankToDefault(item.getResultValue(), ""),
                StrUtil.blankToDefault(item.getResultUnit(), "")))
            .collect(Collectors.joining(";"));

        return HealthAppMessageSceneEnum.REPORT_FOLLOW_UP.getValue()
            + ":" + reportId
            + ":" + DigestUtil.md5Hex(abnormalSignature);
    }

    /**
     * 构建报告建议 Push 统一业务透传载荷。
     */
    private HealthAppPushPayloadDTO buildReportPushPayload(HealthReportEntity reportEntity, HealthReportAdviceDTO adviceDTO) {
        HealthReportAdviceItemDTO firstAdvice = adviceDTO.getAdviceItems() == null || adviceDTO.getAdviceItems().isEmpty()
            ? null
            : adviceDTO.getAdviceItems().get(0);
        HealthFollowUpNavigationDTO navigationDTO = buildReportNavigation(reportEntity.getReportId(), firstAdvice);
        HealthFollowUpRecommendedActionDTO recommendedActionDTO = buildReportRecommendedAction(adviceDTO, firstAdvice,
            navigationDTO);

        HealthAppPushPayloadDTO payloadDTO = new HealthAppPushPayloadDTO();
        payloadDTO.setBusinessScene("REPORT_FOLLOW_UP");
        payloadDTO.setBusinessId(reportEntity.getReportId());
        payloadDTO.setTitle(StrUtil.format("{}的报告需要跟进", StrUtil.blankToDefault(adviceDTO.getMemberName(), "家庭成员")));
        payloadDTO.setContent(StrUtil.blankToDefault(adviceDTO.getSummary(), "检测到新的报告建议，请及时查看并跟进。"));
        payloadDTO.setNavigation(navigationDTO);
        payloadDTO.setRecommendedAction(recommendedActionDTO);
        return payloadDTO;
    }

    /**
     * 构建报告建议通知统一跳转参数。
     */
    private HealthFollowUpNavigationDTO buildReportNavigation(Long reportId, HealthReportAdviceItemDTO firstAdvice) {
        if (reportId == null) {
            return null;
        }
        HealthFollowUpTargetAnchorEnum anchorEnum = resolveReportAnchor(firstAdvice);
        HealthFollowUpNavigationDTO navigationDTO = new HealthFollowUpNavigationDTO();
        navigationDTO.setTargetPageCode(HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue());
        navigationDTO.setTargetPageName(HealthAppI18n.targetPageName(
            HealthFollowUpTargetPageEnum.HEALTH_REPORT_DETAIL.getValue()));
        navigationDTO.setTargetBizId(reportId);
        navigationDTO.setTargetBizType(HealthFollowUpTaskTypeEnum.REPORT_ADVICE.getValue());
        navigationDTO.setTargetTabCode("ADVICE");
        navigationDTO.setTargetAnchorCode(anchorEnum.getValue());
        navigationDTO.setTargetAnchorName(HealthAppI18n.targetAnchorName(anchorEnum.getValue()));
        return navigationDTO;
    }

    /**
     * 构建报告建议通知推荐动作。
     */
    private HealthFollowUpRecommendedActionDTO buildReportRecommendedAction(HealthReportAdviceDTO adviceDTO,
        HealthReportAdviceItemDTO firstAdvice, HealthFollowUpNavigationDTO navigationDTO) {
        HealthFollowUpRecommendedActionDTO actionDTO = new HealthFollowUpRecommendedActionDTO();
        actionDTO.setActionPriority(firstAdvice == null || firstAdvice.getPriority() == null ? 3 : firstAdvice.getPriority());
        fillRiskLevel(actionDTO, resolveReportRiskLevel(firstAdvice));
        actionDTO.setTitle(firstAdvice == null ? "优先查看本次报告建议" : firstAdvice.getTitle());
        actionDTO.setContent(firstAdvice == null || StrUtil.isBlank(firstAdvice.getContent())
            ? StrUtil.blankToDefault(adviceDTO.getSummary(), "建议先查看本次报告的异常指标与后续跟进建议。")
            : firstAdvice.getContent());
        actionDTO.setActionText(firstAdvice == null || StrUtil.isBlank(firstAdvice.getActionText())
            ? "查看报告建议"
            : firstAdvice.getActionText());
        actionDTO.setNavigation(navigationDTO);
        return actionDTO;
    }

    /**
     * 根据异常项构建建议列表。
     *
     * <p>当前一期采用“通用建议 + 重点指标专项建议”的双层模式：
     * 1. 每个异常项至少会生成一条通用建议
     * 2. 命中特定指标关键词时，再追加专项建议
     */
    private List<HealthReportAdviceItemDTO> buildAdviceItems(List<HealthReportItemEntity> abnormalItems,
        int activeMedicationPlanCount) {
        if (abnormalItems == null || abnormalItems.isEmpty()) {
            return Collections.singletonList(buildHealthyObservationAdvice());
        }

        List<HealthReportAdviceItemDTO> adviceItems = new ArrayList<>();
        int priority = 1;
        for (HealthReportItemEntity abnormalItem : abnormalItems) {
            adviceItems.add(buildGeneralAbnormalAdvice(abnormalItem, priority++));

            String itemName = StrUtil.blankToDefault(abnormalItem.getItemName(), "");
            if (containsAnyKeyword(itemName, "血糖", "葡萄糖", "GLU", "糖化血红蛋白")) {
                adviceItems.add(buildBloodSugarAdvice(abnormalItem, activeMedicationPlanCount, priority++));
            } else if (containsAnyKeyword(itemName, "胆固醇", "甘油三酯", "低密度脂蛋白", "高密度脂蛋白")) {
                adviceItems.add(buildBloodLipidAdvice(abnormalItem, priority++));
            } else if (containsAnyKeyword(itemName, "尿酸")) {
                adviceItems.add(buildUricAcidAdvice(abnormalItem, priority++));
            } else if (containsAnyKeyword(itemName, "谷丙转氨酶", "谷草转氨酶", "ALT", "AST", "转氨酶")) {
                adviceItems.add(buildLiverFunctionAdvice(abnormalItem, priority++));
            } else if (containsAnyKeyword(itemName, "白细胞", "中性粒细胞")) {
                adviceItems.add(buildBloodCellAdvice(abnormalItem, priority++));
            }
        }
        return adviceItems;
    }

    /**
     * 构建报告建议摘要。
     */
    private String buildAdviceSummary(List<HealthReportItemEntity> abnormalItems, int activeMedicationPlanCount) {
        if (abnormalItems == null || abnormalItems.isEmpty()) {
            return "当前报告暂未发现需要重点干预的异常项，建议保持规律作息并按周期继续体检。";
        }

        String abnormalItemNames = abnormalItems.stream()
            .limit(4)
            .map(HealthReportItemEntity::getItemName)
            .collect(Collectors.joining("、"));

        StringBuilder summaryBuilder = new StringBuilder("本次报告共发现")
            .append(abnormalItems.size())
            .append("项重点关注指标：")
            .append(abnormalItemNames);
        if (abnormalItems.size() > 4) {
            summaryBuilder.append("等");
        }
        summaryBuilder.append("。");
        if (activeMedicationPlanCount > 0) {
            summaryBuilder.append("该成员当前已有")
                .append(activeMedicationPlanCount)
                .append("条启用中的用药计划，可结合提醒执行情况持续跟踪。");
        } else {
            summaryBuilder.append("当前该成员暂无启用中的用药计划，可视情况补充复查或健康管理安排。");
        }
        return summaryBuilder.toString();
    }

    /**
     * 构建“无异常项”场景下的兜底观察建议。
     */
    private HealthReportAdviceItemDTO buildHealthyObservationAdvice() {
        HealthReportAdviceItemDTO adviceItemDTO = new HealthReportAdviceItemDTO();
        adviceItemDTO.setAdviceType("ROUTINE_OBSERVE");
        adviceItemDTO.setTitle("继续保持定期健康管理");
        adviceItemDTO.setContent("当前报告暂未发现需要重点提示的异常指标，建议继续保持规律作息、按周期复查，并持续维护家庭成员健康档案。");
        adviceItemDTO.setActionText("继续观察并按期体检");
        adviceItemDTO.setPriority(1);
        return adviceItemDTO;
    }

    /**
     * 为每个异常项生成一条通用建议。
     * 这条建议主要承担“说明异常来源 + 给出基本动作”的职责。
     */
    private HealthReportAdviceItemDTO buildGeneralAbnormalAdvice(HealthReportItemEntity abnormalItem, int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = createBaseAdviceItem(abnormalItem, priority);
        adviceItemDTO.setAdviceType("RECHECK");

        if (Objects.equals(abnormalItem.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.HIGH.getValue())) {
            adviceItemDTO.setTitle(StrUtil.format("{}偏高，建议复查确认", abnormalItem.getItemName()));
            adviceItemDTO.setContent(StrUtil.format("{}当前结果为{}，已超出参考范围，建议结合原始报告和近期状态持续观察，必要时尽快复查。",
                abnormalItem.getItemName(), buildDisplayResult(abnormalItem)));
            adviceItemDTO.setActionText("优先安排复查或线下咨询");
        } else if (Objects.equals(abnormalItem.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.LOW.getValue())) {
            adviceItemDTO.setTitle(StrUtil.format("{}偏低，建议关注变化", abnormalItem.getItemName()));
            adviceItemDTO.setContent(StrUtil.format("{}当前结果为{}，已低于参考范围，建议继续观察近期症状变化，并按需安排复查确认。",
                abnormalItem.getItemName(), buildDisplayResult(abnormalItem)));
            adviceItemDTO.setActionText("持续观察并考虑复查");
        } else {
            adviceItemDTO.setTitle(StrUtil.format("{}存在异常提示", abnormalItem.getItemName()));
            adviceItemDTO.setContent(StrUtil.format("{}当前结果为{}，建议结合完整报告内容和医生意见进一步确认。",
                abnormalItem.getItemName(), buildDisplayResult(abnormalItem)));
            adviceItemDTO.setActionText("查看原始报告并咨询医生");
        }
        return adviceItemDTO;
    }

    /**
     * 血糖类指标专项建议。
     * 这类指标与饮食控制、复查节奏和用药执行关系更强，因此额外补充联动提示。
     */
    private HealthReportAdviceItemDTO buildBloodSugarAdvice(HealthReportItemEntity abnormalItem, int activeMedicationPlanCount,
        int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = createBaseAdviceItem(abnormalItem, priority);
        adviceItemDTO.setAdviceType("MEDICATION_TRACK");
        adviceItemDTO.setTitle("关注血糖管理与提醒执行");
        adviceItemDTO.setContent("血糖相关指标出现异常时，建议同步关注近期饮食、作息以及复查节奏。如果成员正在执行用药提醒，可结合提醒完成情况持续跟踪。");
        adviceItemDTO.setActionText(activeMedicationPlanCount > 0 ? "查看现有用药提醒并持续跟踪" : "可补充控糖相关提醒或复查安排");
        return adviceItemDTO;
    }

    /**
     * 血脂类指标专项建议。
     */
    private HealthReportAdviceItemDTO buildBloodLipidAdvice(HealthReportItemEntity abnormalItem, int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = createBaseAdviceItem(abnormalItem, priority);
        adviceItemDTO.setAdviceType("LIFESTYLE_OBSERVE");
        adviceItemDTO.setTitle("关注血脂变化与生活方式管理");
        adviceItemDTO.setContent("血脂类指标异常通常需要结合近期饮食、体重、运动情况持续观察，建议保留本次报告并在下次复查时对比变化。");
        adviceItemDTO.setActionText("记录饮食运动情况并安排后续复查");
        return adviceItemDTO;
    }

    /**
     * 尿酸类指标专项建议。
     */
    private HealthReportAdviceItemDTO buildUricAcidAdvice(HealthReportItemEntity abnormalItem, int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = createBaseAdviceItem(abnormalItem, priority);
        adviceItemDTO.setAdviceType("LIFESTYLE_OBSERVE");
        adviceItemDTO.setTitle("关注尿酸相关饮食与饮水管理");
        adviceItemDTO.setContent("尿酸相关指标异常时，建议结合近期饮食、饮水和复查结果持续观察，必要时进一步做专项随访。");
        adviceItemDTO.setActionText("持续记录并按需安排复查");
        return adviceItemDTO;
    }

    /**
     * 肝功能指标专项建议。
     */
    private HealthReportAdviceItemDTO buildLiverFunctionAdvice(HealthReportItemEntity abnormalItem, int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = createBaseAdviceItem(abnormalItem, priority);
        adviceItemDTO.setAdviceType("FOLLOW_UP");
        adviceItemDTO.setTitle("关注肝功能指标并尽快复核");
        adviceItemDTO.setContent("转氨酶等肝功能指标异常时，建议尽快结合近期用药、饮酒、作息情况做进一步复核，并保存后续报告便于对比。");
        adviceItemDTO.setActionText("优先安排肝功能复核");
        return adviceItemDTO;
    }

    /**
     * 血细胞指标专项建议。
     */
    private HealthReportAdviceItemDTO buildBloodCellAdvice(HealthReportItemEntity abnormalItem, int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = createBaseAdviceItem(abnormalItem, priority);
        adviceItemDTO.setAdviceType("FOLLOW_UP");
        adviceItemDTO.setTitle("关注血常规异常并结合症状复查");
        adviceItemDTO.setContent("白细胞或中性粒细胞异常时，建议结合近期是否存在感染、炎症或不适症状持续观察，并按需安排血常规复查。");
        adviceItemDTO.setActionText("结合症状变化决定复查时点");
        return adviceItemDTO;
    }

    /**
     * 创建建议对象的公共基础字段。
     */
    private HealthReportAdviceItemDTO createBaseAdviceItem(HealthReportItemEntity abnormalItem, int priority) {
        HealthReportAdviceItemDTO adviceItemDTO = new HealthReportAdviceItemDTO();
        adviceItemDTO.setSourceItemId(abnormalItem.getItemId());
        adviceItemDTO.setSourceItemName(abnormalItem.getItemName());
        adviceItemDTO.setSourceResultValue(buildDisplayResult(abnormalItem));
        adviceItemDTO.setAbnormalFlag(abnormalItem.getAbnormalFlag());
        adviceItemDTO.setAbnormalFlagName(resolveAbnormalFlagName(abnormalItem.getAbnormalFlag()));
        adviceItemDTO.setPriority(priority);
        return adviceItemDTO;
    }

    /**
     * 拼接指标结果展示值。
     * 这样建议内容里既能看到结果值，也能保留单位信息。
     */
    private String buildDisplayResult(HealthReportItemEntity itemEntity) {
        if (itemEntity == null) {
            return null;
        }
        return StrUtil.blankToDefault(itemEntity.getResultValue(), "")
            + (StrUtil.isBlank(itemEntity.getResultUnit()) ? "" : " " + itemEntity.getResultUnit());
    }

    /**
     * 根据异常标记值回填描述名称。
     */
    private String resolveAbnormalFlagName(Integer abnormalFlagValue) {
        if (abnormalFlagValue == null) {
            return null;
        }
        for (HealthReportItemAbnormalFlagEnum abnormalFlagEnum : HealthReportItemAbnormalFlagEnum.values()) {
            if (abnormalFlagEnum.getValue().equals(abnormalFlagValue)) {
                return HealthAppI18n.reportAbnormalFlagName(abnormalFlagEnum.getValue());
            }
        }
        return null;
    }

    /**
     * 解析报告建议通知默认落点锚点。
     */
    private HealthFollowUpTargetAnchorEnum resolveReportAnchor(HealthReportAdviceItemDTO adviceItemDTO) {
        if (adviceItemDTO == null) {
            return HealthFollowUpTargetAnchorEnum.REPORT_ADVICE;
        }
        if (StrUtil.containsAnyIgnoreCase(StrUtil.blankToDefault(adviceItemDTO.getActionText(), ""), "原始报告", "完整报告")) {
            return HealthFollowUpTargetAnchorEnum.REPORT_ORIGINAL_FILE;
        }
        if (Objects.equals(adviceItemDTO.getAdviceType(), "RECHECK")
            || Objects.equals(adviceItemDTO.getAdviceType(), "FOLLOW_UP")) {
            return HealthFollowUpTargetAnchorEnum.REPORT_ABNORMAL_ITEMS;
        }
        return HealthFollowUpTargetAnchorEnum.REPORT_ADVICE;
    }

    /**
     * 解析报告建议推荐动作风险等级。
     */
    private HealthFollowUpRiskLevelEnum resolveReportRiskLevel(HealthReportAdviceItemDTO adviceItemDTO) {
        if (adviceItemDTO == null || adviceItemDTO.getAbnormalFlag() == null) {
            return HealthFollowUpRiskLevelEnum.MEDIUM;
        }
        if (Objects.equals(adviceItemDTO.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.HIGH.getValue())
            || Objects.equals(adviceItemDTO.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue())) {
            return HealthFollowUpRiskLevelEnum.HIGH;
        }
        if (Objects.equals(adviceItemDTO.getAbnormalFlag(), HealthReportItemAbnormalFlagEnum.LOW.getValue())) {
            return HealthFollowUpRiskLevelEnum.MEDIUM;
        }
        return HealthFollowUpRiskLevelEnum.LOW;
    }

    /**
     * 回填推荐动作风险等级字段。
     */
    private void fillRiskLevel(HealthFollowUpRecommendedActionDTO actionDTO, HealthFollowUpRiskLevelEnum riskLevelEnum) {
        if (actionDTO == null || riskLevelEnum == null) {
            return;
        }
        actionDTO.setRiskLevel(riskLevelEnum.getValue());
        actionDTO.setRiskLevelName(HealthAppI18n.followUpRiskLevelName(riskLevelEnum.getValue()));
        actionDTO.setRiskCssTag(riskLevelEnum.cssTag());
    }

    /**
     * 归一化导出命令。
     *
     * <p>这里把空命令和空字段统一替换成可工作的默认值，
     * 避免导出接口因为某个勾选项没传而在服务端出现大量判空分支。
     */
    private HealthReportExportDataCommand normalizeExportCommand(HealthReportExportDataCommand command) {
        HealthReportExportDataCommand safeCommand = command == null ? new HealthReportExportDataCommand() : command;
        if (StrUtil.isBlank(safeCommand.getFormat())) {
            safeCommand.setFormat("summaryCard");
        } else {
            safeCommand.setFormat(safeCommand.getFormat().trim());
        }
        safeCommand.setHidePersonalInfo(defaultTrue(safeCommand.getHidePersonalInfo()));
        safeCommand.setIncludeAnalysisSummary(defaultTrue(safeCommand.getIncludeAnalysisSummary()));
        safeCommand.setIncludeResultInterpretation(defaultFalse(safeCommand.getIncludeResultInterpretation()));
        safeCommand.setIncludeAiSummary(defaultFalse(safeCommand.getIncludeAiSummary()));
        safeCommand.setIncludeAbnormalItems(defaultTrue(safeCommand.getIncludeAbnormalItems()));
        safeCommand.setIncludeAdvice(defaultTrue(safeCommand.getIncludeAdvice()));
        safeCommand.setIncludeIndicatorItems(defaultFalse(safeCommand.getIncludeIndicatorItems()));
        safeCommand.setIncludeSupplementalInfo(defaultFalse(safeCommand.getIncludeSupplementalInfo()));
        return safeCommand;
    }

    /**
     * 收集导出口径下的异常指标。
     *
     * <p>当前和 App 端保持一致，只把“偏低 / 偏高 / 异常”三类算作重点异常。
     */
    private List<HealthReportItemDTO> collectShareAbnormalItems(List<HealthReportItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
            .filter(item -> isShareAbnormalFlag(item == null ? null : item.getAbnormalFlag()))
            .collect(Collectors.toList());
    }

    /**
     * 统计导出口径下的待判断指标数。
     */
    private int countSharePendingItems(List<HealthReportItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return (int) items.stream()
            .filter(item -> isSharePendingFlag(item == null ? null : item.getAbnormalFlag()))
            .count();
    }

    /**
     * 选择导出时需要保留的完整指标列表。
     *
     * <p>只有“详细长图 + 勾选完整指标”这两个条件都满足时，才会真正输出完整列表。
     */
    private List<HealthReportItemDTO> selectIndicatorItems(List<HealthReportItemDTO> items, String format, Boolean includeIndicatorItems) {
        if (!Boolean.TRUE.equals(includeIndicatorItems) || !"detailImage".equals(format) || items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(items);
    }

    /**
     * 选择导出时需要保留的异常指标列表。
     *
     * <p>摘要卡片只保留前三条，详细长图和文本摘要则保留全部。
     */
    private List<HealthReportItemDTO> selectAbnormalItems(List<HealthReportItemDTO> abnormalItems, String format,
        Boolean includeAbnormalItems) {
        if (!Boolean.TRUE.equals(includeAbnormalItems) || abnormalItems == null || abnormalItems.isEmpty()) {
            return Collections.emptyList();
        }
        if ("summaryCard".equals(format)) {
            return new ArrayList<>(abnormalItems.subList(0, Math.min(3, abnormalItems.size())));
        }
        return new ArrayList<>(abnormalItems);
    }

    /**
     * 选择导出时需要保留的建议列表。
     */
    private List<HealthReportAdviceItemDTO> selectAdviceItems(HealthReportAdviceDTO advice, String format, Boolean includeAdvice) {
        if (!Boolean.TRUE.equals(includeAdvice) || advice == null || advice.getAdviceItems() == null
            || advice.getAdviceItems().isEmpty()) {
            return Collections.emptyList();
        }
        if ("summaryCard".equals(format)) {
            return new ArrayList<>(advice.getAdviceItems().subList(0, Math.min(2, advice.getAdviceItems().size())));
        }
        return new ArrayList<>(advice.getAdviceItems());
    }

    /**
     * 选择导出时需要保留的健康问题列表。
     *
     * <p>补充信息在摘要卡片里不展开，因此只在“非摘要卡片 + 勾选补充信息”时下发。
     */
    private List<HealthProblemDTO> selectHealthProblems(List<HealthProblemDTO> healthProblems, String format,
        Boolean includeSupplementalInfo) {
        if (!Boolean.TRUE.equals(includeSupplementalInfo) || "summaryCard".equals(format)
            || healthProblems == null || healthProblems.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(healthProblems);
    }

    /**
     * 构建导出文本摘要。
     */
    private String buildReportShareText(HealthReportDTO detail, HealthReportExportDataCommand command, String analysisSummary,
        String resultInterpretation, String aiSummary, String adviceSummary, int totalIndicatorCount, int abnormalCount,
        int pendingIndicatorCount, List<HealthReportItemDTO> abnormalItems, List<HealthReportAdviceItemDTO> adviceItems,
        List<HealthProblemDTO> healthProblems) {
        StringBuffer buffer = new StringBuffer().append("【检查报告摘要】\n");

        buffer.append("报告：").append(buildDisplayReportName(detail)).append('\n');
        if (!Boolean.TRUE.equals(command.getHidePersonalInfo())) {
            buffer.append("家庭成员：").append(buildDisplayMemberLabel(detail)).append('\n');
        }
        buffer.append("日期：").append(formatReportDate(detail.getReportDate())).append('\n');
        String reportTypeText = buildDisplayReportTypeText(detail);
        if (!"-".equals(reportTypeText)) {
            buffer.append("类型：").append(reportTypeText).append('\n');
        }
        if (Boolean.TRUE.equals(command.getIncludeSupplementalInfo()) && StrUtil.isNotBlank(detail.getHospitalName())) {
            buffer.append("医院：").append(detail.getHospitalName().trim()).append('\n');
        }
        buffer.append("总指标：").append(totalIndicatorCount).append(" 项\n");
        buffer.append("异常指标：").append(abnormalCount).append(" 项\n");
        buffer.append("待判断指标：").append(pendingIndicatorCount).append(" 项\n\n");

        if (StrUtil.isNotBlank(analysisSummary)) {
            buffer.append("分析摘要：\n").append(analysisSummary).append("\n\n");
        }
        if (StrUtil.isNotBlank(resultInterpretation)) {
            buffer.append("结果解读：\n").append(resultInterpretation).append("\n\n");
        }
        if (StrUtil.isNotBlank(aiSummary)) {
            buffer.append("AI 总结：\n").append(aiSummary).append("\n\n");
        }
        if (abnormalItems != null && !abnormalItems.isEmpty()) {
            buffer.append("重点结果：\n");
            for (int index = 0; index < abnormalItems.size(); index++) {
                HealthReportItemDTO item = abnormalItems.get(index);
                buffer.append(index + 1)
                    .append(". ")
                    .append(item.getItemName())
                    .append('：')
                    .append(buildItemResultText(item))
                    .append("（")
                    .append(StrUtil.blankToDefault(item.getAbnormalFlagName(), "-"))
                    .append("）\n");
            }
            buffer.append('\n');
        }
        if (adviceItems != null && !adviceItems.isEmpty()) {
            buffer.append("建议：\n");
            for (int index = 0; index < adviceItems.size(); index++) {
                HealthReportAdviceItemDTO item = adviceItems.get(index);
                buffer.append(index + 1)
                    .append(". ")
                    .append(StrUtil.blankToDefault(item.getTitle(), ""))
                    .append('：')
                    .append(StrUtil.blankToDefault(item.getContent(), ""))
                    .append('\n');
            }
            buffer.append('\n');
        } else if (StrUtil.isNotBlank(adviceSummary)) {
            buffer.append("建议：\n").append(adviceSummary).append("\n\n");
        }
        if (healthProblems != null && !healthProblems.isEmpty()) {
            buffer.append("重点问题：")
                .append(healthProblems.stream()
                    .map(HealthProblemDTO::getProblemName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.joining("、")))
                .append("\n\n");
        }
        buffer.append("仅供健康管理参考");
        return buffer.toString().trim();
    }

    /**
     * 生成导出文件名。
     */
    private String buildReportShareFileName(HealthReportDTO detail, String format) {
        String safeDate = formatReportDate(detail.getReportDate()).replace("-", "");
        String suffix;
        switch (format) {
            case "detailImage":
                suffix = "detail";
                break;
            case "textSummary":
                suffix = "text";
                break;
            case "summaryCard":
            default:
                suffix = "summary";
                break;
        }
        return "healthtrail_report_" + detail.getReportId() + "_" + safeDate + "_" + suffix + ".png";
    }

    /**
     * 统一拼接报告展示标题。
     */
    private String buildDisplayReportName(HealthReportDTO detail) {
        String normalizedName = StrUtil.blankToDefault(normalizeText(detail.getReportName()), "检查报告");
        String suffix = buildRecognizedReportTypeSuffix(detail);
        return suffix == null ? normalizedName : normalizedName + "（" + suffix + "）";
    }

    /**
     * 统一拼接报告类型展示文案。
     */
    private String buildDisplayReportTypeText(HealthReportDTO detail) {
        String manualType = normalizeText(detail.getReportType());
        String recognizedType = normalizeText(detail.getRecognizedReportType());
        if (manualType == null && recognizedType == null) {
            return "-";
        }
        if (manualType == null) {
            return recognizedType;
        }
        if (recognizedType == null || Objects.equals(manualType, recognizedType)) {
            return manualType;
        }
        return manualType + "（识别：" + recognizedType + "）";
    }

    /**
     * 统一拼接成员展示文案。
     */
    private String buildDisplayMemberLabel(HealthReportDTO detail) {
        String memberName = normalizeText(detail.getMemberName());
        if (memberName != null) {
            return memberName;
        }
        String memberCode = normalizeText(detail.getMemberCode());
        if (memberCode != null) {
            return memberCode;
        }
        return "MBR" + detail.getMemberId();
    }

    /**
     * 识别类型是否需要作为标题后缀补充。
     */
    private String buildRecognizedReportTypeSuffix(HealthReportDTO detail) {
        String recognizedType = normalizeText(detail.getRecognizedReportType());
        if (recognizedType == null) {
            return null;
        }
        String manualType = normalizeText(detail.getReportType());
        if (Objects.equals(manualType, recognizedType)) {
            return null;
        }
        String reportName = StrUtil.blankToDefault(normalizeText(detail.getReportName()), "");
        if (reportName.contains(recognizedType)) {
            return null;
        }
        return recognizedType;
    }

    /**
     * 统一格式化报告日期。
     */
    private String formatReportDate(Date reportDate) {
        return reportDate == null ? "-" : DateUtil.formatDate(reportDate);
    }

    /**
     * 拼接指标结果展示值。
     */
    private String buildItemResultText(HealthReportItemDTO item) {
        if (item == null) {
            return "-";
        }
        return (StrUtil.blankToDefault(item.getResultValue(), "")
            + (StrUtil.isBlank(item.getResultUnit()) ? "" : " " + item.getResultUnit())).trim();
    }

    /**
     * 分享口径下是否属于异常指标。
     */
    private boolean isShareAbnormalFlag(Integer abnormalFlag) {
        return Objects.equals(abnormalFlag, 2) || Objects.equals(abnormalFlag, 3) || Objects.equals(abnormalFlag, 4);
    }

    /**
     * 分享口径下是否属于待判断指标。
     */
    private boolean isSharePendingFlag(Integer abnormalFlag) {
        return !Objects.equals(abnormalFlag, 1)
            && !Objects.equals(abnormalFlag, 2)
            && !Objects.equals(abnormalFlag, 3)
            && !Objects.equals(abnormalFlag, 4);
    }

    /**
     * 把空白字符串统一归一成 null。
     */
    private String normalizeText(String text) {
        String normalizedText = text == null ? null : text.trim();
        return StrUtil.isBlank(normalizedText) ? null : normalizedText;
    }

    private Boolean defaultTrue(Boolean value) {
        return value == null ? Boolean.TRUE : value;
    }

    private Boolean defaultFalse(Boolean value) {
        return value == null ? Boolean.FALSE : value;
    }

    /**
     * 统一解析成员名称。
     */
    private String resolveMemberName(Long memberId) {
        if (memberId == null) {
            return null;
        }
        HealthFamilyMemberEntity memberEntity = familyMemberService.getById(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 用已预热的成员快照解析成员名称。
     *
     * <p>如果调用方已经提前批量查过成员表，优先直接复用内存快照；
     * 只有调用方没有传快照时，才退回到单条查询版本。
     */
    private String resolveMemberName(Long memberId, Map<Long, HealthFamilyMemberEntity> memberMap) {
        if (memberId == null) {
            return null;
        }
        if (memberMap == null || memberMap.isEmpty()) {
            return resolveMemberName(memberId);
        }
        HealthFamilyMemberEntity memberEntity = memberMap.get(memberId);
        return memberEntity == null ? HealthAppI18n.deletedMemberName() : memberEntity.getMemberName();
    }

    /**
     * 批量加载成员快照。
     *
     * <p>报告分页列表、批量建议构建这类场景都会反复用到成员展示信息，
     * 统一收口到这里，避免不同入口各自再写一套批量查成员逻辑。
     */
    private Map<Long, HealthFamilyMemberEntity> loadMemberMap(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return familyMemberService.lambdaQuery()
            .in(HealthFamilyMemberEntity::getMemberId, memberIds)
            .list()
            .stream()
            .collect(Collectors.toMap(HealthFamilyMemberEntity::getMemberId, memberEntity -> memberEntity,
                (left, right) -> left));
    }

    /**
     * 已完成权限校验的报告访问上下文。
     */
    private static final class AuthorizedReportContext {

        private final HealthReportModel reportModel;

        private final FamilyMemberAccessContextDTO accessContext;

        private final Map<Long, HealthFamilyMemberEntity> memberMap;

        private AuthorizedReportContext(HealthReportModel reportModel, FamilyMemberAccessContextDTO accessContext,
            Map<Long, HealthFamilyMemberEntity> memberMap) {
            this.reportModel = reportModel;
            this.accessContext = accessContext;
            this.memberMap = memberMap;
        }
    }

    /**
     * 报告导出链路的预热数据上下文。
     *
     * <p>导出会同时用到详情、指标、分析、建议、健康问题五类数据，
     * 这里显式收口成一个上下文对象，方便后续继续扩展字段时仍然保持“单次预热、统一复用”。
     */
    private static final class ReportExportRuntimeContext {

        private final HealthReportDTO reportDetail;

        private final List<HealthReportItemDTO> reportItems;

        private final HealthReportAnalysisDTO analysis;

        private final HealthReportAdviceDTO advice;

        private final List<HealthProblemDTO> healthProblems;

        private ReportExportRuntimeContext(HealthReportDTO reportDetail, List<HealthReportItemDTO> reportItems,
            HealthReportAnalysisDTO analysis, HealthReportAdviceDTO advice, List<HealthProblemDTO> healthProblems) {
            this.reportDetail = reportDetail;
            this.reportItems = reportItems;
            this.analysis = analysis;
            this.advice = advice;
            this.healthProblems = healthProblems;
        }
    }

    /**
     * 判断指标名称是否命中任一关键字。
     * 当前阶段使用轻量关键词规则，后续可以平滑升级为标准化指标映射。
     */
    private boolean containsAnyKeyword(String text, String... keywords) {
        if (StrUtil.isBlank(text) || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (StrUtil.containsIgnoreCase(text, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对摘要文本做长度保护。
     */
    private String limitLength(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}

package com.healthtrail.domain.health.report.parser;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.codec.Base64;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.utils.file.FileUploadUtils;
import com.healthtrail.domain.health.report.config.HealthReportAiStructuredParseProperties;
import com.healthtrail.domain.health.report.db.HealthReportEntity;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * 检查报告文件解析器。
 *
 * <p>当前实现先覆盖可稳定落地的服务端解析能力：
 * 1. 文本型 PDF 使用 PDFBox 抽取文字
 * 2. txt/html 等文本文件直接读取
 * 3. 图片报告不再走 OCR，直接把原图交给 AI 视觉结构化解析
 * 4. 从可抽取文字中按常见“指标 结果 单位 参考范围”格式抽取结构化指标
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportFileParser {

    private static final Pattern ITEM_LINE_PATTERN = Pattern.compile(
        "^\\s*([\\u4e00-\\u9fa5A-Za-z][\\u4e00-\\u9fa5A-Za-z0-9%/()（）\\- ]{1,40})\\s+([↑↓+-]?\\d+(?:\\.\\d+)?|阴性|阳性|正常|异常|未见异常)\\s*([A-Za-zμµ%/\\^0-9\\.\\-]*)\\s*(?:参考范围|参考值|范围|ref)?[:：]?\\s*([<>≤≥]?[\\d\\.]+\\s*[-~～至]?\\s*[\\d\\.]*.*)?$");

    /**
     * 用于识别“明显不是报告正文，而是程序日志 / SQL 调试输出”的行。
     *
     * <p>最近联调里发现，某些文件解析链路在异常场景下会把控制台日志、线程名、SQL 调试信息
     * 混进传给大模型的文本里，导致模型把这些噪声当成报告正文，进而完全偏离提取目标。
     *
     * <p>这里单独把常见噪声模式收敛成正则，目的不是“美化抽取文本”，而是：
     * 1. 阻止 requestId / DEBUG / WARN / SQL Parameters 之类内容进入大模型；
     * 2. 避免私有 MinIO URL、线程名、数据库语句被带到外部模型侧；
     * 3. 尽量只清理高置信度噪声，减少误删真实报告内容的风险。
     */
    private static final Pattern NON_REPORT_LOG_LINE_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}[,\\.]\\d{3}.*"
            + "|.*\\brequestId\\s*:\\s*.*"
            + "|.*\\b(?:DEBUG|INFO|WARN|ERROR|TRACE)\\b.*"
            + "|.*(?:==>\\s*Preparing:|==>\\s*Parameters:|<==\\s*Updates:|<==\\s*Total:).*"
            + "|.*\\bpool-\\d+-thread-\\d+\\b.*)$",
        Pattern.CASE_INSENSITIVE);

    /**
     * 统一识别文本中的 URL。
     *
     * <p>这里会在送大模型前把 URL 去掉，尤其是私有 MinIO / OSS 地址。
     * 即使这些链接本身只是异常日志带出来的，也不应该继续往外部模型传。
     */
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    /**
     * 优先匹配带“报告日期 / 检查日期 / 检验日期”等语义标签的日期字段。
     *
     * <p>先走这一层，是为了尽量避免把出生日期、打印日期、预约日期误判成报告日期。
     */
    private static final Pattern LABELED_REPORT_DATE_PATTERN = Pattern.compile(
        "(?:报告日期|检查日期|检验日期|送检日期|采样日期|标本日期|检测日期|体检日期|出具日期)\\s*[:：]?\\s*"
            + "((?:19|20)\\d{2}[年\\-./](?:0?[1-9]|1[0-2])[月\\-./](?:0?[1-9]|[12]\\d|3[01])日?)");
    /**
     * 当没有命中日期标签时，再从全文抓完整日期做兜底。
     *
     * <p>这里仍然限制为完整年月日，避免把化验值、编号片段误识别成日期。
     */
    private static final Pattern GENERIC_FULL_DATE_PATTERN = Pattern.compile(
        "((?:19|20)\\d{2}[年\\-./](?:0?[1-9]|1[0-2])[月\\-./](?:0?[1-9]|[12]\\d|3[01])日?)");

    /** 标准指标解析器 */
    private final HealthReportStandardIndicatorResolver standardIndicatorResolver;

    /** AI结构化解析器 */
    private final HealthReportAiStructuredParser aiStructuredParser;

    /** AI结构化解析配置属性 */
    private final HealthReportAiStructuredParseProperties aiStructuredParseProperties;

    public ParsedReportResult parse(HealthReportEntity reportEntity) {
        File tempFile = null;
        try {
            tempFile = materializeReportFile(reportEntity);
            String normalizedExtension = normalizeExtension(tempFile, reportEntity.getFileExtension());
            byte[] fileBytes = FileUtil.readBytes(tempFile);
            String text = extractText(tempFile, normalizedExtension);
            ParsedReportResult result = new ParsedReportResult();
            result.setExtractedText(StrUtil.blankToDefault(text, ""));
            // 在结构化指标抽取前，先尝试从正文里恢复报告日期候选值。
            // 这样即使后面的 AI 没返回 reportDate，纯文本附件也仍然可以补齐缺失日期。
            result.setReportDate(extractReportDate(text));
            // 这里不再把“规则解析”当成主路径直接先写入结果，
            // 而是先交给统一的“模型优先 + 本地规则兜底”策略决定最终来源。
            //
            // 调整原因：
            // 1. 当前业务已经明确要求图片/拍照单据直接优先走大模型，不再保留 OCR 前置能力；
            // 2. 如果还像以前一样先把规则结果塞进去，再让 AI 只做“补强”，
            //    实际效果仍然会偏向旧链路，和产品期望不一致；
            // 3. 统一在一个入口里做“主路径选择”，后续继续扩展 PDF 视觉解析时也更好维护。
            result.setItems(new ArrayList<>());
            applyPrimaryParsingStrategy(result, text, normalizedExtension, fileBytes);
            enhanceItemsByAiIfNecessary(result, normalizedExtension, fileBytes);
            result.setMessage(result.hasItems()
                ? StrUtil.format("已从报告中解析出 {} 项指标。", result.getItems().size())
                : "报告文件已完成解析，但暂未识别到可结构化的指标，请人工补录或重试。");
            return result;
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("删除报告解析临时文件失败: {}", tempFile.getAbsolutePath());
            }
        }
    }

    /**
     * 把报告文件统一落成本地临时文件后再解析。
     *
     * <p>这样无论底层来自：
     * 1. 本地 /profile/** 静态资源；
     * 2. MinIO object key；
     * 3. 已补全过的公开访问 URL；
     * 后续 PDFBox 和 AI 视觉解析都可以继续复用本地临时文件，不需要重写整条解析链路。
     */
    private File materializeReportFile(HealthReportEntity reportEntity) {
        String fileReference = reportEntity == null ? null : reportEntity.getFileUrl();
        if (StrUtil.isBlank(fileReference)) {
            throw new IllegalStateException("报告文件地址为空，无法解析");
        }
        try {
            byte[] fileBytes = FileUploadUtils.readBytes(fileReference);
            String extension = StrUtil.blankToDefault(reportEntity.getFileExtension(), "tmp").toLowerCase(Locale.ROOT);
            File tempFile = File.createTempFile("health-report-", "." + extension);
            FileUtil.writeBytes(fileBytes, tempFile);
            return tempFile;
        } catch (ApiException ex) {
            throw new IllegalStateException("报告文件读取失败：" + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new IllegalStateException("报告临时文件写入失败：" + ex.getMessage(), ex);
        }
    }

    private String extractText(File localFile, String extension) {
        if (localFile == null || !localFile.exists()) {
            throw new IllegalStateException("报告文件不存在，无法解析");
        }
        String normalizedExt = normalizeExtension(localFile, extension);
        try {
            if ("pdf".equals(normalizedExt)) {
                // PDF 先走 PDFBox 的轻量文本抽取，命中可复制文本时速度最快、稳定性也最好。
                //
                // 如果抽不到文本，通常说明这是一份扫描版 PDF 或图片型 PDF。
                // 按当前“去掉 OCR 功能”的口径，这里不再调用任何云 OCR 或本地命令行 OCR；
                // 后续如需支持扫描 PDF，应优先补“PDF 转图片 + AI 视觉结构化”，而不是重新引入 OCR 客户端。
                return StrUtil.blankToDefault(extractPdfText(localFile), "");
            }
            if (StrUtil.containsAny(normalizedExt, "txt", "html", "htm", "csv")) {
                return FileUtil.readString(localFile, StandardCharsets.UTF_8);
            }
            if (StrUtil.containsAny(normalizedExt, "jpg", "jpeg", "png", "bmp")) {
                // 图片报告不再做文字识别前置处理。
                //
                // 这里返回空文本是刻意设计：
                // 1. 原图字节仍会在后续 `tryParseByVision` 中直接交给 AI 视觉结构化解析；
                // 2. 服务端不再加载云 OCR SDK，也不再执行本地 OCR 命令，避免部署环境额外依赖；
                // 3. 如果 AI 视觉未配置，图片报告会保持“无结构化指标”的可解释状态，由前端引导人工补录。
                return "";
            }
        } catch (Exception ex) {
            throw new IllegalStateException("报告文件解析失败：" + ex.getMessage(), ex);
        }
        throw new IllegalStateException("暂不支持该报告文件类型解析：" + normalizedExt);
    }

    private String extractPdfText(File localFile) throws Exception {
        try (PDDocument document = PDDocument.load(localFile)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private List<ParsedReportItem> parseItems(String text) {
        List<ParsedReportItem> items = new ArrayList<>();
        if (StrUtil.isBlank(text)) {
            return items;
        }
        String[] lines = text.replace("\r", "\n").split("\n+");
        int sort = 1;
        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);
            Matcher matcher = ITEM_LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            ParsedReportItem item = new ParsedReportItem();
            item.setItemName(limit(matcher.group(1).trim(), 100));
            item.setResultValue(limit(matcher.group(2).trim(), 100));
            item.setResultUnit(limit(StrUtil.blankToDefault(matcher.group(3), "").trim(), 50));
            item.setReferenceText(limit(StrUtil.blankToDefault(matcher.group(4), "").trim(), 100));
            item.setStandardItemCode(standardIndicatorResolver.resolve(null, item.getItemName()));
            fillReferenceRange(item);
            item.setSort(sort++);
            item.setRemark("后台解析生成，建议用户确认。");
            items.add(item);
        }
        return items;
    }

    /**
     * 先根据文件类型决定“主解析路径”。
     *
     * <p>当前策略刻意做成“图片优先大模型、文本优先规则”：
     * 1. 图片报告：直接走视觉结构化，让模型看到真实版面；
     * 2. 文本类报告：先吃已有文本，再用规则快速兜一轮；
     * 3. 两条路都不互相阻塞，最终再交给 `enhanceItemsByAiIfNecessary` 做二次补强。
     */
    private void applyPrimaryParsingStrategy(ParsedReportResult result, String text, String extension, byte[] fileBytes) {
        if (result == null) {
            return;
        }
        if (isImageExtension(extension)) {
            String sanitizedAiContextText = sanitizeTextForAiPrompt(text);
            ParsedReportResult visionResult = tryParseByVision(sanitizedAiContextText, extension, fileBytes);
            if (visionResult != null && visionResult.hasItems()) {
                log.info("报告图片主解析路径命中 AI 视觉结构化，识别条数：{}", visionResult.getItems().size());
                result.setItems(visionResult.getItems());
                result.setReportDate(chooseBetterReportDate(result.getReportDate(), visionResult.getReportDate()));
                result.setMessage(StrUtil.blankToDefault(visionResult.getMessage(), "AI 视觉结构化解析已完成。"));
                return;
            }
            log.info("报告图片主解析路径未产出结构化指标，转入后续补强流程，文件类型：{}", extension);
            return;
        }

        List<ParsedReportItem> ruleItems = parseItems(text);
        if (!ruleItems.isEmpty()) {
            log.info("报告文本主解析路径命中规则结构化，识别条数：{}", ruleItems.size());
            result.setItems(ruleItems);
            result.setMessage(StrUtil.format("规则结构化解析已识别出 {} 项指标。", ruleItems.size()));
            return;
        }
        log.info("报告文本主解析路径未命中规则结构化，转入后续 AI 补强流程，文件类型：{}", extension);
    }

    /**
     * 当主解析条数过少时，尝试用 AI 做一次结构化增强。
     *
     * <p>当前触发顺序做了重新收口：
     * 1. 图片已经先尝试过一次视觉主解析，这里只在结果仍不足时补强；
     * 2. 文本类文件先跑规则，规则条数不足时再尝试 AI 文本结构化；
     * 3. 整体目标是让“大模型直解”成为主角，同时保留本地规则的稳定兜底。
     *
     * <p>命中后不会盲目覆盖，而是只在 AI 结果条数更多时才替换，
     * 这样可以尽量减少大模型偶发幻觉对当前稳定规则的影响。
     */
    private void enhanceItemsByAiIfNecessary(ParsedReportResult result, String extension, byte[] fileBytes) {
        if (result == null) {
            return;
        }
        // 这里专门为“大模型输入”准备一份净化后的文本副本。
        //
        // 为什么不直接覆盖 `result.extractedText`：
        // 1. 原始抽取文本仍然可能需要展示给前端做人工核对；
        // 2. 排查 PDF 文本层或文本文件解析问题时，保留原文更有帮助；
        // 3. 我们当前真正要解决的是“不要把日志 / SQL / 私有 URL 发给模型”，
        //    所以只在 AI 调用边界做清洗，影响面最小也最可控。
        String sanitizedAiContextText = sanitizeTextForAiPrompt(result.getExtractedText());

        // 这里不因为 extractedText 为空而提前 return，
        // 因为当前已经支持“图片本身直接走大模型视觉结构化”。
        // 也就是说：
        // 1. 图片报告不再做 OCR -> extractedText 可能为空
        // 2. 但只要大模型可用、且文件是图片，仍然可以继续解析
        // 只有在视觉解析也不可用时，下面的文本增强才会自然跳过。
        int currentItemCount = result.getItems() == null ? 0 : result.getItems().size();

        // 如果前面的主解析已经是图片视觉链路，就不在这里重复再打一遍同样的模型请求；
        // 否则一次上传会对同一张图片发起两次完全一致的视觉解析，既浪费 token，也会让日志更乱。
        if (!isImageExtension(extension)) {
            ParsedReportResult visionResult = tryParseByVision(sanitizedAiContextText, extension, fileBytes);
            if (shouldUseAiResult(visionResult, currentItemCount, true)) {
                result.setItems(visionResult.getItems());
                result.setReportDate(chooseBetterReportDate(result.getReportDate(), visionResult.getReportDate()));
                result.setMessage(StrUtil.blankToDefault(visionResult.getMessage(), "AI 视觉结构化解析已接管本次结果。"));
                return;
            }
        }

        if (currentItemCount >= aiStructuredParseProperties.getMinRuleItemCount()) {
            return;
        }

        if (!aiStructuredParser.isAvailable(sanitizedAiContextText)) {
            return;
        }
        ParsedReportResult aiResult = aiStructuredParser.parse(sanitizedAiContextText);
        if (!shouldUseAiResult(aiResult, currentItemCount, false)) {
            return;
        }

        result.setItems(aiResult.getItems());
        result.setReportDate(chooseBetterReportDate(result.getReportDate(), aiResult.getReportDate()));
        result.setMessage(StrUtil.blankToDefault(aiResult.getMessage(), "AI 结构化解析已接管本次结果。"));
    }

    /**
     * 尝试基于报告图片本身做视觉结构化解析。
     *
     * <p>只对图片文件触发，原因很简单：
     * 1. 用户现在上传拍照体检单的场景很多；
     * 2. 视觉模型可以直接利用版面和表格关系，不需要服务端先做文字识别；
     * 3. 先把图片场景提上来，收益最大，也不会把 PDF 链路复杂度一下拉高。
     */
    private ParsedReportResult tryParseByVision(String extractedText, String extension, byte[] fileBytes) {
        if (!isImageExtension(extension)) {
            return null;
        }
        if (fileBytes == null || fileBytes.length == 0) {
            log.info("报告图片主解析跳过 AI 视觉结构化：文件内容为空，文件类型：{}", extension);
            return null;
        }
        if (fileBytes.length > aiStructuredParseProperties.getMaxImageBytes()) {
            log.info("报告图片大小 {} 超过 AI 视觉解析上限 {}，跳过视觉解析。", fileBytes.length,
                aiStructuredParseProperties.getMaxImageBytes());
            return null;
        }
        String imageDataUrl = buildImageDataUrl(extension, fileBytes);
        if (!aiStructuredParser.isVisionAvailable(extractedText, imageDataUrl)) {
            log.info("报告图片主解析跳过 AI 视觉结构化：当前模型视觉能力未启用或配置不完整，文件类型：{}", extension);
            return null;
        }
        ParsedReportResult visionResult = aiStructuredParser.parseWithVision(extractedText, imageDataUrl);
        if (visionResult == null || !visionResult.hasItems()) {
            log.info("报告图片 AI 视觉结构化未产出指标，文件类型：{}", extension);
            return visionResult;
        }
        log.info("报告图片 AI 视觉结构化完成，识别条数：{}，文件类型：{}",
            visionResult.getItems().size(), extension);
        return visionResult;
    }

    /**
     * 统一决定是否接纳 AI 结果。
     *
     * <p>视觉解析和文本解析共用同一套口径：
     * 1. AI 结果必须非空；
     * 2. 至少要比当前规则结果更多，或者当前规则结果本来就不足阈值；
     * 3. 视觉解析允许在规则结果为 0 时更积极地接管，因为它本来就是为图片错位场景兜底。
     */
    private boolean shouldUseAiResult(ParsedReportResult aiResult, int currentItemCount, boolean isVisionScene) {
        if (aiResult == null || !aiResult.hasItems()) {
            return false;
        }
        if (aiResult.getItems().size() > currentItemCount) {
            return true;
        }
        if (currentItemCount < aiStructuredParseProperties.getMinRuleItemCount()) {
            return aiResult.getItems().size() >= currentItemCount;
        }
        return isVisionScene && currentItemCount == 0;
    }

    private String normalizeExtension(File localFile, String extension) {
        return StrUtil.blankToDefault(extension, FileUtil.extName(localFile)).toLowerCase(Locale.ROOT);
    }

    private boolean isImageExtension(String extension) {
        return StrUtil.containsAnyIgnoreCase(StrUtil.blankToDefault(extension, ""), "jpg", "jpeg", "png", "bmp");
    }

    /**
     * 把本地图片字节包装成标准 data URL，供 OpenAI 兼容多模态接口直接消费。
     *
     * <p>之所以不传公网 URL，是为了避免依赖：
     * 1. MinIO 外链策略；
     * 2. 测试环境是否能被第三方模型网关回源访问；
     * 3. 本地开发场景的内网地址可达性。
     */
    private String buildImageDataUrl(String extension, byte[] fileBytes) {
        String normalizedExtension = StrUtil.blankToDefault(extension, "jpeg").toLowerCase(Locale.ROOT);
        String mimeType = switch (normalizedExtension) {
            case "png" -> "image/png";
            case "bmp" -> "image/bmp";
            default -> "image/jpeg";
        };
        return "data:" + mimeType + ";base64," + Base64.encode(fileBytes);
    }

    private String normalizeLine(String rawLine) {
        return StrUtil.blankToDefault(rawLine, "")
            .replace("：", ":")
            .replace("　", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    /**
     * 清洗即将送给大模型的文本上下文。
     *
     * <p>目标非常聚焦，只处理三类高风险噪声：
     * 1. 应用日志 / 线程信息 / SQL 调试行；
     * 2. 任何 URL，尤其是模型无权限读取的私有对象存储地址；
     * 3. 由清洗造成的多余空行和空白。
     *
     * <p>这样既能避免把无权限资源和内部运行细节暴露给外部模型，
     * 也能让模型把注意力留在真正的报告内容上。
     */
    private String sanitizeTextForAiPrompt(String rawText) {
        if (StrUtil.isBlank(rawText)) {
            return "";
        }
        List<String> keptLines = new ArrayList<>();
        String[] lines = rawText.replace("\r", "\n").split("\n");
        for (String rawLine : lines) {
            String normalizedLine = StrUtil.blankToDefault(rawLine, "").trim();
            if (normalizedLine.isEmpty()) {
                continue;
            }
            if (NON_REPORT_LOG_LINE_PATTERN.matcher(normalizedLine).matches()) {
                continue;
            }
            normalizedLine = URL_PATTERN.matcher(normalizedLine).replaceAll("[FILE_URL_OMITTED]");
            normalizedLine = normalizedLine.replaceAll("\\s+", " ").trim();
            if (normalizedLine.isEmpty() || "[FILE_URL_OMITTED]".equals(normalizedLine)) {
                continue;
            }
            keptLines.add(normalizedLine);
        }
        return StrUtil.join("\n", keptLines);
    }

    private void fillReferenceRange(ParsedReportItem item) {
        if (StrUtil.isBlank(item.getReferenceText())) {
            return;
        }
        Matcher matcher = Pattern.compile("[-+]?\\d+(\\.\\d+)?").matcher(item.getReferenceText());
        BigDecimal first = null;
        BigDecimal second = null;
        while (matcher.find()) {
            if (first == null) {
                first = new BigDecimal(matcher.group());
            } else {
                second = new BigDecimal(matcher.group());
                break;
            }
        }
        if (first != null && second != null) {
            item.setReferenceMin(first.min(second));
            item.setReferenceMax(first.max(second));
        }
    }

    private String limit(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    /**
     * 从已抽取文本中恢复报告日期。
     *
     * <p>这里采用“带语义标签优先、全文完整日期兜底”的两段式策略，
     * 目标是在不引入过多误判的前提下，尽可能补齐用户上传时漏填的 reportDate。
     */
    private Date extractReportDate(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        Date labeledDate = extractNewestDate(text, LABELED_REPORT_DATE_PATTERN);
        if (labeledDate != null) {
            return labeledDate;
        }
        return extractNewestDate(text, GENERIC_FULL_DATE_PATTERN);
    }

    private Date extractNewestDate(String text, Pattern pattern) {
        if (StrUtil.isBlank(text) || pattern == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        List<Date> candidates = new ArrayList<>();
        while (matcher.find()) {
            Date parsedDate = parseDateCandidate(matcher.group(1));
            if (parsedDate != null) {
                candidates.add(parsedDate);
            }
        }
        return candidates.stream()
            .filter(Objects::nonNull)
            // 同一次检查的多附件里如果出现多个完整日期，优先取较新的一个，
            // 通常更接近报告实际出具/检查完成时间。
            .max(Comparator.comparing(Date::getTime))
            .orElse(null);
    }

    private Date parseDateCandidate(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            DateTime dateTime = DateUtil.parseDate(value.trim());
            return dateTime == null ? null : dateTime.toJdkDate();
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 合并本地规则和 AI 给出的报告日期候选值。
     *
     * <p>当前优先保留本地规则命中的日期，因为它依赖明确文本标签，稳定性更高；
     * 只有本地没有抽到时，才让 AI 的多附件综合理解结果补位。
     */
    private Date chooseBetterReportDate(Date localExtractedDate, Date aiExtractedDate) {
        return localExtractedDate != null ? localExtractedDate : aiExtractedDate;
    }
}

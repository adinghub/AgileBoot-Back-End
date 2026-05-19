package com.healthtrail.domain.health.report.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.report.command.HealthReportItemCommand;
import com.healthtrail.domain.health.report.db.HealthReportItemEntity;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 体检报告指标结果领域模型。
 *
 * <p>单条指标虽然看上去只是几列数据，但背后其实承载两层核心业务：
 * 1. 结构化存储，方便列表展示和后续规则统计
 * 2. 自动判定异常标记，驱动报告摘要和后续健康看板
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HealthReportItemModel extends HealthReportItemEntity {

    /**
     * 用于从结果值或参考范围中提取数值的正则。
     * 这里支持整数、小数以及可选的正负号。
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(\\.\\d+)?");

    public HealthReportItemModel(HealthReportItemEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 装载命令对象。
     *
     * @param command     单条指标命令
     * @param reportId    所属报告ID
     * @param ownerUserId 归属用户ID
     * @param defaultSort 当命令里未传排序号时，使用当前循环位置兜底
     */
    public void loadCommand(HealthReportItemCommand command, Long reportId, Long ownerUserId, int defaultSort) {
        if (command == null) {
            return;
        }

        BeanUtil.copyProperties(command, this, "itemId", "reportId", "ownerUserId", "abnormalFlag");
        this.setReportId(reportId);
        this.setOwnerUserId(ownerUserId);
        this.setSort(command.getSort() == null ? defaultSort : command.getSort());

        // 如果只传了原始参考范围文本，则尽量做一次轻量解析，方便自动判断偏高/偏低。
        if (this.getReferenceMin() == null && this.getReferenceMax() == null) {
            fillReferenceRangeFromText();
        }

        this.setAbnormalFlag(resolveAbnormalFlag());
    }

    /**
     * 字段校验。
     */
    public void checkFields() {
        if (StrUtil.isBlank(getItemName())) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_NAME_REQUIRED);
        }
        if (StrUtil.isBlank(getResultValue())) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_RESULT_REQUIRED);
        }
        if (getItemName().length() > 100) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_NAME_TOO_LONG);
        }
        if (getResultValue().length() > 100) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_RESULT_TOO_LONG);
        }
        if (StrUtil.length(getReferenceText()) > 100) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_REFERENCE_TOO_LONG);
        }
        if (StrUtil.length(getItemInterpretation()) > 300) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_INTERPRETATION_TOO_LONG);
        }
        if (getReferenceMin() != null && getReferenceMax() != null
            && getReferenceMin().compareTo(getReferenceMax()) > 0) {
            throw new ApiException(ErrorCode.Business.APP_HEALTH_REPORT_ITEM_REFERENCE_RANGE_INVALID);
        }
        if (getDeleted() == null) {
            setDeleted(0);
        }
    }

    /**
     * 自动计算当前指标的异常标记。
     *
     * <p>一期的判断规则尽量简单透明：
     * 1. 有明确数值结果且有参考上下限时，按数值比较
     * 2. 只有单边阈值时，按单边规则比较
     * 3. 数值规则无法判断时，尝试用常见定性关键字做轻量判断
     * 4. 仍无法判断时，标记为“待判断”
     */
    public Integer resolveAbnormalFlag() {
        BigDecimal numericResult = extractFirstNumber(getResultValue());
        if (numericResult != null) {
            if (getReferenceMin() != null && numericResult.compareTo(getReferenceMin()) < 0) {
                return HealthReportItemAbnormalFlagEnum.LOW.getValue();
            }
            if (getReferenceMax() != null && numericResult.compareTo(getReferenceMax()) > 0) {
                return HealthReportItemAbnormalFlagEnum.HIGH.getValue();
            }
            if (getReferenceMin() != null || getReferenceMax() != null) {
                return HealthReportItemAbnormalFlagEnum.NORMAL.getValue();
            }
        }

        String normalizedValue = StrUtil.trim(StrUtil.blankToDefault(getResultValue(), ""));
        if (StrUtil.isBlank(normalizedValue)) {
            return HealthReportItemAbnormalFlagEnum.UNKNOWN.getValue();
        }
        if (containsAny(normalizedValue, "正常", "阴性", "未见异常")) {
            return HealthReportItemAbnormalFlagEnum.NORMAL.getValue();
        }
        if (containsAny(normalizedValue, "偏高", "升高", "↑")) {
            return HealthReportItemAbnormalFlagEnum.HIGH.getValue();
        }
        if (containsAny(normalizedValue, "偏低", "降低", "↓")) {
            return HealthReportItemAbnormalFlagEnum.LOW.getValue();
        }
        if (containsAny(normalizedValue, "异常", "阳性")) {
            return HealthReportItemAbnormalFlagEnum.ABNORMAL.getValue();
        }
        return HealthReportItemAbnormalFlagEnum.UNKNOWN.getValue();
    }

    /**
     * 根据参考范围原文尽量补齐上下限。
     *
     * <p>这里只覆盖最常见的几种格式，例如：
     * 1. 3.1-5.2
     * 2. 3.1~5.2
     * 3. <5.2 / ≤5.2 / 小于5.2
     * 4. >3.1 / ≥3.1 / 大于3.1
     *
     * <p>如果文本过于复杂，则保持为空，交给“待判断”逻辑兜底。
     */
    private void fillReferenceRangeFromText() {
        if (StrUtil.isBlank(getReferenceText())) {
            return;
        }

        String normalizedText = getReferenceText().replace("～", "~")
            .replace("—", "-")
            .replace("－", "-")
            .replace("至", "-")
            .replace("≤", "<=")
            .replace("＜", "<")
            .replace("≥", ">=")
            .replace("＞", ">");

        Matcher matcher = NUMBER_PATTERN.matcher(normalizedText);
        BigDecimal firstNumber = null;
        BigDecimal secondNumber = null;
        while (matcher.find()) {
            if (firstNumber == null) {
                firstNumber = new BigDecimal(matcher.group());
            } else {
                secondNumber = new BigDecimal(matcher.group());
                break;
            }
        }

        if (firstNumber != null && secondNumber != null) {
            this.setReferenceMin(firstNumber.min(secondNumber));
            this.setReferenceMax(firstNumber.max(secondNumber));
            return;
        }

        if (firstNumber == null) {
            return;
        }

        if (normalizedText.contains("<=") || normalizedText.contains("<") || normalizedText.contains("以下")) {
            this.setReferenceMax(firstNumber);
            return;
        }
        if (normalizedText.contains(">=") || normalizedText.contains(">") || normalizedText.contains("以上")) {
            this.setReferenceMin(firstNumber);
        }
    }

    /**
     * 提取文本中的第一个数值。
     * 例如：`5.6`、`5.6 mmol/L`、`↑5.6` 都会尽量提取出 `5.6`。
     */
    private BigDecimal extractFirstNumber(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group());
    }

    /**
     * 判断文本中是否包含任一关键字。
     */
    private boolean containsAny(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

package com.healthtrail.domain.health.chronic.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 慢病专项指标趋势返回对象。
 */
@Data
public class ChronicDiseaseIndicatorTrendDTO {

    /** 指标编码 */
    private String indicatorCode;

    /** 指标名称 */
    private String indicatorName;

    /** 结果单位 */
    private String resultUnit;

    /** 当前结果值 */
    private String currentResultValue;

    /** 当前异常标记 */
    private Integer currentAbnormalFlag;

    /** 当前异常标记名称 */
    private String currentAbnormalFlagName;

    /** 变化方向 */
    private String changeDirection;

    /** 变化摘要 */
    private String changeSummary;

    /**
     * 当前趋势命中的个性化目标范围。
     *
     * <p>为空表示用户还没有为该指标维护目标；不为空时，targetStatus/targetSummary
     * 会给出最新报告值和目标范围之间的运行时比对结果。
     */
    private ChronicIndicatorTargetDTO target;

    /** 指标目标状态 */
    private String targetStatus;

    /** 指标目标状态名称 */
    private String targetStatusName;

    /** 指标目标摘要 */
    private String targetSummary;

    /** 趋势数据点列表 */
    private List<ChronicDiseaseIndicatorPointDTO> points = new ArrayList<>();
}


package com.healthtrail.domain.health.report.command;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 体检报告单条指标命令。
 *
 * <p>这里把一行指标抽象成统一结构，主要是为了兼容不同来源：
 * 1. App 用户手工录入
 * 2. 文件解析或 AI 结构化服务回写
 * 3. 后台人工补录
 *
 * <p>当前阶段虽然入口主要给 App 用，但字段设计已经尽量给后续自动解析留好了位置。
 */
@Data
public class HealthReportItemCommand {

    /**
     * 指标编码。
     * 例如医院 LIS 系统里的项目编码，当前阶段允许为空，后续做标准化映射时可以继续使用。
     */
    @Size(max = 64, message = "指标编码长度不能超过64个字符")
    private String itemCode;

    /**
     * 标准指标编码。
     *
     * <p>该字段用于把不同医院、不同报告类型里的同一医学指标归并到同一趋势线上。
     * 例如“空腹血糖”“葡萄糖”“GLU”都可以归一成 `GLU`。
     */
    @Size(max = 64, message = "标准指标编码长度不能超过64个字符")
    private String standardItemCode;

    /**
     * 指标名称。
     * 例如：空腹血糖、总胆固醇、白细胞计数。
     */
    @NotBlank(message = "指标名称不能为空")
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    private String itemName;

    /**
     * 检测结果值。
     *
     * <p>这里使用字符串而不是纯数字，是为了兼容：
     * 1. 数值型结果，如 5.6
     * 2. 定性结果，如 阴性 / 阳性
     * 3. 带箭头或符号的历史数据，如 ↑ / ↓
     */
    @NotBlank(message = "检测结果不能为空")
    @Size(max = 100, message = "检测结果长度不能超过100个字符")
    private String resultValue;

    /**
     * 结果单位。
     * 例如 mmol/L、10^9/L。
     */
    @Size(max = 50, message = "结果单位长度不能超过50个字符")
    private String resultUnit;

    /**
     * 参考范围下限。
     * 当 App 已经能拿到结构化下限时，直接传该值，后端可以更准确地判断偏低/正常/偏高。
     */
    private BigDecimal referenceMin;

    /**
     * 参考范围上限。
     */
    private BigDecimal referenceMax;

    /**
     * 参考范围原文。
     * 例如：3.1-5.2、＜5.7、阴性。
     *
     * <p>如果前端只有原始展示文本，也可以只传该字段；
     * 后端会尽量做一次轻量解析，但不会承诺覆盖所有医院格式。
     */
    @Size(max = 100, message = "参考范围长度不能超过100个字符")
    private String referenceText;

    /**
     * 指标解读。
     *
     * <p>它关注的是“这个指标主要反映什么”，
     * 不是针对本次结果做诊断结论。
     * 例如：空腹血糖可解释为“用于反映空腹状态下的血糖水平，是评估糖代谢情况的常用指标”。
     */
    @Size(max = 300, message = "指标解读长度不能超过300个字符")
    private String itemInterpretation;

    /**
     * 排序号。
     * 用于尽量保持和原始报告中的展示顺序一致。
     */
    private Integer sort;

    /**
     * 备注。
     */
    @Size(max = 255, message = "指标备注长度不能超过255个字符")
    private String remark;
}

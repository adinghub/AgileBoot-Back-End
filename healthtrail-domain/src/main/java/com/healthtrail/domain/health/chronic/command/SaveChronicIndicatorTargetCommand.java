package com.healthtrail.domain.health.chronic.command;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 保存慢病指标目标范围命令。
 *
 * <p>目标范围挂在慢病专项档案下，而不是挂在病种配置下，原因是同一病种在不同成员、
 * 不同年龄段、不同医嘱下可能有不同控制目标。这里保存用户自己确认过的目标快照，
 * 慢病详情看板再用该目标和最新报告指标做运行时比对。
 */
@Data
public class SaveChronicIndicatorTargetCommand {

    /**
     * 指标编码，优先使用标准指标编码；没有标准编码时可使用报告原始指标编码或指标名称。
     */
    @NotBlank(message = "指标编码不能为空")
    @Size(max = 100, message = "指标编码长度不能超过100个字符")
    private String indicatorCode;

    /**
     * 指标名称快照，用于目标配置列表和趋势卡展示。
     */
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    private String indicatorName;

    /**
     * 目标下限。为空时表示不限制下限。
     */
    private BigDecimal targetMin;

    /**
     * 目标上限。为空时表示不限制上限。
     */
    private BigDecimal targetMax;

    /**
     * 非数值型目标说明，例如“按医生医嘱控制”或“保持阴性”。
     */
    @Size(max = 200, message = "目标说明长度不能超过200个字符")
    private String targetText;

    /**
     * 指标单位快照。
     */
    @Size(max = 50, message = "指标单位长度不能超过50个字符")
    private String resultUnit;

    /**
     * 状态：1启用，0停用。为空时默认启用。
     */
    private Integer status;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

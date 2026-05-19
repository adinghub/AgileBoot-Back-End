package com.healthtrail.domain.health.medication.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 新增用药计划命令对象。
 *
 * <p>用药计划描述的是用户定义的一条“提醒规则”，例如：
 * “妈妈从 4 月 1 日到 4 月 15 日，每天 08:00 和 20:00 服用阿司匹林 1 片”。
 */
@Data
public class AddMedicationPlanCommand {

    /**
     * 绑定的家庭成员ID。
     */
    @NotNull(message = "家庭成员不能为空")
    private Long memberId;

    /**
     * 药品ID，可为空。
     * 这里既支持系统下发药品，也支持当前用户自己录入的个人药品。
     * 如果未选择任何已录入药品，则必须填写自定义药名。
     */
    private Long drugId;

    /**
     * 自定义药名。
     */
    @Size(max = 100, message = "自定义药名长度不能超过100个字符")
    private String customDrugName;

    /**
     * 开始日期。
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束日期，可为空。
     *
     * <p>为空时表示长期计划，后端生成提醒时会按滚动窗口补齐，
     * 不再要求用户必须提前指定一个人为结束时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 每日提醒时间点列表，格式要求为 HH:mm。
     */
    @NotEmpty(message = "提醒时间不能为空")
    private List<String> reminderTimes;

    /**
     * 饭前饭后说明，例如 BEFORE_MEAL、AFTER_MEAL、NO_LIMIT。
     */
    @Size(max = 30, message = "服药时机长度不能超过30个字符")
    private String mealTiming;

    /**
     * 每次服用剂量。
     */
    private BigDecimal doseAmount;

    /**
     * 剂量单位，例如 片、粒、ml。
     */
    @Size(max = 20, message = "剂量单位长度不能超过20个字符")
    private String doseUnit;

    /**
     * 频率类型，一期默认按 DAILY 处理。
     */
    @Size(max = 30, message = "频率类型长度不能超过30个字符")
    private String frequencyType;

    /**
     * 每周提醒的星期列表。
     *
     * <p>仅当 `frequencyType = WEEKLY` 时需要传递，取值约定遵循 ISO 周数字：
     * 1 表示周一，7 表示周日。
     */
    private List<Integer> weeklyDays;

    /**
     * 每几小时提醒一次。
     *
     * <p>仅当 `frequencyType = INTERVAL_HOURS` 时需要传递。
     */
    @Min(value = 1, message = "间隔小时数不能小于1")
    @Max(value = 24, message = "间隔小时数不能大于24")
    private Integer intervalHours;

    /**
     * 每几天提醒一次。
     *
     * <p>仅当 `frequencyType = INTERVAL_DAYS` 时需要传递；“隔日”仍可继续使用
     * `EVERY_OTHER_DAY`，自定义隔 3 天、隔 5 天等场景使用该字段。
     */
    @Min(value = 1, message = "间隔天数不能小于1")
    @Max(value = 365, message = "间隔天数不能大于365")
    private Integer intervalDays;

    /**
     * 阶段剂量规则。
     *
     * <p>例如“前4天1片，再4天1.5片，之后2片”。当前先作为计划级文本规则保存，
     * 避免混入备注；后续如果需要精确自动计算每条提醒剂量，可在该字段之上再扩展结构化规则。
     */
    @Size(max = 1000, message = "阶段剂量规则长度不能超过1000个字符")
    private String doseRule;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 状态，默认正常。
     */
    private Integer status;
}

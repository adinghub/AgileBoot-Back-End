package com.healthtrail.domain.health.insight.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 保存日常指标记录命令。
 *
 * <p>日常指标仍写入通用慢病日记表，`indicatorType` 最终会转成 `entryType`，
 * `primaryValue/secondaryValue/metricPayloadJson` 共同表达结构化指标内容。
 * 这样血压、血糖、体重、心率、睡眠等记录都能走同一条链路，后续增加新慢病不需要改表。
 */
@Data
public class SaveDailyIndicatorRecordCommand {

    @NotNull(message = "家庭成员不能为空")
    /** 家庭成员ID */
    private Long memberId;

    /** 慢病专项档案ID */
    private Long profileId;

    @Size(max = 80, message = "慢病编码长度不能超过80个字符")
    /** 慢病病种编码 */
    private String diseaseCode;

    @Size(max = 40, message = "指标类型长度不能超过40个字符")
    /** 指标类型 */
    private String indicatorType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 记录时间 */
    private Date recordTime;

    /** 主要指标数值 */
    private BigDecimal primaryValue;

    /** 次要指标数值 */
    private BigDecimal secondaryValue;

    @Size(max = 20, message = "单位长度不能超过20个字符")
    /** 指标单位 */
    private String metricUnit;

    @Size(max = 40, message = "测量场景长度不能超过40个字符")
    /** 测量场景 */
    private String measureScene;

    @Size(max = 100, message = "记录标题长度不能超过100个字符")
    /** 记录标题 */
    private String entryTitle;

    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    /** 备注说明 */
    private String note;

    @Size(max = 4000, message = "结构化指标JSON长度不能超过4000个字符")
    /** 结构化指标JSON数据 */
    private String metricPayloadJson;
}

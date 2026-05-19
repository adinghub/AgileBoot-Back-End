package com.healthtrail.domain.health.insight.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 保存慢病日记命令。
 *
 * <p>慢病日记是通用记录容器，不限制只能记录血压或血糖。
 * `entryType` 用来表达记录类别，`metricPayloadJson` 用来保存结构化指标扩展，
 * 因此后续支持更多慢病时不需要为每类记录新增字段。
 */
@Data
public class SaveChronicDiaryEntryCommand {

    @NotNull(message = "家庭成员不能为空")
    /** 家庭成员ID */
    private Long memberId;

    /** 慢病专项档案ID */
    private Long profileId;

    /** 慢病病种编码 */
    private String diseaseCode;

    @Size(max = 40, message = "记录类型长度不能超过40个字符")
    /** 记录类型 */
    private String entryType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 记录时间 */
    private Date recordTime;

    @Size(max = 100, message = "记录标题长度不能超过100个字符")
    /** 记录标题 */
    private String entryTitle;

    @Size(max = 2000, message = "记录正文长度不能超过2000个字符")
    /** 记录正文 */
    private String entryContent;

    @Size(max = 4000, message = "结构化指标JSON长度不能超过4000个字符")
    /** 结构化指标JSON数据 */
    private String metricPayloadJson;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    /** 备注 */
    private String remark;
}

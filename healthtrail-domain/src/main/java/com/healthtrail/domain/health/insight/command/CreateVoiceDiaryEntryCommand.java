package com.healthtrail.domain.health.insight.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 语音日记创建命令。
 *
 * <p>当前 1.0 先承接“语音识别后的文本”，不在后端直接处理音频文件。
 * App 或后续语音网关完成 ASR 后，把识别文本提交到这里，
 * 后端会按慢病日记保存并标记 `sourceType=VOICE`。
 */
@Data
public class CreateVoiceDiaryEntryCommand {

    @NotNull(message = "家庭成员不能为空")
    /** 家庭成员ID */
    private Long memberId;

    /** 慢病专项档案ID */
    private Long profileId;

    /** 慢病病种编码 */
    private String diseaseCode;

    @NotBlank(message = "语音识别文本不能为空")
    @Size(max = 2000, message = "语音识别文本长度不能超过2000个字符")
    /** 语音识别文本 */
    private String recognizedText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 录音时间 */
    private Date recordTime;
}

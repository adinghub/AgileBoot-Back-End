package com.healthtrail.domain.health.insight.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 确认复查建议命令。
 *
 * <p>一期的复查建议只是服务端推导出的只读提醒；二期增加用户确认动作后，
 * App 将建议标题、原因、目标类型和目标业务主键回传，服务端再创建或刷新首页任务流中的复查任务。
 * 这样建议生成和任务落地仍保持解耦，后续更换建议规则不会影响任务承载方式。
 */
@Data
public class ConfirmReviewSuggestionCommand {

    @Size(max = 120, message = "建议编码长度不能超过120个字符")
    /** 复查建议编码 */
    private String suggestionCode;

    @NotBlank(message = "建议标题不能为空")
    @Size(max = 100, message = "建议标题长度不能超过100个字符")
    /** 建议标题 */
    private String title;

    @Size(max = 500, message = "建议原因长度不能超过500个字符")
    /** 建议原因 */
    private String reason;

    @NotBlank(message = "目标类型不能为空")
    @Size(max = 40, message = "目标类型长度不能超过40个字符")
    /** 建议目标类型 */
    private String targetType;

    @NotNull(message = "家庭成员不能为空")
    /** 家庭成员ID */
    private Long memberId;

    /** 目标业务主键ID */
    private Long targetBizId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    /** 建议复查时间 */
    private Date suggestReviewTime;

    @Size(max = 20, message = "优先级长度不能超过20个字符")
    /** 优先级 */
    private String priorityLevel;
}

package com.healthtrail.domain.health.chronic.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 保存慢病专项档案命令。
 *
 * <p>该命令只接收“成员 + 病种 + 用户维护信息”，
 * 不接收具体指标趋势。趋势由后端按照病种配置实时聚合，
 * 避免 App 把指标快照写回数据库后与报告原始数据发生口径偏差。
 */
@Data
public class SaveChronicDiseaseProfileCommand {

    /**
     * 家庭成员ID。
     */
    @NotNull(message = "家庭成员不能为空")
    private Long memberId;

    /**
     * 慢病病种编码。
     */
    @NotBlank(message = "慢病病种不能为空")
    @Size(max = 64, message = "慢病病种编码长度不能超过64个字符")
    private String diseaseCode;

    /**
     * 专项状态：1跟进中，2已稳定，3已关闭。
     */
    private Integer profileStatus;

    /**
     * 关注优先级：LOW/MEDIUM/HIGH。
     */
    @Size(max = 20, message = "关注优先级长度不能超过20个字符")
    private String riskLevel;

    /**
     * 确诊或建档日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date diagnosedDate;

    /**
     * 个性化管理目标。
     */
    @Size(max = 500, message = "管理目标长度不能超过500个字符")
    private String targetSummary;

    /**
     * 当前情况摘要。
     */
    @Size(max = 1000, message = "当前情况摘要长度不能超过1000个字符")
    private String currentSummary;

    /**
     * 最近复盘日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastReviewDate;

    /**
     * 备注。
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

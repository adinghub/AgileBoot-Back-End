package com.healthtrail.domain.health.insight.command;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 健康问题关联慢病专项命令。
 *
 * <p>问题和慢病专项必须属于同一个家庭成员，具体校验在应用服务里完成。
 * 命令只接收目标专项和备注，避免 App 伪造 ownerUserId、memberId 等归属字段。
 */
@Data
public class LinkHealthProblemChronicProfileCommand {

    @NotNull(message = "慢病专项不能为空")
    /** 慢病专项档案ID */
    private Long profileId;

    @Size(max = 500, message = "关联备注长度不能超过500个字符")
    /** 关联备注 */
    private String linkRemark;
}

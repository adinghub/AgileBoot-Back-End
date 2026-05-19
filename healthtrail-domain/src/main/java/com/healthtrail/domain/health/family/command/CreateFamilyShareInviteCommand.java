package com.healthtrail.domain.health.family.command;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建家庭共享邀请命令。
 *
 * <p>当前版本先支持两类分享角色：
 * 1. EDITOR：可协同管理
 * 2. VIEWER：只读查看
 */
@Data
public class CreateFamilyShareInviteCommand {

    /**
     * 邀请角色。
     */
    @NotBlank(message = "邀请角色不能为空")
    private String shareRole;

    /**
     * 邀请有效期小时数。
     * 默认 72 小时，限定范围主要是避免前端传入过长有效期导致历史邀请码长期暴露。
     */
    @Min(value = 1, message = "邀请有效期至少1小时")
    @Max(value = 720, message = "邀请有效期不能超过720小时")
    private Integer expireHours;

    /**
     * 备注。
     */
    private String remark;
}

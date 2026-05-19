package com.healthtrail.domain.system.member.command;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增会员权益命令。
 */
@Data
public class AddMemberFeatureCommand {

    @NotBlank(message = "权益编码不能为空")
    private String featureCode;

    @NotBlank(message = "权益名称不能为空")
    private String featureName;

    @NotBlank(message = "权益类型不能为空")
    private String featureType;

    private String quotaPeriodType;

    private Integer freeEnabled;

    private Integer freeLimitValue;

    private Integer featureSort;

    private Integer status;

    private Integer isBuiltin;

    private String remark;
}

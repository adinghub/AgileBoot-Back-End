package com.healthtrail.domain.system.member.command;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增会员等级命令。
 */
@Data
public class AddMemberLevelCommand {

    @NotBlank(message = "会员等级编码不能为空")
    private String levelCode;

    @NotBlank(message = "会员等级名称不能为空")
    private String levelName;

    private Integer levelSort;

    private BigDecimal price;

    private Integer durationDays;

    private String benefitDesc;

    private Integer status;

    private String remark;
}

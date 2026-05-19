package com.healthtrail.domain.system.config.command;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

/**
 * 系统参数配置修改命令
 * @author valarchie
 */
@Data
@Schema
public class ConfigUpdateCommand {

    /** 配置ID */
    @NotNull
    @Positive
    private Long configId;

    /** 配置值 */
    @NotNull
    @NotEmpty
    private String configValue;

}

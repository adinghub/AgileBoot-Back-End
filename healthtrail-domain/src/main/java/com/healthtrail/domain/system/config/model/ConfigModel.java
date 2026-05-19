package com.healthtrail.domain.system.config.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.system.config.command.ConfigUpdateCommand;
import com.healthtrail.domain.system.config.db.SysConfigEntity;
import com.healthtrail.domain.system.config.db.SysConfigService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置领域模型，负责配置值的更新装载和合法性校验。
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigModel extends SysConfigEntity {

    /** 系统配置数据库服务 */
    private SysConfigService configService;

    /** 当前配置的可选值集合，用于校验配置值是否在允许范围内 */
    private Set<String> configOptionSet;

    public ConfigModel(SysConfigService configService) {
        this.configService = configService;
    }

    public ConfigModel(SysConfigEntity entity, SysConfigService configService) {
        BeanUtil.copyProperties(entity, this);

        List<String> options =
            JSONUtil.isTypeJSONArray(entity.getConfigOptions()) ? JSONUtil.toList(entity.getConfigOptions(),
                String.class) : ListUtil.empty();

        this.configOptionSet = new HashSet<>(options);

        this.configService = configService;
    }

    public void loadUpdateCommand(ConfigUpdateCommand updateCommand) {
        this.setConfigValue(updateCommand.getConfigValue());
    }


    /** 校验配置值是否合法：不能为空且必须在可选范围内 */
    public void checkCanBeModify() {
        if (StrUtil.isBlank(getConfigValue())) {
            throw new ApiException(ErrorCode.Business.CONFIG_VALUE_IS_NOT_ALLOW_TO_EMPTY);
        }

        if (!configOptionSet.isEmpty() && !configOptionSet.contains(getConfigValue())) {
            throw new ApiException(ErrorCode.Business.CONFIG_VALUE_IS_NOT_IN_OPTIONS);
        }
    }

}

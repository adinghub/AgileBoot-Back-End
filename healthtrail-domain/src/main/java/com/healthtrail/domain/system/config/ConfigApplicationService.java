package com.healthtrail.domain.system.config;

import com.healthtrail.common.core.page.PageDTO;
import com.healthtrail.domain.common.cache.CacheCenter;
import com.healthtrail.domain.system.config.command.ConfigUpdateCommand;
import com.healthtrail.domain.system.config.dto.ConfigDTO;
import com.healthtrail.domain.system.config.model.ConfigModel;
import com.healthtrail.domain.system.config.model.ConfigModelFactory;
import com.healthtrail.domain.system.config.query.ConfigQuery;
import com.healthtrail.domain.system.config.db.SysConfigEntity;
import com.healthtrail.domain.system.config.db.SysConfigService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 系统参数配置应用服务 */
@Service
@RequiredArgsConstructor
public class ConfigApplicationService {

    /** 系统配置领域模型工厂 */
    private final ConfigModelFactory configModelFactory;

    /** 系统配置数据库服务 */
    private final SysConfigService configService;

    public PageDTO<ConfigDTO> getConfigList(ConfigQuery query) {
        Page<SysConfigEntity> page = configService.page(query.toPage(), query.toQueryWrapper());
        List<ConfigDTO> records = page.getRecords().stream().map(ConfigDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public ConfigDTO getConfigInfo(Long id) {
        SysConfigEntity byId = configService.getById(id);
        return new ConfigDTO(byId);
    }

    public void updateConfig(ConfigUpdateCommand updateCommand) {
        ConfigModel configModel = configModelFactory.loadById(updateCommand.getConfigId());
        configModel.loadUpdateCommand(updateCommand);

        configModel.checkCanBeModify();

        configModel.updateById();

        CacheCenter.configCache.invalidate(configModel.getConfigKey());
    }


}

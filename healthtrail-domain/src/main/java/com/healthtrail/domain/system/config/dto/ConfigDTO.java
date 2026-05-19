package com.healthtrail.domain.system.config.dto;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.domain.system.config.db.SysConfigEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 系统参数配置返回对象
 * @author valarchie
 */
@Data
@Schema(name = "ConfigDTO", description = "配置信息")
public class ConfigDTO {

    public ConfigDTO(SysConfigEntity entity) {
        if (entity != null) {
            configId = entity.getConfigId() + "";
            configName = entity.getConfigName();
            configKey = entity.getConfigKey();
            configValue = entity.getConfigValue();
            configOptions =
                JSONUtil.isTypeJSONArray(entity.getConfigOptions()) ? JSONUtil.toList(entity.getConfigOptions(),
                    String.class) : ListUtil.empty();
            isAllowChange = Convert.toInt(entity.getIsAllowChange());
            isAllowChangeStr = BasicEnumUtil.getDescriptionByBool(YesOrNoEnum.class, entity.getIsAllowChange());
            remark = entity.getRemark();
            createTime = entity.getCreateTime();
        }
    }

    /** 配置ID */
    private String configId;
    /** 配置名称 */
    private String configName;
    /** 配置键名 */
    private String configKey;
    /** 配置值 */
    private String configValue;
    /** 可选值列表 */
    private List<String> configOptions;
    /** 是否允许修改 */
    private Integer isAllowChange;
    /** 是否允许修改文本描述 */
    private String isAllowChangeStr;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private Date createTime;

}

package com.healthtrail.domain.health.device.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.BasicEnumUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.health.AppPushPlatformEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.device.command.RegisterAppDeviceCommand;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * App设备领域模型，负责设备注册命令装载、平台校验和状态维护。
 *
 * <p>设备注册不是单纯落一条 Token，而是维护"当前账号和当前设备"的稳定绑定关系。
 * 因此这里承担：
 * 1. 命令字段装载
 * 2. 平台枚举合法性校验
 * 3. 最后活跃时间和状态维护
 *
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AppDeviceModel extends HealthAppDeviceEntity {

    public AppDeviceModel(HealthAppDeviceEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 装载设备注册命令。
     *
     * <p>无论是首次注册还是已有设备重新上报 Token，都会走这套装载逻辑，
     * 确保设备信息、版本信息、活跃时间统一刷新。
     */
    public void loadRegisterCommand(RegisterAppDeviceCommand command, Long ownerUserId) {
        if (command == null) {
            return;
        }
        BeanUtil.copyProperties(command, this);
        this.setOwnerUserId(ownerUserId);
        this.setLastActiveTime(new Date());
        this.setStatus(StatusEnum.ENABLE.getValue());
    }

    /**
     * 停用设备。
     * 当前采用"状态停用"而不是直接删记录，便于后续排查设备切换、重复绑定问题。
     */
    public void disable() {
        this.setStatus(StatusEnum.DISABLE.getValue());
        this.setLastActiveTime(new Date());
    }

    /**
     * 校验设备字段合法性。
     */
    public void checkFields() {
        BasicEnumUtil.fromValue(AppPushPlatformEnum.class, getPushPlatform());
        if (StrUtil.isBlank(getDeviceCode())) {
            throw new ApiException(ErrorCode.Business.APP_DEVICE_CODE_REQUIRED);
        }
        if (StrUtil.isBlank(getDeviceToken())) {
            throw new ApiException(ErrorCode.Business.APP_DEVICE_TOKEN_REQUIRED);
        }
    }
}

package com.healthtrail.domain.health.device;

import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.domain.health.device.command.RegisterAppDeviceCommand;
import com.healthtrail.domain.health.device.db.HealthAppDeviceEntity;
import com.healthtrail.domain.health.device.db.HealthAppDeviceService;
import com.healthtrail.domain.health.device.dto.AppDeviceDTO;
import com.healthtrail.domain.health.device.model.AppDeviceModel;
import com.healthtrail.domain.health.device.model.AppDeviceModelFactory;
import com.healthtrail.domain.health.drug.DrugApplicationService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * App 设备应用服务。
 *
 * <p>该服务负责维护“账号 - 设备 - Token”的绑定关系，
 * 是后续真实 Push 发送的核心数据入口。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppDeviceApplicationService {

    /** App 设备数据库服务 */
    private final HealthAppDeviceService appDeviceService;

    /** App 设备领域模型工厂 */
    private final AppDeviceModelFactory appDeviceModelFactory;

    /** 药品管理应用服务 */
    private final DrugApplicationService drugApplicationService;

    /**
     * 注册或刷新设备。
     *
     * <p>如果设备编码已存在，则视为同一安装实例重新上报 Token，
     * 后端会直接覆盖归属账号和 Token 信息，保证多账号切换场景下的绑定关系最新。
     *
     * <p>设备注册成功后，会顺手补跑一次当前用户仍未完成的低库存提醒。
     * 这样当用户在“低库存发生之后”才重新登录、重新上报 Token 时，
     * 也能把之前因为没有活跃设备而错过的 Push 再补发一次。
     */
    public void registerDevice(RegisterAppDeviceCommand registerCommand, Long ownerUserId) {
        AppDeviceModel appDeviceModel = appDeviceModelFactory.loadByDeviceCode(registerCommand.getDeviceCode());
        if (appDeviceModel == null) {
            appDeviceModel = appDeviceModelFactory.create();
            appDeviceModel.loadRegisterCommand(registerCommand, ownerUserId);
            appDeviceModel.checkFields();
            appDeviceService.save(appDeviceModel);
            triggerLowStockAlertRedriveQuietly(ownerUserId);
            return;
        }

        appDeviceModel.loadRegisterCommand(registerCommand, ownerUserId);
        appDeviceModel.checkFields();
        appDeviceService.updateById(appDeviceModel);
        triggerLowStockAlertRedriveQuietly(ownerUserId);
    }

    /**
     * 获取当前用户设备列表。
     */
    public List<AppDeviceDTO> getCurrentUserDevices(Long ownerUserId) {
        return appDeviceService.listActiveDevices(ownerUserId)
            .stream()
            .map(AppDeviceDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * 停用当前用户指定设备。
     *
     * <p>这里不做物理删除，避免 App 切换账号、重复注册、问题排查时完全丢失设备轨迹。
     */
    public void unregisterDevice(String deviceCode, Long ownerUserId) {
        UpdateWrapper<HealthAppDeviceEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("device_code", deviceCode)
            .eq("owner_user_id", ownerUserId)
            .set("status", StatusEnum.DISABLE.getValue())
            .set("last_active_time", new Date());
        appDeviceService.update(updateWrapper);
    }

    /**
     * 设备注册成功后，安静地补跑一次低库存提醒。
     *
     * <p>这里故意吞掉补发异常，只记录日志，不往上抛：
     * 设备注册是 App 建立 Push 能力的前提入口，不能因为某条药品提醒补发失败导致整个设备注册接口失败。
     */
    private void triggerLowStockAlertRedriveQuietly(Long ownerUserId) {
        try {
            drugApplicationService.redrivePendingLowStockAlertsAfterDeviceRegistration(ownerUserId);
        } catch (Exception ex) {
            log.warn("设备注册后补跑低库存提醒失败，ownerUserId={}, reason={}", ownerUserId, ex.getMessage());
        }
    }
}

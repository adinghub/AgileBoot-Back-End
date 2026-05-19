package com.healthtrail.domain.health.user;

import com.healthtrail.domain.health.user.command.AppRegisterCommand;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.healthtrail.domain.health.user.dto.AppUserDTO;
import com.healthtrail.domain.health.user.model.AppUserModel;
import com.healthtrail.domain.health.user.model.AppUserModelFactory;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * App 用户应用服务。
 *
 * <p>该服务负责组织 App 用户的核心业务流程，
 * 例如注册成功后如何落库、登录成功后如何更新最后登录信息、如何对外返回 DTO。
 */
@Service
@RequiredArgsConstructor
public class AppUserApplicationService {

    /** App用户数据库服务 */
    private final HealthAppUserService appUserService;

    /** App用户领域模型工厂 */
    private final AppUserModelFactory appUserModelFactory;

    /**
     * 注册 App 用户。
     *
     * @param registerCommand 注册命令
     * @return 新注册完成的用户实体
     */
    public HealthAppUserEntity register(AppRegisterCommand registerCommand) {
        AppUserModel appUserModel = appUserModelFactory.create();
        appUserModel.checkPasswordConfirm(registerCommand);
        appUserModel.loadFromRegisterCommand(registerCommand);
        appUserModel.checkMobileUnique();
        appUserModel.fillDefaultNickname();
        appUserModel.encryptPassword();
        appUserModel.insert();
        return appUserModel;
    }

    /**
     * 根据用户ID获取当前 App 用户信息。
     *
     * @param userId App 用户ID
     * @return 当前用户信息
     */
    public AppUserDTO getCurrentUser(Long userId) {
        AppUserModel appUserModel = appUserModelFactory.loadById(userId);
        return new AppUserDTO(appUserModel);
    }

    /**
     * 登录成功后更新最后登录信息。
     *
     * @param userId      App 用户ID
     * @param lastLoginIp 最后登录IP
     */
    public void recordLoginSuccess(Long userId, String lastLoginIp) {
        AppUserModel appUserModel = appUserModelFactory.loadById(userId);
        appUserModel.setLastLoginIp(lastLoginIp);
        appUserModel.setLastLoginTime(new Date());
        appUserModel.updateById();
    }

    /**
     * 通过手机号查询用户，供登录流程复用。
     *
     * @param mobile 手机号
     * @return App 用户实体
     */
    public HealthAppUserEntity getByMobile(String mobile) {
        return appUserService.getByMobile(mobile);
    }

    /**
     * 根据用户ID获取 App 用户实体。
     *
     * <p>refresh token 续签场景需要在不依赖旧 access 登录态的前提下重新构建登录结果，
     * 因此这里补一个实体查询入口供 API 层登录服务复用。
     */
    public HealthAppUserEntity getById(Long userId) {
        return userId == null ? null : appUserService.getById(userId);
    }
}

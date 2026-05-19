package com.healthtrail.domain.health.user.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.health.user.command.AppRegisterCommand;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.db.HealthAppUserService;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * App 用户领域模型。
 *
 * <p>这里集中处理注册阶段的核心业务规则，例如：
 * 手机号唯一校验、确认密码校验、默认昵称生成、密码加密等。
 * 这样 Controller 和 Service 就不会堆积过多细碎判断。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AppUserModel extends HealthAppUserEntity {

    /** App用户数据库服务 */
    private HealthAppUserService appUserService;

    public AppUserModel(HealthAppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public AppUserModel(HealthAppUserEntity entity, HealthAppUserService appUserService) {
        this(appUserService);
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 从注册命令中装载用户数据。
     * 注册来源当前统一写成 APP，后续如果增加微信小程序、H5 等端，
     * 可以继续在这里做来源拆分。
     *
     * @param command 注册命令
     */
    public void loadFromRegisterCommand(AppRegisterCommand command) {
        if (command == null) {
            return;
        }

        this.setMobile(command.getMobile());
        this.setNickname(command.getNickname());
        this.setPassword(command.getPassword());
        this.setRegisterSource("APP");
        this.setStatus(StatusEnum.ENABLE.getValue());
    }

    /**
     * 校验两次输入密码是否一致。
     *
     * @param command 注册命令
     */
    public void checkPasswordConfirm(AppRegisterCommand command) {
        if (command == null || !Objects.equals(command.getPassword(), command.getConfirmPassword())) {
            throw new ApiException(ErrorCode.Business.APP_USER_REGISTER_PASSWORD_CONFIRM_NOT_MATCH);
        }
    }

    /**
     * 校验手机号是否重复。
     */
    public void checkMobileUnique() {
        if (appUserService.isMobileDuplicated(getMobile(), getUserId())) {
            throw new ApiException(ErrorCode.Business.APP_USER_MOBILE_IS_NOT_UNIQUE);
        }
    }

    /**
     * 填充默认昵称。
     * 如果用户注册时没有主动输入昵称，则用手机号后四位生成一个更友好的默认展示名。
     */
    public void fillDefaultNickname() {
        if (StrUtil.isBlank(getNickname()) && StrUtil.isNotBlank(getMobile())) {
            String mobileSuffix = getMobile().length() > 4 ? getMobile().substring(getMobile().length() - 4) : getMobile();
            this.setNickname("健康用户" + mobileSuffix);
        }
    }

    /**
     * 将明文密码加密后再持久化。
     * 这里复用项目已经存在的 BCrypt 工具方法，避免出现多套密码加密规则。
     */
    public void encryptPassword() {
        this.setPassword(AuthenticationUtils.encryptPassword(getPassword()));
    }
}

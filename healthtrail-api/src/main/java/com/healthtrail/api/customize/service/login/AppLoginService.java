package com.healthtrail.api.customize.service.login;

import cn.hutool.extra.servlet.ServletUtil;
import com.healthtrail.api.customize.service.JwtTokenService;
import com.healthtrail.common.constant.Constants;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.common.utils.ServletHolderUtil;
import com.healthtrail.domain.common.cache.RedisCacheService;
import com.healthtrail.domain.health.user.AppUserApplicationService;
import com.healthtrail.domain.health.user.command.AppLoginCommand;
import com.healthtrail.domain.health.user.command.AppRefreshTokenCommand;
import com.healthtrail.domain.health.user.command.AppRegisterCommand;
import com.healthtrail.domain.health.user.db.HealthAppUserEntity;
import com.healthtrail.domain.health.user.dto.AppLoginDTO;
import com.healthtrail.domain.health.user.dto.AppUserDTO;
import com.healthtrail.infrastructure.user.AuthenticationUtils;
import com.healthtrail.infrastructure.user.app.AppLoginUser;
import com.healthtrail.infrastructure.user.app.AppRefreshTokenSession;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * App 登录注册服务。
 *
 * <p>该服务位于 api 模块，专门负责 App 接口层需要的登录编排逻辑：
 * 1. 注册成功后自动登录
 * 2. 登录成功后签发 token
 * 3. 维护当前登录用户上下文需要的缓存信息
 *
 * <p>真正的 App 用户落库逻辑仍然放在 domain 模块中，这里只做接口层业务编排。
 */
@Service
@RequiredArgsConstructor
public class AppLoginService {

    private final AppUserApplicationService appUserApplicationService;

    private final JwtTokenService jwtTokenService;

    private final RedisCacheService redisCacheService;

    /**
     * App 用户注册。
     * 一期为了减少前端接入复杂度，注册成功后直接返回登录态。
     *
     * @param registerCommand 注册命令
     * @return 登录结果
     */
    public AppLoginDTO register(AppRegisterCommand registerCommand) {
        HealthAppUserEntity appUserEntity = appUserApplicationService.register(registerCommand);
        return buildLoginResult(appUserEntity, true);
    }

    /**
     * App 用户登录。
     *
     * @param loginCommand 登录命令
     * @return 登录结果
     */
    public AppLoginDTO login(AppLoginCommand loginCommand) {
        HealthAppUserEntity appUserEntity = appUserApplicationService.getByMobile(loginCommand.getMobile());
        if (appUserEntity == null || !AuthenticationUtils.matchesPassword(loginCommand.getPassword(), appUserEntity.getPassword())) {
            throw new ApiException(ErrorCode.Business.APP_USER_LOGIN_MOBILE_OR_PASSWORD_INVALID);
        }

        if (!StatusEnum.ENABLE.getValue().equals(appUserEntity.getStatus())) {
            throw new ApiException(ErrorCode.Business.APP_USER_IS_DISABLE);
        }

        return buildLoginResult(appUserEntity, true);
    }

    /**
     * 获取当前登录 App 用户信息。
     *
     * @return 当前登录用户信息
     */
    public AppUserDTO getCurrentUser() {
        AppLoginUser appLoginUser = AuthenticationUtils.getAppLoginUser();
        return appUserApplicationService.getCurrentUser(appLoginUser.getUserId());
    }

    /**
     * 使用 refresh token 换取一套新的 access / refresh 令牌。
     *
     * <p>这里会做一次完整轮换：
     * 1. 校验 refresh token 与 Redis refresh 会话
     * 2. 删除旧 access 登录缓存与旧 refresh 会话
     * 3. 重新生成新的 access / refresh 令牌对
     */
    public AppLoginDTO refresh(AppRefreshTokenCommand refreshTokenCommand) {
        AppRefreshTokenSession refreshSession = jwtTokenService.getRefreshTokenSession(refreshTokenCommand.getRefreshToken());
        jwtTokenService.deleteAccessLoginSession(refreshSession.getAccessCachedKey());
        jwtTokenService.deleteRefreshTokenSession(refreshSession.getRefreshTokenKey());
        HealthAppUserEntity appUserEntity = appUserApplicationService.getById(refreshSession.getUserId());
        if (appUserEntity == null || !StatusEnum.ENABLE.getValue().equals(appUserEntity.getStatus())) {
            throw new ApiException(ErrorCode.Client.INVALID_TOKEN);
        }
        return buildLoginResult(appUserEntity, false);
    }

    /**
     * App 用户退出登录。
     *
     * <p>当前采用 JWT + Redis 登录态模型，因此退出登录的关键动作是删除 Redis 中的登录缓存。
     * JWT 本身即使还在客户端本地，只要缓存被删除，后端也无法再还原出有效登录用户。
     */
    public void logout() {
        AppLoginUser appLoginUser = AuthenticationUtils.getAppLoginUser();
        if (appLoginUser.getCachedKey() != null) {
            redisCacheService.appLoginUserCache.delete(appLoginUser.getCachedKey());
        }
        if (appLoginUser.getRefreshTokenKey() != null) {
            redisCacheService.appRefreshTokenCache.delete(appLoginUser.getRefreshTokenKey());
        }
    }

    /**
     * 构建统一的登录返回结果。
     *
     * <p>无论是注册后自动登录还是普通登录，都需要复用下面几步：
     * 1. 更新最后登录信息
     * 2. 构建登录态对象
     * 3. 写入 Redis 并生成 JWT
     * 4. 返回 token 和用户摘要
     *
     * @param appUserEntity      App 用户实体
     * @param recordLoginSuccess 是否记录本次登录成功
     * @return 登录返回对象
     */
    private AppLoginDTO buildLoginResult(HealthAppUserEntity appUserEntity, boolean recordLoginSuccess) {
        if (recordLoginSuccess) {
            String ip = ServletUtil.getClientIP(ServletHolderUtil.getRequest());
            appUserApplicationService.recordLoginSuccess(appUserEntity.getUserId(), ip);
            appUserEntity.setLastLoginIp(ip);
        }

        AppLoginUser appLoginUser = new AppLoginUser(
            appUserEntity.getUserId(),
            false,
            appUserEntity.getMobile(),
            appUserEntity.getPassword(),
            null
        );
        appLoginUser.fillLoginInfo();
        appLoginUser.setAutoRefreshCacheTime(appLoginUser.getLoginInfo().getLoginTime()
            + TimeUnit.MINUTES.toMillis(jwtTokenService.getAutoRefreshTime()));

        JwtTokenService.AppTokenBundle tokenBundle = jwtTokenService.createTokenPairAndPutUserInCache(appLoginUser);
        AppUserDTO userInfo = appUserApplicationService.getCurrentUser(appUserEntity.getUserId());
        return new AppLoginDTO(tokenBundle.getAccessToken(), Constants.Token.PREFIX.trim(), tokenBundle.getRefreshToken(), userInfo);
    }
}

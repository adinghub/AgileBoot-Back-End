package com.healthtrail.api.customize.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.healthtrail.common.constant.Constants.Token;
import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.healthtrail.domain.common.cache.RedisCacheService;
import com.healthtrail.infrastructure.user.app.AppLoginUser;
import com.healthtrail.infrastructure.user.app.AppRefreshTokenSession;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * App 端 token 验证与缓存处理服务。
 *
 * <p>当前项目同时存在后台管理端和 App 端两套入口，因此 token 服务也需要明确区分。
 * 这里专门只处理 App 登录用户，不复用后台 SystemLoginUser 的缓存空间，
 * 这样能避免两套用户体系在 Redis 中互相污染。
 *
 * @author valarchie
 */
@Component
@Slf4j
@Data
@RequiredArgsConstructor
public class JwtTokenService {

    /**
     * 自定义令牌标识。
     */
    @Value("${token.header}")
    private String header;

    /**
     * JWT 签名密钥。
     */
    @Value("${token.secret}")
    private String secret;

    /**
     * 自动刷新 token 缓存的时间窗口。
     * 当活跃用户访问接口时，会顺带刷新 Redis 中的登录态有效期。
     */
    @Value("${token.autoRefreshTime}")
    private long autoRefreshTime;

    /**
     * access token 的 JWT 过期时间，默认 2 小时。
     *
     * <p>access token 适合做短周期令牌：一方面降低泄露后的可用时间窗口，
     * 另一方面配合 refresh token 实现用户无感续期。
     */
    @Value("${token.appAccessExpireMinutes:120}")
    private long appAccessExpireMinutes;

    /**
     * refresh token 的 JWT 过期时间，默认 30 天。
     *
     * <p>这和 Redis refresh 会话缓存共同组成“双保险”：
     * - JWT 负责自带时间边界
     * - Redis 负责服务端主动失效能力
     */
    @Value("${token.appRefreshExpireDays:30}")
    private long appRefreshExpireDays;

    private final RedisCacheService redisCache;

    /**
     * 获取当前请求对应的 App 登录用户。
     *
     * <p>JWT 中只保存一个缓存 key，真正的用户信息还是放在 Redis 中。
     * 这样后续如果要做强制下线、会员状态调整、设备踢出等能力时，会更容易扩展。
     *
     * @param request 当前 HTTP 请求
     * @return App 登录用户；如果请求未携带 token，则返回 null
     */
    public AppLoginUser getLoginUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StrUtil.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                if (!isAccessTokenClaims(claims)) {
                    return null;
                }
                String uuid = (String) claims.get(Token.LOGIN_USER_KEY);
                return redisCache.appLoginUserCache.getObjectOnlyInCacheById(uuid);
            } catch (ExpiredJwtException | SignatureException | MalformedJwtException | UnsupportedJwtException
                     | IllegalArgumentException jwtException) {
                log.error("parse token failed.", jwtException);
                throw new ApiException(jwtException, ErrorCode.Client.INVALID_TOKEN);
            } catch (Exception e) {
                log.error("fail to get cached app user from redis", e);
                throw new ApiException(e, ErrorCode.Client.TOKEN_PROCESS_FAILED, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 创建 token 并将 App 登录用户写入缓存。
     *
     * @param loginUser App 登录用户
     * @return JWT token
     */
    public AppTokenBundle createTokenPairAndPutUserInCache(AppLoginUser loginUser) {
        loginUser.setCachedKey(IdUtil.fastUUID());
        loginUser.setRefreshTokenKey(IdUtil.fastUUID());
        redisCache.appLoginUserCache.set(loginUser.getCachedKey(), loginUser);
        redisCache.appRefreshTokenCache.set(
            loginUser.getRefreshTokenKey(),
            new AppRefreshTokenSession(loginUser.getUserId(), loginUser.getRefreshTokenKey(), loginUser.getCachedKey()));
        return new AppTokenBundle(
            generateAccessToken(loginUser.getCachedKey()),
            generateRefreshToken(loginUser.getRefreshTokenKey()));
    }

    /**
     * App token 的滑动续期逻辑。
     * 这里刷新的是 Redis 缓存时间，而不是重新签发 JWT，
     * 这样前端不需要频繁替换 token，整体接入成本更低。
     *
     * @param loginUser App 登录用户
     */
    public void refreshToken(AppLoginUser loginUser) {
        long currentTime = System.currentTimeMillis();
        if (loginUser.getAutoRefreshCacheTime() != null && currentTime > loginUser.getAutoRefreshCacheTime()) {
            loginUser.setAutoRefreshCacheTime(currentTime + TimeUnit.MINUTES.toMillis(autoRefreshTime));
            redisCache.appLoginUserCache.set(loginUser.getCachedKey(), loginUser);
        }
    }

    /**
     * 解析 refresh token 对应的服务端会话。
     *
     * <p>只有同时满足下面条件才视为可续签：
     * 1. JWT 签名合法且未过期
     * 2. token_scene 为 refresh
     * 3. Redis 中仍然存在 refresh 会话
     */
    public AppRefreshTokenSession getRefreshTokenSession(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            throw new ApiException(ErrorCode.Client.INVALID_TOKEN);
        }
        try {
            Claims claims = parseToken(stripTokenPrefix(refreshToken));
            if (!isRefreshTokenClaims(claims)) {
                throw new ApiException(ErrorCode.Client.INVALID_TOKEN);
            }
            String refreshTokenKey = claims.get(Token.REFRESH_TOKEN_KEY, String.class);
            AppRefreshTokenSession session = redisCache.appRefreshTokenCache.getObjectOnlyInCacheById(refreshTokenKey);
            if (session == null || session.getUserId() == null) {
                throw new ApiException(ErrorCode.Client.INVALID_TOKEN);
            }
            return session;
        } catch (ApiException ex) {
            throw ex;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | UnsupportedJwtException
                 | IllegalArgumentException jwtException) {
            log.error("parse refresh token failed.", jwtException);
            throw new ApiException(jwtException, ErrorCode.Client.INVALID_TOKEN);
        } catch (Exception ex) {
            log.error("fail to get refresh session from redis", ex);
            throw new ApiException(ex, ErrorCode.Client.TOKEN_PROCESS_FAILED, ex.getMessage());
        }
    }

    /**
     * 让当前 refresh 会话失效。
     *
     * <p>用于 logout 或 refresh token 轮换时清理旧 refresh 会话。
     */
    public void deleteRefreshTokenSession(String refreshTokenKey) {
        if (StrUtil.isBlank(refreshTokenKey)) {
            return;
        }
        redisCache.appRefreshTokenCache.delete(refreshTokenKey);
    }

    /**
     * 让当前 access 登录缓存失效。
     */
    public void deleteAccessLoginSession(String cachedKey) {
        if (StrUtil.isBlank(cachedKey)) {
            return;
        }
        redisCache.appLoginUserCache.delete(cachedKey);
    }

    /**
     * 生成 JWT token。
     *
     * @param claims token 中存放的载荷
     * @return JWT token
     */
    private String generateToken(Map<String, Object> claims, Date expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }

    private String generateAccessToken(String cachedKey) {
        return generateToken(
            MapUtil.<String, Object>builder()
                .put(Token.LOGIN_USER_KEY, cachedKey)
                .put(Token.TOKEN_SCENE_KEY, Token.ACCESS_TOKEN_SCENE)
                .build(),
            new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(appAccessExpireMinutes)));
    }

    private String generateRefreshToken(String refreshTokenKey) {
        return generateToken(
            MapUtil.<String, Object>builder()
                .put(Token.REFRESH_TOKEN_KEY, refreshTokenKey)
                .put(Token.TOKEN_SCENE_KEY, Token.REFRESH_TOKEN_SCENE)
                .build(),
            new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(appRefreshExpireDays)));
    }

    /**
     * 解析 token。
     *
     * @param token JWT token
     * @return token claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * 从请求头中提取 token 字符串。
     *
     * @param request 当前 HTTP 请求
     * @return 纯净 token 内容
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(header);
        return stripTokenPrefix(token);
    }

    private String stripTokenPrefix(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(Token.PREFIX)) {
            token = StrUtil.stripIgnoreCase(token, Token.PREFIX, null);
        }
        return token;
    }

    private boolean isAccessTokenClaims(Claims claims) {
        String tokenScene = claims == null ? null : claims.get(Token.TOKEN_SCENE_KEY, String.class);
        return StrUtil.isBlank(tokenScene) || StrUtil.equals(tokenScene, Token.ACCESS_TOKEN_SCENE);
    }

    private boolean isRefreshTokenClaims(Claims claims) {
        String tokenScene = claims == null ? null : claims.get(Token.TOKEN_SCENE_KEY, String.class);
        return StrUtil.equals(tokenScene, Token.REFRESH_TOKEN_SCENE);
    }

    /**
     * access / refresh 令牌对。
     */
    @Data
    @RequiredArgsConstructor
    public static class AppTokenBundle {

        private final String accessToken;

        private final String refreshToken;
    }
}

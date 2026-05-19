package com.healthtrail.api.customize.config;

import com.healthtrail.api.customize.service.JwtTokenService;
import com.healthtrail.infrastructure.user.app.AppLoginUser;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * App 端 JWT 认证过滤器。
 *
 * <p>过滤器不处理具体的登录业务，只负责把 token 还原成当前登录用户，
 * 然后交给 Spring Security 上下文管理。这样业务 Controller 中只需要拿当前用户，
 * 不需要重复写 token 解析逻辑。
 *
 * @author valarchie
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        AppLoginUser loginUser = jwtTokenService.getLoginUser(request);
        if (loginUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            /**
             * 这里在写入认证上下文前先刷新缓存有效期。
             * 这样可以保证用户只要在持续访问 App 接口，登录态就会平滑续期。
             */
            jwtTokenService.refreshToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}

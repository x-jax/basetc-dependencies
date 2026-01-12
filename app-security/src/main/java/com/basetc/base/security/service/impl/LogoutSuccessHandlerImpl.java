package com.basetc.base.security.service.impl;

import com.basetc.base.common.response.R;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.event.LogoutEvent;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.service.SecurityAuthenticateUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.Objects;

/**
 * 登出成功处理器,处理用户登出成功后的响应逻辑.
 *
 * <p>此类实现了 Spring Security 的 {@link org.springframework.security.web.authentication.logout.LogoutSuccessHandler} 接口,
 * 在用户登出成功后被调用,负责:
 * <ul>
 *   <li>清理服务器端的用户会话信息</li>
 *   <li>返回登出成功的响应</li>
 *   <li>记录登出日志</li>
 * </ul>
 *
 * <h3>登出流程:</h3>
 * <pre>{@code
 * 1. 用户发起登出请求 (POST /logout)
 *    ↓
 * 2. Spring Security 处理登出逻辑
 *    ↓
 * 3. 调用 LogoutSuccessHandlerImpl.onLogoutSuccess()
 *    ├─ 清理用户会话 (Redis/Session)
 *    ├─ 清除认证信息
 *    └─ 返回成功响应
 *    ↓
 * 4. 客户端清除本地存储的令牌
 *    ↓
 * 5. 跳转到登录页
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Configuration
 * public class SecurityConfig {
 *
 *     @Bean
 *     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 *         http
 *             .logout()
 *                 .logoutUrl("/logout")
 *                 .logoutSuccessHandler(new LogoutSuccessHandlerImpl())
 *                 .permitAll();
 *
 *         return http.build();
 *     }
 * }
 * }</pre>
 *
 * <h3>前端配合:</h3>
 * <pre>{@code
 * // JavaScript 示例
 * function logout() {
 *     fetch('/logout', { method: 'POST' })
 *         .then(response => response.json())
 *         .then(data => {
 *             if (data.code === 200) {
 *                 // 清除本地存储的令牌
 *                 localStorage.removeItem('token');
 *                 sessionStorage.clear();
 *                 // 跳转到登录页
 *                 window.location.href = '/login';
 *             }
 *         });
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>登出操作会清除服务器端的所有会话信息</li>
 *   <li>客户端也需要清除本地存储的令牌</li>
 *   <li>返回 HTTP 200 状态码表示登出成功</li>
 *   <li>记录登出日志用于安全审计</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.security.web.authentication.logout.LogoutSuccessHandler
 * @see org.springframework.security.web.authentication.logout.LogoutFilter
 */
@Slf4j
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    /**
     * 安全认证配置属性.
     */
    private final BasetcSecurityAuthProperties basetcSecurityAuthProperties;

    /**
     * 安全认证用户服务.
     */
    private final SecurityAuthenticateUserService securityAuthenticateUserService;

    /**
     * 应用上下文.
     */
    private final ApplicationContext applicationContext;

    /**
     * 处理登出成功事件.
     *
     * <p>当用户成功登出时,Spring Security 会调用此方法.
     * 此方法会:
     * <ol>
     *   <li>调用认证服务清理用户会话 (Redis/Session)</li>
     *   <li>记录登出日志</li>
     *   <li>返回 JSON 格式的成功响应</li>
     * </ol>
     *
     * <h3>响应格式:</h3>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json;charset=UTF-8
     *
     * {
     *   "code": 200,
     *   "msg": "操作成功",
     *   "data": null
     * }
     * }</pre>
     *
     * @param request        HTTP 请求对象,可以为 {@code null}
     * @param response       HTTP 响应对象
     * @param authentication 认证信息,登出后可能为 {@code null}
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) throws IOException, ServletException {

        LoginUser loginUser = securityAuthenticateUserService.getLoginUser(request);
        if(Objects.nonNull(loginUser)){
            // 1. 清理用户会话
            securityAuthenticateUserService.logout(loginUser);

            // 2. 登出成功事件 (可选,如果需要在登出成功后执行某些操作)
            applicationContext.publishEvent(new LogoutEvent(loginUser, null));
        }
        // 2. 记录登出日志 (可选,如果需要在日志中记录用户信息)
        // log.info("用户登出成功: {}", authentication != null ? authentication.getName() : "anonymous");

        // 3. 设置响应状态码
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 4. 构建并返回成功响应
        R<Void> result = R.success();
        response.getWriter().write(result.toJsonString());
    }

}

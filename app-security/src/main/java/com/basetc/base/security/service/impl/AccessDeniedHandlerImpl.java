package com.basetc.base.security.service.impl;

import com.basetc.base.common.response.R;
import com.basetc.base.common.utils.IpUtils;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.properties.BasetcSecurityResponseProperties;
import com.basetc.base.security.utils.SecurityUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 访问拒绝处理器,处理权限不足时的响应逻辑.
 *
 * <p>此类实现了 Spring Security 的 {@link org.springframework.security.web.access.AccessDeniedHandler} 接口,
 * 在用户已登录但权限不足以访问资源时被调用.
 *
 * <h3>触发场景:</h3>
 * <ul>
 *   <li>用户访问需要特定权限的接口</li>
 *   <li>用户角色不具有所需的权限标识</li>
 *   <li>使用 {@code @PreAuthorize} 注解权限验证失败</li>
 *   <li>使用 {@code @Secured} 注解权限验证失败</li>
 * </ul>
 *
 * <h3>处理流程:</h3>
 * <pre>{@code
 * 1. 用户请求受保护的资源
 *    ↓
 * 2. Spring Security 检查用户权限
 *    ↓
 * 3. 权限检查失败
 *    ↓
 * 4. 调用 AccessDeniedHandlerImpl.handle()
 *    ├─ 记录访问拒绝日志
 *    ├─ 获取用户信息 (User ID, Username, IP)
 *    ├─ 构建错误响应
 *    └─ 返回 403 响应
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
 *             .exceptionHandling()
 *                 .accessDeniedHandler(new AccessDeniedHandlerImpl());
 *
 *         return http.build();
 *     }
 * }
 * }</pre>
 *
 * <h3>在 Controller 中使用权限注解:</h3>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/admin")
 * public class AdminController {
 *
 *     @PreAuthorize("hasAuthority('admin:delete')")
 *     @DeleteMapping("/user/{id}")
 *     public R<Void> deleteUser(@PathVariable Long id) {
 *         // 需要 admin:delete 权限
 *         userService.deleteUser(id);
 *         return R.success();
 *     }
 *     // 如果用户没有 admin:delete 权限,
 *     // 将触发 AccessDeniedHandlerImpl
 * }
 * }</pre>
 *
 * <h3>响应格式:</h3>
 * <pre>{@code
 * HTTP/1.1 403 Forbidden
 * Content-Type: application/json;charset=UTF-8
 *
 * {
 *   "code": 403,
 *   "msg": "权限不足,无法访问当前资源",
 *   "data": null
 * }
 * }</pre>
 *
 * <h3>日志记录:</h3>
 * <p>此处理器会记录以下信息:
 * <ul>
 *   <li>请求 URI (经过 HTML 转义防止 XSS)</li>
 *   <li>用户 ID (如果已登录)</li>
   *   <li>用户名 (如果已登录)</li>
 *   <li>请求 IP 地址</li>
 * </ul>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>返回的 HTTP 状态码为 403 Forbidden</li>
 *   <li>响应内容由配置文件 {@code basetc.security.auth.access-denied} 定义</li>
 *   <li>所有敏感信息都会记录日志用于安全审计</li>
 *   <li>URI 会被 HTML 转义防止 XSS 攻击</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.security.web.access.AccessDeniedHandler
 * @see org.springframework.security.web.authentication.AuthenticationEntryPoint
 */
@Slf4j
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * 安全认证配置属性.
     */
    private final BasetcSecurityAuthProperties basetcSecurityAuthProperties;

    /**
     * 处理访问拒绝异常.
     *
     * <p>当用户没有足够的权限访问资源时,此方法被调用.
     * 会记录访问拒绝日志并返回配置的错误响应.
     *
     * <h3>处理步骤:</h3>
     * <ol>
     *   <li>获取当前登录用户信息</li>
     *   <li>记录访问拒绝日志 (包含 URI、用户信息、IP 等)</li>
     *   <li>从配置中获取权限不足响应配置</li>
     *   <li>设置响应状态码和内容类型</li>
     *   <li>写入 JSON 格式的错误响应</li>
     * </ol>
     *
     * @param request               HTTP 请求对象
     * @param response              HTTP 响应对象
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       @NonNull AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 获取当前登录用户信息
        LoginUser loginUser = SecurityUtils.getLoginUser();

        // 记录访问拒绝日志 (HTML 转义 URI 防止 XSS 攻击)
        log.info("[AccessDeniedHandler] 权限不足访问拒绝, URI: {}, UserId: {}, Username: {}, IP: {}",
                HtmlUtils.htmlEscape(request.getRequestURI()),
                Objects.nonNull(loginUser) ? loginUser.getUserId() : "",
                Objects.nonNull(loginUser) ? loginUser.getUsername() : "",
                IpUtils.getIpAddr(request)
        );

        // 获取配置的权限不足响应
        BasetcSecurityResponseProperties accessDenied = basetcSecurityAuthProperties.getAccessDenied();

        // 设置响应状态码和内容类型
        response.setStatus(accessDenied.getHttpCode());
        response.setContentType(accessDenied.getContentType());
        response.setCharacterEncoding("UTF-8");

        // 构建并返回错误响应
        R<Void> error = R.error(accessDenied.getBody().getCode(), accessDenied.getBody().getMsg());
        response.getWriter().write(error.toJsonString());
    }

}

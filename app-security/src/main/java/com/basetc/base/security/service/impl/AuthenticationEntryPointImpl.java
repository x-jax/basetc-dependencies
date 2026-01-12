package com.basetc.base.security.service.impl;

import com.basetc.base.common.response.R;
import com.basetc.base.common.utils.IpUtils;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.properties.BasetcSecurityResponseProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/**
 * 认证入口处理器,处理用户未登录或认证无效时的响应逻辑.
 *
 * <p>此类实现了 Spring Security 的 {@link org.springframework.security.web.AuthenticationEntryPoint} 接口,
 * 在用户未登录或认证信息无效时被调用.
 *
 * <h3>触发场景:</h3>
 * <ul>
 *   <li>用户未提供访问令牌</li>
   <li>访问令牌已过期</li>
   <li>访问令牌格式错误</li>
 *   <li>访问令牌验证失败</li>
 *   <li>访问令牌对应的会话不存在</li>
 * </ul>
 *
 * <h3>处理流程:</h3>
 * <pre>{@code
 * 1. 用户请求受保护的资源
 *    ↓
 * 2. Spring Security 检查用户认证状态
 *    ↓
 *   *3. 认证失败 (未登录或令牌无效)
 *    ↓
 * 4. 调用 AuthenticationEntryPointImpl.commence()
 *    ├─ 记录未登录访问日志
 *    ├─ 获取请求信息 (URI, IP)
 *    ├─ 构建错误响应
 *    └─ 返回 401 响应
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
 *                 .authenticationEntryPoint(new AuthenticationEntryPointImpl());
 *
 *         return http.build();
 *     }
 * }
 * }</pre>
 *
 * <h3>典型使用场景:</h3>
 * <pre>{@code
 * // 场景 1: 用户直接访问需要登录的接口
 * GET /api/user/profile
 * // 没有 Authorization Header
 * // -> 触发 AuthenticationEntryPointImpl
 * // -> 返回 401 Unauthorized
     *
     * // 场景 2: 用户令牌已过期
     * GET /api/user/profile
     * Authorization: Bearer expired_token
     * // -> 触发 AuthenticationEntryPointImpl
     * // -> 返回 401 Unauthorized
     *
     * // 场景 3: 用户令牌格式错误
     * GET /api/user/profile
     * Authorization: InvalidFormat token
     * // -> 触发 AuthenticationEntryPointImpl
     * // -> 返回 401 Unauthorized
     * }</pre>
     *
     * <h3>响应格式:</h3>
     * <pre>{@code
     * HTTP/1.1 401 Unauthorized
     * Content-Type: application/json;charset=UTF-8
     *
     * {
     *   "code": 401,
     *   "msg": "当前资源无法访问,请登录",
     *   "data": null
     * }
     * }</pre>
     *
     * <h3>与 AccessDeniedHandler 的区别:</h3>
     * <table border="1">
     *   <tr>
     *     <th>处理器</th>
     *     <th>触发条件</th>
     *     <th>HTTP 状态码</th>
     *     <th>典型场景</th>
     *   </tr>
     *   <tr>
     *     <td>AuthenticationEntryPoint</td>
     *     *   <td>未登录或令牌无效</td>
     *     *   <td>401 Unauthorized</td>
     *     *   <td>用户未登录、令牌过期</td>
     *   </tr>
     *   <tr>
     *     <td>AccessDeniedHandler</td>
     *     *   <td>已登录但权限不足</td>
     *     *   <td>403 Forbidden</td>
     *     *   <td>用户角色权限不够</td>
     *   </tr>
     * </table>
     *
     * <h3>日志记录:</h3>
     * <p>此处理器会记录以下信息:
     * <ul>
     *   <li>请求 URI (经过 HTML 转义防止 XSS)</li>
     *   <li>请求 IP 地址</li>
     *   <li>认证异常信息 (可选)</li>
     * </ul>
     *
     * <h3>注意事项:</h3>
     * <ul>
     *   <li>返回的 HTTP 状态码为 401 Unauthorized</li>
     *   <li>响应内容由配置文件 {@code basetc.security.auth.un-authorized} 定义</li>
     *   <li>URI 会被 HTML 转义防止 XSS 攻击</li>
     *   <li>所有未授权访问都会被记录日志用于安全审计</li>
     * </ul>
     *
     * @author Liu,Dongdong
     * @since 1.0.0
     * @see org.springframework.security.web.AuthenticationEntryPoint
     * @see org.springframework.security.web.access.AccessDeniedHandler
     */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    /**
     * 安全认证配置属性.
     */
    private final BasetcSecurityAuthProperties basetcSecurityAuthProperties;

    /**
     * 处理认证异常.
     *
     * <p>当用户未认证或认证信息无效时,此方法被调用.
     * 会记录未授权访问日志并返回配置的错误响应.
     *
     * <h3>处理步骤:</h3>
     * <ol>
     *   <li>记录未授权访问日志 (包含 URI、IP 等)</li>
     *   <li>从配置中获取未授权响应配置</li>
     *   <li>设置响应状态码和内容类型</li>
     *   <li>写入 JSON 格式的错误响应</li>
     * </ol>
     *
     * <h3>日志记录:</h3>
     * <pre>{@code
     * [AuthenticationEntryPoint] 未登录访问, URI: /api/user/profile, IP: 192.168.1.100
     * }</pre>
     *
     * @param request       HTTP 请求对象
     * @param response      HTTP 响应对象
     * @param authException 认证异常,可能为 {@code null}
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        // 记录未授权访问日志 (HTML 转义 URI 防止 XSS 攻击)
        log.info("[AuthenticationEntryPoint] 未登录访问, URI: {}, IP: {}",
                HtmlUtils.htmlEscape(request.getRequestURI()),
                IpUtils.getIpAddr(request)
        );

        // 获取配置的未授权响应
        BasetcSecurityResponseProperties unAuthorized = basetcSecurityAuthProperties.getUnAuthorized();

        // 设置响应状态码和内容类型
        response.setStatus(unAuthorized.getHttpCode());
        response.setContentType(unAuthorized.getContentType());
        response.setCharacterEncoding("UTF-8");

        // 构建并返回错误响应
        R<Void> error = R.error(unAuthorized.getBody().getCode(), unAuthorized.getBody().getMsg());
        response.getWriter().write(error.toJsonString());
    }

}

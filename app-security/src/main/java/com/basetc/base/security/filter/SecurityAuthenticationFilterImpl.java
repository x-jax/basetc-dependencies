package com.basetc.base.security.filter;

import com.basetc.base.common.exception.BasetcException;
import com.basetc.base.common.utils.IpUtils;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.enums.BasetcSecurityAuthFilter;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.service.SecurityAuthenticateUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 安全认证过滤器实现类,提供具体的安全认证逻辑.
 *
 * <p>此过滤器是 Security 模块的核心组件,负责:
 * <ul>
 *   <li>从请求中提取访问令牌</li>
 *   <li>验证令牌有效性并加载用户信息</li>
 *   <li>执行安全检查 (IP 地址、User-Agent 等)</li>
 *   <li>将认证信息设置到 SecurityContext</li>
 * </ul>
 *
 * <h3>过滤流程:</h3>
 * <pre>{@code
 * HTTP 请求
 *    ↓
 * 提取访问令牌 (从 Authorization Header)
 *    ↓
 * 验证令牌有效性
 *    ↓
 * 从 Redis/Session 加载用户信息
 *    ↓
 * 执行安全检查
 *    ├─ IP 地址检查 (如果启用)
 *    ├─ User-Agent 检查 (如果启用)
 *    └─ 单点登录检查 (如果启用)
 *    ↓
 * 设置认证信息到 SecurityContext
 *    ↓
 * 继续执行过滤器链
 * }</pre>
 *
 * <h3>安全检查策略:</h3>
 * <p>当安全检查失败时,有两种处理策略:
 * <ul>
 *   <li><b>EXCEPTION (抛出异常)</b>: 立即登出用户并抛出异常,阻止请求继续</li>
 *   <li><b>SKIP (跳过检查)</b>: 不设置认证信息,但允许请求继续 (适用于匿名访问)</li>
 * </ul>
 *
 * <h3>配置示例:</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     auth:
 *       filter:
 *         single-enabled: true    # 启用单点登录
 *         ip-enabled: true         # 启用 IP 检查
 *         user-agent-enabled: true # 启用 User-Agent 检查
 * }</pre>
 *
 * <h3>日志记录:</h3>
 * <p>此过滤器会记录以下安全事件:
 * <ul>
 *   <li>IP 地址校验失败</li>
 *   <li>User-Agent 校验失败</li>
 *   <li>令牌无效或过期</li>
 * </ul>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>IP 检查在代理环境 (如 Nginx) 中需要正确配置 X-Real-IP 头</li>
 *   <li>User-Agent 检查可能影响用户体验,谨慎启用</li>
 *   <li>单点登录功能需要 Redis 或 Session 支持</li>
 *   <li>校验失败会自动登出用户,清除会话信息</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see SecurityAuthenticationFilter
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityAuthenticationFilterImpl extends SecurityAuthenticationFilter {

    /**
     * 安全认证用户服务,用于获取当前登录用户信息.
     */
    private final SecurityAuthenticateUserService securityAuthenticateUserService;

    /**
     * 安全认证配置属性,用于获取过滤器配置.
     */
    private final BasetcSecurityAuthProperties basetcSecurityAuthProperties;

    /**
     * 执行过滤器内部逻辑.
     *
     * <p>此方法在每个请求到达时被调用,负责:
     * <ol>
     *   <li>从请求中获取当前登录用户信息</li>
     *   <li>执行 IP 地址校验 (如果启用)</li>
     *   <li>执行 User-Agent 校验 (如果启用)</li>
     *   <li>将认证信息设置到 SecurityContext</li>
     *   <li>继续执行过滤器链</li>
     * </ol>
     *
     * <h3>IP 地址校验:</h3>
     * <p>如果启用了 IP 校验,会比较当前请求的 IP 与登录时的 IP 是否一致。
     * 如果不一致,根据配置决定是否抛出异常。
     *
     * <h3>User-Agent 校验:</h3>
     * <p>如果启用了 User-Agent 校验,会比较当前请求的 User-Agent 与登录时的是否一致。
     * 如果不一致,根据配置决定是否抛出异常。
     *
     * <h3>处理策略:</h3>
     * <ul>
     *   <li>{@link BasetcSecurityAuthFilter#EXCEPTION}: 校验失败时登出并抛出异常</li>
     *   <li>{@link BasetcSecurityAuthFilter#SKIP}: 校验失败时不设置认证信息,允许请求继续</li>
     * </ul>
     *
     * @param request     HTTP 请求对象
     * @param response    HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 获取当前登录用户信息
        LoginUser loginUser = securityAuthenticateUserService.getLoginUser(request);
        if (Objects.nonNull(loginUser)) {

            // 获取过滤器配置
            final BasetcSecurityAuthProperties.Filter filter = basetcSecurityAuthProperties.getFilter();

            // 判断是否需要抛出异常
            boolean shouldThrowException = Objects.equals(BasetcSecurityAuthFilter.EXCEPTION, filter.getAction());
            boolean allChecksPassed = true;

            // IP 地址校验
            if (filter.isIpEnabled()) {
                String currentIp = IpUtils.getIpAddr(request);
                String loginIp = loginUser.getLoginIp();

                if (!Objects.equals(loginIp, currentIp)) {
                    log.info("[IP地址校验失败] 当前用户ID: {}, username: {}, tokenIp: {}, 当前IP: {}",
                            loginUser.getUserId(), loginUser.getUsername(), loginIp, currentIp);

                    if (shouldThrowException) {
                        // 如果校验失败且需要抛出异常，则退出登录并抛出异常
                        securityAuthenticateUserService.logout(request);
                        throw new BasetcException(401, "当前账号IP地址发生变动,请重新登录");
                    } else {
                        // 如果校验失败但不需要抛出异常，则设置标志位为false
                        allChecksPassed = false;
                    }
                }
            }

            // User-Agent 校验
            if (allChecksPassed && filter.isUserAgentEnabled()) {
                String currentUserAgent = request.getHeader("User-Agent");
                String loginUserAgent = loginUser.getRequestUserAgent();

                if (!Objects.equals(loginUserAgent, currentUserAgent)) {
                    log.info("[UserAgent校验失败] 当前用户ID: {}, username: {}, tokenUserAgent: {}, 当前UserAgent: {}",
                            loginUser.getUserId(), loginUser.getUsername(), loginUserAgent, currentUserAgent);

                    if (shouldThrowException) {
                        // 如果校验失败且需要抛出异常，则退出登录并抛出异常
                        securityAuthenticateUserService.logout(request);
                        throw new BasetcException(401, "当前账号浏览器发生变动,请重新登录");
                    } else {
                        // 如果校验失败但不需要抛出异常，则设置标志位为false
                        allChecksPassed = false;
                    }
                }
            }

            // 如果所有校验都通过，则设置认证信息到安全上下文中
            if (allChecksPassed) {
                final UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

}

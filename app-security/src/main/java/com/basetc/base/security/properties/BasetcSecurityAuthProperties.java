package com.basetc.base.security.properties;

import com.basetc.base.security.enums.BasetcSecurityAuthFilter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全认证配置属性类,定义认证相关的配置参数.
 *
 * <p>此类负责配置用户认证相关的核心参数,包括:
 * <ul>
 *   <li>登录和登出 URL 配置</li>
 *   <li>白名单路径配置</li>
 *   <li>CSRF 保护开关</li>
 *   <li>过滤器链配置 (单点登录、IP 限制、User-Agent 限制)</li>
 *   <li>未授权和权限不足响应配置</li>
 * </ul>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     auth:
 *       auto-configure: true
 *       logout-url: /logout
 *       csrf-enabled: false
 *       white-list:
 *         - /api/public/**
 *         - /auth/login
 *         - /auth/register
 *         - /swagger-ui/**
 *         - /v3/api-docs/**
 *       filter:
 *         single-enabled: true
 *         ip-enabled: true
 *         user-agent-enabled: false
 * }</pre>
 *
 * <h3>白名单配置说明:</h3>
 * <p>白名单中的路径无需认证即可访问,支持 Ant 风格的路径匹配符:
 * <ul>
 *   <li>{@code *} - 匹配 0 级或多级目录</li>
 *   <li>{@code **} - 匹配多级目录</li>
 *   <li>{@code ?} - 匹配单个字符</li>
 * </ul>
 *
 * <p>常见白名单路径示例:
 * <pre>{@code
 * white-list:
 *   - /api/public/**           # 所有 /api/public/ 下的路径
 *   - /auth/login              # 登录接口
 *   - /auth/register           # 注册接口
 *   - /swagger-ui/**           # Swagger UI
 *   - /v3/api-docs/**          # OpenAPI 文档
 *   - /actuator/health         # 健康检查
 *   - /static/**               # 静态资源
 * }</pre>
 *
 * <h3>过滤器配置说明:</h3>
 * <ul>
 *   <li><b>single-enabled (单点登录)</b>: 启用后,同一用户只能有一个有效登录,新的登录会使旧登录失效</li>
 *   <li><b>ip-enabled (IP 限制)</b>: 启用后,会验证请求 IP 是否与登录时的 IP 一致</li>
 *   <li><b>user-agent-enabled (User-Agent 限制)</b>: 启用后,会验证请求的 User-Agent 是否与登录时一致</li>
 * </ul>
 *
 * <h3>CSRF 保护:</h3>
 * <p>默认禁用 CSRF 保护。如果需要启用:
 * <pre>{@code
 * basetc:
 *   security:
 *     auth:
 *       csrf-enabled: true
 * }</pre>
 *
 * <p>启用后,所有修改状态的请求 (POST, PUT, DELETE) 都需要携带 CSRF Token。
 * 通常前后端分离的项目使用 JWT Token,不需要 CSRF 保护。
 *
 * <h3>自定义响应配置:</h3>
 * <p>可以通过配置自定义未授权和权限不足的响应:
 * <pre>{@code
 * basetc:
 *   security:
 *     auth:
 *       un-authorized:
 *         http-code: 401
 *         content-type: application/json
 *         body:
 *           code: 401
 *           msg: "请先登录"
 *       access-denied:
 *         http-code: 403
 *         content-type: application/json
 *         body:
 *           code: 403
 *           msg: "权限不足"
 * }</pre>
 *
 * <h3>在代码中使用配置:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class AuthService {
 *
 *     private final BasetcSecurityAuthProperties authProperties;
 *
 *     public boolean isWhitelisted(String requestPath) {
 *         return authProperties.getWhiteList().stream()
 *             .anyMatch(pattern -> antPathMatcher.match(pattern, requestPath));
 *     }
 *
 *     public boolean isIpCheckEnabled() {
 *         return authProperties.getFilter().isIpEnabled();
 *     }
 *
 *     public String getLogoutUrl() {
 *         return authProperties.getLogoutUrl();
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>白名单配置的路径必须以 {@code /} 开头</li>
 *   <li>单点登录功能需要 Redis 或 Session 支持</li>
 *   <li>IP 限制功能在代理环境 (如 Nginx) 中需要正确配置 X-Real-IP 或 X-Forwarded-For 头</li>
 *   <li>User-Agent 限制可用于防止 Session 劫持,但可能影响用户体验</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see BasetcSecurityProperties
 * @see BasetcSecurityResponseProperties
 */
@Data
@ConfigurationProperties(prefix = "basetc.security.auth")
public class BasetcSecurityAuthProperties {

    /**
     * 是否自动配置认证模块.
     *
     * <p>设置为 {@code false} 时,需要手动配置 Spring Security 的认证相关组件.
     *
     * <p>默认值为 {@code true}.
     */
    private boolean autoConfigure = true;

    /**
     * 登出 URL.
     *
     * <p>用户访问此 URL 时会触发登出操作,清除用户的认证信息.
     * 支持 POST 和 GET 请求.
     *
     * <p>默认值为 {@code /logout}.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 前端调用登出接口
     * fetch('/logout', { method: 'POST' })
     *   .then(response => {
     *     // 清除本地存储的 token
     *     localStorage.removeItem('token');
     *     // 跳转到登录页
     *     window.location.href = '/login';
     *   });
     * }</pre>
     *
     * @see org.springframework.security.web.authentication.logout.LogoutFilter
     */
    private String logoutUrl = "/logout";

    /**
     * 是否开启 CSRF 保护.
     *
     * <p>CSRF (Cross-Site Request Forgery) 跨站请求伪造保护.
     * 启用后,所有修改状态的请求都需要携带 CSRF Token.
     *
     * <p>对于使用 JWT Token 的前后端分离项目,通常不需要 CSRF 保护,
     * 因为 JWT Token 本身已经提供了足够的防护.
     *
     * <p>默认值为 {@code false}.
     *
     * @see org.springframework.security.web.csrf.CsrfFilter
     */
    private boolean csrfEnabled = false;

    /**
     * 白名单路径列表.
     *
     * <p>白名单中的路径无需认证即可访问,支持 Ant 风格的路径匹配符.
     *
     * <p>常见白名单路径:
     * <ul>
     *   <li>登录接口: {@code /auth/login}</li>
     *   <li>注册接口: {@code /auth/register}</li>
     *   <li>公共接口: {@code /api/public/**}</li>
     *   <li>静态资源: {@code /static/**}</li>
     *   <li>健康检查: {@code /actuator/health}</li>
     *   <li>API 文档: {@code /swagger-ui/**}, {@code /v3/api-docs/**}</li>
     * </ul>
     *
     * <p>默认值为空列表.
     *
     * <h3>配置示例:</h3>
     * <pre>{@code
     * white-list:
     *   - /api/public/**
     *   - /auth/login
     *   - /swagger-ui/**
     *   - /actuator/health
     * }</pre>
     *
     */
    private List<String> whiteList = new ArrayList<>();

    /**
     * 登录过滤器配置.
     *
     * <p>用于配置单点登录、IP 限制、User-Agent 限制等功能.
     *
     * @see Filter
     */
    @NestedConfigurationProperty
    private Filter filter = new Filter();

    /**
     * 未授权响应配置.
     *
     * <p>当用户未登录或 Token 无效时返回的响应信息.
     *
     * <p>默认配置:
     * <ul>
     *   <li>HTTP 状态码: 401</li>
     *   <li>业务码: 401</li>
     *   <li>消息: "当前资源无法访问,请登录"</li>
     * </ul>
     *
     * @see BasetcSecurityResponseProperties
     * @see org.springframework.security.web.AuthenticationEntryPoint
     */
    @NestedConfigurationProperty
    private BasetcSecurityResponseProperties unAuthorized = new BasetcSecurityResponseProperties() {
        {
            setHttpCode(401);
            setContentType("application/json");
            setBody(new Body() {
                {
                    setCode(401);
                    setMsg("当前资源无法访问,请登录");
                }
            });
        }
    };

    /**
     * 权限不足响应配置.
     *
     * <p>当用户已登录但权限不足时返回的响应信息.
     *
     * <p>默认配置:
     * <ul>
     *   <li>HTTP 状态码: 403</li>
     *   <li>业务码: 403</li>
     *   <li>消息: "权限不足,无法访问当前资源"</li>
     * </ul>
     *
     * @see BasetcSecurityResponseProperties
     * @see org.springframework.security.web.access.AccessDeniedHandler
     */
    @NestedConfigurationProperty
    private BasetcSecurityResponseProperties accessDenied = new BasetcSecurityResponseProperties() {
        {
            setHttpCode(403);
            setContentType("application/json");
            setBody(new Body() {
                {
                    setCode(403);
                    setMsg("权限不足,无法访问当前资源");
                }
            });
        }
    };

    /**
     * 登录过滤器配置内部类.
     *
     * <p>用于配置各种登录后的安全检查策略.
     *
     * @author Liu,Dongdong
     * @since 1.0.0
     */
    @Data
    public static class Filter {

        private BasetcSecurityAuthFilter action = BasetcSecurityAuthFilter.NONE;

        /**
         * 是否启用单点登录.
         *
         * <p>启用后,同一用户只能有一个有效登录.
         * 当用户在新设备登录时,旧设备的登录会自动失效.
         *
         * <p>此功能需要 Redis 或 Session 支持.
         * 使用 Redis 时,会为每个用户维护一个唯一的 Token ID;
         * 使用 Session 时,会使旧 Session 失效.
         *
         * <p>默认值为 {@code false}.
         *
         * <h3>启用单点登录的配置:</h3>
         * <pre>{@code
         * basetc:
         *   security:
         *     auth:
         *       filter:
         *         single-enabled: true
         * }</pre>
         *
         * <h3>实现原理:</h3>
         * <ol>
         *   <li>用户登录时,生成唯一的 Token ID (UUID)</li>
         *   <li>将 Token ID 存储到 Redis/Session 中: {@code user:token:id:userId = tokenId}</li>
         *   <li>每次请求时,验证请求中的 Token ID 是否与存储的一致</li>
         *   <li>新的登录会更新 Token ID,使旧的 Token 失效</li>
         * </ol>
         */
        private boolean singleEnabled = false;

        /**
         * 是否覆盖旧登录.
         *
         * <p>启用后,当用户在多个设备上登录时,旧登录会被覆盖.
         *
         * <p>此功能需要 Redis 或 Session 支持.
         * 使用 Redis 时,会为每个用户维护一个唯一的 Token ID;
         * 使用 Session 时,会使旧 Session 失效.
         *
         * <p>默认值为 {@code false}.
         *
         * <h3>启用覆盖旧登录的配置:</h3>
         * <pre>{@code
         * basetc:
         *   security:
         *     auth:
         *       filter:
         *         overwrite-old-auth: true
         * }</pre>
         */
        private boolean overwriteOldAuth = false;

        /**
         * 是否启用 IP 地址访问控制.
         *
         * <p>启用后,会验证请求的 IP 地址是否与登录时的 IP 地址一致.
         * 如果不一致,则拒绝访问.
         *
         * <p>此功能可用于防止 Session 劫持和 Token 盗用.
         *
         * <p>默认值为 {@code false}.
         *
         * <h3>注意事项:</h3>
         * <ul>
         *   <li>在代理环境 (如 Nginx) 中,需要正确配置 X-Real-IP 或 X-Forwarded-For 头</li>
         *   <li>如果用户使用移动网络,IP 可能会频繁变化,需要谨慎启用</li>
         *   <li>可以配置 IP 白名单,允许部分 IP 变化</li>
         * </ul>
         *
         * <h3>Nginx 配置示例:</h3>
         * <pre>{@code
         * location / {
         *     proxy_set_header X-Real-IP $remote_addr;
         *     proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         *     proxy_pass http://backend;
         * }
         * }</pre>
         */
        private boolean ipEnabled = false;

        /**
         * 是否启用 User-Agent 访问控制.
         *
         * <p>启用后,会验证请求的 User-Agent 是否与登录时的 User-Agent 一致.
         * 如果不一致,则拒绝访问.
         *
         * <p>此功能可用于防止 Token 在不同浏览器或设备间被滥用.
         *
         * <p>默认值为 {@code false}.
         *
         * <h3>注意事项:</h3>
         * <ul>
         *   <li>User-Agent 可能会被浏览器更新或插件修改</li>
         *   <li>某些隐私浏览器会随机生成 User-Agent</li>
         *   <li>建议与 IP 限制配合使用,而不是单独使用</li>
         * </ul>
         *
         * <h3>User-Agent 示例:</h3>
         * <pre>{@code
         * Chrome:  Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
         * Firefox: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0
         * Safari:  Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15
         * }</pre>
         */
        private boolean userAgentEnabled = false;

    }

}

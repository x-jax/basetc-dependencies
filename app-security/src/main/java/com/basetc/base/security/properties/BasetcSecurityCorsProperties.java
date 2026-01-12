package com.basetc.base.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全 CORS 配置属性类,定义跨域资源共享的相关配置参数.
 *
 * <p>CORS (Cross-Origin Resource Sharing) 跨域资源共享是一种基于 HTTP 头的机制,
 * 允许服务器标示除了它自己以外的其他源 (域、协议或端口),浏览器应该允许从这些源加载资源.
 *
 * <p>此类用于配置前后端分离项目中的跨域访问策略,包括允许的源、方法、头部等.
 *
 * <h3>同源策略 vs CORS:</h3>
 * <ul>
 *   <li><b>同源策略</b>: 浏览器的安全机制,限制一个源的文档或脚本如何与另一个源的资源进行交互</li>
 *   <li><b>CORS</b>: 放宽同源策略的限制,允许跨域访问受控的资源</li>
 * </ul>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     cors:
 *       enabled: true
 *       allowed-origin-patterns:
 *         - http://localhost:3000
 *         - https://example.com
 *         - https://*.example.com
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *         - OPTIONS
 *       allowed-headers: "*"
 *       allow-credentials: true
 *       max-age: 3600
 * }</pre>
 *
 * <h3>配置示例 (application.properties):</h3>
 * <pre>{@code
 * basetc.security.cors.enabled=true
 * basetc.security.cors.allowed-origin-patterns=http://localhost:3000,https://example.com
 * basetc.security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
 * basetc.security.cors.allowed-headers=*
 * basetc.security.cors.allow-credentials=true
 * basetc.security.cors.max-age=3600
 * }</pre>
 *
 * <h3>简单请求 vs 预检请求:</h3>
 * <p><b>简单请求</b>满足以下所有条件:
 * <ul>
 *   <li>方法: GET, HEAD, POST</li>
 *   <li>头部: Accept, Accept-Language, Content-Language, Content-Type</li>
 *   <li>Content-Type: application/x-www-form-urlencoded, multipart/form-data, text/plain</li>
 * </ul>
 *
 * <p><b>预检请求</b> (OPTIONS) 不满足简单请求的条件时,
 * 浏览器会先发送 OPTIONS 请求,服务器响应允许后,再发送实际请求.
 *
 * <h3>常见跨域场景:</h3>
 * <pre>{@code
 * 前端: http://localhost:3000
 * 后端: http://localhost:8080
 *
 * # 跨域! (端口不同)
 *
 * 前端: https://www.example.com
 * 后端: https://api.example.com
 *
 * # 跨域! (子域名不同)
 *
 * 前端: http://example.com
 * 后端: https://example.com
 *
 * # 跨域! (协议不同)
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>{@code allow-credentials=true} 时, {@code allowed-origin-patterns} 不能使用 {@code *}</li>
 *   <li>生产环境不要使用 {@code *},应该明确指定允许的源</li>
 *   <li>预检请求的缓存时间 ({@code max-age}) 可以减少 OPTIONS 请求</li>
 *   <li>如果使用 Nginx,也可以在 Nginx 中配置 CORS</li>
 *   <li>前端不需要配置 CORS,只需后端配置</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.web.bind.annotation.CrossOrigin
 * @see org.springframework.web.cors.CorsConfiguration
 */
@Data
@ConfigurationProperties(prefix = "basetc.security.cors")
public class BasetcSecurityCorsProperties {

    /**
     * 是否启用 CORS 支持.
     *
     * <p>设置为 {@code true} 时,会自动配置 CORS 过滤器,允许跨域请求.
     * 前后端分离项目必须启用.
     *
     * <p>默认值为 {@code false}.
     *
     * <h3>启用 CORS 的场景:</h3>
     * <ul>
     *   <li>前后端分离项目 (前端和后端部署在不同的域或端口)</li>
     *   <li>微服务架构 (服务之间跨域调用)</li>
     *   <li>第三方集成 (允许其他网站访问 API)</li>
     * </ul>
     *
     * <h3>配置示例:</h3>
     * <pre>{@code
     * # 开发环境: 启用 CORS
     * basetc:
     *   security:
     *     cors:
     *       enabled: true
     *
     * # 生产环境: 根据需要启用
     * basetc:
     *   security:
     *     cors:
     *       enabled: ${CORS_ENABLED:false}
     * }</pre>
     */
    private boolean enabled = false;

    /**
     * 允许访问的源模式列表.
     *
     * <p>支持 Ant 风格的路径匹配符:
     * <ul>
     *   <li>{@code *} - 匹配任意字符</li>
     *   <li>{@code ?} - 匹配单个字符</li>
     *   <li>{@code **} - 匹配多级路径 (仅在路径中有效)</li>
     * </ul>
     *
     * <p>默认值为 {@code ["*"]},表示允许所有源.
     * <b>生产环境不应该使用 {@code *},应该明确指定允许的源.</b>
     *
     * <h3>源模式示例:</h3>
     * <pre>{@code
     * # 允许所有源 (不推荐用于生产环境)
     * allowed-origin-patterns: "*"
     *
     * # 允许特定源
     * allowed-origin-patterns:
     *   - http://localhost:3000
     *   - https://example.com
     *   - https://www.example.com
     *
     * # 使用通配符
     * allowed-origin-patterns:
     *   - https://*.example.com     # 允许所有子域名
     *   - http://localhost:*       # 允许所有端口
     *
     * # 正则表达式 (Spring 5.3+)
     * allowed-origin-patterns:
     *   - "https://\\.example\\.com"  # 正则匹配
     * }</pre>
     *
     * <h3>注意事项:</h3>
     * <ul>
     *   <li>当 {@code allow-credentials=true} 时,不能使用 {@code *}</li>
     *   <li>源必须包含协议 (http 或 https)</li>
     *   <li>IP 地址和域名都可以作为源</li>
     *   <li>浏览器不允许跨子域名访问,除非配置了通配符</li>
     * </ul>
     *
     * @see #allowCredentials
     */
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));

    /**
     * 允许的 HTTP 方法列表.
     *
     * <p>指定哪些 HTTP 方法可以用于跨域请求.
     *
     * <p>默认值为 {@code ["*"]},表示允许所有方法.
     *
     * <h3>常见 HTTP 方法:</h3>
     * <ul>
     *   <li>GET - 获取资源</li>
     *   <li>POST - 创建资源</li>
     *   <li>PUT - 更新资源</li>
     *   <li>PATCH - 部分更新资源</li>
     *   <li>DELETE - 删除资源</li>
     *   <li>OPTIONS - 预检请求</li>
     *   <li>HEAD - 获取响应头</li>
     * </ul>
     *
     * <h3>配置示例:</h3>
     * <pre>{@code
     * # 允许所有方法
     * allowed-methods: "*"
     *
     * # 允许常用方法
     * allowed-methods:
     *   - GET
     *   - POST
     *   - PUT
     *   - DELETE
     *   - OPTIONS
     *
     * # 只读 API
     * allowed-methods:
     *   - GET
     *   - HEAD
     * }</pre>
     */
    private List<String> allowedMethods = new ArrayList<>(List.of("*"));

    /**
     * 允许的请求头列表.
     *
     * <p>指定哪些 HTTP 请求头可以用于跨域请求.
     *
     * <p>默认值为 {@code ["*"]},表示允许所有请求头.
     *
     * <h3>常见请求头:</h3>
     * <ul>
     *   <li>Authorization - 认证信息 (Bearer Token)</li>
     *   <li>Content-Type - 请求内容类型</li>
     *   <li>Accept - 可接受的响应类型</li>
     *   <li>Origin - 请求的源</li>
     *   <li>User-Agent - 用户代理</li>
     *   <li>X-Requested-With - XMLHttpRequest 标识</li>
     * </ul>
     *
     * <h3>配置示例:</h3>
     * <pre>{@code
     * # 允许所有请求头
     * allowed-headers: "*"
     *
     * # 允许常用请求头
     * allowed-headers:
     *   - Authorization
     *   - Content-Type
     *   - Accept
     *   - Origin
     *   - X-Requested-With
     *
     * # 自定义请求头
     * allowed-headers:
     *   - "*"
     *   - X-Custom-Header
     * }</pre>
     *
     * <h3>注意事项:</h3>
     * <ul>
     *   <li>简单请求头 (Accept, Accept-Language, Content-Language, Content-Type) 始终允许</li>
     *   <li>自定义请求头必须在此列表中声明</li>
     *   <li>预检请求会检查此列表</li>
     * </ul>
     */
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    /**
     * 是否允许携带凭证.
     *
     * <p>凭证包括 Cookies、Authorization headers、TLS client certificates 等.
     *
     * <p>默认值为 {@code true}.
     *
     * <p><b>重要:</b> 当设置为 {@code true} 时:
     * <ul>
     *   <li>{@code allowed-origin-patterns} 不能使用 {@code *}</li>
     *   <li>必须明确指定允许的源</li>
     * </ul>
     *
     * <h3>使用场景:</h3>
     * <pre>{@code
     * # 场景 1: 使用 Cookie 认证
     * allow-credentials: true
     * allowed-origin-patterns:
     *   - http://localhost:3000
     *
     * # 场景 2: 使用 JWT Token (在 Authorization header 中)
     * allow-credentials: true
     * allowed-origin-patterns:
     *   - https://example.com
     *
     * # 场景 3: 公共 API (不需要认证)
     * allow-credentials: false
     * allowed-origin-patterns: "*"
     * }</pre>
     *
     * <h3>前端配置:</h3>
     * <pre>{@code
     * // JavaScript fetch API
     * fetch('https://api.example.com/data', {
     *     credentials: 'include'  // 包含凭证
     * });
     *
     * // Axios
     * axios.defaults.withCredentials = true;
     * }</pre>
     *
     * @see #allowedOriginPatterns
     */
    private Boolean allowCredentials = true;

    /**
     * 预检请求的有效期.
     *
     * <p>指定预检请求 (OPTIONS) 的响应可以被缓存多长时间.
     * 单位为秒.
     *
     * <p>默认值为 {@code 3600} 秒 (1 小时).
     *
     * <h3>预检请求流程:</h3>
     * <ol>
     *   <li>浏览器发送 OPTIONS 请求</li>
     *   <li>服务器返回允许的方法、头部等</li>
     *   <li>浏览器缓存响应 {@code max-age} 秒</li>
     *   <li>在缓存期内,不再发送预检请求</li>
     *   <li>缓存期后,重新发送预检请求</li>
     * </ol>
     *
     * <h3>配置建议:</h3>
     * <pre>{@code
     * # 开发环境: 较短的缓存时间 (便于调试)
     * max-age: 60
     *
     * # 生产环境: 较长的缓存时间 (减少请求)
     * max-age: 3600
     *
     * # 不缓存 (每次都预检)
     * max-age: 0
     * }</pre>
     *
     * <h3>响应头示例:</h3>
     * <pre>{@code
     * Access-Control-Max-Age: 3600
     * }</pre>
     *
     * <h3>注意事项:</h3>
     * <ul>
     *   <li>浏览器有最大缓存时间限制 (通常为 86400 秒)</li>
     *   <li>设置为 {@code 0} 表示不缓存</li>
     *   <li>设置为 {@code -1} 表示禁用缓存</li>
     * </ul>
     */
    private Long maxAge = 3600L;

}

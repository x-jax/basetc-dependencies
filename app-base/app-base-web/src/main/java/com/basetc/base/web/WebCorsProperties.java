package com.basetc.base.web;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Web模块CORS跨域配置属性类.
 *
 * <p>此类通过Spring Boot的 {@code @ConfigurationProperties} 注解自动绑定配置文件中的CORS相关配置.
 * <p>配置前缀为 {@code basetc.web.cors},用于精细控制跨域访问策略.
 *
 * <h3>配置属性说明:</h3>
 * <ul>
 *   <li><b>autoConfigure:</b> 是否启用CORS自动配置</li>
 *   <li><b>allowedOriginPatterns:</b> 允许的源模式,支持通配符</li>
 *   <li><b>allowedMethods:</b> 允许的HTTP方法</li>
 *   <li><b>allowedHeaders:</b> 允许的请求头</li>
 *   <li><b>allowCredentials:</b> 是否允许携带凭证</li>
 *   <li><b>maxAge:</b> 预检请求的缓存时间(秒)</li>
 * </ul>
 *
 * <h3>配置示例:</h3>
 * <h4>1. 开发环境配置(允许所有):</h4>
 * <pre>{@code
 * basetc:
 *   web:
 *     cors:
 *       auto-configure: true
 *       allowed-origin-patterns: "*"
 *       allowed-methods: "*"
 *       allowed-headers: "*"
 *       allow-credentials: false  # 通配符时不能为true
 *       max-age: 3600
 * }</pre>
 *
 * <h4>2. 生产环境配置(安全限制):</h4>
 * <pre>{@code
 * basetc:
 *   web:
 *     cors:
 *       auto-configure: true
 *       allowed-origin-patterns:
 *         - https://www.example.com
 *         - https://admin.example.com
 *         - https://app.example.com
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *       allowed-headers:
 *         - Authorization
 *         - Content-Type
 *         - Accept
 *       allow-credentials: true
 *       max-age: 1800  # 30分钟
 * }</pre>
 *
 * <h4>3. 前后端分离开发配置:</h4>
 * <pre>{@code
 * basetc:
 *   web:
 *     cors:
 *       auto-configure: true
 *       allowed-origin-patterns:
 *         - http://localhost:3000    # React开发服务器
 *         - http://localhost:8080    # Vue开发服务器
 *         - http://127.0.0.1:5500    # Live Server
 *       allowed-methods: "*"
 *       allowed-headers: "*"
 *       allow-credentials: true
 *       max-age: 3600
 * }</pre>
 *
 * <h4>4. 多环境配置:</h4>
 * <pre>{@code
 * # application-dev.yml (开发环境)
 * basetc:
 *   web:
 *     cors:
 *       allowed-origin-patterns: "*"
 *       allow-credentials: false
 *
 * # application-prod.yml (生产环境)
 * basetc:
 *   web:
 *     cors:
 *       allowed-origin-patterns:
 *         - https://www.example.com
 *       allow-credentials: true
 * }</pre>
 *
 * <h3>属性详解:</h3>
 *
 * <h4>1. allowedOriginPatterns (允许的源模式):</h4>
 * <ul>
 *   <li>支持通配符 {@code *}: 允许所有域名(不推荐用于生产环境)</li>
 *   <li>支持前缀通配符: {@code https://*.example.com}</li>
 *   <li>支持IP通配符: {@code http://192.168.1.*}</li>
 *   <li>支持端口号: {@code http://localhost:3000}</li>
 *   <li>注意: 当 allowCredentials=true 时,不能使用 {@code *} 通配符</li>
 * </ul>
 *
 * <h4>2. allowedMethods (允许的HTTP方法):</h4>
 * <ul>
 *   <li>常用方法: GET, POST, PUT, DELETE, PATCH, OPTIONS</li>
 *   <li>使用通配符 {@code *}: 允许所有HTTP方法</li>
 *   <li>建议: 只允许必要的HTTP方法,提升安全性</li>
 * </ul>
 *
 * <h4>3. allowedHeaders (允许的请求头):</h4>
 * <ul>
 *   <li>常用请求头: Authorization, Content-Type, Accept, Origin</li>
 *   <li>使用通配符 {@code *}: 允许所有请求头</li>
 *   <li>注意: 某些请求头需要后端明确允许才能在跨域请求中使用</li>
 * </ul>
 *
 * <h4>4. allowCredentials (允许携带凭证):</h4>
 * <ul>
 *   <li>设置为true: 允许前端携带Cookie、Authorization header等</li>
 *   <li>设置为false: 不允许携带凭证,前端请求的 credentials 模式需设为 "same-origin"</li>
 *   <li>重要: 设置为true时,allowedOriginPatterns 必须指定具体域名,不能使用通配符</li>
 *   <li>适用场景: 需要登录状态、Session、Token认证等</li>
 * </ul>
 *
 * <h4>5. maxAge (预检请求缓存时间):</h4>
 * <ul>
 *   <li>单位: 秒</li>
 *   <li>作用: 浏览器会缓存预检请求(OPTIONS)的结果,在缓存时间内不再重复发送</li>
 *   <li>推荐值: 1800(30分钟) 或 3600(1小时)</li>
 *   <li>过大值: 可能导致配置变更后不及时生效</li>
 *   <li>过小值: 增加OPTIONS请求次数,影响性能</li>
 * </ul>
 *
 * <h3>安全建议:</h3>
 * <ul>
 *   <li><b>生产环境:</b> 明确指定允许的域名,不要使用通配符</li>
 *   <li><b>开发环境:</b> 可以使用通配符方便开发调试</li>
 *   <li><b>限制方法:</b> 只允许应用实际需要的HTTP方法</li>
 *   <li><b>限制头:</b> 只允许必要的请求头</li>
 *   <li><b>凭证控制:</b> 非必要不开启 allowCredentials</li>
 *   <li><b>定期审查:</b> 定期检查CORS配置是否符合安全要求</li>
 * </ul>
 *
 * <h3>常见问题:</h3>
 * <p><b>1. 为什么出现 "CORS policy" 错误?</b>
 * <br>原因: 前端域名不在 allowedOriginPatterns 中,或者请求头/方法未被允许.
 *
 * <p><b>2. 为什么 Cookie 没有发送到服务器?</b>
 * <br>原因: allowCredentials=false 或 allowedOriginPatterns 使用了通配符.
 *
 * <p><b>3. 为什么频繁发送 OPTIONS 请求?</b>
 * <br>原因: maxAge 设置过小,或者浏览器缓存被清除.
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>CORS配置只在浏览器环境生效,服务端调用(如Postman、curl)不受影响</li>
 *   <li>如果使用了Spring Security,需要确保CORS配置在Security过滤器之前生效</li>
 *   <li>修改CORS配置后,可能需要清除浏览器缓存才能看到效果</li>
 *   <li>allowCredentials=true 时,前端请求需要设置 credentials: 'include'</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.web.cors.CorsConfiguration
 */
@Data
@ConfigurationProperties(prefix = "basetc.web.cors")
class WebCorsProperties {

    /**
     * 是否启用CORS自动配置.
     * <p>默认值为true,表示自动配置CORS跨域支持.
     * <p>如果设置为false,则不会创建CorsConfigurationSource Bean,
     * 需要开发者手动配置CORS.
     */
    private boolean autoConfigure = false;

    /**
     * 允许的源模式列表.
     * <p>定义哪些源(域名、IP、端口)可以跨域访问API.
     *
     * <p>支持以下格式:
     * <ul>
     *   <li>完整域名: {@code https://www.example.com}</li>
     *   <li>域名通配符: {@code https://*.example.com}</li>
     *   <li>IP地址: {@code http://192.168.1.100}</li>
     *   <li>IP通配符: {@code http://192.168.1.*}</li>
     *   <li>本地开发: {@code http://localhost:3000}</li>
     *   <li>全部允许: {@code *} (仅在allowCredentials=false时可用)</li>
     * </ul>
     *
     * <p>默认值为 {@code *},表示允许所有源(仅用于开发环境).
     */
    private List<String> allowedOriginPatterns = List.of("*");

    /**
     * 允许的HTTP方法列表.
     * <p>定义哪些HTTP方法可以跨域使用.
     *
     * <p>常用方法包括:
     * <ul>
     *   <li>GET - 查询数据</li>
     *   <li>POST - 创建数据</li>
     *   <li>PUT - 更新数据</li>
     *   <li>DELETE - 删除数据</li>
     *   <li>PATCH - 部分更新</li>
     *   <li>OPTIONS - 预检请求</li>
     *   <li>HEAD - 获取响应头</li>
     * </ul>
     *
     * <p>默认值为 {@code *},表示允许所有HTTP方法.
     */
    private List<String> allowedMethods = List.of("*");

    /**
     * 允许的请求头列表.
     * <p>定义哪些请求头可以在跨域请求中使用.
     *
     * <p>常用请求头包括:
     * <ul>
     *   <li>Authorization - 认证信息(如Bearer Token)</li>
     *   <li>Content-Type - 内容类型(如application/json)</li>
     *   <li>Accept - 接受的内容类型</li>
     *   <li>Origin - 请求源</li>
     * </ul>
     *
     * <p>默认值为 {@code *},表示允许所有请求头.
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * 是否允许携带凭证.
     * <p>凭证包括Cookie、Authorization header、TLS客户端证书等.
     *
     * <p>设置为true时:
     * <ul>
     *   <li>前端可以在请求中携带Cookie</li>
     *   <li>前端可以发送Authorization header</li>
     *   <li>前端请求的credentials模式需要设置为"include"</li>
     *   <li>allowedOriginPatterns 不能使用通配符 {@code *},必须指定具体域名</li>
     * </ul>
     *
     * <p>设置为false时:
     * <ul>
     *   <li>前端不能携带凭证</li>
     *   <li>前端请求的credentials模式应该是"same-origin"或"omit"</li>
     *   <li>allowedOriginPatterns 可以使用通配符 {@code *}</li>
     * </ul>
     *
     * <p>默认值为true.
     */
    private Boolean allowCredentials = true;

    /**
     * 预检请求(OPTIONS)的缓存时间(秒).
     * <p>浏览器在发送复杂请求前,会先发送OPTIONS预检请求,
     * 服务器返回的Access-Control-Max-Age头指定了预检结果的缓存时间.
     *
     * <p>在缓存时间内,浏览器不会重复发送OPTIONS请求,直接发送实际请求,
     * 这样可以减少HTTP请求数量,提升性能.
     *
     * <p>推荐配置:
     * <ul>
     *   <li>开发环境: 3600秒(1小时)</li>
     *   <li>生产环境: 1800秒(30分钟)</li>
     *   <li>频繁变更: 600秒(10分钟)</li>
     * </ul>
     *
     * <p>默认值为3600秒(1小时).
     */
    private Long maxAge = 3600L;
}

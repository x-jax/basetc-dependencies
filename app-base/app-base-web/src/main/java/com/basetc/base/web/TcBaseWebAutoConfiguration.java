package com.basetc.base.web;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Tc Base Web模块自动配置类,配置Web层相关组件.
 *
 * <p>此类是Spring Boot的自动配置类,通过 {@code @AutoConfiguration} 注解在Web应用中自动生效.
 * <p>主要功能包括:
 * <ul>
 *   <li>配置CORS跨域资源共享策略</li>
 *   <li>提供灵活的跨域配置选项,支持白名单、请求方法、请求头等控制</li>
 *   <li>支持预检请求缓存,提升跨域性能</li>
 * </ul>
 *
 * <h3>自动配置条件:</h3>
 * <p>此配置类在以下条件下自动生效:
 * <ul>
 *   <li>应用必须是Web应用(存在Spring Web依赖)</li>
 *   <li>配置文件中 {@code basetc.web.cors.auto-configure} 为 {@code true} 或未配置(默认启用)</li>
 * </ul>
 *
 * <h3>配置示例:</h3>
 * <h4>1. application.yml 配置:</h4>
 * <pre>{@code
 * basetc:
 *   web:
 *     cors:
 *       # 是否启用CORS自动配置(默认true)
 *       auto-configure: true
 *       # 允许的源模式(支持通配符)
 *       allowed-origin-patterns:
 *         - http://localhost:3000
 *         - https://example.com
 *         - http://192.168.1.*  # 支持IP段通配
 *       # 允许的HTTP方法
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *         - OPTIONS
 *       # 允许的请求头
 *       allowed-headers:
 *         - "*"
 *       # 是否允许携带凭证(Cookie、Authorization等)
 *       allow-credentials: true
 *       # 预检请求缓存时间(秒)
 *       max-age: 3600
 * }</pre>
 *
 * <h4>2. 禁用CORS自动配置:</h4>
 * <pre>{@code
 * # 方式1: 通过配置文件禁用
 * basetc:
 *   web:
 *     cors:
 *       auto-configure: false
 *
 * # 方式2: 通过@SpringBootApplication排除
 * @SpringBootApplication(exclude = TcBaseWebAutoConfiguration.class)
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>CORS跨域说明:</h3>
 * <p>CORS(Cross-Origin Resource Sharing)是W3C标准,用于解决浏览器的同源策略限制。
 * 当前端应用与后端API不在同一域名、端口或协议时,需要配置CORS允许跨域访问.
 *
 * <h4>常见跨域场景:</h4>
 * <ul>
 *   <li>前后端分离: 前端在 {@code http://localhost:3000}, 后端在 {@code http://localhost:8080}</li>
 *   <li>多端访问: Web端、小程序、H5共用同一套后端API</li>
 *   <li>微服务架构: 前端网关与后端服务跨域</li>
 *   <li>CDN加速: 静态资源托管在CDN,API在业务服务器</li>
 * </ul>
 *
 * <h4>CORS请求类型:</h4>
 * <p><b>1. 简单请求(Simple Request):</b>
 * <ul>
 *   <li>方法: GET、POST、HEAD</li>
 *   <li>请求头: Accept、Accept-Language、Content-Language、Content-Type(特定值)</li>
 *   <li>浏览器直接发送请求,服务器返回响应时包含CORS头</li>
 * </ul>
 *
 * <p><b>2. 预检请求(Preflight Request):</b>
 * <ul>
 *   <li>在复杂请求前自动发送OPTIONS请求</li>
 *   <li>目的: 检查服务器是否允许该实际请求</li>
 *   <li>可通过 {@code max-age} 配置缓存预检结果,减少OPTIONS请求次数</li>
 * </ul>
 *
 * <p><b>3. 带凭证的请求(Credited Request):</b>
 * <ul>
 *   <li>需要设置 {@code allow-credentials: true}</li>
 *   <li>此时 {@code allowed-origin-patterns} 不能使用通配符 {@code *},必须指定具体域名</li>
 *   <li>支持Cookie、Authorization header等</li>
 * </ul>
 *
 * <h3>安全建议:</h3>
 * <ul>
 *   <li><b>生产环境:</b> 不要使用 {@code *} 通配符,明确指定允许的源</li>
 *   <li><b>开发环境:</b> 可以使用通配符或 {@code *} 方便调试</li>
 *   <li><b>带凭证场景:</b> 必须指定具体域名,不能使用 {@code *}</li>
 *   <li><b>限制方法:</b> 只允许必要的HTTP方法(如GET、POST)</li>
 *   <li><b>限制头:</b> 只允许必要的请求头,避免安全风险</li>
 * </ul>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>CORS配置只在浏览器环境生效,服务端调用(如Postman)不受影响</li>
 *   <li>如果使用了Spring Security,需要确保CORS配置在Security之前生效</li>
 *   <li>预检请求缓存时间不宜过长,建议根据实际需求调整</li>
 *   <li>允许凭证时,allowedOriginPatterns必须指定具体域名</li>
 * </ul>
 *
 * <h3>Nginx反向代理配置:</h3>
 * <p>如果使用Nginx反向代理,也可以在Nginx层配置CORS:
 * <pre>{@code
 * location /api {
 *     add_header 'Access-Control-Allow-Origin' '$http_origin' always;
 *     add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
 *     add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type' always;
 *     add_header 'Access-Control-Allow-Credentials' 'true' always;
 *     add_header 'Access-Control-Max-Age' '3600' always;
 *
 *     if ($request_method = 'OPTIONS') {
 *         return 204;
 *     }
 *
 *     proxy_pass http://backend;
 * }
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see CorsConfiguration
 * @see CorsConfigurationSource
 * @see WebCorsProperties
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(WebCorsProperties.class)
@RequiredArgsConstructor
public class TcBaseWebAutoConfiguration {

    /**
     * Web模块CORS配置属性.
     * <p>通过Spring Boot自动注入,包含跨域相关配置项.
     */
    private final WebCorsProperties webCorsProperties;

    /**
     * 配置CORS跨域资源共享策略.
     *
     * <p>此方法创建并配置CorsConfigurationSource,定义跨域访问的规则.
     * <p>配置项包括:
     * <ul>
     *   <li>允许的源(允许的域名、IP、端口等)</li>
     *   <li>允许的HTTP方法(GET、POST、PUT、DELETE等)</li>
     *   <li>允许的请求头(Authorization、Content-Type等)</li>
     *   <li>是否允许携带凭证(Cookie、Token等)</li>
     *   <li>预检请求的缓存时间</li>
     * </ul>
     *
     * <h3>配置生效条件:</h3>
     * <ul>
     *   <li>{@code basetc.web.cors.auto-configure} 为 {@code true} 或未配置</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 前端发起跨域请求
     * fetch('http://localhost:8080/api/users', {
     *     method: 'GET',
     *     credentials: 'include',  // 携带Cookie
     *     headers: {
     *         'Authorization': 'Bearer ' + token,
     *         'Content-Type': 'application/json'
     *     }
     * })
     * .then(response => response.json())
     * .then(data => console.log(data));
     * }</pre>
     *
     * <h3>前端错误示例:</h3>
     * <pre>{@code
     * // 错误1: 未配置CORS时的错误
     * // Access to XMLHttpRequest at 'http://localhost:8080/api/users' from origin
     * // 'http://localhost:3000' has been blocked by CORS policy
     *
     * // 错误2: 允许凭证但使用通配符
     * // When allowCredentials is true, allowedOriginPatterns cannot contain the "*"
     *
     * // 错误3: 请求头未被允许
     * // Request header field authorization is not allowed by Access-Control-Allow-Headers
     * }</pre>
     *
     * @return 配置好的CorsConfigurationSource对象,应用于所有路径(/**)
     * @see CorsConfiguration
     * @see UrlBasedCorsConfigurationSource
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "basetc.web.cors",
            name = "auto-configure",
            havingValue = "true",
            matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 设置允许的源(支持通配符,如: http://localhost:*, https://*.example.com)
        configuration.setAllowedOriginPatterns(webCorsProperties.getAllowedOriginPatterns());
        // 设置允许的HTTP方法(如: GET, POST, PUT, DELETE)
        configuration.setAllowedMethods(webCorsProperties.getAllowedMethods());
        // 设置允许的请求头(如: Authorization, Content-Type)
        configuration.setAllowedHeaders(webCorsProperties.getAllowedHeaders());
        // 允许携带凭证(Cookie、Authorization等)
        // 注意: 设置为true时,allowedOriginPatterns不能使用"*"通配符
        configuration.setAllowCredentials(webCorsProperties.getAllowCredentials());
        // 设置预检请求的有效期(秒),减少OPTIONS请求次数
        configuration.setMaxAge(webCorsProperties.getMaxAge());

        // 创建基于URL的CORS配置源,应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
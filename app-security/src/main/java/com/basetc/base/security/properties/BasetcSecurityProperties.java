package com.basetc.base.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 安全配置属性根类,统一管理安全模块的所有配置.
 *
 * <p>此类是 BaseTC 安全模块的根配置类,通过 Spring Boot 的 {@link ConfigurationProperties} 机制
 * 自动绑定配置文件中以 {@code basetc.security} 为前缀的配置项.
 *
 * <p>主要配置包括:
 * <ul>
 *   <li>认证配置 ({@link BasetcSecurityAuthProperties}) - 登录、登出、白名单等</li>
 *   <li>权限配置 ({@link BasetcSecurityPermissionsProperties}) - 角色和权限定义</li>
 *   <li>JWT 配置 ({@link BasetcSecurityJwtProperties}) - 令牌生成和验证</li>
 *   <li>Redis 配置 ({@link BasetcSecurityRedisProperties}) - Redis 存储模式</li>
 *   <li>Session 配置 ({@link BasetcSecuritySessionProperties}) - Session 存储模式</li>
 *   <li>CORS 配置 ({@link BasetcSecurityCorsProperties}) - 跨域资源共享</li>
 * </ul>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     auto-configure: true
 *     auth:
 *       logout-url: /logout
 *       csrf-enabled: false
 *       white-list:
 *         - /api/public/**
 *         - /actuator/**
 *       filter:
 *         single-enabled: true
 *         ip-enabled: true
 *         user-agent-enabled: false
 *     jwt:
 *       header: Authorization
 *       prefix: "Bearer "
 *       expire: 30
 *       refresh-scope: 15
 *       secret: your-secret-key-here
 *     redis:
 *       enable: true
 *       redis-key-prefix: "basetc:user:"
 *     session:
 *       enable: false
 *       session-key-prefix: "basetc_user"
 *     permissions:
 *       super-role: SUPER_ADMIN
 *       all-permission: "*"
 *     cors:
 *       enabled: true
 *       allowed-origin-patterns:
 *         - http://localhost:3000
 *         - https://example.com
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *       allowed-headers: "*"
 *       allow-credentials: true
 *       max-age: 3600
 * }</pre>
 *
 * <h3>配置示例 (application.properties):</h3>
 * <pre>{@code
 * basetc.security.auto-configure=true
 * basetc.security.auth.logout-url=/logout
 * basetc.security.auth.csrf-enabled=false
 * basetc.security.auth.white-list=/api/public/**,/actuator/**
 * basetc.security.jwt.header=Authorization
 * basetc.security.jwt.prefix=Bearer
 * basetc.security.jwt.expire=30
 * basetc.security.redis.enable=true
 * basetc.security.redis.redis-key-prefix=basetc:user:
 * basetc.security.permissions.super-role=SUPER_ADMIN
 * }</pre>
 *
 * <h3>存储模式切换:</h3>
 * <p>本模块支持两种用户认证信息存储模式:
 * <ul>
 *   <li><b>Redis 模式</b>: 适用于分布式应用,支持多节点共享认证信息</li>
 *   <li><b>Session 模式</b>: 适用于单机应用,使用本地 Session 存储</li>
 * </ul>
 *
 * <p>默认使用 Session 模式。如需切换到 Redis 模式,请配置:
 * <pre>{@code
 * basetc:
 *   security:
 *     redis:
 *       enable: true
 *     session:
 *       enable: false
 * }</pre>
 *
 * <h3>禁用自动配置:</h3>
 * <p>如果需要自定义安全配置,可以禁用自动配置:
 * <pre>{@code
 * basetc:
 *   security:
 *     auto-configure: false
 * }</pre>
 *
 * <h3>在代码中使用配置:</h3>
 * <pre>{@code
 * @RestController
 * @RequiredArgsConstructor
 * public class SecurityConfigController {
 *
 *     private final BasetcSecurityProperties properties;
 *
 *     @GetMapping("/security/config")
 *     public BasetcSecurityProperties getConfig() {
 *         return properties;
 *     }
 *
 *     @GetMapping("/security/storage-mode")
 *     public String getStorageMode() {
 *         if (properties.redisEnabled()) {
 *             return "Redis存储模式";
 *         } else {
 *             return "Session存储模式";
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>配置优先级:</h3>
 * <ol>
 *   <li>命令行参数</li>
 *   <li>系统环境变量</li>
 *   <li>application-{profile}.properties</li>
 *   <li>application.properties</li>
 *   <li>默认值</li>
 * </ol>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>生产环境必须修改 {@code basetc.security.jwt.secret} 为强密码</li>
 *   <li>Redis 和 Session 配置不能同时启用,同时启用时 Redis 优先</li>
 *   <li>白名单配置支持 Ant 风格的路径匹配符</li>
 *   <li>CORS 配置只在 {@code cors.enabled=true} 时生效</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see BasetcSecurityAuthProperties
 * @see BasetcSecurityJwtProperties
 * @see BasetcSecurityRedisProperties
 * @see BasetcSecuritySessionProperties
 * @see BasetcSecurityPermissionsProperties
 * @see BasetcSecurityCorsProperties
 */
@Data
@ConfigurationProperties(prefix = "basetc.security")
public class BasetcSecurityProperties {

    /**
     * 是否自动配置安全模块.
     *
     * <p>设置为 {@code true} 时,Spring Boot 会自动配置安全过滤器链、认证入口点等组件.
     * 设置为 {@code false} 时,需要手动配置 Spring Security.
     *
     * <p>默认值为 {@code true}.
     */
    private boolean autoConfigure = true;

    /**
     * 认证相关配置.
     *
     * <p>包括登录、登出、白名单、过滤器等认证相关配置.
     *
     * @see BasetcSecurityAuthProperties
     */
    @NestedConfigurationProperty
    private BasetcSecurityAuthProperties auth = new BasetcSecurityAuthProperties();

    /**
     * 权限相关配置.
     *
     * <p>包括超级管理员角色、所有权限标识等权限相关配置.
     *
     * @see BasetcSecurityPermissionsProperties
     */
    @NestedConfigurationProperty
    private BasetcSecurityPermissionsProperties permissions = new BasetcSecurityPermissionsProperties();

    /**
     * JWT 令牌相关配置.
     *
     * <p>包括令牌头、前缀、过期时间、密钥等 JWT 相关配置.
     *
     * @see BasetcSecurityJwtProperties
     */
    @NestedConfigurationProperty
    private BasetcSecurityJwtProperties jwt = new BasetcSecurityJwtProperties();

    /**
     * Redis 存储相关配置.
     *
     * <p>包括 Redis 是否启用、Redis 键前缀等配置.
     * 适用于分布式应用场景.
     *
     * @see BasetcSecurityRedisProperties
     */
    @NestedConfigurationProperty
    private BasetcSecurityRedisProperties redis = new BasetcSecurityRedisProperties();

    /**
     * Session 存储相关配置.
     *
     * <p>包括 Session 是否启用、Session 键前缀等配置.
     * 适用于单机应用场景.
     *
     * @see BasetcSecuritySessionProperties
     */
    @NestedConfigurationProperty
    private BasetcSecuritySessionProperties session = new BasetcSecuritySessionProperties();

    /**
     * CORS 跨域相关配置.
     *
     * <p>包括是否启用 CORS、允许的源、方法、头部等配置.
     *
     * @see BasetcSecurityCorsProperties
     */
    @NestedConfigurationProperty
    private BasetcSecurityCorsProperties cors = new BasetcSecurityCorsProperties();

    /**
     * 判断是否启用 Redis 存储模式.
     *
     * <p>此方法用于判断当前应用使用 Redis 还是 Session 存储用户认证信息.
     * Redis 模式适用于分布式应用,Session 模式适用于单机应用.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * if (securityProperties.redisEnabled()) {
     *     // 使用 Redis 存储用户信息
     *     redisTemplate.opsForValue().set(key, user);
     * } else {
     *     // 使用 Session 存储用户信息
     *     session.setAttribute(key, user);
     * }
     * }</pre>
     *
     * <h3>配置切换:</h3>
     * <pre>{@code
     * # 启用 Redis 模式
     * basetc.security.redis.enable=true
     * basetc.security.session.enable=false
     *
     * # 启用 Session 模式
     * basetc.security.redis.enable=false
     * basetc.security.session.enable=true
     * }</pre>
     *
     * @return {@code true} 如果启用 Redis 存储模式; {@code false} 如果使用 Session 存储模式
     */
    public boolean redisEnabled() {
        return redis.isEnable();
    }

}

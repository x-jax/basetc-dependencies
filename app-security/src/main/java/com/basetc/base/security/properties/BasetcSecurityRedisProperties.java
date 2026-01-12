package com.basetc.base.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全 Redis 配置属性类,定义 Redis 存储模式的相关配置参数.
 *
 * <p>此类用于配置使用 Redis 存储用户认证信息的相关参数.
 * Redis 存储模式适用于分布式应用,支持多节点共享认证信息.
 *
 * <h3>Redis vs Session 存储模式:</h3>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>Redis 模式</th>
 *     <th>Session 模式</th>
 *   </tr>
 *   <tr>
 *     <td>适用场景</td>
 *     <td>分布式应用、多节点部署</td>
 *     <td>单机应用</td>
 *   </tr>
 *   <tr>
 *     <td>性能</td>
 *     <td>依赖 Redis,网络开销</td>
 *     <td>内存访问,速度快</td>
 *   </tr>
 *   <tr>
 *     <td>可扩展性</td>
 *     <td>好,支持横向扩展</td>
 *     <td>差,不支持跨节点</td>
 *   </tr>
 *   <tr>
 *     <td>可靠性</td>
 *     <td>依赖 Redis 可用性</td>
 *     <td>依赖应用服务器</td>
 *   </tr>
 * </table>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     redis:
 *       enable: true
 *       redis-key-prefix: "basetc:user:"
 *     session:
 *       enable: false
 * }</pre>
 *
 * <h3>配置示例 (application.properties):</h3>
 * <pre>{@code
 * basetc.security.redis.enable=true
 * basetc.security.redis.redis-key-prefix=basetc:user:
 * basetc.security.session.enable=false
 * }</pre>
 *
 * <h3>Redis 键命名规范:</h3>
 * <p>Redis 中存储的键格式为: {@code {prefix}{userId}}
 * <ul>
 *   <li>用户信息键: {@code basetc:user:12345} -> {@code LoginUser JSON}</li>
 *   <li>Token ID 键: {@code basetc:user:token:id:12345} -> {@code UUID}</li>
 *   <li>IP 键: {@code basetc:user:ip:12345} -> {@code "192.168.1.100"}</li>
 *   <li>User-Agent 键: {@code basetc:user:ua:12345} -> {@code "Mozilla/5.0..."}</li>
 * </ul>
 *
 * <h3>在代码中使用配置:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class RedisAuthService {
 *
 *     private final BasetcSecurityRedisProperties redisProperties;
 *     private final RedisTemplate<String, Object> redisTemplate;
 *
 *     public void saveUserLogin(Long userId, LoginUser user) {
 *         String key = redisProperties.getRedisKeyPrefix() + userId;
 *         redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(30));
 *     }
 *
 *     public LoginUser getUserLogin(Long userId) {
 *         String key = redisProperties.getRedisKeyPrefix() + userId;
 *         return (LoginUser) redisTemplate.opsForValue().get(key);
 *     }
 *
 *     public void deleteUserLogin(Long userId) {
 *         String key = redisProperties.getRedisKeyPrefix() + userId;
 *         redisTemplate.delete(key);
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>启用 Redis 模式时,需要确保 Redis 服务可用</li>
 *   <li>Redis 和 Session 配置不能同时启用</li>
 *   <li>键前缀建议以冒号结尾,便于分类管理</li>
 *   <li>生产环境建议配置 Redis 密码和哨兵/集群模式</li>
 *   <li>Redis 中的数据会自动过期,无需手动清理</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see BasetcSecuritySessionProperties
 * @see org.springframework.data.redis.core.RedisTemplate
 */
@Data
@ConfigurationProperties(prefix = "basetc.security.redis")
public class BasetcSecurityRedisProperties {

    /**
     * 是否启用 Redis 存储模式.
     *
     * <p>设置为 {@code true} 时,用户认证信息会存储在 Redis 中,
     * 适用于分布式应用场景.
     *
     * <p>默认值为 {@code false}.
     *
     * <h3>启用 Redis 模式的步骤:</h3>
     * <ol>
     *   <li>确保项目依赖了 {@code spring-boot-starter-data-redis}</li>
     *   <li>配置 Redis 连接信息 (host, port, password 等)</li>
     *   <li>设置 {@code basetc.security.redis.enable=true}</li>
     *   <li>设置 {@code basetc.security.session.enable=false}</li>
     * </ol>
     *
     * <h3>Redis 配置示例:</h3>
     * <pre>{@code
     * spring:
     *   data:
     *     redis:
     *       host: localhost
     *       port: 6379
     *       password: your-password
     *       database: 0
     *       timeout: 5000ms
     *       lettuce:
     *         pool:
     *           max-active: 8
     *           max-idle: 8
     *           min-idle: 0
     *           max-wait: -1ms
     * }</pre>
     *
     * @see #redisKeyPrefix
     */
    private boolean enable = false;

    /**
     * Redis 键前缀.
     *
     * <p>用于在 Redis 中存储用户认证信息时作为键的前缀,
     * 便于分类管理和避免键冲突.
     *
     * <p>默认值为 {@code "basetc:user:"}.
     *
     * <h3>键前缀规范:</h3>
     * <ul>
     *   <li>使用冒号 ({@code :}) 分隔不同层级</li>
     *   <li>前缀应具有明确的业务含义</li>
     *   <li>建议以冒号结尾,便于拼接用户 ID</li>
     * </ul>
     *
     * <h3>键前缀示例:</h3>
     * <pre>{@code
     * # 标准格式
     * redis-key-prefix: "basetc:user:"
     * # 完整键: basetc:user:12345
     *
     * # 带应用名
     * redis-key-prefix: "myapp:security:user:"
     * # 完整键: myapp:security:user:12345
     *
     * # 带环境
     * redis-key-prefix: "prod:app:user:"
     * # 完整键: prod:app:user:12345
     * }</pre>
     *
     * <h3>Redis 命令示例:</h3>
     * <pre>{@code
     * # 查看所有用户登录信息
     * redis-cli keys "basetc:user:*"
     *
     * # 查看特定用户信息
     * redis-cli get "basetc:user:12345"
     *
     * # 删除特定用户登录
     * redis-cli del "basetc:user:12345"
     *
     * # 查看用户数量
     * redis-cli dbsize
     * }</pre>
     */
    private String redisKeyPrefix = "basetc:user:";

}

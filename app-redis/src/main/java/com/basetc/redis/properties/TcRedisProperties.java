package com.basetc.redis.properties;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Redis 模块配置属性类.
 *
 * <p>此类是 BaseTC Redis 模块的根配置类,通过 Spring Boot 的 {@link ConfigurationProperties} 机制
 * 自动绑定配置文件中以 {@code basetc.redis} 为前缀的配置项.
 *
 * <p>主要配置项包括:
 * <ul>
 *   <li>自动配置开关</li>
 *   <li>FastJSON2 反序列化白名单 (安全配置)</li>
 *   <li>分布式锁配置 (锁前缀、超时时间、过期时间)</li>
 * </ul>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   redis:
 *     # 是否启用自动配置
 *     auto-configure: true
 *
 *     # FastJSON2 反序列化白名单 (防止反序列化漏洞)
 *     auto-type-accept:
 *       - com.basetc.base.common.response.R
 *       - com.basetc.base.common.domain.*
 *       - com.example.model.*
 *
 *     # 分布式锁配置
 *     lock-key-prefix: "app:lock:"    # 锁键前缀
 *     lock-timeout: 3000               # 获取锁超时时间 (毫秒)
 *     lock-expire-time: 30000          # 锁过期时间 (毫秒)
 *     lock-sleep-time: 10              # 锁竞争休眠时间 (毫秒)
 * }</pre>
 *
 * <h3>分布式锁配置说明:</h3>
 * <table border="1">
 *   <tr>
 *     <th>配置项</th>
 *     <th>默认值</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>lock-key-prefix</td>
 *     <td>"lock:"</td>
 *     <td>Redis 中分布式锁键的前缀,用于区分不同应用的锁</td>
 *   </tr>
 *   <tr>
 *     <td>lock-timeout</td>
 *     <td>3000ms</td>
 *     <td>获取锁的最大等待时间,超过此时间将抛出 {@link com.basetc.redis.exception.RedisTryLockTimeoutException}</td>
 *   </tr>
 *   <tr>
 *     <td>lock-expire-time</td>
 *     <td>30000ms</td>
 *     <td>锁的自动过期时间,防止死锁。应大于业务执行时间</td>
 *   </tr>
 *   <tr>
 *     <td>lock-sleep-time</td>
 *     <td>10ms</td>
 *     <td>获取锁失败后的休眠时间,避免忙等待消耗 CPU 资源</td>
 *   </tr>
 * </table>
 *
 * <h3>安全性配置:</h3>
 * <p><b>重要</b>: {@code auto-type-accept} 配置用于防止 FastJSON2 反序列化漏洞。
 * 在生产环境中,建议明确配置允许反序列化的类型列表。
 *
 * <h4>配置建议:</h4>
 * <ul>
 *   <li>开发环境: 可以使用通配符 {@code com.basetc.**} 方便调试</li>
 *   <li>生产环境: 明确列出所有需要反序列化的类路径,不要使用通配符</li>
 *   <li>不要配置: {@code java.**} 或 {@code org.springframework.**} 等过于宽泛的包路径</li>
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class UserService {
 *
 *     private final RedisTemplateClient redisClient;
 *
 *     public User getUser(Long userId) {
 *         // 使用缓存加载器
 *         return redisClient.getCacheObjectOrLoad(
 *             "user:" + userId,
 *             () -> {
 *                 // 从数据库加载
 *                 User user = userRepository.findById(userId);
 *                 // 返回缓存数据,30分钟过期
 *                 return RedisCacheData.of(user, 30, TimeUnit.MINUTES);
 *             }
 *         );
 *     }
 *
 *     public void updateUserWithLock(User user) {
 *         // 使用分布式锁防止并发更新
 *         redisClient.getCacheObjectOrLoadWithLock(
 *             "user:lock:" + user.getId(),
 *             RedisLoadWithLock.of(() -> {
 *                 // 加载并更新用户数据
 *                 User updated = userRepository.update(user);
 *                 return RedisCacheData.of(updated, 30, TimeUnit.MINUTES);
 *             })
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h3>分布式锁实现原理:</h3>
 * <pre>{@code
 * 1. 尝试获取锁
 *    SET lock:key value NX PX 30000
 *    ├─ 成功 -> 执行业务逻辑
 *    └─ 失败 -> 休眠 10ms,重试 (直到超时)
 *
 * 2. 释放锁 (Lua 脚本保证原子性)
 *    if redis.call("GET", KEYS[1]) == ARGV[1] then
 *        return redis.call("DEL", KEYS[1])
 *    else
 *        return 0
 *    end
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see com.basetc.redis.client.RedisTemplateClient
 * @see com.basetc.redis.exception.RedisTryLockTimeoutException
 */
@Data
@FieldNameConstants
@ConfigurationProperties(prefix = "basetc.redis")
public class TcRedisProperties {

    /**
     * 是否自动配置.
     * <p>默认为true,启用Redis自动配置.
     */
    private boolean autoConfigure = true;

    /**
     * 缓存序列化白名单.
     * <p>FastJSON2反序列化时允许的类型列表,用于防止反序列化漏洞.
     * <p>支持包名通配符,例如: {@code com.basetc.base.common.domain.*}
     * <p>默认为空列表,表示不限制类型(生产环境建议配置).
     */
    private List<String> autoTypeAccept = List.of();

    /**
     * 分布式锁键前缀.
     * <p>Redis中分布式键的前缀,用于区分不同应用的锁.
     * <p>默认值: "lock:"
     */
    private String lockKeyPrefix = "lock:";

    /**
     * 获取锁超时时间(毫秒).
     * <p>防止线程在获取锁后无限期等待,超过此时间将抛出异常.
     * <p>默认值: 3000ms (3秒)
     */
    private long lockTimeout = 3000;

    /**
     * 锁过期时间(毫秒).
     * <p>锁的自动过期时间,防止死锁.
     * <p>默认值: 30000ms (30秒)
     */
    private long lockExpireTime = 30000;

    /**
     * 锁竞争休眠时间(毫秒).
     * <p>获取锁失败后的休眠时间,避免忙等待消耗CPU资源.
     * <p>默认值: 10ms
     */
    private long lockSleepTime = 10;

    /**
     * 获取锁超时时间( Duration).
     *
     * @return 超时时间
     */
    public Duration getLockTimeoutDuration() {
        return Duration.ofMillis(lockTimeout);
    }

    /**
     * 获取锁过期时间(Duration).
     *
     * @return 过期时间
     */
    public Duration getLockExpireDuration() {
        return Duration.ofMillis(lockExpireTime);
    }
}
package com.basetc.redis.exception;

import java.io.Serial;

/**
 * Redis 分布式锁获取超时异常.
 *
 * <p>当尝试获取 Redis 分布式锁超过配置的超时时间时,抛出此异常.
 * 此异常通常表示系统中存在较严重的并发竞争或锁持有者执行时间过长.
 *
 * <h3>触发场景:</h3>
 * <ul>
 *   <li>多个请求同时竞争同一个锁,且在配置的超时时间内未获取到锁</li>
 *   <li>锁的持有者执行时间过长,导致其他等待者超时</li>
 *   <li>系统负载过高,Redis 响应缓慢</li>
 * </ul>
 *
 * <h3>处理建议:</h3>
 * <ol>
 *   <li>检查锁的配置超时时间 {@code basetc.redis.lock-timeout} 是否过短</li>
 *   <li>检查锁持有者的业务逻辑执行时间是否合理</li>
 *   <li>考虑优化业务逻辑或调整锁的粒度</li>
 *   <li>检查 Redis 服务器的性能和可用性</li>
 * </ol>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class OrderService {
 *
 *     private final RedisTemplateClient redisClient;
 *
 *     public void createOrder(Order order) {
 *         try {
 *             // 使用分布式锁防止重复创建订单
 *             redisClient.getCacheObjectOrLoadWithLock(
 *                 "order:lock:" + order.getUserId(),
 *                 RedisLoadWithLock.of(() -> {
 *                     // 创建订单逻辑
 *                     orderRepository.create(order);
 *                     return RedisCacheData.of(order, 5, TimeUnit.MINUTES);
 *                 }, 10, TimeUnit.SECONDS)  // 锁超时时间10秒
 *             );
 *         } catch (RedisTryLockTimeoutException e) {
 *             // 获取锁超时,处理并发冲突
 *             log.warn("创建订单失败,系统繁忙,请稍后重试: {}", e.getMessage());
 *             throw new BusinessException("系统繁忙,请稍后重试");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>配置示例:</h3>
 * <pre>{@code
 * basetc:
 *   redis:
 *     # 获取锁超时时间 (根据业务执行时间调整)
 *     lock-timeout: 5000  # 5秒
 *
 *     # 锁过期时间 (应大于业务执行时间)
 *     lock-expire-time: 30000  # 30秒
 *
 *     # 锁竞争休眠时间
 *     lock-sleep-time: 10  # 10毫秒
 * }</pre>
 *
 * <h3>与相关异常的区别:</h3>
 * <table border="1">
 *   <tr>
 *     <th>异常类型</th>
 *     <th>触发条件</th>
 *     <th>处理方式</th>
 *   </tr>
 *   <tr>
 *     <td>RedisTryLockTimeoutException</td>
 *     <td>获取锁超时</td>
 *     <td>提示用户重试或降级处理</td>
 *   </tr>
 *   <tr>
 *     <td>RedisConnectionFailureException</td>
 *     <td>Redis 连接失败</td>
 *     <td>检查 Redis 服务状态</td>
 *   </tr>
 *   <tr>
 *     <td>RedisCommandExecutionException</td>
 *     <td>Redis 命令执行失败</td>
 *     <td>检查命令语法和参数</td>
 *   </tr>
 * </table>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see com.basetc.redis.client.RedisTemplateClient
 * @see com.basetc.redis.properties.TcRedisProperties#getLockTimeout()
 */
public class RedisTryLockTimeoutException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6160042008460566072L;

    /**
     * 构造 Redis 分布式锁获取超时异常.
     *
     * @param lockKey 锁的键名
     */
    public RedisTryLockTimeoutException(String lockKey) {
        super("获取 Redis 分布式锁超时: " + lockKey);
    }

    /**
     * 构造 Redis 分布式锁获取超时异常.
     *
     * @param lockKey 锁的键名
     * @param timeout 超时时间 (毫秒)
     */
    public RedisTryLockTimeoutException(String lockKey, long timeout) {
        super(String.format("获取 Redis 分布式锁超时: lockKey=%s, timeout=%dms", lockKey, timeout));
    }

    /**
     * 获取锁键名.
     *
     * @return 锁键名
     */
    public String getLockKey() {
        return getMessage().replace("获取 Redis 分布式锁超时: ", "");
    }
}

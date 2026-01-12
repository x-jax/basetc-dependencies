package com.basetc.redis.client;

import com.basetc.redis.exception.RedisTryLockTimeoutException;
import com.basetc.redis.properties.TcRedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RedisTemplate客户端，提供对Redis的便捷操作，包括字符串、List、Set、Hash等数据类型的存取，
 * 以及分布式锁功能等。此类封装了RedisTemplate的常用操作，简化了Redis的使用。
 *
 * <h3>架构设计</h3>
 * <p>
 * 此类采用分层设计，提供了三个层次的Redis操作能力：
 * </p>
 * <ul>
 * <li><strong>基础层</strong>: 封装RedisTemplate的基本操作（字符串、List、Set、Hash等）</li>
 * <li><strong>功能层</strong>: 提供缓存加载器、原子操作、批量操作等高级功能</li>
 * <li><strong>安全层</strong>: 实现分布式锁机制，防止缓存击穿等问题</li>
 * </ul>
 *
 * <h3>核心功能</h3>
 * <ul>
 * <li><strong>基本数据类型操作</strong>: 字符串、List、Set、Hash等常用数据结构的存取</li>
 * <li><strong>智能缓存加载</strong>: 支持缓存不存在时自动加载数据，避免缓存穿透</li>
 * <li><strong>分布式锁</strong>: 基于Redis的分布式锁实现，防止缓存击穿</li>
 * <li><strong>原子操作</strong>: 自增、自减等原子性操作，适用于计数器等场景</li>
 * <li><strong>批量操作</strong>: 支持批量读取和写入，提高性能</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 * <li><strong>高频数据缓存</strong>: 用户信息、配置数据等频繁访问的数据</li>
 * <li><strong>计数器实现</strong>: 页面访问量、接口调用次数等计数场景</li>
 * <li><strong>分布式环境协调</strong>: 任务调度、资源分配等需要分布式协调的场景</li>
 * <li><strong>会话管理</strong>: 分布式环境下的用户会话存储</li>
 * <li><strong>限流控制</strong>: 基于Redis的令牌桶算法实现接口限流</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * 
 * <pre>{@code
 * // 基本字符串操作
 * redisClient.setCacheObject("user:1", user);
 * User user = redisClient.getCacheObject("user:1");
 *
 * // 带过期时间的缓存
 * redisClient.setCacheObject("token:user1", token, 30, TimeUnit.MINUTES);
 *
 * // 使用缓存加载器
 * User user = redisClient.getCacheObjectOrLoad("user:1",
 *         () -> RedisCacheData.of(
 *                 userService.findById(1L),
 *                 10, TimeUnit.MINUTES));
 *
 * // 使用带锁的缓存加载器防止缓存击穿
 * User user = redisClient.getCacheObjectOrLoadWithLock("user:1",
 *         RedisLoadWithLock.of(() -> RedisCacheData.of(
 *                 userService.findById(1L),
 *                 10, TimeUnit.MINUTES)));
 * }</pre>
 *
 * <h3>设计决策</h3>
 * <ul>
 * <li><strong>API设计</strong>: 采用简洁直观的方法命名，降低学习成本</li>
 * <li><strong>异常处理</strong>: 内部捕获Redis相关异常并转换为统一的异常类型</li>
 * <li><strong>性能优化</strong>: 批量操作减少网络开销，缓存预热提高响应速度</li>
 * <li><strong>安全性</strong>: 分布式锁使用UUID作为锁值，防止误释放；Lua脚本确保原子性</li>
 * <li><strong>可扩展性</strong>: 基于接口设计，支持自定义序列化方式和Redis客户端</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see RedisCacheLoader
 * @see RedisLoadWithLock
 * @see RedisCacheData
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class RedisTemplateClient {

    /** Lua 脚本用于实现原子性的分布式锁释放 */
    private static final String UNLOCK_SCRIPT = """
            if redis.call("GET", KEYS[1]) == ARGV[1] then
                return redis.call("DEL", KEYS[1])
            else
                return 0
            end
            """;
    private final TcRedisProperties componentRedisProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> unlockScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);

    /**
     * 设置缓存键的过期时间。
     *
     * @param key      缓存键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void expire(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 获取缓存键的过期时间。
     *
     * @param key      缓存键
     * @param timeUnit 时间单位
     * @return 有效期
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        Long expire = redisTemplate.getExpire(key, timeUnit);
        return expire != null ? expire : -1;
    }

    /**
     * 判断缓存键是否存在。
     *
     * @param key 缓存键
     * @return true=存在 false=不存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 缓存基本对象（无过期时间）。
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param <T>   值类型
     */
    public <T> void setCacheObject(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本对象（指定过期时间）。
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @param <T>      值类型
     */
    public <T> void setCacheObject(String key, T value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 获取缓存的基本对象。
     *
     * @param key 缓存键
     * @param <T> 返回类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T getCacheObject(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存的基本对象，如果不存在则使用缓存加载器加载并缓存。
     *
     * @param key              缓存键
     * @param <T>              返回类型
     * @param redisCacheLoader 缓存加载器
     * @return 缓存值
     */
    public <T> T getCacheObjectOrLoad(String key, RedisCacheLoader<T> redisCacheLoader) {
        T o = getCacheObject(key);
        if (Objects.isNull(o)) {
            final RedisCacheData<T> load = redisCacheLoader.load();
            if (Objects.isNull(load.value())) {
                return null;
            }
            if (Objects.nonNull(load.timeUnit())) {
                redisTemplate.opsForValue().set(key, load.value(), load.expire(), load.timeUnit());
            } else {
                redisTemplate.opsForValue().set(key, load.value());
            }
            o = load.value();
        }
        return o;
    }

    /**
     * 获取缓存的基本对象，如果不存在则使用带锁的缓存加载器加载并缓存。
     * 此方法使用分布式锁来防止缓存击穿。
     *
     * @param key               缓存键
     * @param <T>               返回类型
     * @param redisLoadWithLock 缓存加载器
     * @return 缓存值
     */
    public <T> T getCacheObjectOrLoadWithLock(String key, RedisLoadWithLock<T> redisLoadWithLock) {
        T o = getCacheObject(key);
        if (Objects.isNull(o)) {
            final AtomicReference<T> reference = new AtomicReference<>();
            tryLockWhile(
                    key,
                    redisLoadWithLock,
                    () -> {
                        // 获取锁成功后再次检查缓存（防止并发情况下重复加载）
                        T value = getCacheObject(key);
                        if (Objects.nonNull(value)) {
                            reference.set(value);
                            return;
                        }
                        RedisCacheData<T> data = redisLoadWithLock.cacheData().load();
                        T result = data.value();
                        if (Objects.isNull(result)) {
                            return;
                        }
                        reference.set(result);
                        if (data.expire() > 0) {
                            redisTemplate.opsForValue().set(key, result, data.expire(), data.timeUnit());
                        } else {
                            redisTemplate.opsForValue().set(key, result);
                        }
                    });
            o = reference.get();
        }
        return o;
    }

    /**
     * 删除单个缓存对象。
     *
     * @param key 缓存键
     * @return true=删除成功 false=删除失败
     */
    public boolean deleteObject(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 删除多个缓存对象。
     *
     * @param keys 缓存键集合
     * @return 删除数量
     */
    public long deleteObject(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 缓存List数据。
     *
     * @param key      缓存键
     * @param dataList List数据
     * @param <T>      元素类型
     */
    public <T> void setCacheList(String key, List<T> dataList) {
        redisTemplate.opsForList().rightPushAll(key, dataList);
    }

    /**
     * 缓存List数据（指定过期时间）。
     *
     * @param key      缓存键
     * @param dataList List数据
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @param <T>      元素类型
     */
    public <T> void setCacheList(String key, List<T> dataList, long timeout, TimeUnit timeUnit) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        expire(key, timeout, timeUnit);
    }

    /**
     * 获取缓存的List对象。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return 缓存的List对象
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getCacheList(String key) {
        return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 获取缓存的List对象，如果不存在则使用缓存加载器加载并缓存。
     *
     * @param key              缓存键
     * @param redisCacheLoader 缓存加载器
     * @param <T>              元素类型
     * @return 缓存的List对象
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getCacheListOrLoad(String key, RedisCacheLoader<List<T>> redisCacheLoader) {
        List<T> list = (List<T>) redisTemplate.opsForList().range(key, 0, -1);
        if (list == null || list.isEmpty()) {
            final RedisCacheData<List<T>> load = redisCacheLoader.load();
            if (Objects.isNull(load.value())) {
                return Collections.emptyList();
            }
            if (load.expire() > 0) {
                setCacheList(key, load.value(), load.expire(), load.timeUnit());
            } else {
                setCacheList(key, load.value());
            }
            list = load.value();
        }
        return list;
    }

    /**
     * 获取缓存的List对象，如果不存在则使用带锁的缓存加载器加载并缓存。
     * 此方法使用分布式锁来防止缓存击穿。
     *
     * @param key               缓存键
     * @param redisLoadWithLock 缓存加载器
     * @param <T>               元素类型
     * @return 缓存的List对象
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getCacheListOrLoadWithLock(
            String key, RedisLoadWithLock<List<T>> redisLoadWithLock) {
        List<T> list = (List<T>) redisTemplate.opsForList().range(key, 0, -1);
        if (list == null || list.isEmpty()) {
            final AtomicReference<List<T>> reference = new AtomicReference<>();
            tryLockWhile(
                    key,
                    redisLoadWithLock,
                    () -> {
                        // 获取锁成功后再次检查缓存（防止并发情况下重复加载）
                        List<T> value = (List<T>) redisTemplate.opsForList().range(key, 0, -1);
                        if (Objects.nonNull(value) && !value.isEmpty()) {
                            reference.set(value);
                            return;
                        }
                        RedisCacheData<List<T>> data = redisLoadWithLock.cacheData().load();
                        List<T> result = data.value();
                        if (Objects.isNull(result)) {
                            return;
                        }
                        reference.set(result);
                        if (data.expire() > 0) {
                            setCacheList(key, result, data.expire(), data.timeUnit());
                        } else {
                            setCacheList(key, result);
                        }
                    });
            list = reference.get();
        }
        return list;
    }

    /**
     * 向List左侧添加元素。
     *
     * @param key   缓存键
     * @param value 元素值
     * @param <T>   元素类型
     * @return 添加后List的长度
     */
    public <T> long leftPushList(String key, T value) {
        Long count = redisTemplate.opsForList().leftPush(key, value);
        return count != null ? count : 0;
    }

    /**
     * 向List右侧添加元素。
     *
     * @param key   缓存键
     * @param value 元素值
     * @param <T>   元素类型
     * @return 添加后List的长度
     */
    public <T> long rightPushList(String key, T value) {
        Long count = redisTemplate.opsForList().rightPush(key, value);
        return count != null ? count : 0;
    }

    /**
     * 从List左侧弹出元素。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return 弹出的元素
     */
    @SuppressWarnings("unchecked")
    public <T> T leftPopList(String key) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 从List右侧弹出元素。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return 弹出的元素
     */
    @SuppressWarnings("unchecked")
    public <T> T rightPopList(String key) {
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取List的长度。
     *
     * @param key 缓存键
     * @return List长度
     */
    public long getListSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    /**
     * 根据索引获取List中的元素。
     *
     * @param key   缓存键
     * @param index 索引
     * @param <T>   元素类型
     * @return 元素值
     */
    @SuppressWarnings("unchecked")
    public <T> T getListIndex(String key, long index) {
        return (T) redisTemplate.opsForList().index(key, index);
    }

    /*
     * ---------------------------------- Set 操作 ----------------------------------
     */

    /**
     * 缓存Set数据。
     *
     * @param key    缓存键
     * @param values Set值
     * @param <T>    元素类型
     * @return 添加的数量
     */
    @SafeVarargs
    public final <T> long setCacheSet(String key, T... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        return count != null ? count : 0;
    }

    /**
     * 缓存Set数据（指定过期时间）。
     *
     * @param key      缓存键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @param values   Set值
     * @param <T>      元素类型
     * @return 添加的数量
     */
    @SafeVarargs
    public final <T> long setCacheSet(String key, long timeout, TimeUnit timeUnit, T... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        expire(key, timeout, timeUnit);
        return count != null ? count : 0;
    }

    /**
     * 获取缓存的Set对象。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return Set对象
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getCacheSet(String key) {
        return (Collection<T>) redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断Set中是否存在某个元素。
     *
     * @param key   缓存键
     * @param value 元素值
     * @param <T>   元素类型
     * @return true=存在 false=不存在
     */
    public <T> boolean isMemberOfSet(String key, T value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * 获取Set的大小。
     *
     * @param key 缓存键
     * @return Set大小
     */
    public long getSetSize(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }

    /**
     * 移除Set中的元素。
     *
     * @param key    缓存键
     * @param values 元素值
     * @param <T>    元素类型
     * @return 移除的个数
     */
    @SafeVarargs
    public final <T> long removeSet(String key, T... values) {
        Long count = redisTemplate.opsForSet().remove(key, (Object[]) values);
        return count != null ? count : 0;
    }

    /**
     * 随机获取Set中的元素。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return 随机元素
     */
    @SuppressWarnings("unchecked")
    public <T> T randomMemberOfSet(String key) {
        return (T) redisTemplate.opsForSet().randomMember(key);
    }

    /*
     * ---------------------------------- Hash (Map) 操作
     * ----------------------------------
     */

    /**
     * 缓存Hash数据。
     *
     * @param key     缓存键
     * @param dataMap Hash数据
     * @param <K>     Hash键类型
     * @param <V>     Hash值类型
     */
    public <K, V> void setCacheMap(String key, Map<K, V> dataMap) {
        redisTemplate.opsForHash().putAll(key, dataMap);
    }

    /**
     * 缓存Hash数据（指定过期时间）。
     *
     * @param key      缓存键
     * @param dataMap  Hash数据
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @param <K>      Hash键类型
     * @param <V>      Hash值类型
     */
    public <K, V> void setCacheMap(String key, Map<K, V> dataMap, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForHash().putAll(key, dataMap);
        expire(key, timeout, timeUnit);
    }

    /**
     * 向Hash中添加单个键值对。
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @param value   Hash值
     * @param <K>     Hash键类型
     * @param <V>     Hash值类型
     */
    public <K, V> void putCacheMapValue(String key, K hashKey, V value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取缓存的Hash对象。
     *
     * @param key 缓存键
     * @param <K> Hash键类型
     * @param <V> Hash值类型
     * @return Hash对象
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getCacheMap(String key) {
        return (Map<K, V>) redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取缓存的Hash对象，如果不存在则使用缓存加载器加载并缓存。
     *
     * @param key         缓存键
     * @param cacheLoader 缓存加载器
     * @param <K>         Hash键类型
     * @param <V>         Hash值类型
     * @return Hash对象
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getCacheMapOrLoad(String key, RedisCacheLoader<Map<K, V>> cacheLoader) {
        Map<K, V> entries = (Map<K, V>) redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            final RedisCacheData<Map<K, V>> load = cacheLoader.load();
            if (Objects.isNull(load.value())) {
                return null;
            }
            if (load.expire() > 0) {
                setCacheMap(key, load.value(), load.expire(), load.timeUnit());
            } else {
                setCacheMap(key, load.value());
            }
            entries = load.value();
        }
        return entries;
    }

    /**
     * 获取缓存的Hash对象，如果不存在则使用带锁的缓存加载器加载并缓存。
     * 此方法使用分布式锁来防止缓存击穿。
     *
     * @param key               缓存键
     * @param redisLoadWithLock 缓存加载器
     * @param <K>               Hash键类型
     * @param <V>               Hash值类型
     * @return Hash对象
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getCacheMapOrLoadWithLock(
            String key, RedisLoadWithLock<Map<K, V>> redisLoadWithLock) {
        Map<K, V> entries = (Map<K, V>) redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            final AtomicReference<Map<K, V>> reference = new AtomicReference<>();
            tryLockWhile(
                    key,
                    redisLoadWithLock,
                    () -> {
                        // 获取锁成功后再次检查缓存（防止并发情况下重复加载）
                        Map<K, V> value = (Map<K, V>) redisTemplate.opsForHash().entries(key);
                        if (Objects.nonNull(value) && !value.isEmpty()) {
                            reference.set(value);
                            return;
                        }
                        RedisCacheData<Map<K, V>> data = redisLoadWithLock.cacheData().load();
                        Map<K, V> result = data.value();
                        if (Objects.isNull(result)) {
                            return;
                        }
                        reference.set(result);
                        if (data.expire() > 0) {
                            setCacheMap(key, result, data.expire(), data.timeUnit());
                        } else {
                            setCacheMap(key, result);
                        }
                    });
            entries = reference.get();
        }
        return entries;
    }

    /**
     * 获取Hash中的单个值。
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @param <K>     Hash键类型
     * @param <V>     Hash值类型
     * @return Hash值
     */
    @SuppressWarnings("unchecked")
    public <K, V> V getCacheMapValue(String key, K hashKey) {
        return (V) redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 获取Hash中多个键的值。
     *
     * @param key      缓存键
     * @param hashKeys Hash键集合
     * @param <K>      Hash键类型
     * @param <V>      Hash值类型
     * @return Hash值列表
     */
    @SuppressWarnings("unchecked")
    public <K, V> List<V> getMultiCacheMapValue(String key, Collection<K> hashKeys) {
        return (List<V>) redisTemplate.opsForHash().multiGet(key, (Collection<Object>) hashKeys);
    }

    /**
     * 判断Hash中是否存在某个键。
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @param <K>     Hash键类型
     * @return true=存在 false=不存在
     */
    public <K> boolean hasMapKey(String key, K hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 获取Hash的大小。
     *
     * @param key 缓存键
     * @return Hash大小
     */
    public long getMapSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 删除Hash中的键。
     *
     * @param key      缓存键
     * @param hashKeys Hash键
     * @param <K>      Hash键类型
     * @return 删除的数量
     */
    @SafeVarargs
    public final <K> long deleteCacheMapValue(String key, K... hashKeys) {
        return redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    /*
     * ---------------------------------- 自增自减操作 ----------------------------------
     */

    /**
     * 自增（Long类型）。
     *
     * @param key 缓存键
     * @return 自增后的值
     */
    public long increment(String key) {
        return increment(key, 1L);
    }

    /**
     * 自增指定值（Long类型）。
     *
     * @param key   缓存键
     * @param delta 增量
     * @return 自增后的值
     */
    public long increment(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0L;
    }

    /**
     * 自增（Double类型）。
     *
     * @param key   缓存键
     * @param delta 增量
     * @return 自增后的值
     */
    public double increment(String key, double delta) {
        Double result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0.0;
    }

    /**
     * 自减（Long类型）。
     *
     * @param key 缓存键
     * @return 自减后的值
     */
    public long decrement(String key) {
        return decrement(key, 1L);
    }

    /**
     * 自减指定值（Long类型）。
     *
     * @param key   缓存键
     * @param delta 减量
     * @return 自减后的值
     */
    public long decrement(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0L;
    }

    /**
     * Hash字段自增。
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @param delta   增量
     * @param <K>     Hash键类型
     * @return 自增后的值
     */
    public <K> long incrementMapValue(String key, K hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    /**
     * Hash字段自增（Double类型）。
     *
     * @param key     缓存键
     * @param hashKey Hash键
     * @param delta   增量
     * @param <K>     Hash键类型
     * @return 自增后的值
     */
    public <K> double incrementMapValue(String key, K hashKey, double delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    /**
     * 尝试获取分布式锁。
     *
     * @param lockKey    锁的键
     * @param lockValue  锁的值
     * @param expireTime 过期时间(毫秒)
     * @return 是否获取到锁
     */
    private boolean tryLock(String lockKey, String lockValue, long expireTime) {
        Boolean result = redisTemplate
                .opsForValue()
                .setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
        return result != null && result;
    }

    /**
     * 尝试获取分布式锁，并执行加载逻辑。该方法会循环尝试获取锁，直到超时或成功获取锁。
     * 获取锁后会执行指定的回调逻辑，并在finally块中释放锁，确保锁的正确释放。
     *
     * @param key                 缓存键
     * @param redisLoadWithLock   带锁的缓存加载器
     * @param redisLockHolderNext 获取锁后的执行逻辑
     * @param <T>                 加载逻辑返回值类型
     */
    private <T> void tryLockWhile(
            String key, RedisLoadWithLock<T> redisLoadWithLock, RedisLockHolderNext redisLockHolderNext) {
        String lockKey = componentRedisProperties.getLockKeyPrefix() + key;
        // 最大等待时间
        long waitExpireTime = System.currentTimeMillis() + componentRedisProperties.getLockTimeout();
        String lockValue = String.valueOf(System.currentTimeMillis());
        long expireTime = componentRedisProperties.getLockTimeout();
        if (Objects.nonNull(redisLoadWithLock.lockerExpire())) {
            expireTime = redisLoadWithLock.lockerExpire();
        }
        while (System.currentTimeMillis() < waitExpireTime) {
            if (tryLock(lockKey, lockValue, expireTime)) {
                try {
                    redisLockHolderNext.apply();
                    return;
                } finally {
                    releaseLock(lockKey, lockValue);
                }
            }

            // 添加短暂延迟，避免忙等待消耗CPU资源
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // 恢复中断状态
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new RedisTryLockTimeoutException(lockKey);
    }

    /**
     * 释放分布式锁。
     *
     * @param lockKey   锁的键
     * @param lockValue 锁的值
     */
    private void releaseLock(String lockKey, String lockValue) {
        redisTemplate.execute(unlockScript, Collections.singletonList(lockKey), lockValue);
    }

    /**
     * 支持自定义过期时间的缓存加载器接口。
     *
     * @param <T> 缓存数据类型
     * @author Liu,Dongdong
     */
    @FunctionalInterface
    public interface RedisCacheLoader<T> {
        RedisCacheData<T> load();
    }

    /**
     * Redis锁持有者后续操作函数式接口。
     *
     * @author Liu,Dongdong
     */
    @FunctionalInterface
    public interface RedisLockHolderNext {

        void apply();
    }

    /**
     * Redis缓存数据封装类。
     *
     * @param <T> 缓存数据类型
     * @author Liu,Dongdong
     */
    public record RedisCacheData<T>(T value, long expire, TimeUnit timeUnit) {

        public static <T> RedisCacheData<T> of(T value) {
            return new RedisCacheData<>(value, -1, null);
        }

        public static <T> RedisCacheData<T> of(T value, long expire, TimeUnit timeUnit) {
            return new RedisCacheData<>(value, expire, timeUnit);
        }

    }

    /**
     * Redis带锁缓存加载器。
     *
     * @param <T> 缓存数据类型
     * @author Liu,Dongdong
     */
    public record RedisLoadWithLock<T>(RedisCacheLoader<T> cacheData, Long lockerExpire) {

        /**
         * 创建Redis带锁缓存加载器实例（使用默认锁过期时间）。
         *
         * @param cacheData 缓存数据加载器
         * @param <T>       缓存数据类型
         * @return Redis带锁缓存加载器实例
         */
        public static <T> RedisLoadWithLock<T> of(RedisCacheLoader<T> cacheData) {
            return new RedisLoadWithLock<>(cacheData, -1L);
        }

        /**
         * 创建Redis带锁缓存加载器实例（指定锁过期时间）。
         *
         * @param cacheData    缓存数据加载器
         * @param lockerExpire 锁过期时间
         * @param timeUnit     时间单位
         * @param <T>          缓存数据类型
         * @return Redis带锁缓存加载器实例
         */
        public static <T> RedisLoadWithLock<T> of(
                RedisCacheLoader<T> cacheData, long lockerExpire, TimeUnit timeUnit) {
            return new RedisLoadWithLock<>(cacheData, timeUnit.toMillis(lockerExpire));
        }
    }
}

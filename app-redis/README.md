# TC Redis 模块

TC Redis 模块是基于 Spring Data Redis 的 Redis 客户端封装，提供便捷的 Redis 操作功能，包括字符串、List、Set、Hash 等数据类型的存取，以及分布式锁功能等。

## 功能特性

- **基本数据类型操作**：支持字符串、List、Set、Hash 等 Redis 数据类型的操作
- **缓存加载器**：支持缓存不存在时自动加载数据
- **分布式锁**：防止缓存击穿的锁机制
- **原子操作**：自增、自减等原子性操作
- **类型安全**：泛型支持，类型转换安全

## 快速开始

### 依赖配置

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.basetc</groupId>
    <artifactId>app-redis</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 配置

在 `application.yml` 或 `application.properties` 中配置 Redis 连接信息：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 2000ms
    jedis:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

### 启用 Redis 配置

在 Spring Boot 启动类上添加注解：

```java
@SpringBootApplication
@EnableTcRedis
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 核心类说明

### RedisTemplateClient

[RedisTemplateClient](file:///Users/administrators/Desktop/appbase/basetc-dependencies/app-redis/src/main/java/com/basetc/redis/client/RedisTemplateClient.java#L30-L889) 是主要的 Redis 客户端类，提供对 Redis 的便捷操作。

#### 字符串操作

```java
@Autowired
private RedisTemplateClient redisClient;

// 设置缓存
redisClient.setCacheObject("key", "value");

// 设置缓存并指定过期时间
redisClient.setCacheObject("key", "value", 60, TimeUnit.SECONDS);

// 获取缓存
String value = redisClient.getCacheObject("key");

// 缓存不存在时自动加载
String value = redisClient.getCacheObjectOrLoad("key", () -> {
    // 加载逻辑
    String data = loadDataFromDatabase();
    return RedisCacheData.of(data, 60, TimeUnit.SECONDS);
});
```

#### List 操作

```java
// 设置 List 缓存
List<String> list = Arrays.asList("item1", "item2", "item3");
redisClient.setCacheList("listKey", list);

// 获取 List 缓存
List<String> cachedList = redisClient.getCacheList("listKey");

// 向 List 左侧添加元素
redisClient.leftPushList("listKey", "newItem");

// 从 List 右侧弹出元素
String item = redisClient.rightPopList("listKey");
```

#### Set 操作

```java
// 设置 Set 缓存
long count = redisClient.setCacheSet("setKey", "value1", "value2", "value3");

// 获取 Set 缓存
Collection<String> setValues = redisClient.getCacheSet("setKey");

// 判断 Set 中是否存在某个元素
boolean isMember = redisClient.isMemberOfSet("setKey", "value1");

// 随机获取 Set 中的元素
String randomValue = redisClient.randomMemberOfSet("setKey");
```

#### Hash 操作

```java
// 设置 Hash 缓存
Map<String, Object> hashMap = new HashMap<>();
hashMap.put("field1", "value1");
hashMap.put("field2", "value2");
redisClient.setCacheMap("hashKey", hashMap);

// 获取 Hash 缓存
Map<String, Object> hashValues = redisClient.getCacheMap("hashKey");

// 获取 Hash 中单个值
Object value = redisClient.getCacheMapValue("hashKey", "field1");

// 向 Hash 中添加单个键值对
redisClient.putCacheMapValue("hashKey", "field3", "value3");
```

#### 分布式锁

```java
// 使用分布式锁加载数据，防止缓存击穿
String result = redisClient.getCacheObjectOrLoadWithLock("key", RedisLoadWithLock.of(() -> {
    // 加载逻辑，只会在没有缓存时执行一次
    String data = loadDataFromDatabase();
    return RedisCacheData.of(data, 60, TimeUnit.SECONDS);
}));

// 自定义锁过期时间
String result = redisClient.getCacheObjectOrLoadWithLock("key", RedisLoadWithLock.of(
    () -> {
        String data = loadDataFromDatabase();
        return RedisCacheData.of(data, 60, TimeUnit.SECONDS);
    },
    30, TimeUnit.SECONDS
));
```

## 核心接口和类

### RedisCacheLoader

缓存加载器接口，用于在缓存不存在时加载数据。

```java
@FunctionalInterface
public interface RedisCacheLoader<T> {
    RedisCacheData<T> load();
}
```

### RedisCacheData

缓存数据封装类，包含缓存值、过期时间和时间单位。

```java
public record RedisCacheData<T>(T value, long expire, TimeUnit timeUnit) {
    // 静态方法创建实例
    public static <T> RedisCacheData<T> of(T value) { ... }
    public static <T> RedisCacheData<T> of(T value, long expire, TimeUnit timeUnit) { ... }
}
```

### RedisLoadWithLock

带锁的缓存加载器，用于防止缓存击穿。

```java
public record RedisLoadWithLock<T>(RedisCacheLoader<T> cacheData, Long lockerExpire) {
    public static <T> RedisLoadWithLock<T> of(RedisCacheLoader<T> cacheData) { ... }
    public static <T> RedisLoadWithLock<T> of(RedisCacheLoader<T> cacheData, long lockerExpire, TimeUnit timeUnit) { ... }
}
```

## 使用示例

### 缓存用户信息

```java
@Service
public class UserService {
    
    @Autowired
    private RedisTemplateClient redisClient;
    
    public User getUserById(Long userId) {
        String key = "user:" + userId;
        
        // 尝试从缓存获取用户信息
        User user = redisClient.getCacheObject(key);
        
        if (user == null) {
            // 缓存不存在，从数据库加载
            user = userRepository.findById(userId);
            
            if (user != null) {
                // 将用户信息存入缓存，过期时间60秒
                redisClient.setCacheObject(key, user, 60, TimeUnit.SECONDS);
            }
        }
        
        return user;
    }
    
    // 使用缓存加载器自动加载
    public User getUserByIdWithLoader(Long userId) {
        String key = "user:" + userId;
        
        return redisClient.getCacheObjectOrLoad(key, () -> {
            User user = userRepository.findById(userId);
            if (user != null) {
                // 缓存用户信息，过期时间60秒
                return RedisCacheData.of(user, 60, TimeUnit.SECONDS);
            }
            return RedisCacheData.of(null);
        });
    }
    
    // 使用分布式锁防止缓存击穿
    public User getUserByIdWithLock(Long userId) {
        String key = "user:" + userId;
        
        return redisClient.getCacheObjectOrLoadWithLock(key, RedisLoadWithLock.of(() -> {
            User user = userRepository.findById(userId);
            if (user != null) {
                // 缓存用户信息，过期时间60秒
                return RedisCacheData.of(user, 60, TimeUnit.SECONDS);
            }
            return RedisCacheData.of(null);
        }));
    }
}
```

### 缓存列表数据

```java
@Service
public class ProductService {
    
    @Autowired
    private RedisTemplateClient redisClient;
    
    public List<Product> getProductsByCategory(String category) {
        String key = "products:" + category;
        
        return redisClient.getCacheListOrLoadWithLock(key, RedisLoadWithLock.of(() -> {
            List<Product> products = productRepository.findByCategory(category);
            return RedisCacheData.of(products, 300, TimeUnit.SECONDS); // 缓存5分钟
        }));
    }
}
```

## 配置属性

### TcRedisProperties

Redis 模块的配置属性类：

- `lock-key-prefix`: 分布式锁键前缀，默认为 "lock:"
- `lock-timeout`: 分布式锁超时时间，默认为 30000 毫秒

## 注意事项

1. **线程安全**：[RedisTemplateClient](file:///Users/administrators/Desktop/appbase/basetc-dependencies/app-redis/src/main/java/com/basetc/redis/client/RedisTemplateClient.java#L30-L889) 是线程安全的，可以放心在多线程环境下使用。

2. **分布式锁**：使用分布式锁时，请确保 Redis 服务器时间同步，以避免因时间不一致导致的锁问题。

3. **异常处理**：当使用 `getCacheObjectOrLoadWithLock` 方法时，如果获取锁超时会抛出 [RedisTryLockTimeoutException](file:///Users/administrators/Desktop/appbase/basetc-dependencies/app-redis/src/main/java/com/basetc/redis/exception/RedisTryLockTimeoutException.java#L11-L16) 异常。

4. **性能考虑**：在高并发场景下，使用分布式锁可以有效防止缓存击穿，但也会增加系统开销，需要根据实际业务场景选择合适的缓存策略。

## 最佳实践

1. **合理设置过期时间**：为缓存设置合理的过期时间，避免缓存数据过期导致缓存雪崩。

2. **使用缓存加载器**：对于需要复杂计算或数据库查询的数据，使用缓存加载器可以简化缓存逻辑。

3. **防止缓存穿透**：对于查询不存在的数据，可以缓存空值并设置较短的过期时间。

4. **监控缓存命中率**：定期监控缓存命中率，优化缓存策略。

## 常见问题

### 为什么需要分布式锁？

在高并发场景下，当缓存失效时，多个请求可能同时查询数据库，导致数据库压力过大。使用分布式锁可以确保只有一个请求去查询数据库，其他请求等待锁释放后直接从缓存获取数据。

### 如何选择缓存策略？

- 对于读多写少的数据，可以使用 `getCacheObjectOrLoad` 方法
- 对于可能被大量并发请求访问的数据，建议使用 `getCacheObjectOrLoadWithLock` 方法
- 对于实时性要求较高的数据，可以设置较短的过期时间

## 贡献

欢迎提交 Issue 和 Pull Request 来改进本项目。
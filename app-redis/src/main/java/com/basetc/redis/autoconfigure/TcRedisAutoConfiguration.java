package com.basetc.redis.autoconfigure;

import com.basetc.redis.client.RedisTemplateClient;
import com.basetc.redis.properties.TcRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * Redis 模块自动配置类.
 *
 * <p>此类是 BaseTC Redis 模块的核心自动配置类,通过 Spring Boot 的 {@link AutoConfiguration} 机制,
 * 自动配置 Redis 相关组件,包括:
 * <ul>
 *   <li>{@link RedisTemplate} - Redis 操作模板类</li>
 *   <li>{@link com.basetc.redis.client.RedisTemplateClient} - Redis 客户端封装类</li>
 *   <li>序列化器配置 - FastJSON2 序列化器</li>
 * </ul>
 *
 * <h3>自动配置条件:</h3>
 * <ul>
 *   <li>classpath 下存在 {@link RedisTemplate} 类</li>
 *   <li>配置文件中 {@code basetc.redis.auto-configure} 未设置为 false</li>
 * </ul>
 *
 * <h3>配置序列化特性:</h3>
 * <pre>{@code
 * 1. Key 序列化: StringRedisSerializer (字符串序列化)
 * 2. Value 序列化: FastJson2RedisSerializer (JSON 序列化)
 * 3. Hash Key 序列化: StringRedisSerializer
 * 4. Hash Value 序列化: FastJson2RedisSerializer
 * 5. 事务支持: 启用
 * }</pre>
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
 *             () -> RedisCacheData.of(
 *                 userRepository.findById(userId),
 *                 30,
 *                 TimeUnit.MINUTES
 *             )
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h3>禁用自动配置:</h3>
 * <p>如果需要禁用 Redis 自动配置,可以通过以下方式:
 *
 * <h4>方式 1: 配置文件</h4>
 * <pre>{@code
 * basetc:
 *   redis:
 *     auto-configure: false
 * }</pre>
 *
 * <h4>方式 2: 注解排除</h4>
 * <pre>{@code
 * @SpringBootApplication(
 *     exclude = {TcRedisAutoConfiguration.class}
 * )
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>自定义配置:</h3>
 * <p>如果自定义了 {@link RedisTemplate} Bean,自动配置会自动回退。
 *
 * <pre>{@code
 * @Configuration
 * public class CustomRedisConfig {
 *
 *     @Bean
 *     @ConditionalOnMissingBean(RedisTemplate.class)
 *     public RedisTemplate<String, Object> redisTemplate(
 *             RedisConnectionFactory connectionFactory) {
 *
 *         // 自定义配置...
 *         return template;
 *     }
 * }
 * }</pre>
 *
 * <h3>Bean 依赖关系:</h3>
 * <pre>{@code
 * TcRedisAutoConfiguration
 *    ├─ RedisTemplate (当不存在时自动创建)
 *    │   └─ RedisConnectionFactory (由 Spring Data Redis 提供)
 *    └─ RedisTemplateClient (依赖 RedisTemplate)
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.boot.autoconfigure.AutoConfiguration
 * @see com.basetc.redis.properties.TcRedisProperties
 * @see com.basetc.redis.client.RedisTemplateClient
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(TcRedisProperties.class)
@RequiredArgsConstructor
public class TcRedisAutoConfiguration {

    private final TcRedisProperties tcRedisProperties;

    /**
     * 创建并配置RedisTemplate实例.
     *
     * <p>
     * 该方法会创建一个RedisTemplate实例，并配置以下特性：
     *
     * <ul>
     * <li>使用FastJson2RedisSerializer进行value序列化
     * <li>使用StringRedisSerializer进行key序列化
     * <li>开启事务支持
     * <li>配置FastJson的autoTypeAccept列表
     * </ul>
     *
     * @param connectionFactory Redis连接工厂
     * @return 配置完成的RedisTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // 使用FastJson序列化
        final List<String> autoTypeAccept = tcRedisProperties.getAutoTypeAccept();

        // 配置FastJson2RedisSerializer，将autoTypeAccept作为构造参数传递
        FastJson2RedisSerializer<Object> fastJson2RedisSerializer = new FastJson2RedisSerializer<>(Object.class,
                autoTypeAccept);

        // value序列化方式采用FastJson
        template.setDefaultSerializer(fastJson2RedisSerializer);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringSerializer);
        // value序列化方式
        template.setValueSerializer(fastJson2RedisSerializer);
        // hash的value序列化方式
        template.setHashValueSerializer(fastJson2RedisSerializer);
        // 开启事务
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建RedisTemplateClient实例.
     *
     * <p>
     * 该方法会创建一个RedisTemplateClient实例，用于简化Redis操作.
     *
     * @param redisTemplate RedisTemplate实例
     * @return 配置完成的RedisTemplateClient实例
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RedisTemplateClient redisTemplateClient(RedisTemplate<String, Object> redisTemplate) {
        return new RedisTemplateClient(tcRedisProperties, redisTemplate);
    }

}

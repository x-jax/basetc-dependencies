package com.basetc.base.security.service;

import java.util.Set;

/**
 * 匿名访问服务接口,定义获取允许匿名访问路径的方法.
 *
 * <p>此接口负责扫描和管理允许匿名访问的路径,
 * 这些路径无需用户登录即可访问,如登录接口、注册接口、公共 API 等.
 *
 * <h3>匿名访问路径来源:</h3>
 * <ul>
 *   <li>配置文件中的白名单配置 ({@code basetc.security.auth.white-list})</li>
 *   <li>标记了 {@code @AnonymousAccess} 注解的 Controller 方法</li>
 *   <li>Spring Security 默认的匿名访问路径 (如 /login, /logout 等)</li>
 * </ul>
 *
 * <h3>扫描流程:</h3>
 * <pre>{@code
 * 1. 扫描所有 Controller 方法
 *    ↓
 * 2. 检查方法是否标记了 @AnonymousAccess 注解
 *    ↓
 * 3. 提取方法的请求路径 (URL pattern)
 *    ↓
 * 4. 检查注解的环境配置是否匹配当前环境
 *    ↓
 * 5. 收集所有允许匿名访问的路径
 *    ↓
 * 6. 返回路径集合
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Configuration
 * @RequiredArgsConstructor
 * public class SecurityConfig {
 *
 *     private final AnonymousAccessService anonymousAccessService;
 *
 *     @Bean
 *     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 *         http
 *             .authorizeHttpRequests(auth -> auth
 *                 // 允许匿名访问的路径
 *                 .requestMatchers(
 *                     anonymousAccessService.getAnonymousAccessPaths()
 *                         .toArray(new String[0])
 *                 ).permitAll()
 *                 // 其他请求需要认证
 *                 .anyRequest().authenticated()
 *             );
 *
 *         return http.build();
 *     }
 * }
 * }</pre>
 *
 * <h3>@AnonymousAccess 注解使用:</h3>
 * <pre>{@code
 * // 在 Controller 方法上使用注解
 * @RestController
 * public class PublicController {
 *
 *     @AnonymousAccess
 *     @GetMapping("/api/public/hello")
 *     public R<String> hello() {
 *         return R.success("Hello, Anonymous!");
 *     }
 *
 *     @AnonymousAccess(env = {Env.DEV, Env.TEST})  // 仅在开发和测试环境允许匿名访问
 *     @GetMapping("/api/dev/debug")
 *     public R<String> debug() {
 *         return R.success("Debug Info");
 *     }
 * }
 * }</pre>
 *
 * <h3>配置文件白名单:</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     auth:
 *       white-list:
 *         - /api/public/**
 *         - /auth/login
 *         - /auth/register
 *         - /swagger-ui/**
 *         - /v3/api-docs/**
 *         - /actuator/health
 * }</pre>
 *
 * <h3>路径匹配规则:</h3>
 * <p>支持 Ant 风格的路径匹配符:
 * <ul>
 *   <li>{@code *} - 匹配任意字符</li>
 *   <li>{@code **} - 匹配多级路径</li>
 *   <li>{@code ?} - 匹配单个字符</li>
 * </ul>
 *
 * <h3>性能优化建议:</h3>
 * <ul>
 *   <li>在应用启动时扫描并缓存路径集合</li>
 *   <li>使用 Set 结构存储路径,提高查询效率</li>
 *   <li>路径集合不应该频繁变化,避免动态更新</li>
 *   <li>可以使用 @PostConstruct 在启动时预热</li>
 * </ul>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>匿名访问路径应该谨慎配置,避免泄露敏感信息</li>
 *   <li>生产环境应该禁用调试和测试相关的匿名访问路径</li>
 *   <li>路径匹配是前缀匹配,需要注意路径冲突</li>
 *   <li>注解和配置文件中的白名单会合并</li>
 *   <li>路径集合会在应用启动时初始化</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see com.basetc.base.security.annotation.AnonymousAccess
 * @see com.basetc.base.security.properties.BasetcSecurityAuthProperties#getWhiteList()
 */
public interface AnonymousAccessService {

    /**
     * 获取所有允许匿名访问的路径.
     *
     * <p>扫描所有控制器方法,检查是否标记了 {@code @AnonymousAccess} 注解,
     * 并结合配置文件中的白名单配置,返回所有允许匿名访问的路径集合.
     *
     * <h3>路径来源:</h3>
     * <ol>
     *   <li>配置文件白名单: {@code basetc.security.auth.white-list}</li>
     *   <li>注解标记: 标记了 {@code @AnonymousAccess} 的方法</li>
     *   <li>环境过滤: 根据注解的 env 配置过滤路径</li>
     * </ol>
     *
     * <h3>环境匹配规则:</h3>
     * <pre>{@code
     * @AnonymousAccess(env = {Env.DEV})        // 仅在开发环境允许
     * @AnonymousAccess(env = {Env.DEV, Env.TEST})  // 在开发和测试环境允许
     * @AnonymousAccess(env = {})               // 不指定环境,所有环境都允许
     * }</pre>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * @Service
     * @RequiredArgsConstructor
 *     public class SecurityConfigService {
     *
     *     private final AnonymousAccessService anonymousAccessService;
     *
     *     @PostConstruct
     *     public void initAnonymousPaths() {
     *         // 获取所有匿名访问路径
     *         Set<String> paths = anonymousAccessService.getAnonymousAccessPaths();
     *
     *         // 打印路径信息
     *         log.info("允许匿名访问的路径:");
     *         paths.forEach(path -> log.info("  - {}", path));
     *
     *         // 配置到 Spring Security
     *         configureAnonymousAccess(paths);
     *     }
     * }
     * }</pre>
     *
     * <h3>路径示例:</h3>
     * <pre>{@code
     * // 返回的路径集合示例
     * [
     *   "/api/public/**",           // 配置文件白名单
     *   "/auth/login",              // 配置文件白名单
     *   "/auth/register",           // 配置文件白名单
     *   "/swagger-ui/**",           // 配置文件白名单
     *   "/v3/api-docs/**",          // 配置文件白名单
     *   "/api/public/hello",        // @AnonymousAccess 注解
     *   "/api/dev/debug",           // @AnonymousAccess(env=DEV) 注解
     *   "/actuator/health"          // 配置文件白名单
     * ]
     * }</pre>
     *
     * <h3>在 Spring Security 中使用:</h3>
     * <pre>{@code
     * @Bean
     * public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
     *     Set<String> anonymousPaths = anonymousAccessService.getAnonymousAccessPaths();
     *
     *     http
     *         .authorizeHttpRequests(auth -> auth
     *             // 允许匿名访问
     *             .requestMatchers(anonymousPaths.toArray(new String[0])).permitAll()
     *             // 其他请求需要认证
     *             .anyRequest().authenticated()
     *         );
     *
     *     return http.build();
     * }
     * }</pre>
     *
     * @return 允许匿名访问的路径集合,如果没有任何路径则返回空集合
     * @see com.basetc.base.security.annotation.AnonymousAccess
     * @see com.basetc.base.security.properties.BasetcSecurityAuthProperties#getWhiteList()
     */
    Set<String> getAnonymousAccessPaths();

}

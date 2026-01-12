package com.basetc.base.security.autoconfigure;


import com.basetc.base.security.filter.SecurityAuthenticationFilterImpl;
import com.basetc.base.security.listener.SessionManagerListener;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.properties.BasetcSecurityCorsProperties;
import com.basetc.base.security.properties.BasetcSecurityJwtProperties;
import com.basetc.base.security.properties.BasetcSecurityPermissionsProperties;
import com.basetc.base.security.properties.BasetcSecurityProperties;
import com.basetc.base.security.properties.BasetcSecurityRedisProperties;
import com.basetc.base.security.properties.BasetcSecuritySessionProperties;
import com.basetc.base.security.service.AnonymousAccessService;
import com.basetc.base.security.service.PermissionService;
import com.basetc.base.security.service.SecurityAuthenticateService;
import com.basetc.base.security.service.SecurityAuthenticateUserService;
import com.basetc.base.security.service.SecurityOauthAuthenticateService;
import com.basetc.base.security.service.SecurityTokenGenerate;
import com.basetc.base.security.service.SecurityTokenIdGenerate;
import com.basetc.base.security.service.SecurityUserDetailService;
import com.basetc.base.security.service.impl.AccessDeniedHandlerImpl;
import com.basetc.base.security.service.impl.AnonymousAccessServiceImpl;
import com.basetc.base.security.service.impl.AuthenticationEntryPointImpl;
import com.basetc.base.security.service.impl.LogoutSuccessHandlerImpl;
import com.basetc.base.security.service.impl.PermissionServiceImpl;
import com.basetc.base.security.service.impl.RedisSecurityAuthenticateUserServiceImpl;
import com.basetc.base.security.service.impl.SecurityAuthenticateServiceImpl;
import com.basetc.base.security.service.impl.SecurityOauthAuthenticateServiceImpl;
import com.basetc.base.security.service.impl.SecurityTokenGenerateImpl;
import com.basetc.base.security.service.impl.SecurityTokenIdGenerateImpl;
import com.basetc.base.security.service.impl.SessionSecurityAuthenticateUserServiceImpl;
import com.basetc.base.security.service.suport.CaptchaAuthenticate;
import com.basetc.base.security.service.warp.SecurityAuthenticateServiceWarp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

/**
 * 安全模块自动配置类.
 *
 * <p>提供安全相关组件的自动配置功能,包括权限管理、认证服务、令牌生成等组件的初始化.
 * 通过Spring Boot的自动配置机制,根据条件自动装配安全相关的Bean.</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>权限管理: 提供权限验证和访问控制功能</li>
 *   <li>认证服务: 支持多种认证方式(用户名密码、OAuth等)</li>
 *   <li>令牌管理: JWT令牌生成和验证</li>
 *   <li>会话管理: 支持Redis和Session两种会话存储方式</li>
 *   <li>安全过滤: 集成Spring Security过滤器链</li>
 * </ul>
 *
 * <h3>配置属性</h3>
 * <ul>
 *   <li>{@link BasetcSecurityProperties}: 基础安全配置</li>
 *   <li>{@link BasetcSecurityAuthProperties}: 认证相关配置</li>
 *   <li>{@link BasetcSecurityPermissionsProperties}: 权限相关配置</li>
 *   <li>{@link BasetcSecurityJwtProperties}: JWT相关配置</li>
 *   <li>{@link BasetcSecurityRedisProperties}: Redis安全相关配置</li>
 *   <li>{@link BasetcSecuritySessionProperties}: Session安全相关配置</li>
 *   <li>{@link BasetcSecurityCorsProperties}: CORS相关配置</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在application.yml中配置
 * basetc:
 *   security:
 *     auth:
 *       white-list: /api/public/**, /health
 *       logout-url: /logout
 *     jwt:
 *       secret: your-secret-key
 *       expire-time: 3600
 *     redis:
 *       enabled: true
 *       key-prefix: security:
 * }</pre>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>开箱即用: 提供默认配置,可直接使用</li>
 *   <li>灵活扩展: 支持自定义配置和组件替换</li>
 *   <li>安全可靠: 遵循安全最佳实践</li>
 *   <li>性能优化: 高效的认证和授权机制</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @since 1.0.0
 */
@EnableConfigurationProperties({BasetcSecurityProperties.class,
        BasetcSecurityAuthProperties.class,
        BasetcSecurityPermissionsProperties.class,
        BasetcSecurityJwtProperties.class,
        BasetcSecurityRedisProperties.class,
        BasetcSecuritySessionProperties.class,
        BasetcSecurityCorsProperties.class})
@ConditionalOnBooleanProperty(prefix = "basetc.security", name = "auto-configure", matchIfMissing = true)
public class BasetcSecurityAutoConfiguration {

    /**
     * 创建匿名访问服务实例
     * 用于处理允许匿名访问的接口和资源
     *
     * @param requestMappingHandlerMapping 请求映射处理器
     * @param environment                  环境信息
     * @return 匿名访问服务实例
     */
    @Bean
    @ConditionalOnMissingBean(AnonymousAccessService.class)
    public AnonymousAccessService anonymousAccessService(
            RequestMappingHandlerMapping requestMappingHandlerMapping,
            Environment environment) {
        return new AnonymousAccessServiceImpl(requestMappingHandlerMapping, environment);
    }

    /**
     * 创建权限服务实例
     * 用于处理用户权限验证逻辑
     *
     * @param basetcSecurityPermissionsProperties 权限配置属性
     * @return 权限服务实例
     */
    @Bean("ss")
    @ConditionalOnMissingBean(PermissionService.class)
    public PermissionService permissionService(
            BasetcSecurityPermissionsProperties basetcSecurityPermissionsProperties) {
        return new PermissionServiceImpl(basetcSecurityPermissionsProperties);
    }

    /**
     * 创建安全令牌ID生成器实例
     * 用于生成唯一的安全令牌ID
     *
     * @return 安全令牌ID生成器实例
     */
    @Bean
    @ConditionalOnMissingBean(SecurityTokenIdGenerate.class)
    public SecurityTokenIdGenerate securityTokenIdGenerate() {
        return new SecurityTokenIdGenerateImpl();
    }

    /**
     * 创建安全令牌生成器实例
     * 用于生成JWT令牌
     *
     * @param basetcSecurityJwtProperties JWT配置属性
     * @return 安全令牌生成器实例
     */
    @Bean
    @ConditionalOnMissingBean(SecurityTokenGenerate.class)
    public SecurityTokenGenerate securityTokenGenerate(BasetcSecurityJwtProperties basetcSecurityJwtProperties) {
        return new SecurityTokenGenerateImpl(basetcSecurityJwtProperties);
    }

    /**
     * 创建安全认证服务实例
     * 用于处理基本认证流程
     *
     * @param captchaAuthenticate       验证码认证组件
     * @param securityUserDetailService 用户详情服务
     * @return 安全认证服务实例
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthenticateService.class)
    @SuppressWarnings("all")
    public SecurityAuthenticateService securityAuthenticateService(CaptchaAuthenticate captchaAuthenticate,
                                                                   SecurityUserDetailService securityUserDetailService) {
        return new SecurityAuthenticateServiceImpl(captchaAuthenticate, securityUserDetailService);
    }


    /**
     * 创建OAuth安全认证服务实例
     * 用于处理OAuth认证流程
     *
     * @param applicationContext 应用上下文
     * @return OAuth安全认证服务实例
     */
    @Bean
    @ConditionalOnMissingBean(SecurityOauthAuthenticateService.class)
    public SecurityOauthAuthenticateService securityOauthAuthenticateService(ApplicationContext applicationContext) {
        return new SecurityOauthAuthenticateServiceImpl(applicationContext);
    }

    /**
     * 创建基于Redis的安全认证用户服务实例
     * 用于处理基于Redis的用户会话管理
     *
     * @param redisTemplate                 Redis模板
     * @param securityTokenIdGenerate       安全令牌ID生成器
     * @param securityTokenGenerate         安全令牌生成器
     * @param basetcSecurityRedisProperties Redis安全配置属性
     * @param basetcSecurityJwtProperties   JWT配置属性
     * @param basetcSecurityAuthProperties  认证配置属性
     * @return 基于Redis的安全认证用户服务实例
     */
    @Bean
    @ConditionalOnMissingBean({SecurityAuthenticateUserService.class, RedisSecurityAuthenticateUserServiceImpl.class})
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnBooleanProperty(prefix = "basetc.security.redis", name = "enabled", matchIfMissing = true)
    public SecurityAuthenticateUserService redisSecurityAuthenticateUserService(
            RedisTemplate<String, Object> redisTemplate,
            SecurityTokenIdGenerate securityTokenIdGenerate,
            SecurityTokenGenerate securityTokenGenerate,
            BasetcSecurityRedisProperties basetcSecurityRedisProperties,
            BasetcSecurityJwtProperties basetcSecurityJwtProperties,
            BasetcSecurityAuthProperties basetcSecurityAuthProperties,
            ApplicationContext applicationContext) {
        return new RedisSecurityAuthenticateUserServiceImpl(redisTemplate,
                securityTokenIdGenerate,
                securityTokenGenerate,
                basetcSecurityRedisProperties,
                basetcSecurityJwtProperties,
                basetcSecurityAuthProperties,
                applicationContext);
    }

    /**
     * 创建Session管理监听器实例
     * 用于处理Session会话管理
     *
     * @param basetcSecuritySessionProperties Session安全配置属性
     * @return Session管理监听器实例
     */
    @Bean
    @ConditionalOnMissingBean({SessionManagerListener.class})
    public SessionManagerListener sessionManagerListener(BasetcSecuritySessionProperties basetcSecuritySessionProperties) {
        return new SessionManagerListener(basetcSecuritySessionProperties);
    }

    /**
     * 创建基于Session的安全认证用户服务实例
     * 用于处理基于Session的用户会话管理
     *
     * @param basetcSecuritySessionProperties Session安全配置属性
     * @return 基于Session的安全认证用户服务实例
     */
    @Bean
    @ConditionalOnMissingBean({SecurityAuthenticateUserService.class, SessionSecurityAuthenticateUserServiceImpl.class})
    @ConditionalOnBooleanProperty(prefix = "basetc.security.session", name = "enabled", matchIfMissing = true)
    public SecurityAuthenticateUserService sessionSecurityAuthenticateUserService(
            BasetcSecuritySessionProperties basetcSecuritySessionProperties,
            BasetcSecurityAuthProperties basetcSecurityAuthProperties,
            SessionManagerListener sessionManagerListener,
            ApplicationContext applicationContext) {
        return new SessionSecurityAuthenticateUserServiceImpl(basetcSecuritySessionProperties,
                basetcSecurityAuthProperties,sessionManagerListener, applicationContext);
    }

    /**
     * 创建安全认证服务包装器实例
     * 用于统一管理各种认证服务
     *
     * @param applicationContext               应用上下文
     * @param securityAuthenticateService      安全认证服务
     * @param securityOauthAuthenticateService OAuth安全认证服务
     * @param securityAuthenticateUserService  安全认证用户服务
     * @return 安全认证服务包装器实例
     */
    @Bean
    @ConditionalOnMissingBean(SecurityAuthenticateServiceWarp.class)
    public SecurityAuthenticateServiceWarp securityAuthenticateServiceWarp(
            ApplicationContext applicationContext,
            SecurityAuthenticateService securityAuthenticateService,
            SecurityOauthAuthenticateService securityOauthAuthenticateService,
            SecurityAuthenticateUserService securityAuthenticateUserService) {
        return new SecurityAuthenticateServiceWarp(applicationContext,
                securityAuthenticateService, securityOauthAuthenticateService, securityAuthenticateUserService);
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler accessDeniedHandler(BasetcSecurityAuthProperties basetcSecurityAuthProperties) {
        return new AccessDeniedHandlerImpl(basetcSecurityAuthProperties);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint authenticationEntryPoint(BasetcSecurityAuthProperties basetcSecurityAuthProperties) {
        return new AuthenticationEntryPointImpl(basetcSecurityAuthProperties);
    }

    @Bean
    @ConditionalOnMissingBean(LogoutSuccessHandler.class)
    public LogoutSuccessHandler logoutSuccessHandler(BasetcSecurityAuthProperties basetcSecurityAuthProperties,
                                                     SecurityAuthenticateUserService securityAuthenticateUserService,
                                                     ApplicationContext applicationContext) {
        return new LogoutSuccessHandlerImpl(basetcSecurityAuthProperties,
                securityAuthenticateUserService, applicationContext);
    }

    /**
     * 配置密码编码器.
     *
     * <p>使用BCrypt算法进行密码加密，提供安全的密码存储方案.
     *
     * @return BCrypt密码编码器
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 创建安全过滤链实例
     * 用于处理安全过滤
     *
     * @param httpSecurity                 HTTP安全配置
     * @param authenticateUserService      安全认证用户服务
     * @param accessDeniedHandler          访问拒绝处理器
     * @param authenticationEntryPoint     认证入口点
     * @param securityLogoutSuccessHandler 安全登出成功处理器
     * @param anonymousAccessService       匿名访问服务
     * @param basetcSecurityProperties     认证安全配置属性
     * @return 安全过滤链实例
     *
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   SecurityAuthenticateUserService authenticateUserService,
                                                   AccessDeniedHandler accessDeniedHandler,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   LogoutSuccessHandler securityLogoutSuccessHandler,
                                                   AnonymousAccessService anonymousAccessService,
                                                   BasetcSecurityProperties basetcSecurityProperties) {

        final BasetcSecurityAuthProperties auth = basetcSecurityProperties.getAuth();
        // CSRF
        httpSecurity.csrf(csrf -> {
            if (auth.isCsrfEnabled()) {
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
            } else {
                csrf.disable();
            }
        });
        // SESSION
        httpSecurity.sessionManagement(session -> {
            if (basetcSecurityProperties.redisEnabled()) {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            } else {
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            }
        });
        // authorizeHttpRequests
        httpSecurity.authorizeHttpRequests(request -> {
            // 白名单
            request.requestMatchers(auth.getWhiteList().toArray(new String[0])).permitAll();
            // 注解白名单
            Set<String> anonymousAccessPaths = anonymousAccessService.getAnonymousAccessPaths();
            request.requestMatchers(anonymousAccessPaths.toArray(new String[0])).permitAll();
            request.anyRequest().authenticated();
        });
        // 异常处理
        httpSecurity.exceptionHandling(ex -> {
            ex.accessDeniedHandler(accessDeniedHandler)
                    .authenticationEntryPoint(authenticationEntryPoint);
        });
        // 退出
        httpSecurity.logout(logout -> {
            logout.logoutUrl(auth.getLogoutUrl())
                    .logoutSuccessHandler(securityLogoutSuccessHandler);
        });
        // CORS
        if (!basetcSecurityProperties.getCors().isEnabled()) {
            httpSecurity.cors(AbstractHttpConfigurer::disable);
        } else {
            httpSecurity.cors(cors -> {
                cors.configurationSource(request -> {
                    BasetcSecurityCorsProperties propertiesCors = basetcSecurityProperties.getCors();
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOriginPatterns(propertiesCors.getAllowedOriginPatterns());
                    configuration.setAllowedMethods(propertiesCors.getAllowedMethods());
                    configuration.setAllowedHeaders(propertiesCors.getAllowedHeaders());
                    configuration.setAllowCredentials(propertiesCors.getAllowCredentials());
                    configuration.setMaxAge(propertiesCors.getMaxAge());
                    return configuration;
                });
            });
        }

        httpSecurity.addFilterBefore(new SecurityAuthenticationFilterImpl(authenticateUserService, auth),
                UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }


}

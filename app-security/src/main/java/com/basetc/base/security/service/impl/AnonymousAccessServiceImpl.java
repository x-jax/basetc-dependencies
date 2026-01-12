package com.basetc.base.security.service.impl;


import com.basetc.base.security.annotation.AnonymousAccess;
import com.basetc.base.security.service.AnonymousAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 匿名访问服务实现类
 * 实现获取允许匿名访问路径的功能
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class AnonymousAccessServiceImpl implements AnonymousAccessService {

    /**
     * 请求映射处理器映射
     */
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    
    /**
     * 环境信息
     */
    private final Environment environment;

    /**
     * 获取所有允许匿名访问的路径.
     *
     * <p>扫描所有控制器方法，检查是否标记了 {@link AnonymousAccessService} 注解， 并根据注解中指定的环境配置和当前激活的环境，判断是否应该允许匿名访问.
     *
     * @return 允许匿名访问的路径集合
     */
    @Override
    public Set<String> getAnonymousAccessPaths() {
        Set<String> anonymousPaths = new HashSet<>();
        // 获取当前激活的环境配置
        String[] activeProfiles = environment.getActiveProfiles();
        // 遍历所有的RequestMapping
        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            RequestMappingInfo mappingInfo = entry.getKey();
            // 检查方法上的注解
            AnonymousAccess methodAnnotation = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
            // 检查类上的注解
            AnonymousAccess classAnnotation =
                    handlerMethod.getBeanType().getAnnotation(AnonymousAccess.class);
            AnonymousAccess annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
            if (annotation != null && shouldAllowAnonymousAccess(annotation, activeProfiles)) {
                // 获取该方法的所有 URL 路径
                Set<String> patterns = mappingInfo.getPatternValues();
                anonymousPaths.addAll(patterns);
                if (log.isDebugEnabled()) {
                    log.debug("已添加匿名访问路径: {} (环境: {})", patterns, Arrays.toString(annotation.value()));
                }
            }
        }
        if (log.isInfoEnabled() && !anonymousPaths.isEmpty()) {
            log.info("共发现 {} 个匿名访问路径: {}", anonymousPaths.size(), anonymousPaths);
        }
        return anonymousPaths;
    }

    /**
     * 判断在当前环境下是否应该允许匿名访问.
     *
     * <p>如果注解未指定环境（value为空），则在所有环境下都允许访问. 如果注解指定了环境，则只有当前激活的环境与指定环境匹配时才允许访问.
     *
     * @param annotation 匿名访问注解
     * @param activeProfiles 当前激活的环境配置
     * @return 如果应该允许匿名访问返回 true，否则返回 false
     */
    private boolean shouldAllowAnonymousAccess(AnonymousAccess annotation, String[] activeProfiles) {
        String[] allowedProfiles = annotation.value();
        // 如果未指定环境，则在所有环境下都允许
        if (allowedProfiles == null || allowedProfiles.length == 0) {
            return true;
        }
        // 如果没有激活的环境，但注解指定了环境，则不允许
        if (activeProfiles == null) {
            return false;
        }
        // 检查当前激活的环境是否在允许的环境列表中
        for (String activeProfile : activeProfiles) {
            for (String allowedProfile : allowedProfiles) {
                if (activeProfile.equals(allowedProfile)) {
                    return true;
                }
            }
        }

        return false;
    }
}

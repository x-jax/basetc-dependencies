package com.basetc.base.security.service.warp;


import com.basetc.base.security.domain.AuthenticateRequest;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.domain.OauthAuthenticateRequest;
import com.basetc.base.security.event.LoginEvent;
import com.basetc.base.security.service.SecurityAuthenticateService;
import com.basetc.base.security.service.SecurityAuthenticateUserService;
import com.basetc.base.security.service.SecurityOauthAuthenticateService;
import com.basetc.base.security.service.suport.SecurityAuthenticateAfter;
import com.basetc.base.security.service.suport.SecurityAuthenticateBefore;
import com.basetc.base.security.service.suport.SecurityOauthAuthenticateBefore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Objects;

/**
 * 安全认证服务包装器
 * 统一处理认证流程，包括前置处理、认证执行和后置处理
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SecurityAuthenticateServiceWarp  {

    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;
    
    /**
     * 安全认证服务
     */
    private final SecurityAuthenticateService securityAuthenticateService;
    
    /**
     * OAuth安全认证服务
     */
    private final SecurityOauthAuthenticateService securityOauthAuthenticateService;
    
    /**
     * 安全认证用户服务
     */
    private final SecurityAuthenticateUserService securityAuthenticateUserService;

    /**
     * 执行普通认证
     * 包括前置处理、认证执行和后置处理
     *
     * @param authenticateRequest 认证请求信息
     * @return 认证成功后生成的令牌
     */
    public String authenticate(AuthenticateRequest authenticateRequest) {
        // 获取所有认证前置处理器并执行
        Map<String, SecurityAuthenticateBefore> beansOfType = applicationContext.getBeansOfType(SecurityAuthenticateBefore.class);
        for (SecurityAuthenticateBefore before : beansOfType.values()) {
            before.doBefore(authenticateRequest);
        }
        // 执行认证
        LoginUser loginUser =  securityAuthenticateService.authenticate(authenticateRequest);
        // 获取所有认证后置处理器并执行
        Map<String, SecurityAuthenticateAfter> ofType = applicationContext.getBeansOfType(SecurityAuthenticateAfter.class);
        for (SecurityAuthenticateAfter after : ofType.values()) {
            after.doAfter(loginUser);
        }
        // 创建并返回认证令牌
        String token = securityAuthenticateUserService.createToken(loginUser);
        LoginEvent password = new LoginEvent(loginUser, loginUser.getGrantType(), true, null);
        applicationContext.publishEvent(password);
        return token;
    }

    /**
     * 执行OAuth认证
     * 包括前置处理、认证执行和后置处理
     *
     * @param authenticateRequest OAuth认证请求信息
     * @return 认证成功后生成的令牌
     */
    public String authenticate(OauthAuthenticateRequest authenticateRequest) {
        // 获取所有OAuth认证前置处理器并执行
        Map<String, SecurityOauthAuthenticateBefore> beansOfType = applicationContext.getBeansOfType(SecurityOauthAuthenticateBefore.class);
        for (SecurityOauthAuthenticateBefore before : beansOfType.values()) {
            before.doBefore(authenticateRequest);
        }
        // 执行OAuth认证
        LoginUser loginUser =  securityOauthAuthenticateService.authenticate(authenticateRequest);
        // 获取所有认证后置处理器并执行
        Map<String, SecurityAuthenticateAfter> ofType = applicationContext.getBeansOfType(SecurityAuthenticateAfter.class);
        for (SecurityAuthenticateAfter after : ofType.values()) {
            after.doAfter(loginUser);
        }
        String token = securityAuthenticateUserService.createToken(loginUser);
        LoginEvent password = new LoginEvent(loginUser, loginUser.getGrantType(), true, null);
        applicationContext.publishEvent(password);
        return token;
    }

}

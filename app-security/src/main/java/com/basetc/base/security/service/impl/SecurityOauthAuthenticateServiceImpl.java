package com.basetc.base.security.service.impl;


import com.basetc.base.common.exception.BasetcException;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.domain.OauthAuthenticateRequest;
import com.basetc.base.security.service.SecurityOauthAuthenticateService;
import com.basetc.base.security.service.suport.SecurityOauthAuthenticate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OAuth安全认证服务实现类
 * 实现OAuth认证相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SecurityOauthAuthenticateServiceImpl implements InitializingBean, SecurityOauthAuthenticateService {

    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;

    private final Map<String, SecurityOauthAuthenticate> securityOauthAuthenticateMap = new HashMap<>();

    /**
     * 认证
     *
     * @param authenticateRequest 认证请求
     * @return 登录用户
     */
    @Override
    public LoginUser authenticate(@NonNull OauthAuthenticateRequest authenticateRequest) {

        SecurityOauthAuthenticate securityOauthAuthenticate = securityOauthAuthenticateMap.get(authenticateRequest.getGrantType());
        if (Objects.isNull(securityOauthAuthenticate)) {
            throw new BasetcException("暂不支持的授权类型");
        }
        LoginUser loginUser = securityOauthAuthenticate.authenticate(authenticateRequest);
        if(Objects.nonNull(loginUser) && Objects.isNull(loginUser.getGrantType())){
            loginUser.setGrantType(authenticateRequest.getGrantType());
        }
        return loginUser;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, SecurityOauthAuthenticate> beansOfType = applicationContext.getBeansOfType(SecurityOauthAuthenticate.class);
        beansOfType.forEach((k, v) -> securityOauthAuthenticateMap.put(v.grantType(), v));
    }
}

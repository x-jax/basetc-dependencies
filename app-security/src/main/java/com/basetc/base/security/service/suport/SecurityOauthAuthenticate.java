package com.basetc.base.security.service.suport;


import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.domain.OauthAuthenticateRequest;

/**
 * OAuth安全认证支持接口
 * 定义OAuth认证相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */

public interface SecurityOauthAuthenticate {

    /**
     * 获取授权类型
     *
     * @return 授权类型字符串
     */
    String grantType();

    /**
     * 执行OAuth认证
     *
     * @param request OAuth认证请求信息
     * @return 认证成功的用户信息
     */
    LoginUser authenticate(OauthAuthenticateRequest request);

}

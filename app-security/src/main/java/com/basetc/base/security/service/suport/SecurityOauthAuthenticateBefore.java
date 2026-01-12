package com.basetc.base.security.service.suport;


import com.basetc.base.security.domain.OauthAuthenticateRequest;
import org.jspecify.annotations.NonNull;

/**
 * OAuth安全认证前置处理支持接口
 * 定义OAuth认证前的预处理方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public interface SecurityOauthAuthenticateBefore {

    /**
     * 执行OAuth认证前置处理
     *
     * @param request OAuth认证请求信息，不能为空
     */
    void doBefore(@NonNull OauthAuthenticateRequest request);

}

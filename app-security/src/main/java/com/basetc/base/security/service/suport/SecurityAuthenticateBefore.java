package com.basetc.base.security.service.suport;


import com.basetc.base.security.domain.AuthenticateRequest;
import org.jspecify.annotations.NonNull;

/**
 * 安全认证前置处理支持接口
 * 定义认证前的预处理方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public interface SecurityAuthenticateBefore {

    /**
     * 执行认证前置处理
     *
     * @param request 认证请求信息，不能为空
     */
    void doBefore(@NonNull AuthenticateRequest request);

}

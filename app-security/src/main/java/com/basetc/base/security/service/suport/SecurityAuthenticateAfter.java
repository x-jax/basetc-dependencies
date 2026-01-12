package com.basetc.base.security.service.suport;


import com.basetc.base.security.domain.AuthenticateRequest;
import com.basetc.base.security.domain.LoginUser;
import org.jspecify.annotations.NonNull;

/**
 * 安全认证后置处理支持接口
 * 定义认证成功后的后置处理方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public interface SecurityAuthenticateAfter {


    /**
     * 执行认证后置处理
     *
     * @param loginUser 认证成功的用户信息，不能为空
     */
    void doAfter(@NonNull LoginUser loginUser);

}

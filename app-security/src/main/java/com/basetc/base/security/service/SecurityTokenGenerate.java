package com.basetc.base.security.service;


import com.basetc.base.security.domain.LoginUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全令牌生成服务接口
 * 定义令牌生成和解析相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */

public interface SecurityTokenGenerate {

    /**
     * 令牌载荷接口
     * 定义令牌中包含的用户信息
     */
    interface TokenPayload {
       /**
        * 获取用户ID
        *
        * @return 用户ID
        */
       Long getUserId();
       
       /**
        * 获取令牌ID
        *
        * @return 令牌ID
        */
       String getTokenId();
    }

    /**
     * 从HTTP请求中解析令牌载荷信息
     *
     * @param request HTTP请求对象
     * @return 令牌载荷信息
     */
    TokenPayload parseTokenId(HttpServletRequest request);

    /**
     * 为登录用户生成令牌
     *
     * @param loginUser 登录用户信息
     * @return 生成的令牌字符串
     */
    String generate(LoginUser loginUser);

}

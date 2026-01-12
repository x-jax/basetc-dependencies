package com.basetc.base.security.domain;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 认证请求实体类
 * 用于封装用户认证所需的信息
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
public class AuthenticateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -120256752554408397L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码ID
     */
    private String captchaId;

}

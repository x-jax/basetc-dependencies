package com.basetc.base.security.domain;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth认证请求实体类
 * 用于封装OAuth认证所需的信息
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
public class OauthAuthenticateRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = -3805748867328470346L;

    /**
     * 授权类型
     */
    private String grantType;
    
    /**
     * 授权码
     */
    private String code;
    
    /**
     * 状态参数
     */
    private String state;


}

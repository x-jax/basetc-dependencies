package com.basetc.base.security.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全JWT配置属性类
 * 定义JWT令牌相关的配置属性，包括令牌头、前缀、过期时间等
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "basetc.security.jwt")
public class BasetcSecurityJwtProperties {

    /**
     * 令牌头
     */
    private String header = "Authorization";

    /**
     * 令牌前缀
     */
    private String prefix = "Bearer ";

    /**
     * 令牌过期时间.单位分钟
     */
    private Long expire = 30L;

    /**
     * 当过期时间小于指定间隔时间则刷新
     */
    private Long refreshScope = expire / 2;

    /**
     * 令牌密钥
     */
    private String secret = "BaseTC:139fc8d7b794540fa52621ec8c211a82";

}

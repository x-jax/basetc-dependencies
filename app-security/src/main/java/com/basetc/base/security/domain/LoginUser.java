package com.basetc.base.security.domain;


import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户抽象类
 * 继承Spring Security的UserDetails，提供用户认证和授权相关信息
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
public class LoginUser implements UserDetails {


    @Serial
    private static final long serialVersionUID = 5082752755996653555L;

    /**
     * 授权类型
     */
    private String grantType;
    /**
     * 用户ID
     */
    private Long userId;

    private String username;

    private String password;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 角色集合
     */
    private Set<String> roles;

    /**
     * 权限集合
     */
    private Set<String> permissions;

    /**
     * 令牌ID
     */
    private String tokenId;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 请求User-Agent
     */
    private String requestUserAgent;

    /** 用户属性. */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 获取用户的权限集合.
     *
     * <p>Spring Security用于授权的方法. 将用户的权限转换为Spring Security的GrantedAuthority格式.
     *
     * @return 权限集合，不为null
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    /**
     * 检查账户是否未过期.
     *
     * <p>Spring Security用于认证的方法. 当前实现默认返回true，表示账户永不过期.
     *
     * @return 账户是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 检查账户是否未锁定.
     *
     * <p>Spring Security用于认证的方法. 当前实现默认返回true，表示账户永不锁定.
     *
     * @return 账户是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 检查凭证是否未过期.
     *
     * <p>Spring Security用于认证的方法. 当前实现默认返回true，表示凭证永不过期.
     *
     * @return 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 检查账户是否启用.
     *
     * <p>Spring Security用于认证的方法. 当前实现默认返回true，表示账户始终启用.
     *
     * @return 账户是否启用
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}

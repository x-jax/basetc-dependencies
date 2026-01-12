package com.basetc.base.security.service.impl;


import com.basetc.base.common.exception.BasetcException;
import com.basetc.base.common.utils.DateAppendUtils;
import com.basetc.base.common.utils.IpUtils;
import com.basetc.base.common.utils.RequestUtils;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.event.LogoutEvent;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.properties.BasetcSecurityJwtProperties;
import com.basetc.base.security.properties.BasetcSecurityRedisProperties;
import com.basetc.base.security.service.SecurityAuthenticateUserService;
import com.basetc.base.security.service.SecurityTokenGenerate;
import com.basetc.base.security.service.SecurityTokenIdGenerate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的安全认证用户服务实现类
 * 实现用户会话管理和令牌操作相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class RedisSecurityAuthenticateUserServiceImpl implements SecurityAuthenticateUserService {

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 安全令牌ID生成器
     */
    private final SecurityTokenIdGenerate securityTokenIdGenerate;

    /**
     * 安全令牌生成器
     */
    private final SecurityTokenGenerate securityTokenGenerate;

    /**
     * 安全Redis配置属性
     */
    private final BasetcSecurityRedisProperties basetcSecurityRedisProperties;

    /**
     * 安全JWT配置属性
     */
    private final BasetcSecurityJwtProperties basetcSecurityJwtProperties;

    /**
     * 安全认证配置属性
     */
    private final BasetcSecurityAuthProperties basetcSecurityAuthProperties;

    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;


    /**
     * 从HTTP请求中获取当前登录用户信息.
     *
     * <p>通过解析请求头或请求参数中的令牌，获取已登录用户的详细信息. 如果请求中没有有效的令牌或令牌已失效，可能返回 null.
     *
     * @param request HTTP请求对象
     * @return 登录用户信息，如果未登录或令牌无效则返回 null
     */
    @Override
    public LoginUser getLoginUser(HttpServletRequest request) {
        String redisKey = getRedisKey(request);
        if (Objects.nonNull(redisKey)) {
            Object o = redisTemplate.opsForValue().get(redisKey);
            if (o instanceof LoginUser loginUser) {
                long expireTime = loginUser.getExpireTime();
                long time = DateAppendUtils.addMinutes(new Date(expireTime),
                        -basetcSecurityJwtProperties.getRefreshScope().intValue()).getTime();
                if (System.currentTimeMillis() >= time) {
                    refreshToken(loginUser);
                }
                return loginUser;
            }
        }
        return null;
    }

    /**
     * 登出当前用户.
     *
     * <p>从HTTP请求中获取当前登录用户信息，并注销当前用户.
     *
     * @param request HTTP请求对象
     */
    @Override
    public void logout(HttpServletRequest request) {
        LoginUser loginUser = getLoginUser(request);
        if (Objects.nonNull(loginUser)) {
            redisTemplate.delete(getRedisKey(loginUser));
        }
    }

    /**
     * 登出当前用户.
     *
     * <p>从HTTP请求中获取当前登录用户信息，并注销当前用户.
     *
     * @param loginUser 登录用户信息
     */
    @Override
    public void logout(LoginUser loginUser) {
        if (Objects.nonNull(loginUser)) {
            redisTemplate.delete(getRedisKey(loginUser));
        }
    }

    /**
     * 刷新.
     *
     * <p>根据给定的登录用户信息刷新令牌. 具体实现可能基于JWT、Session或其他认证机制.
     *
     * @param loginUser 登录用户信息
     */
    @Override
    public void refreshToken(LoginUser loginUser) {
        String redisKey = getRedisKey(loginUser);
        redisTemplate.opsForValue().set(redisKey, loginUser);
        redisTemplate.expire(redisKey, basetcSecurityJwtProperties.getExpire(), TimeUnit.MINUTES);
    }

    /**
     * 创建访问令牌.
     *
     * <p>根据给定的登录用户信息创建访问令牌. 具体实现可能基于JWT、Session或其他认证机制.
     *
     * @param loginUser 登录用户信息
     * @return 创建的访问令牌
     */
    @Override
    public String createToken(LoginUser loginUser) {
        String uid = securityTokenIdGenerate.generate(loginUser);
        loginUser.setTokenId(uid);
        loginUser.setLoginIp(IpUtils.getIpAddr());
        loginUser.setRequestUserAgent(RequestUtils.getHeader("User-Agent"));

        boolean singleEnabled = basetcSecurityAuthProperties.getFilter().isSingleEnabled();

        if (singleEnabled && !checkSignLogin(loginUser)) {
            // 覆盖旧登录
            if (basetcSecurityAuthProperties.getFilter().isOverwriteOldAuth()) {
                Set<String> redisKeys = getRedisKeys(loginUser);
                if (Objects.nonNull(redisKeys) && !redisKeys.isEmpty()) {
                    for (String redisKey : redisKeys) {
                        Object o = redisTemplate.opsForValue().get(redisKey);
                        if (Objects.nonNull(o) && o instanceof LoginUser loginUser1) {
                            redisTemplate.delete(redisKey);
                            applicationContext.publishEvent(new LogoutEvent(loginUser1, "顶号,强制下线"));
                        }
                    }
                }

            } else {
                throw new BasetcException("当前账号已在其他地点登录, 如不是本人操作请立即修改密码");
            }
        }
        Date date = DateAppendUtils.addHours(new Date(), basetcSecurityJwtProperties.getExpire().intValue());
        loginUser.setExpireTime(date.getTime());

        String token = securityTokenGenerate.generate(loginUser);
        redisTemplate.opsForValue().set(getRedisKey(loginUser), loginUser);
        redisTemplate.expire(getRedisKey(loginUser), basetcSecurityJwtProperties.getExpire(), TimeUnit.MINUTES);
        return token;
    }

    /**
     * 检查单点登录.
     *
     * <p>检查当前用户是否已登录，如果已登录，则返回 true，否则返回 false.
     *
     * @param loginUser 登录用户信息
     * @return true 表示已登录，false 表示未登录
     */
    protected boolean checkSignLogin(LoginUser loginUser) {
        Set<String> keys = getRedisKeys(loginUser);
        return Objects.isNull(keys) || keys.isEmpty();
    }

    protected Set<String> getRedisKeys(LoginUser loginUser) {
        String redisKeyPrefix = basetcSecurityRedisProperties.getRedisKeyPrefix();
        String redisKey = redisKeyPrefix + ":" + loginUser.getUserId() + ":*";
        return redisTemplate.keys(redisKey);
    }

    protected String getRedisKey(LoginUser loginUser) {
        String redisKeyPrefix = basetcSecurityRedisProperties.getRedisKeyPrefix();
        return redisKeyPrefix + ":" + loginUser.getUserId() + ":" + loginUser.getTokenId();
    }

    protected String getRedisKey(HttpServletRequest request) {
        SecurityTokenGenerate.TokenPayload payload = securityTokenGenerate.parseTokenId(request);
        if (Objects.isNull(payload)) {
            return null;
        }
        String redisKeyPrefix = basetcSecurityRedisProperties.getRedisKeyPrefix();
        return redisKeyPrefix + ":" + payload.getUserId() + ":" + payload.getTokenId();
    }
}

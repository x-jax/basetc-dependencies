package com.basetc.base.security.service.impl;


import com.basetc.base.security.context.PasswordScoped;
import com.basetc.base.security.domain.AuthenticateRequest;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.service.SecurityAuthenticateService;
import com.basetc.base.security.service.SecurityUserDetailService;
import com.basetc.base.security.service.suport.CaptchaAuthenticate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * 安全认证服务实现类.
 *
 * <p>实现用户认证相关的方法,提供用户名密码认证、验证码校验等安全认证功能.</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>用户名密码认证: 验证用户提供凭证的有效性</li>
 *   <li>验证码校验: 防止暴力破解攻击</li>
 *   <li>用户信息加载: 从安全用户详情服务加载用户信息</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建认证请求
 * AuthenticateRequest request = AuthenticateRequest.builder()
 *     .username("admin")
 *     .password("password")
 *     .captcha("abc123")
 *     .captchaId("captcha-id")
 *     .build();
 *
 * // 执行认证
 * LoginUser loginUser = securityAuthenticateService.authenticate(request);
 * }</pre>
 *
 * <h3>设计特点</h3>
 * <ul>
 *   <li>可插拔验证: 支持启用或禁用验证码校验</li>
 *   <li>安全防护: 结合验证码防止暴力破解</li>
 *   <li>集成用户服务: 与安全用户详情服务紧密集成</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see AuthenticateRequest
 * @see LoginUser
 * @see SecurityUserDetailService
 * @see CaptchaAuthenticate
 */
@RequiredArgsConstructor
public class SecurityAuthenticateServiceImpl implements SecurityAuthenticateService {

        /**
     * 验证码认证组件.
     * <p>用于校验用户提交的验证码是否正确,防止暴力破解攻击.</p>
     */
    private final CaptchaAuthenticate captchaAuthenticate;
    
        /**
     * 安全用户详情服务.
     * <p>用于根据用户名加载用户详细信息,包括用户权限、角色等.</p>
     */
    private final SecurityUserDetailService securityUserDetailService;

        /**
     * 执行用户身份认证.
     *
     * <p>根据提供的认证请求信息,执行用户身份验证流程.如果配置了验证码认证,
     * 会先校验验证码的正确性,然后验证用户名和密码.</p>
     *
     * <h3>认证流程</h3>
     * <ol>
     *   <li>校验验证码(如果启用了验证码功能)</li>
     *   <li>验证用户名和密码</li>
     *   <li>加载用户详细信息</li>
     *   <li>返回认证结果</li>
     * </ol>
     *
     * @param authenticateRequest 认证请求对象,包含用户名、密码、验证码等信息
     * @return 认证成功的用户信息,如果认证失败则返回null
     * @throws IllegalArgumentException 如果认证请求为null
     * @see AuthenticateRequest
     * @see LoginUser
     */
    @Override
    public LoginUser authenticate(@NonNull AuthenticateRequest authenticateRequest) {
        if (Objects.nonNull(captchaAuthenticate)) {
            captchaAuthenticate.doCaptcha(authenticateRequest.getCaptcha(), authenticateRequest.getCaptchaId());
        }
        if (Objects.nonNull(securityUserDetailService)) {
            LoginUser loginUser = securityUserDetailService.loadUserByUsername(authenticateRequest.getUsername(),
                    authenticateRequest.getPassword());
            if(Objects.nonNull(loginUser) && Objects.isNull(loginUser.getGrantType())){
                loginUser.setGrantType("username&password");
            }
            return loginUser;
        }
        return null;
    }
}

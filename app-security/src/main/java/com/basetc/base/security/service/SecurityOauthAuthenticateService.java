package com.basetc.base.security.service;

import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.domain.OauthAuthenticateRequest;
import org.jspecify.annotations.NonNull;

/**
 * OAuth 安全认证服务接口,定义 OAuth 认证的核心方法.
 *
 * <p>此接口用于处理第三方登录 (OAuth 2.0) 认证流程,
 * 支持如 GitHub、Google、Facebook、微信、QQ 等第三方平台登录.
 *
 * <h3>OAuth 2.0 认证流程:</h3>
 * <ol>
 *   <li>用户点击第三方登录按钮</li>
 *   <li>重定向到第三方平台授权页面</li>
 *   <li>用户在第三方平台授权</li>
 *   <li>第三方平台回调应用,携带授权码</li>
 *   <li>应用通过授权码获取访问令牌</li>
 *   <li>应用通过访问令牌获取用户信息</li>
 *   <li>authenticate() - 验证并创建/更新本地用户</li>
 *   <li>生成本地访问令牌,完成登录</li>
 * </ol>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // OAuth 回调接口示例
 * @GetMapping("/oauth/callback/{provider}")
 * public R<LoginUser> oauthCallback(String provider, String code, String state) {
 *     OauthAuthenticateRequest request = new OauthAuthenticateRequest();
 *     request.setProvider(provider);
 *     request.setCode(code);
 *     request.setState(state);
 *     LoginUser user = oauthAuthenticateService.authenticate(request);
 *     return R.success(user);
 * }
 * }</pre>
 *
 * <h3>支持的第三方平台:</h3>
 * <ul>
 *   <li>GitHub</li>
 *   <li>Google</li>
 *   <li>Facebook</li>
 *   <li>微信 (WeChat)</li>
 *   <li>QQ</li>
 *   <li>微博 (Weibo)</li>
 *   <li>钉钉 (DingTalk)</li>
 *   <li>企业微信</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see SecurityAuthenticateService
 */
public interface SecurityOauthAuthenticateService {

    /**
     * 执行 OAuth 用户认证.
     *
     * <p>根据提供的 OAuth 认证请求信息,通过第三方平台验证用户身份,
     * 并创建或更新本地用户账号,最后返回登录用户信息.
     *
     * @param authenticateRequest OAuth 认证请求信息,包含 provider、code、state 等,不能为 {@code null}
     * @return 认证成功的用户信息,包含用户基本信息、角色、权限和访问令牌
     */
    LoginUser authenticate(@NonNull OauthAuthenticateRequest authenticateRequest);

}

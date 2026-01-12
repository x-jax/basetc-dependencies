package com.basetc.base.security.service;

import com.basetc.base.security.domain.AuthenticateRequest;
import com.basetc.base.security.domain.LoginUser;
import org.jspecify.annotations.NonNull;

/**
 * 安全认证服务接口,定义用户认证的核心方法.
 *
 * <p>此接口负责处理用户登录认证流程,包括:
 * <ul>
 *   <li>用户名和密码验证</li>
 *   <li>加载用户信息和权限</li>
 *   <li>构建登录用户上下文</li>
 * </ul>
 *
 * <h3>认证流程:</h3>
 * <pre>{@code
 * 1. 接收认证请求 (用户名、密码等)
 * 2. 调用 UserDetailsService 加载用户信息
 * 3. 验证密码 (使用 PasswordEncoder)
 * 4. 加载用户角色和权限
 * 5. 构建 LoginUser 对象
 * 6. 生成访问令牌 (JWT 或 Session)
 * 7. 返回认证结果
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class AuthServiceImpl implements SecurityAuthenticateService {
 *
 *     private final SecurityUserDetailService userDetailService;
 *     private final SecurityAuthenticateUserService authenticateUserService;
 *
 *     @Override
 *     public LoginUser authenticate(@NonNull AuthenticateRequest request) {
 *         // 1. 加载用户信息
 *         LoginUser user = userDetailService.loadUserByUsername(
 *             request.getUsername(),
 *             request.getPassword()
 *         );
 *
 *         // 2. 验证账号状态
 *         if (!user.isEnabled()) {
 *             throw new DisabledException("账号已禁用");
 *         }
 *
 *         // 3. 创建访问令牌
 *         String token = authenticateUserService.createToken(user);
 *         user.setToken(token);
 *
 *         // 4. 返回登录用户信息
 *         return user;
 *     }
 * }
 * }</pre>
 *
 * <h3>在 Controller 中使用:</h3>
 * <pre>{@code
 * @RestController
 * @RequiredArgsConstructor
 * public class AuthController {
 *
 *     private final SecurityAuthenticateService authenticateService;
 *
 *     @PostMapping("/login")
 *     public R<LoginUser> login(@RequestBody AuthenticateRequest request) {
 *         try {
 *             LoginUser user = authenticateService.authenticate(request);
 *             return R.success(user);
 *         } catch (AuthenticationException e) {
 *             return R.error(401, "用户名或密码错误");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>异常处理:</h3>
 * <pre>{@code
 * try {
 *     LoginUser user = authenticateService.authenticate(request);
 * } catch (UsernameNotFoundException e) {
 *     // 用户不存在
 *     return R.error(404, "用户不存在");
 * } catch (BadCredentialsException e) {
 *     // 密码错误
 *     return R.error(401, "密码错误");
 * } catch (DisabledException e) {
 *     // 账号已禁用
 *     return R.error(403, "账号已禁用");
 * } catch (LockedException e) {
 *     // 账号已锁定
 *     return R.error(403, "账号已锁定");
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>认证成功后需要创建访问令牌并存储</li>
 *   <li>密码验证应该使用 PasswordEncoder,不要手动比较</li>
 *   <li>需要检查账号状态 (启用、锁定、过期等)</li>
 *   <li>认证失败应该抛出明确的异常信息</li>
 *   <li>敏感信息 (如密码) 不应该存储在 LoginUser 中</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see SecurityUserDetailService
 * @see SecurityAuthenticateUserService
 * @see org.springframework.security.authentication.AuthenticationManager
 */
public interface SecurityAuthenticateService {

    /**
     * 执行用户认证.
     *
     * <p>根据提供的认证请求信息进行用户登录认证,
     * 认证成功后返回完整的用户信息,包括用户基本信息、角色和权限列表.
     *
     * <h3>认证步骤:</h3>
     * <ol>
     *   <li>验证请求参数的合法性</li>
     *   <li>调用 {@link SecurityUserDetailService#loadUserByUsername(String, String)} 加载用户信息</li>
     *   <li>验证密码正确性</li>
     *   <li>检查账号状态 (启用、锁定、过期等)</li>
     *   <li>加载用户角色和权限</li>
     *   <li>构建并返回 {@link LoginUser} 对象</li>
     * </ol>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 构建认证请求
     * AuthenticateRequest request = new AuthenticateRequest();
     * request.setUsername("admin");
     * request.setPassword("123456");
     *
     * // 执行认证
     * LoginUser user = authenticateService.authenticate(request);
     *
     * // 认证成功,获取令牌
     * String token = user.getToken();
     * </pre>
     *
     * @param authenticateRequest 认证请求信息,包含用户名、密码等,不能为 {@code null}
     * @return 认证成功的用户信息,包含用户基本信息、角色、权限和访问令牌
     * @throws org.springframework.security.authentication.BadCredentialsException 密码错误时抛出
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException 用户不存在时抛出
     * @throws org.springframework.security.authentication.DisabledException 账号已禁用时抛出
     * @throws org.springframework.security.authentication.LockedException 账号已锁定时抛出
     * @see SecurityUserDetailService#loadUserByUsername(String, String)
     */
    LoginUser authenticate(@NonNull AuthenticateRequest authenticateRequest);

}

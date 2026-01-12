package com.basetc.base.security.service;

import com.basetc.base.security.context.PasswordScoped;
import com.basetc.base.security.domain.LoginUser;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 安全用户详情服务接口,扩展 Spring Security 的 UserDetailsService.
 *
 * <p>此接口继承 Spring Security 的 {@link UserDetailsService},
 * 用于在认证过程中加载用户详细信息,包括用户基本信息、角色、权限等.
 *
 * <p>主要功能:
 * <ul>
 *   <li>根据用户名加载用户详情</li>
 *   <li>验证用户密码</li>
 *   <li>加载用户角色和权限</li>
 *   <li>检查账号状态 (启用、锁定、过期等)</li>
 * </ul>
 *
 * <h3>认证流程中的角色:</h3>
 * <pre>{@code
 * 1. 用户提交登录请求
 *    ↓
 * 2. SecurityAuthenticateService.authenticate()
 *    ↓
 * 3. SecurityUserDetailService.loadUserByUsername(username, password)
 *    ↓
 * 4. 查询数据库获取用户信息
 *    ↓
 * 5. 验证密码 (使用 PasswordEncoder)
 *    ↓
 * 6. 加载用户角色和权限
 *    ↓
 * 7. 构建 LoginUser 对象并返回
 *    ↓
 * 8. 创建访问令牌
 * }</pre>
 *
 * <h3>实现示例:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class UserDetailsServiceImpl implements SecurityUserDetailService {
 *
 *     private final UserMapper userMapper;
 *     private final PasswordEncoder passwordEncoder;
 *
 *     @Override
 *     public LoginUser loadUserByUsername(String username, String password) {
 *         // 1. 查询用户信息
 *         User user = userMapper.selectByUsername(username);
 *         if (user == null) {
 *             throw new UsernameNotFoundException("用户不存在");
 *         }
 *
 *         // 2. 验证密码
 *         if (!passwordEncoder.matches(password, user.getPassword())) {
 *             throw new BadCredentialsException("密码错误");
 *         }
 *
 *         // 3. 检查账号状态
 *         if (!user.isEnabled()) {
 *             throw new DisabledException("账号已禁用");
 *         }
 *
 *         // 4. 构建登录用户对象
 *         LoginUser loginUser = new LoginUser();
 *         loginUser.setId(user.getId());
 *         loginUser.setUsername(user.getUsername());
 *         loginUser.setPassword(null); // 不要存储密码
 *
 *         // 5. 加载角色和权限
 *         Set<String> roles = loadUserRoles(user.getId());
 *         Set<String> permissions = loadUserPermissions(user.getId());
 *         loginUser.setRoles(roles);
 *         loginUser.setPermissions(permissions);
 *
 *         return loginUser;
 *     }
 * }
 * }</pre>
 *
 * <h3>密码作用域:</h3>
 * <p>此类使用 {@link PasswordScoped} 来在当前线程中传递密码,
 * 避免在方法签名中显式传递密码,提高安全性.
 *
 * <h3>异常处理:</h3>
 * <ul>
 *   <li>{@link UsernameNotFoundException} - 用户不存在</li>
 *   <li>{@link org.springframework.security.authentication.BadCredentialsException} - 密码错误</li>
 *   <li>{@link org.springframework.security.authentication.DisabledException} - 账号已禁用</li>
 *   <li>{@link org.springframework.security.authentication.LockedException} - 账号已锁定</li>
 *   <li>{@link org.springframework.security.authentication.AccountExpiredException} - 账号已过期</li>
 * </ul>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>密码验证应该使用 PasswordEncoder,不要手动比较</li>
 *   <li>返回的 LoginUser 对象中不应该包含明文密码</li>
 *   <li>需要同时加载用户的角色和权限信息</li>
 *   <li>应该检查账号的各种状态 (启用、锁定、过期等)</li>
 *   <li>用户不存在时应该抛出 UsernameNotFoundException</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see org.springframework.security.crypto.password.PasswordEncoder
 * @see PasswordScoped
 */
public interface SecurityUserDetailService extends UserDetailsService {

    /**
     * 根据用户名加载用户详情.
     *
     * <p>重写父接口方法,使用当前作用域中的密码进行认证.
     * 此方法是 Spring Security 认证流程的入口点.
     *
     * <p>默认实现从 {@link PasswordScoped} 中获取密码,
     * 然后调用 {@link #loadUserByUsername(String, String)} 方法.
     *
     * <h3>调用时机:</h3>
     * <ul>
     *   <li>用户登录时</li>
     *   <li>JWT 令牌验证时</li>
     *   <li>Remember Me 认证时</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // Spring Security 会自动调用此方法
     * Authentication authentication = authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(username, password)
     * );
     *
     * // 上面的代码会触发:
     * // 1. PasswordScoped.set(password)
     * // 2. loadUserByUsername(username) - 调用此方法
     * // 3. loadUserByUsername(username, password) - 内部调用
     * }</pre>
     *
     * @param username 用户名,不能为 {@code null}
     * @return 用户详情对象,包含用户信息和权限
     * @throws UsernameNotFoundException 用户不存在时抛出
     * @see #loadUserByUsername(String, String)
     * @see PasswordScoped#get()
     */
    @Override
    @NullMarked
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByUsername(username, PasswordScoped.get());
    }

    /**
     * 根据用户名和密码加载用户详情.
     *
     * <p>此方法提供完整的用户认证流程,包括:
     * <ol>
     *   <li>根据用户名查询用户信息</li>
     *   <li>验证密码正确性</li>
     *   <li>检查账号状态</li>
     *   <li>加载用户角色和权限</li>
     *   <li>构建并返回 {@link LoginUser} 对象</li>
     * </ol>
     *
     * <h3>实现要点:</h3>
     * <pre>{@code
     * @Override
     * public LoginUser loadUserByUsername(String username, String password) {
     *     // 1. 查询用户
     *     User user = userRepository.findByUsername(username);
     *     if (user == null) {
     *         throw new UsernameNotFoundException("用户不存在: " + username);
     *     }
     *
     *     // 2. 验证密码
     *     if (!passwordEncoder.matches(password, user.getPassword())) {
     *         throw new BadCredentialsException("密码错误");
     *     }
     *
     *     // 3. 检查账号状态
     *     if (!user.isEnabled()) {
     *         throw new DisabledException("账号已禁用");
     *     }
     *     if (user.isLocked()) {
     *         throw new LockedException("账号已锁定");
     *     }
     *
     *     // 4. 构建登录用户
     *     LoginUser loginUser = new LoginUser();
     *     loginUser.setId(user.getId());
     *     loginUser.setUsername(user.getUsername());
     *
     *     // 5. 加载角色和权限
     *     loginUser.setRoles(loadRoles(user.getId()));
     *     loginUser.setPermissions(loadPermissions(user.getId()));
     *
     *     return loginUser;
     * }
     * }</pre>
     *
     * <h3>性能优化建议:</h3>
     * <ul>
     *   <li>使用缓存减少数据库查询</li>
     *   <li>角色和权限信息可以缓存到 Redis</li>
     *   <li>使用 LEFT JOIN 一次性加载用户、角色、权限</li>
     *   <li>避免 N+1 查询问题</li>
     * </ul>
     *
     * @param username 用户名,不能为 {@code null}
     * @param password 密码明文,不能为 {@code null}
     * @return 登录用户信息,包含用户基本信息、角色和权限列表
     * @throws UsernameNotFoundException 用户不存在时抛出
     * @throws org.springframework.security.authentication.BadCredentialsException 密码错误时抛出
     * @throws org.springframework.security.authentication.DisabledException 账号已禁用时抛出
     * @throws org.springframework.security.authentication.LockedException 账号已锁定时抛出
     */
    LoginUser loadUserByUsername(String username, String password);

}

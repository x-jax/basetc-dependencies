package com.basetc.base.security.service;

import com.basetc.base.security.domain.LoginUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全认证用户服务接口,定义用户会话管理和令牌操作的核心方法.
 *
 * <p>此接口负责管理用户登录后的会话状态和令牌生命周期,包括:
 * <ul>
 *   <li>从请求中获取当前登录用户</li>
 *   <li>创建访问令牌 (JWT 或 Session ID)</li>
 *   <li>刷新令牌 (延长有效期)</li>
 *   <li>用户登出 (清除会话和令牌)</li>
 * </ul>
 *
 * <h3>支持的存储模式:</h3>
 * <ul>
 *   <li><b>Redis 模式</b>: 分布式部署,支持多节点共享认证信息</li>
 *   <li><b>Session 模式</b>: 单机部署,使用本地 Session 存储</li>
 * </ul>
 *
 * <h3>令牌生命周期:</h3>
 * <pre>{@code
 * 1. 用户登录 -> createToken() -> 生成新令牌
 * 2. 每次请求 -> getLoginUser() -> 解析令牌获取用户信息
 * 3. 令牌即将过期 -> refreshToken() -> 延长令牌有效期
 * 4. 用户登出 -> logout() -> 清除令牌
 * 5. 令牌过期 -> 需要重新登录
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class UserServiceImpl implements UserService {
 *
 *     private final SecurityAuthenticateUserService authenticateUserService;
 *
 *     @Override
 *     public void updateProfile(HttpServletRequest request, UserProfile profile) {
 *         // 获取当前登录用户
 *         LoginUser user = authenticateUserService.getLoginUser(request);
 *         if (user == null) {
 *             throw new AuthenticationException("用户未登录");
 *         }
 *
 *         // 更新用户信息
 *         user.setProfile(profile);
 *         updateUser(user);
 *
 *         // 刷新令牌
 *         authenticateUserService.refreshToken(user);
 *     }
 *
 *     @Override
 *     public void logout(HttpServletRequest request) {
 *         // 登出用户
 *         authenticateUserService.logout(request);
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>从请求中获取用户时,如果令牌无效会返回 {@code null}</li>
 *   <li>刷新令牌会延长令牌的有效期,通常是过期时间的两倍</li>
 *   <li>登出操作会清除服务器端的会话信息</li>
 *   <li>客户端也需要清除本地存储的令牌</li>
 *   <li>Redis 和 Session 模式的实现逻辑不同,但接口一致</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see SecurityAuthenticateService
 * @see com.basetc.base.security.properties.BasetcSecurityRedisProperties
 * @see com.basetc.base.security.properties.BasetcSecuritySessionProperties
 */
public interface SecurityAuthenticateUserService {

    /**
     * 从 HTTP 请求中获取当前登录用户信息.
     *
     * <p>通过解析请求头或请求参数中的访问令牌,从 Redis 或 Session 中获取已登录用户的详细信息.
     *
     * <h3>获取流程:</h3>
     * <ol>
     *   <li>从请求头中提取令牌 (默认: {@code Authorization: Bearer <token>})</li>
     *   <li>验证令牌格式和有效性</li>
     *   <li>从存储中获取用户信息 (Redis 或 Session)</li>
     *   <li>验证用户状态 (启用、锁定等)</li>
     *   <li>返回用户信息或 {@code null}</li>
     * </ol>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * @GetMapping("/api/user/profile")
     * public R<UserProfile> getUserProfile(HttpServletRequest request) {
     *     LoginUser user = authenticateUserService.getLoginUser(request);
     *     if (user == null) {
     *         return R.error(401, "用户未登录");
     *     }
     *     return R.success(user.getProfile());
     * }
     * }</pre>
     *
     * @param request HTTP 请求对象,不能为 {@code null}
     * @return 登录用户信息,如果未登录或令牌无效则返回 {@code null}
     * @see com.basetc.base.security.properties.BasetcSecurityJwtProperties#getHeader()
     * @see com.basetc.base.security.properties.BasetcSecurityJwtProperties#getPrefix()
     */
    LoginUser getLoginUser(HttpServletRequest request);

    /**
     * 登出当前用户.
     *
     * <p>从 HTTP 请求中获取当前登录用户信息,并清除服务器端的会话数据和访问令牌.
     *
     * <h3>登出流程:</h3>
     * <ol>
     *   <li>从请求中获取当前登录用户</li>
     *   <li>从 Redis/Session 中删除用户信息</li>
     *   <li>清除令牌相关的缓存数据 (Token ID, IP, User-Agent 等)</li>
     *   <li>记录登出日志</li>
     * </ol>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * @PostMapping("/logout")
     * public R<Void> logout(HttpServletRequest request) {
     *     authenticateUserService.logout(request);
     *     return R.success("登出成功");
     * }
     * }</pre>
     *
     * <h3>前端配合:</h3>
     * <pre>{@code
     * // JavaScript
     * fetch('/logout', { method: 'POST' })
     *   .then(() => {
     *     // 清除本地存储的令牌
     *     localStorage.removeItem('token');
     *     // 跳转到登录页
     *     window.location.href = '/login';
     *   });
     * }</pre>
     *
     * @param request HTTP 请求对象,不能为 {@code null}
     * @see #logout(LoginUser)
     */
    void logout(HttpServletRequest request);

    /**
     * 登出指定用户.
     *
     * <p>根据提供的登录用户信息,清除服务器端的会话数据和访问令牌.
     * 此方法用于强制登出场景,如管理员踢出用户、账号异常等.
     *
     * <h3>使用场景:</h3>
     * <ul>
     *   <li>管理员踢出违规用户</li>
     *   <li>账号异常时强制登出</li>
     *   <li>重置用户权限后刷新会话</li>
     *   <li>单点登录 (同一用户在新设备登录时踢出旧设备)</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 管理员踢出用户
     * public void kickOutUser(Long userId) {
     *     LoginUser user = userService.getById(userId);
     *     authenticateUserService.logout(user);
     *     log.info("已踢出用户: {}", user.getUsername());
     * }
     *
     * // 单点登录实现
     * public LoginUser login(AuthenticateRequest request) {
     *     LoginUser user = authenticate(request);
     *
     *     // 启用单点登录,先登出旧设备
     *     if (authProperties.getFilter().isSingleEnabled()) {
     *         authenticateUserService.logout(user);
     *     }
     *
     *     // 在新设备登录
     *     String newToken = createToken(user);
     *     return user;
     * }
     * }</pre>
     *
     * @param loginUser 登录用户信息,不能为 {@code null}
     * @see #logout(HttpServletRequest)
     */
    void logout(LoginUser loginUser);

    /**
     * 刷新访问令牌.
     *
     * <p>根据给定的登录用户信息刷新访问令牌,延长其有效期.
     * 通常在令牌即将过期时调用,避免用户频繁重新登录.
     *
     * <h3>刷新策略:</h3>
     * <ul>
     *   <li><b>JWT 模式</b>: 生成新的 JWT 令牌,延长过期时间</li>
     *   <li><b>Session 模式</b>: 更新 Session 的最后访问时间</li>
     *   <li><b>Redis 模式</b>: 更新 Redis 中的过期时间</li>
     * </ul>
     *
     * <h3>刷新时机:</h3>
     * <p>通常在令牌剩余有效期小于 {@code refresh-scope} 时刷新.
     * 例如: expire=30分钟, refresh-scope=15分钟,则在剩余时间小于15分钟时刷新.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 在每次请求时检查并刷新令牌
     * @GetMapping("/api/data")
     * public R<Data> getData(HttpServletRequest request, HttpServletResponse response) {
     *     LoginUser user = authenticateUserService.getLoginUser(request);
     *
     *     // 检查令牌是否即将过期
     *     if (isTokenExpiringSoon(user)) {
     *         // 刷新令牌
     *         authenticateUserService.refreshToken(user);
     *         // 将新令牌返回给客户端
     *         response.setHeader("X-New-Token", user.getToken());
     *         response.setHeader("X-New-Token-Expires", user.getExpireTime().toString());
     *     }
     *
     *     return R.success(getData());
     * }
     * }</pre>
     *
     * <h3>配置刷新间隔:</h3>
     * <pre>{@code
     * basetc:
     *   security:
     *     jwt:
     *       expire: 30              # 令牌有效期 30 分钟
     *       refresh-scope: 15       # 剩余 15 分钟时刷新
     * }</pre>
     *
     * @param loginUser 登录用户信息,不能为 {@code null}
     * @see com.basetc.base.security.properties.BasetcSecurityJwtProperties#getRefreshScope()
     * @see #createToken(LoginUser)
     */
    void refreshToken(LoginUser loginUser);

    /**
     * 创建访问令牌.
     *
     * <p>根据给定的登录用户信息创建访问令牌,并将其存储到 Redis 或 Session 中.
     *
     * <h3>创建流程:</h3>
     * <ol>
     *   <li>生成唯一的令牌 ID (UUID)</li>
     *   <li>根据存储模式创建令牌:
     *     <ul>
     *       <li>JWT 模式: 生成 JWT 字符串,包含用户 ID、过期时间等</li>
     *       <li>Session 模式: 返回 Session ID</li>
     *     </ul>
     *   </li>
     *   <li>将用户信息存储到 Redis/Session</li>
     *   <li>设置过期时间 (expire 配置)</li>
     *   <li>如果是单点登录,存储 Token ID</li>
     *   <li>如果启用 IP 限制,存储请求 IP</li>
     *   <li>如果启用 User-Agent 限制,存储请求 UA</li>
     *   <li>返回令牌字符串</li>
     * </ol>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * @Service
     * @RequiredArgsConstructor
     * public class AuthServiceImpl implements SecurityAuthenticateService {
     *
     *     private final SecurityAuthenticateUserService authenticateUserService;
     *
     *     @Override
     *     public LoginUser authenticate(AuthenticateRequest request) {
     *         // 1. 验证用户名和密码
     *         LoginUser user = validateCredentials(request);
     *
     *         // 2. 加载用户角色和权限
     *         loadUserRolesAndPermissions(user);
     *
     *         // 3. 创建访问令牌
     *         String token = authenticateUserService.createToken(user);
     *
     *         // 4. 设置令牌到用户对象
     *         user.setToken(token);
     *
     *         // 5. 返回登录用户信息
     *         return user;
     *     }
     * }
     * }</pre>
     *
     * <h3>JWT 令牌示例:</h3>
     * <pre>{@code
     * // JWT 结构
     * Header.Payload.Signature
     *
     * // Header
     * {
     *   "alg": "HS256",
     *   "typ": "JWT"
     * }
     *
     * // Payload
     * {
     *   "sub": "1234567890",    // 用户 ID
     *   "name": "John Doe",     // 用户名
     *   "iat": 1516239022,      // 签发时间
     *   "exp": 1516242622       // 过期时间
     * }
     *
     * // 完整令牌
     * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U
     * }</pre>
     *
     * @param loginUser 登录用户信息,不能为 {@code null}
     * @return 创建的访问令牌字符串
     * @see com.basetc.base.security.properties.BasetcSecurityJwtProperties
     * @see #refreshToken(LoginUser)
     */
    String createToken(LoginUser loginUser);

}

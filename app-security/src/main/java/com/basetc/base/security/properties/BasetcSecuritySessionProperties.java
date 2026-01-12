package com.basetc.base.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全 Session 配置属性类,定义 Session 存储模式的相关配置参数.
 *
 * <p>此类用于配置使用 Session 存储用户认证信息的相关参数.
 * Session 存储模式适用于单机应用,使用应用服务器的内存存储用户认证信息.
 *
 * <h3>Session vs Redis 存储模式:</h3>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>Session 模式</th>
 *     <th>Redis 模式</th>
 *   </tr>
 *   <tr>
 *     <td>适用场景</td>
 *     <td>单机应用、开发测试</td>
 *     <td>分布式应用、生产环境</td>
 *   </tr>
 *   <tr>
 *     <td>性能</td>
 *     <td>内存访问,速度快</td>
 *     <td>依赖 Redis,有网络开销</td>
 *   </tr>
 *   <tr>
 *     <td>可扩展性</td>
 *     <td>差,不支持跨节点</td>
 *     <td>好,支持横向扩展</td>
 *   </tr>
 *   <tr>
 *     <td>可靠性</td>
 *     <td>依赖应用服务器</td>
 *     <td>依赖 Redis 可用性</td>
 *   </tr>
 * </table>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     session:
 *       enable: true
 *       session-key-prefix: "basetc_user"
 *     redis:
 *       enable: false
 * }</pre>
 *
 * <h3>配置示例 (application.properties):</h3>
 * <pre>{@code
 * basetc.security.session.enable=true
 * basetc.security.session.session-key-prefix=basetc_user
 * basetc.security.redis.enable=false
 * }</pre>
 *
 * <h3>Session 属性命名规范:</h3>
 * <p>Session 中存储的属性格式为: {@code {prefix}:{attributeName}}
 * <ul>
 *   <li>用户信息: {@code basetc_user:login} -> {@code LoginUser 对象}</li>
 *   <li>Token ID: {@code basetc_user:token_id} -> {@code UUID}</li>
 *   <li>IP 地址: {@code basetc_user:ip} -> {@code "192.168.1.100"}</li>
 *   <li>User-Agent: {@code basetc_user:ua} -> {@code "Mozilla/5.0..."}</li>
 * </ul>
 *
 * <h3>在代码中使用配置:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class SessionAuthService {
 *
 *     private final BasetcSecuritySessionProperties sessionProperties;
 *
 *     public void saveUserLogin(HttpServletRequest request, LoginUser user) {
 *         String key = sessionProperties.getSessionKeyPrefix() + ":login";
 *         request.getSession().setAttribute(key, user);
 *     }
 *
 *     public LoginUser getUserLogin(HttpServletRequest request) {
 *         String key = sessionProperties.getSessionKeyPrefix() + ":login";
 *         return (LoginUser) request.getSession().getAttribute(key);
 *     }
 *
 *     public void deleteUserLogin(HttpServletRequest request) {
 *         String key = sessionProperties.getSessionKeyPrefix() + ":login";
 *         request.getSession().removeAttribute(key);
 *     }
 * }
 * }</pre>
 *
 * <h3>Session 配置建议:</h3>
 * <pre>{@code
 * server:
 *   servlet:
 *     session:
 *       timeout: 30m              # Session 超时时间
 *       cookie:
 *         http-only: true         # 防止 XSS 攻击
 *         secure: true            # 仅 HTTPS 传输
 *         same-site: strict       # 防止 CSRF 攻击
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>Session 模式不适合分布式部署</li>
 *   <li>Session 数据在服务器重启后会丢失</li>
 *   <li>生产环境建议使用 Redis 模式</li>
 *   <li>属性前缀应具有明确的业务含义</li>
 *   <li>建议使用下划线分隔单词</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see BasetcSecurityRedisProperties
 * @see javax.servlet.http.HttpSession
 */
@Data
@ConfigurationProperties(prefix = "basetc.security.session")
public class BasetcSecuritySessionProperties {

    /**
     * 是否启用 Session 存储模式.
     *
     * <p>设置为 {@code true} 时,用户认证信息会存储在 HttpSession 中,
     * 适用于单机应用场景.
     *
     * <p>默认值为 {@code true}.
     *
     * <h3>存储模式切换:</h3>
     * <ul>
     *   <li>单机应用: Session 模式 ({@code enable=true})</li>
     *   <li>分布式应用: Redis 模式 ({@code enable=false})</li>
     * </ul>
     *
     * <h3>切换到 Redis 模式:</h3>
     * <pre>{@code
     * basetc:
     *   security:
     *     session:
     *       enable: false
     *     redis:
     *       enable: true
     * }</pre>
     *
     * @see #sessionKeyPrefix
     */
    private boolean enable = true;

    /**
     * Session 属性前缀.
     *
     * <p>用于在 HttpSession 中存储用户认证信息时作为属性名的前缀,
     * 便于分类管理和避免属性名冲突.
     *
     * <p>默认值为 {@code "basetc_user"}.
     *
     * <h3>属性前缀规范:</h3>
     * <ul>
     *   <li>使用下划线 ({@code _}) 分隔单词</li>
     *   <li>前缀应具有明确的业务含义</li>
     *   <li>不要以数字或特殊字符开头</li>
     * </ul>
     *
     * <h3>属性前缀示例:</h3>
     * <pre>{@code
     * # 标准格式
     * session-key-prefix: "basetc_user"
     * # 完整属性名: basetc_user:login
     *
     * # 带应用名
     * session-key-prefix: "myapp_security_user"
     * # 完整属性名: myapp_security_user:login
     *
     * # 带环境
     * session-key-prefix: "prod_app_user"
     * # 完整属性名: prod_app_user:login
     * }</pre>
     *
     * <h3>Session 属性操作示例:</h3>
     * <pre>{@code
     * // 保存属性
     * String key = sessionProperties.getSessionKeyPrefix() + ":login";
     * session.setAttribute(key, loginUser);
     *
     * // 获取属性
     * LoginUser user = (LoginUser) session.getAttribute(key);
     *
     * // 删除属性
     * session.removeAttribute(key);
     *
     * // 获取所有属性名
     * Enumeration<String> names = session.getAttributeNames();
     * while (names.hasMoreElements()) {
     *     String name = names.nextElement();
     *     if (name.startsWith(sessionProperties.getSessionKeyPrefix())) {
     *         // 处理安全相关属性
     *     }
     * }
     * }</pre>
     */
    private String sessionKeyPrefix = "basetc_user";

}

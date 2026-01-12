package com.basetc.base.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全权限配置属性类,定义权限相关的配置参数.
 *
 * <p>此类负责配置权限管理相关的核心参数,包括超级管理员角色标识和所有权限标识.
 * 这些标识用于权限验证和访问控制.
 *
 * <h3>权限控制流程:</h3>
 * <pre>{@code
 * 1. 用户登录 -> 获取用户角色列表
 * 2. 角色列表 -> 获取角色对应的权限列表
 * 3. 权限列表 -> 判断是否包含所需权限
 * 4. 特殊判断:
 *    - 如果用户角色是 SUPER_ADMIN,直接通过 (拥有所有权限)
 *    - 如果用户权限包含 *,直接通过 (拥有所有权限)
 * }</pre>
 *
 * <h3>配置示例 (application.yml):</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     permissions:
 *       super-role: SUPER_ADMIN
 *       all-permission: "*"
 * }</pre>
 *
 * <h3>配置示例 (application.properties):</h3>
 * <pre>{@code
 * basetc.security.permissions.super-role=SUPER_ADMIN
 * basetc.security.permissions.all-permission=*
 * }</pre>
 *
 * <h3>权限注解使用:</h3>
 * <pre>{@code
 * // 在 Controller 方法上使用权限注解
 * @RestController
 * @RequestMapping("/api/user")
 * public class UserController {
 *
 *     @PreAuthorize("hasAuthority('user:create')")
 *     public R<Void> create(@RequestBody User user) {
 *         // 需要 user:create 权限
 *     }
 *
 *     @PreAuthorize("hasAnyAuthority('user:update', 'user:delete')")
 *     public R<Void> updateOrDelete(@PathVariable Long id) {
 *         // 需要 user:update 或 user:delete 权限
 *     }
 *
 *     @PreAuthorize("hasRole('ADMIN')")
 *     public R<Void> adminOnly() {
 *         // 需要 ADMIN 角色
 *     }
 * }
 * }</pre>
 *
 * <h3>自定义权限验证:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class PermissionService {
 *
 *     private final BasetcSecurityPermissionsProperties permissions;
 *
 *     public boolean hasPermission(LoginUser user, String requiredPermission) {
 *         // 1. 检查是否是超级管理员
 *         if (isSuperAdmin(user)) {
 *             return true;
 *         }
 *
 *         // 2. 检查是否拥有所有权限
 *         if (hasAllPermission(user)) {
 *             return true;
 *         }
 *
 *         // 3. 检查是否拥有所需权限
 *         return user.getPermissions().contains(requiredPermission);
 *     }
 *
 *     private boolean isSuperAdmin(LoginUser user) {
 *         return user.getRoles().contains(permissions.getSuperRole());
 *     }
 *
 *     private boolean hasAllPermission(LoginUser user) {
 *         return user.getPermissions().contains(permissions.getAllPermission());
 *     }
 * }
 * }</pre>
 *
 * <h3>角色和权限设计建议:</h3>
 * <pre>{@code
 * 角色层级:
 * - SUPER_ADMIN  (超级管理员)  -> 拥有所有权限
 * - ADMIN        (管理员)      -> 拥有大部分管理权限
 * - MANAGER      (经理)        -> 拥有部分管理权限
 * - USER         (普通用户)    -> 拥有基本操作权限
 * - GUEST        (访客)        -> 拥有只读权限
 *
 * 权限命名规范:
 * - 资源:操作 (user:create, user:update, user:delete, user:view)
 * - 模块:资源:操作 (system:user:create, system:role:update)
 * - 通配符: user:* (所有用户权限), *:* (所有权限)
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>超级管理员角色应该谨慎授予,通常只授予系统核心人员</li>
 *   <li>所有权限标识 (*) 应该谨慎使用,避免权限泄露</li>
 *   <li>角色和权限标识建议使用大写字母和下划线</li>
 *   <li>权限标识应该具有明确的业务含义</li>
 *   <li>建议使用资源:操作的格式命名权限</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see org.springframework.security.access.prepost.PostAuthorize
 */
@Data
@ConfigurationProperties(prefix = "basetc.security.permissions")
public class BasetcSecurityPermissionsProperties {

    /**
     * 超级管理员角色标识.
     *
     * <p>拥有此角色的用户拥有所有权限,无需进行权限验证.
     * 此标识应该谨慎授予,通常只授予系统核心人员.
     *
     * <p>默认值为 {@code "SUPER_ADMIN"}.
     *
     * <h3>超级管理员特性:</h3>
     * <ul>
     *   <li>拥有所有权限,无需验证</li>
     *   <li>可以访问所有受保护的资源</li>
     *   <li>可以执行所有操作</li>
     *   <li>不受权限限制</li>
     * </ul>
     *
     * <h3>角色标识示例:</h3>
     * <pre>{@code
     * # 标准格式
     * super-role: SUPER_ADMIN
     *
     * # 带应用名前缀
     * super-role: MYAPP_SUPER_ADMIN
     *
     * # 带环境
     * super-role: PROD_SUPER_ADMIN
     * }</pre>
     *
     * <h3>权限验证示例:</h3>
     * <pre>{@code
     * @Service
     * public class SecurityService {
     *
     *     private final BasetcSecurityPermissionsProperties permissions;
     *
     *     public boolean canAccess(LoginUser user) {
     *         // 如果是超级管理员,直接允许访问
     *         if (user.getRoles().contains(permissions.getSuperRole())) {
     *             return true;
     *         }
     *         // 否则进行权限验证
     *         return checkPermission(user);
     *     }
     * }
     * }</pre>
     *
     * @see #allPermission
     */
    private String superRole = "SUPER_ADMIN";

    /**
     * 所有权限标识.
     *
     * <p>拥有此权限的用户拥有所有权限,等同于超级管理员.
     * 通常使用通配符 ({@code *}) 表示.
     *
     * <p>默认值为 {@code "*"}.
     *
     * <h3>所有权限特性:</h3>
     * <ul>
     *   <li>拥有所有权限,等同于超级管理员</li>
     *   <li>可以通过权限配置授予</li>
     *   <li>比角色更灵活</li>
     * </ul>
     *
     * <h3>权限标识示例:</h3>
     * <pre>{@code
     * # 标准通配符
     * all-permission: "*"
     *
     * # 模块级通配符
     * all-permission: "system:*"      # 所有系统权限
     * all-permission: "user:*"        # 所有用户权限
     *
     * # 多级通配符
     * all-permission: "*:*"           # 所有模块的所有权限
     * }</pre>
     *
     * <h3>权限验证示例:</h3>
     * <pre>{@code
     * @Service
     * public class SecurityService {
     *
     *     private final BasetcSecurityPermissionsProperties permissions;
     *
     *     public boolean hasAllPermission(LoginUser user) {
     *         // 检查是否拥有所有权限
     *         return user.getPermissions().contains(permissions.getAllPermission());
     *     }
     *
     *     public boolean checkPermission(LoginUser user, String required) {
     *         // 1. 拥有所有权限
     *         if (hasAllPermission(user)) {
     *             return true;
     *         }
     *
     *         // 2. 精确匹配
     *         if (user.getPermissions().contains(required)) {
     *             return true;
     *         }
     *
     *         // 3. 通配符匹配 (user:* 匹配 user:create)
     *         return user.getPermissions().stream()
     *             .anyMatch(p -> matchWildcard(p, required));
     *     }
     * }
     * }</pre>
     *
     * @see #superRole
     */
    private String allPermission = "*";

}

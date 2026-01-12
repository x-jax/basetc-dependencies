package com.basetc.base.security.service;

import com.basetc.base.security.domain.LoginUser;

import java.util.Set;

/**
 * 权限服务接口,定义权限验证和角色检查的核心方法.
 *
 * <p>此接口提供了全面的权限验证功能,包括:
 * <ul>
 *   <li>管理员角色验证</li>
 *   <li>超级权限验证</li>
 *   <li>权限包含验证 (精确匹配)</li>
 *   <li>角色包含验证</li>
 *   <li>批量权限和角色验证</li>
 * </ul>
 *
 * <h3>权限验证流程:</h3>
 * <pre>{@code
 * 1. 检查是否是管理员 (SUPER_ADMIN)
 *    ├─ 是 -> 自动通过所有权限检查
 *    └─ 否 -> 继续下一步
 * 2. 检查是否拥有超级权限 (*)
 *    ├─ 是 -> 自动通过所有权限检查
 *    └─ 否 -> 继续下一步
 * 3. 检查是否拥有所需权限
 *    ├─ 精确匹配 (user:create)
 *    ├─ 通配符匹配 (user:* 匹配 user:create)
 *    └─ 返回验证结果
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class UserService {
 *
 *     private final PermissionService permissionService;
 *
 *     public void createUser(User user) {
 *         // 检查是否有创建用户权限
 *         if (!permissionService.hasPermission("user:create")) {
 *             throw new AccessDeniedException("权限不足");
 *         }
 *         // 执行创建用户逻辑
 *     }
 *
 *     public void deleteUser(Long userId) {
 *         // 检查是否有删除用户权限或管理员权限
 *         if (!permissionService.hasAnyPermission("user:delete", "admin:delete")) {
 *             throw new AccessDeniedException("权限不足");
 *         }
 *         // 执行删除用户逻辑
 *     }
 *
 *     public void adminOperation() {
 *         // 只有管理员才能执行
 *         if (!permissionService.isAdmin()) {
 *             throw new AccessDeniedException("仅管理员可执行");
 *         }
 *         // 执行管理员操作
 *     }
 * }
 * }</pre>
 *
 * <h3>在 Controller 中使用:</h3>
 * <pre>{@code
 * @RestController
 * @RequiredArgsConstructor
 * public class UserController {
 *
 *     private final PermissionService permissionService;
 *
 *     @GetMapping("/admin/dashboard")
 *     public R<Void> adminDashboard() {
 *         // 方法级权限检查
 *         if (!permissionService.isAdmin()) {
 *             return R.error(403, "仅管理员可访问");
 *         }
 *         return R.success();
 *     }
 *
 *     @PostMapping("/user/create")
 *     public R<Void> createUser(@RequestBody User user) {
 *         // 权限检查
 *         if (!permissionService.hasPermission("user:create")) {
 *             return R.error(403, "权限不足");
 *         }
 *         return R.success();
 *     }
 * }
 * }</pre>
 *
 * <h3>与 Spring Security 注解配合:</h3>
 * <pre>{@code
 * // 使用 @PreAuthorize 注解 (推荐)
 * @PreAuthorize("hasAuthority('user:create')")
 * public R<Void> createUser(@RequestBody User user) {
 *     // Spring Security 会自动调用 PermissionService 进行验证
 * }
 *
 * // 手动验证
 * public R<Void> createUser(@RequestBody User user) {
 *     if (!permissionService.hasPermission("user:create")) {
 *         throw new AccessDeniedException("权限不足");
 *     }
 * }
 * }</pre>
 *
 * <h3>权限设计规范:</h3>
 * <pre>{@code
 * // 格式: 资源:操作
 * user:create      - 创建用户
 * user:update      - 更新用户
 * user:delete      - 删除用户
 * user:view        - 查看用户
 *
 * // 格式: 模块:资源:操作
 * system:user:create   - 系统管理:创建用户
 * system:role:update   - 系统管理:更新角色
 * business:order:view  - 业务管理:查看订单
 *
 * // 通配符
 * user:*          - 所有用户相关权限
 * system:*        - 所有系统管理权限
 * *               - 所有权限 (超级权限)
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>管理员角色和超级权限具有相同的效力,都会通过所有权限检查</li>
 *   <li>权限标识区分大小写,建议使用小写字母</li>
 *   <li>通配符匹配仅支持 * 匹配任意字符</li>
 *   <li>建议使用资源:操作的格式命名权限</li>
 *   <li>权限验证会自动从 SecurityContext 获取当前登录用户</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see org.springframework.security.access.annotation.Secured
 */
public interface PermissionService {

    /**
     * 检查当前登录用户是否为管理员.
     *
     * <p>通过判断用户角色列表中是否包含管理员角色标识来确定.
     * 管理员拥有所有权限,无需进行额外的权限验证.
     *
     * <h3>使用场景:</h3>
     * <ul>
     *   <li>管理员专用功能 (如系统配置、用户管理等)</li>
     *   <li>需要绕过权限检查的操作</li>
     *   <li>审计和日志记录</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * if (permissionService.isAdmin()) {
     *     // 执行管理员专属操作
     *     systemConfig.update(config);
     * } else {
     *     throw new AccessDeniedException("仅管理员可执行");
     * }
     * }</pre>
     *
     * @return 如果是管理员返回 {@code true},否则返回 {@code false}
     * @see #hasAdminRole(Set)
     * @see #getSuperRole()
     */
    boolean isAdmin();

    /**
     * 检查指定用户是否为管理员.
     *
     * <p>通过判断用户角色列表中是否包含管理员角色标识来确定.
     * 此方法可以检查任意用户的管理员状态,不限于当前登录用户.
     *
     * <h3>使用场景:</h3>
     * <ul>
     *   <li>审计和日志记录 (记录操作者是否为管理员)</li>
     *   <li>权限委派和转移</li>
     *   <li>批量用户操作</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * public void auditLog(LoginUser operator, LoginUser target) {
     *     log.info("操作者: {}, 是否管理员: {}, 目标用户: {}",
     *         operator.getUsername(),
     *         permissionService.isAdmin(operator),
     *         target.getUsername());
     * }
     * }</pre>
     *
     * @param loginUser 登录用户信息,不能为 {@code null}
     * @return 如果是管理员返回 {@code true},否则返回 {@code false}
     * @see #isAdmin()
     * @see #hasAdminRole(Set)
     */
    boolean isAdmin(LoginUser loginUser);

    /**
     * 检查角色集合中是否包含管理员角色.
     *
     * <p>此方法用于检查一组角色中是否包含管理员角色标识.
     * 适用于不完整用户信息的场景.
     *
     * <h3>使用场景:</h3>
     * <ul>
     *   <li>快速判断,无需加载完整用户信息</li>
     *   <li>批量角色验证</li>
     *   <li>缓存和性能优化</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 从缓存中获取角色
     * Set<String> roles = redisTemplate.opsForSet().members("user:roles:" + userId);
     *
     * // 快速判断是否为管理员
     * if (permissionService.hasAdminRole(roles)) {
     *     // 执行管理员操作
     * }
     * }</pre>
     *
     * @param roles 角色标识集合,不能为 {@code null}
     * @return 如果包含管理员角色返回 {@code true},否则返回 {@code false}
     * @see #isAdmin()
     * @see #isAdmin(LoginUser)
     */
    boolean hasAdminRole(Set<String> roles);

    /**
     * 检查当前登录用户是否拥有超级权限.
     *
     * <p>超级权限 (通常为 {@code *}) 表示拥有所有权限,
     * 效力等同于管理员角色,但通过权限而非角色实现.
     *
     * <h3>超级权限 vs 管理员角色:</h3>
     * <ul>
     *   <li>超级权限通过权限列表实现,更灵活</li>
     *   <li>管理员角色通过角色列表实现,更明确</li>
     *   <li>两者具有相同的效力,都会通过所有权限检查</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * if (permissionService.hasAllPermission()) {
     *     // 拥有超级权限,可以执行任何操作
     *     return ResponseEntity.ok().body(allData);
     * } else {
     *     // 根据实际权限过滤数据
     *     return ResponseEntity.ok().body(filterData(currentUser));
     * }
     * }</pre>
     *
     * @return 如果拥有超级权限返回 {@code true},否则返回 {@code false}
     * @see #hasAllPermission(LoginUser)
     * @see #getAllPermission()
     */
    boolean hasAllPermission();

    /**
     * 检查指定用户是否拥有超级权限.
     *
     * <p>此方法可以检查任意用户的超级权限状态,
     * 不限于当前登录用户.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * public void shareResource(LoginUser owner, LoginUser recipient) {
     *     // 如果资源所有者拥有超级权限,允许分享给任何人
     *     if (permissionService.hasAllPermission(owner)) {
     *         resource.share(recipient);
     *     }
     * }
     * }</pre>
     *
     * @param loginUser 登录用户信息,不能为 {@code null}
     * @return 如果拥有超级权限返回 {@code true},否则返回 {@code false}
     * @see #hasAllPermission()
     */
    boolean hasAllPermission(LoginUser loginUser);

    /**
     * 检查当前登录用户是否拥有指定权限.
     *
     * <p>权限验证逻辑:
     * <ol>
     *   <li>如果是管理员角色,直接返回 {@code true}</li>
     *   <li>如果拥有超级权限,直接返回 {@code true}</li>
     *   <li>检查权限列表是否包含指定权限 (精确匹配或通配符匹配)</li>
     *   <li>返回验证结果</li>
     * </ol>
     *
     * <h3>匹配规则:</h3>
     * <ul>
     *   <li>精确匹配: {@code user:create} 匹配 {@code user:create}</li>
     *   <li>通配符匹配: {@code user:*} 匹配 {@code user:create}, {@code user:update}</li>
     *   <li>超级权限: {@code *} 匹配所有权限</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 精确匹配
     * if (permissionService.hasPermission("user:create")) {
     *     // 可以创建用户
     * }
     *
     * // 通配符匹配 (用户权限为 user:*)
     * if (permissionService.hasPermission("user:create")) {
     *     // user:* 匹配 user:create,返回 true
     * }
     *
     * // 超级权限 (用户权限为 *)
     * if (permissionService.hasPermission("any:permission")) {
     *     // * 匹配任何权限,返回 true
     * }
     * }</pre>
     *
     * @param permission 权限标识,可以为 {@code null}
     * @return 如果拥有权限返回 {@code true},否则返回 {@code false}
     * @see #hasAnyPermission(String...)
     */
    boolean hasPermission(String permission);

    /**
     * 检查当前登录用户是否拥有任意一个指定权限.
     *
     * <p>只要拥有其中一个权限即返回 {@code true},
     * 适用于需要多种权限中任意一种的场景.
     *
     * <h3>使用场景:</h3>
     * <ul>
     *   <li>多种方式可以完成的操作</li>
     *   <li>角色和权限混合验证</li>
     *   <li>复杂的权限控制逻辑</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 用户可以通过多种方式删除数据
     * if (permissionService.hasAnyPermission("user:delete", "admin:delete", "data:delete")) {
     *     // 拥有任意一种删除权限即可
     *     userService.delete(userId);
     * }
     *
     * // 角色和权限混合
     * if (permissionService.hasAnyPermission("user:update", "admin:all")) {
     *     // 拥有 user:update 权限或 admin:all 权限均可
     *     userService.update(user);
     * }
     * }</pre>
     *
     * @param permissions 权限标识数组,可以为 {@code null} 或空数组
     * @return 如果拥有任意一个权限返回 {@code true},如果参数为空或不拥有任何权限返回 {@code false}
     * @see #hasPermission(String)
     */
    boolean hasAnyPermission(String... permissions);

    /**
     * 检查当前登录用户是否拥有指定角色.
     *
     * <p>角色验证逻辑:
     * <ol>
     *   <li>如果是管理员角色,直接返回 {@code true}</li>
     *   <li>检查角色列表是否包含指定角色</li>
     *   <li>返回验证结果</li>
     * </ol>
     *
     * <h3>角色 vs 权限:</h3>
     * <ul>
     *   <li>角色是用户的身份标识 (如 ADMIN, MANAGER)</li>
     *   <li>权限是具体的操作能力 (如 user:create)</li>
     *   <li>角色通常对应一组权限</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 检查是否为经理角色
     * if (permissionService.hasRole("MANAGER")) {
     *     // 经理专属功能
     *     return managerDashboard();
     * }
     *
     * // 检查是否为测试人员
     * if (permissionService.hasRole("TESTER")) {
     *     // 测试人员功能
     *     return testerTools();
     * }
     * }</pre>
     *
     * @param role 角色标识,可以为 {@code null}
     * @return 如果拥有角色返回 {@code true},否则返回 {@code false}
     * @see #hasAnyRole(String...)
     */
    boolean hasRole(String role);

    /**
     * 检查当前登录用户是否拥有任意一个指定角色.
     *
     * <p>只要拥有其中一个角色即返回 {@code true},
     * 适用于需要多种角色中任意一种的场景.
     *
     * <h3>使用场景:</h3>
     * <ul>
     *   <li>多种角色可以访问的功能</li>
     *   <li>层级角色验证</li>
     *   <li>角色组管理</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 管理员和经理都可以访问
     * if (permissionService.hasAnyRole("ADMIN", "MANAGER")) {
     *     return adminOrManagerDashboard();
     * }
     *
     * // 任意级别的管理员都可以
     * if (permissionService.hasAnyRole("SUPER_ADMIN", "ADMIN", "MANAGER")) {
     *     return managementTools();
     * }
     * }</pre>
     *
     * @param roles 角色标识数组,可以为 {@code null} 或空数组
     * @return 如果拥有任意一个角色返回 {@code true},如果参数为空或不拥有任何角色返回 {@code false}
     * @see #hasRole(String)
     */
    boolean hasAnyRole(String... roles);

    /**
     * 获取管理员角色标识.
     *
     * <p>返回配置的管理员角色标识,默认为 {@code SUPER_ADMIN}.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * String superRole = permissionService.getSuperRole();
     * log.info("管理员角色标识: {}", superRole); // SUPER_ADMIN
     * }</pre>
     *
     * @return 管理员角色标识
     * @see com.basetc.base.security.properties.BasetcSecurityPermissionsProperties#getSuperRole()
     */
    String getSuperRole();

    /**
     * 获取超级权限标识.
     *
     * <p>返回配置的超级权限标识,默认为 {@code *}.
     * 拥有此权限的用户拥有所有权限,等同于管理员.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * String allPermission = permissionService.getAllPermission();
     * log.info("超级权限标识: {}", allPermission); // *
     * }</pre>
     *
     * @return 超级权限标识
     * @see com.basetc.base.security.properties.BasetcSecurityPermissionsProperties#getAllPermission()
     */
    String getAllPermission();

}

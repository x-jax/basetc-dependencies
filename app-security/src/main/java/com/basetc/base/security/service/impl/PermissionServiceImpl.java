package com.basetc.base.security.service.impl;


import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.properties.BasetcSecurityPermissionsProperties;
import com.basetc.base.security.service.PermissionService;
import com.basetc.base.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * 权限服务实现类.
 *
 * <p>实现权限验证相关的方法,提供完整的权限和角色验证功能.
 * 通过配置属性和安全上下文中的用户信息,进行精确的权限验证.</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>管理员角色验证: 检查用户是否为管理员</li>
 *   <li>超级权限验证: 检查用户是否拥有所有权限</li>
 *   <li>权限精确匹配: 验证用户是否拥有特定权限</li>
 *   <li>批量权限验证: 检查用户是否拥有多个权限中的任意一个</li>
 *   <li>角色验证: 检查用户是否拥有特定角色</li>
 * </ul>
 *
 * <h3>设计特点</h3>
 * <ul>
 *   <li>层次化验证: 管理员和超级权限优先验证</li>
 *   <li>安全上下文集成: 与SecurityUtils紧密集成</li>
 *   <li>配置化: 通过BasetcSecurityPermissionsProperties进行配置</li>
 *   <li>高效验证: 使用Set集合进行快速权限查找</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在Spring容器中配置
 * @Bean
 * public PermissionService permissionService(
 *         BasetcSecurityPermissionsProperties properties) {
 *     return new PermissionServiceImpl(properties);
 * }
 * 
 * // 在业务逻辑中使用
 * if (permissionService.hasPermission("user:create")) {
 *     // 用户有创建用户的权限
 *     userService.createUser(user);
 * }
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see PermissionService
 * @see BasetcSecurityPermissionsProperties
 * @see SecurityUtils
 */
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    /**
     * 安全权限配置属性
     */
    private final BasetcSecurityPermissionsProperties basetcSecurityPermissionsProperties;


    /**
     * 检查当前登录用户是否为管理员.
     *
     * <p>通过判断用户角色列表中是否包含管理员角色标识来确定.
     *
     * @return 如果是管理员返回 true，否则返回 false
     */
    @Override
    public boolean isAdmin() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (Objects.isNull(loginUser)) {
            return false;
        }
        return isAdmin(loginUser);
    }

    /**
     * 检查登录用户是否为管理员.
     *
     * <p>通过判断用户角色列表中是否包含管理员角色标识来确定.
     *
     * @param loginUser 登录用户信息
     * @return 如果是管理员返回 true，否则返回 false
     */
    @Override
    public boolean isAdmin(@NonNull LoginUser loginUser) {
        Set<String> roles = loginUser.getRoles();
        if (Objects.isNull(roles) || roles.isEmpty()) {
            return false;
        }
        return hasAdminRole(roles);
    }

    /**
     * 检查登录用户是否为管理员角色.
     *
     * <p>通过判断用户角色列表中是否包含管理员角色标识来确定.
     *
     * @param roles 角色标识数组
     * @return 如果是管理员角色返回 true，否则返回 false
     */
    @Override
    public boolean hasAdminRole(@NonNull Set<String> roles) {
        if (roles.isEmpty()) {
            return false;
        }
        return roles.contains(basetcSecurityPermissionsProperties.getSuperRole());
    }

    /**
     * 检查登录用户是否拥有超级权限.
     *
     * <p>通过判断用户权限列表中是否包含超级权限标识来确定.
     *
     * @return 如果拥有超级权限返回 true，否则返回 false
     */
    @Override
    public boolean hasAllPermission() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (Objects.isNull(loginUser)) {
            return false;
        }
        return hasAllPermission(loginUser);
    }

    /**
     * 检查登录用户是否拥有超级权限.
     *
     * <p>通过判断用户权限列表中是否包含超级权限标识来确定.
     *
     * @param loginUser 登录用户信息
     * @return 如果拥有超级权限返回 true，否则返回 false
     */
    @Override
    public boolean hasAllPermission(@NonNull LoginUser loginUser) {
        Set<String> permissions = loginUser.getPermissions();
        if (Objects.isNull(permissions) || permissions.isEmpty()) {
            return false;
        }
        return permissions.contains(basetcSecurityPermissionsProperties.getAllPermission());
    }

    /**
     * 检查当前登录用户是否拥有指定权限.
     *
     * <p>如果用户是管理员角色或拥有超级权限，则自动返回 true. 否则检查用户权限列表中是否包含指定权限.
     *
     * @param permission 权限标识，可以为 null
     * @return 如果拥有权限返回 true，否则返回 false
     */
    @Override
    public boolean hasPermission(@NonNull String permission) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (Objects.isNull(loginUser)) {
            return false;
        }
        Set<String> permissions = loginUser.getPermissions();
        if (Objects.isNull(permissions) || permissions.isEmpty()) {
            return false;
        }
        return hasAllPermission(loginUser) || permissions.contains(permission);
    }

    /**
     * 检查当前登录用户是否拥有任意一个指定权限.
     *
     * <p>如果用户是管理员角色或拥有超级权限，则自动返回 true. 否则检查用户权限列表中是否包含任意一个指定的权限.
     *
     * @param permissions 权限标识数组，可以为 null 或空数组
     * @return 如果拥有任意一个权限返回 true，如果参数为空或不拥有任何权限返回 false
     */
    @Override
    public boolean hasAnyPermission(@NonNull String... permissions) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (Objects.isNull(loginUser)) {
            return false;
        }
        Set<String> perms = loginUser.getPermissions();
        if (Objects.isNull(perms) || perms.isEmpty()) {
            return false;
        }
        return hasAllPermission(loginUser) || Arrays.stream(permissions).anyMatch(perms::contains);
    }

    /**
     * 检查当前登录用户是否拥有指定角色.
     *
     * <p>如果用户是管理员角色，则自动返回 true. 否则检查用户角色列表中是否包含指定角色.
     *
     * @param role 角色标识，可以为 null
     * @return 如果拥有角色返回 true，否则返回 false
     */
    @Override
    public boolean hasRole(@NonNull String role) {
        return hasAnyRole(role);
    }

    /**
     * 检查当前登录用户是否拥有任意一个指定角色.
     *
     * <p>如果用户是管理员角色，则自动返回 true. 否则检查用户角色列表中是否包含任意一个指定的角色.
     *
     * @param roles 角色标识数组，可以为 null 或空数组
     * @return 如果拥有任意一个角色返回 true，如果参数为空或不拥有任何角色返回 false
     */
    @Override
    public boolean hasAnyRole(@NonNull String... roles) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (Objects.isNull(loginUser)) {
            return false;
        }
        Set<String> roleSet = loginUser.getRoles();
        if (Objects.isNull(roleSet) || roleSet.isEmpty()) {
            return false;
        }
        return isAdmin(loginUser) || Arrays.stream(roles).anyMatch(roleSet::contains);
    }

    /**
     * 获取管理员角色标识.
     *
     * @return 管理员角色标识
     */
    @Override
    public String getSuperRole() {
        return basetcSecurityPermissionsProperties.getSuperRole();
    }

    /**
     * 获取超级权限标识.
     *
     * @return 超级权限标识
     */
    @Override
    public String getAllPermission() {
        return basetcSecurityPermissionsProperties.getAllPermission();
    }
}

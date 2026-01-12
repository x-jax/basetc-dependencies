package com.basetc.base.security.utils;

import com.basetc.base.security.domain.LoginUser;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类.
 *
 * <p>提供便捷的方法获取当前登录用户信息，包括用户ID、用户名等. 基于Spring Security的安全上下文实现，线程安全.</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>获取认证对象: 从SecurityContext获取当前认证信息</li>
 *   <li>获取登录用户: 提取完整的LoginUser对象</li>
 *   <li>获取用户信息: 便捷获取用户ID、用户名等基础信息</li>
 *   <li>权限检查: 检查当前用户是否拥有特定权限或角色</li>
 *   <li>认证状态: 检查用户是否已通过身份验证</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 获取当前登录用户
 * LoginUser user = SecurityUtils.getLoginUser();
 * 
 * // 获取当前用户ID
 * Long userId = SecurityUtils.getUserId();
 * 
 * // 检查用户是否已认证
 * if (SecurityUtils.isAuthenticated()) {
 *     // 用户已登录
 * }
 * 
 * // 检查用户权限
 * if (SecurityUtils.hasPermission("user:create")) {
 *     // 用户有创建权限
 * }
 * }</pre>
 *
 * <h3>设计特点</h3>
 * <ul>
 *   <li>线程安全: 使用UtilityClass确保线程安全</li>
 *   <li>便捷访问: 提供简化的API访问安全上下文</li>
 *   <li>类型安全: 支持泛型扩展类型</li>
 *   <li>安全防护: 妥善处理未认证情况</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see org.springframework.security.core.context.SecurityContextHolder
 * @see org.springframework.security.core.Authentication
 * @see LoginUser
 */
@UtilityClass
public class SecurityUtils {

  /**
   * 获取当前认证对象.
   *
   * <p>从Spring Security的安全上下文中获取当前线程的认证信息.
   *
   * @return 认证对象，如果未认证则返回null
   */
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * 获取当前登录用户信息.
   *
   * <p>从认证对象中提取登录用户信息.
   *
   * @return 登录用户信息，如果未登录或principal不是LoginUser类型则返回null
   */
  public static LoginUser getLoginUser() {
    Authentication authentication = getAuthentication();
    if (authentication == null) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof LoginUser loginUser) {
      return loginUser;
    }
    return null;
  }

  /**
   * 获取当前登录用户信息（支持扩展类型）.
   *
   * <p>从认证对象中提取登录用户信息，支持返回LoginUser的子类类型.
   *
   * @param <T> LoginUser的子类类型
   * @return 登录用户信息，如果未登录或principal不是LoginUser类型则返回null
   */
  @SuppressWarnings("all")
  public static <T extends LoginUser> T getLoginUserExt() {
    Authentication authentication = getAuthentication();
    if (authentication == null) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof LoginUser loginUser) {
      return (T) loginUser;
    }
    return null;
  }

  /**
   * 获取当前登录用户ID.
   *
   * <p>便捷方法，直接获取当前用户的用户ID.
   *
   * @return 用户ID，如果未登录则返回null
   */
  public static Long getUserId() {
    final LoginUser loginUser = getLoginUser();
    if (loginUser == null) {
      return null;
    }
    return loginUser.getUserId();
  }

  /**
   * 获取当前登录用户名.
   *
   * <p>便捷方法，直接获取当前用户的用户名.
   *
   * @return 用户名，如果未登录则返回null
   */
  public static String getUsername() {
    final LoginUser loginUser = getLoginUser();
    if (loginUser == null) {
      return null;
    }
    return loginUser.getUsername();
  }

  /**
   * 检查当前用户是否已认证.
   *
   * <p>判断当前用户是否已通过身份验证.
   *
   * @return 如果已认证返回true，否则返回false
   */
  public static boolean isAuthenticated() {
    Authentication authentication = getAuthentication();
    return authentication != null && authentication.isAuthenticated();
  }

  /**
   * 检查当前用户是否拥有指定权限.
   *
   * <p>便捷方法，直接检查当前用户是否拥有指定权限.
   *
   * @param permission 权限标识
   * @return 如果拥有权限返回true，否则返回false
   */
  public static boolean hasPermission(String permission) {
    LoginUser loginUser = getLoginUser();
    if (loginUser == null) {
      return false;
    }
    return loginUser.getPermissions().contains(permission);
  }

  /**
   * 检查当前用户是否拥有指定角色.
   *
   * <p>便捷方法，直接检查当前用户是否拥有指定角色.
   *
   * @param role 角色标识
   * @return 如果拥有角色返回true，否则返回false
   */
  public static boolean hasRole(String role) {
    LoginUser loginUser = getLoginUser();
    if (loginUser == null) {
      return false;
    }
    return loginUser.getRoles().contains(role);
  }
}

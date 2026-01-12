# BaseTC 安全模块 (Security Module)

## 概述

BaseTC 安全模块是基于 Spring Security 框架构建的轻量级安全解决方案，旨在为微服务架构提供统一的安全认证和授权机制。该模块集成了 JWT 认证、权限控制、会话管理、单点登录等多种安全特性。

## 核心功能

### 1. 认证机制

#### JWT 认证
- **无状态认证**：使用 JWT Token 实现无状态的用户认证
- **自动刷新**：支持 Token 自动刷新机制
- **安全传输**：通过 HTTP Header 传输，支持 HTTPS 加密

#### 多种认证方式
- 用户名/密码认证
- OAuth2 认证
- 验证码认证
- 单点登录(SSO)支持

### 2. 授权机制

#### 基于角色的访问控制 (RBAC)
- **角色管理**：支持多角色分配
- **权限粒度**：细粒度的权限控制
- **动态授权**：运行时权限验证

#### 权限表达式
```java
@PreAuthorize("hasPermission('user:create')")
public void createUser(User user) {
    // 只有拥有 user:create 权限的用户才能访问
}
```

### 3. 安全防护

#### 会话管理
- **单点登录**：同一用户只能在一个设备上登录
- **IP 绑定**：可选择性地将 Token 与 IP 地址绑定
- **User-Agent 验证**：防止 Token 在不同浏览器间被劫持

#### CSRF 保护
- 可配置的 CSRF 保护机制
- 适用于传统 Web 应用

## 模块架构

```
app-security/
├── annotation/           # 安全注解
│   ├── AnonymousAccess.java
│   └── Permission.java
├── autoconfigure/        # 自动配置
│   └── BasetcSecurityAutoConfiguration.java
├── context/              # 安全上下文
│   └── PasswordScoped.java
├── domain/               # 安全领域模型
│   ├── AuthenticateRequest.java
│   ├── LoginUser.java
│   └── OauthAuthenticateRequest.java
├── enums/                # 安全枚举
│   └── BasetcSecurityAuthFilter.java
├── filter/               # 安全过滤器
│   ├── SecurityAuthenticationFilter.java
│   └── SecurityAuthenticationFilterImpl.java
├── listener/             # 安全监听器
│   └── SessionManagerListener.java
├── properties/           # 安全配置属性
│   ├── BasetcSecurityAuthProperties.java
│   ├── BasetcSecurityCorsProperties.java
│   ├── BasetcSecurityJwtProperties.java
│   ├── BasetcSecurityPermissionsProperties.java
│   ├── BasetcSecurityProperties.java
│   ├── BasetcSecurityRedisProperties.java
│   ├── BasetcSecurityResponseProperties.java
│   └── BasetcSecuritySessionProperties.java
├── service/              # 安全服务接口
│   ├── AnonymousAccessService.java
│   ├── PermissionService.java
│   ├── SecurityAuthenticateService.java
│   ├── SecurityAuthenticateUserService.java
│   ├── SecurityOauthAuthenticateService.java
│   ├── SecurityTokenGenerate.java
│   ├── SecurityTokenIdGenerate.java
│   └── SecurityUserDetailService.java
├── service/impl/         # 安全服务实现
│   ├── AccessDeniedHandlerImpl.java
│   ├── AnonymousAccessServiceImpl.java
│   ├── AuthenticationEntryPointImpl.java
│   ├── LogoutSuccessHandlerImpl.java
│   ├── PermissionServiceImpl.java
│   ├── RedisSecurityAuthenticateUserServiceImpl.java
│   ├── SecurityAuthenticateServiceImpl.java
│   ├── SecurityOauthAuthenticateServiceImpl.java
│   ├── SecurityTokenGenerateImpl.java
│   ├── SecurityTokenIdGenerateImpl.java
│   └── SessionSecurityAuthenticateUserServiceImpl.java
├── service/suport/       # 安全支持服务
│   ├── CaptchaAuthenticate.java
│   ├── SecurityAuthenticateAfter.java
│   ├── SecurityAuthenticateBefore.java
│   ├── SecurityOauthAuthenticate.java
│   └── SecurityOauthAuthenticateBefore.java
├── service/warp/         # 安全服务包装器
│   └── SecurityAuthenticateServiceWarp.java
└── utils/                # 安全工具类
    └── SecurityUtils.java
```

## 配置详解

### 基础配置

```yaml
basetc:
  security:
    # 是否启用自动配置，默认为 true
    auto-configure: true
    
    # 认证配置
    auth:
      # 登出URL
      logout-url: /logout
      # 是否启用CSRF保护
      csrf-enabled: false
      # 白名单路径
      white-list:
        - /api/public/**
        - /auth/login
        - /auth/register
        - /swagger-ui/**
        - /v3/api-docs/**
      
      # 过滤器配置
      filter:
        # 单点登录
        single-enabled: true
        # IP验证
        ip-enabled: false
        # User-Agent验证
        user-agent-enabled: false
        # 覆盖旧登录
        overwrite-old-auth: false
    
    # JWT配置
    jwt:
      # Token头
      header: Authorization
      # Token前缀
      prefix: Bearer 
      # 密钥
      secret: your-jwt-secret-key
      # 过期时间(秒)
      expire-time: 3600
      # 刷新时间(秒)
      refresh-time: 300
    
    # 权限配置
    permissions:
      # 超级管理员角色
      super-role: SUPER_ADMIN
      # 超级权限标识
      all-permission: '*'
    
    # Redis配置
    redis:
      enabled: true
      key-prefix: security:
      timeout: 3600
    
    # Session配置
    session:
      enabled: false
      max-concurrent: 1
    
    # CORS配置
    cors:
      enabled: true
      allowed-origin-patterns: ["*"]
      allowed-methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
      allowed-headers: ["*"]
      allow-credentials: true
      max-age: 3600
    
    # 响应配置
    response:
      # 未授权响应
      unauthorized:
        http-code: 401
        content-type: application/json
        body:
          code: 401
          msg: "请先登录"
      
      # 权限不足响应
      access-denied:
        http-code: 403
        content-type: application/json
        body:
          code: 403
          msg: "权限不足"
```

### 详细配置说明

#### 认证配置 (auth)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| logout-url | String | /logout | 登出URL |
| csrf-enabled | boolean | false | 是否启用CSRF保护 |
| white-list | List | [] | 白名单路径列表 |

#### JWT 配置 (jwt)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| header | String | Authorization | Token头部名称 |
| prefix | String | Bearer | Token前缀 |
| secret | String | - | JWT密钥 |
| expire-time | long | 3600 | Token过期时间(秒) |
| refresh-time | long | 300 | Token刷新时间(秒) |

#### 过滤器配置 (auth.filter)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| single-enabled | boolean | false | 是否启用单点登录 |
| ip-enabled | boolean | false | 是否启用IP验证 |
| user-agent-enabled | boolean | false | 是否启用User-Agent验证 |
| overwrite-old-auth | boolean | false | 是否覆盖旧登录 |

## 使用指南

### 1. 基础集成

#### Maven 依赖
```xml
<dependency>
    <groupId>com.basetc</groupId>
    <artifactId>app-security</artifactId>
    <version>${project.version}</version>
</dependency>
```

#### 启用安全配置
```java
@SpringBootApplication
@EnableWebSecurity
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 认证服务使用

#### 用户登录
```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final SecurityAuthenticateService authenticateService;
    private final SecurityAuthenticateUserService authenticateUserService;
    
    @PostMapping("/login")
    public R<LoginUser> login(@RequestBody AuthenticateRequest request) {
        try {
            // 执行认证
            LoginUser loginUser = authenticateService.authenticate(request);
            // 生成Token
            String token = authenticateUserService.createToken(loginUser);
            loginUser.setToken(token);
            return R.success(loginUser);
        } catch (AuthenticationException e) {
            return R.error(401, "用户名或密码错误");
        }
    }
    
    @PostMapping("/logout")
    public R<Void> logout() {
        authenticateUserService.logout();
        return R.success();
    }
}
```

#### 获取当前用户
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    public LoginUser getCurrentUser() {
        return SecurityUtils.getLoginUser();
    }
    
    public Long getCurrentUserId() {
        return SecurityUtils.getUserId();
    }
    
    public String getCurrentUsername() {
        return SecurityUtils.getUsername();
    }
}
```

### 3. 权限控制

#### 接口权限验证
```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final PermissionService permissionService;
    
    @PostMapping
    @PreAuthorize("hasPermission('user:create')")
    public R<Void> createUser(@RequestBody User user) {
        // 创建用户逻辑
        return R.success();
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('user:update')")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        // 更新用户逻辑
        return R.success();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('user:delete')")
    public R<Void> deleteUser(@PathVariable Long id) {
        // 删除用户逻辑
        return R.success();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('user:view')")
    public R<User> getUser(@PathVariable Long id) {
        // 获取用户逻辑
        return R.success();
    }
}
```

#### 手动权限验证
```java
@Service
@RequiredArgsConstructor
public class BusinessService {
    
    private final PermissionService permissionService;
    
    public void sensitiveOperation() {
        // 检查是否为管理员
        if (permissionService.isAdmin()) {
            // 执行管理员操作
        }
        
        // 检查特定权限
        if (permissionService.hasPermission("business:execute")) {
            // 执行业务操作
        }
        
        // 检查多个权限中的任意一个
        if (permissionService.hasAnyPermission("business:execute", "admin:all")) {
            // 执行业务操作
        }
    }
}
```

### 4. 自定义配置

#### 自定义用户详情服务
```java
@Service
public class CustomUserDetailService implements SecurityUserDetailService {
    
    @Override
    public LoginUser loadUserByUsername(String username, String password) {
        // 从数据库加载用户信息
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }
        
        // 构建LoginUser对象
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRoles(getUserRoles(user.getId()));
        loginUser.setPermissions(getUserPermissions(user.getId()));
        
        return loginUser;
    }
}
```

#### 自定义权限服务
```java
@Component("ss")  // 注意：Bean名称必须是 "ss"
public class CustomPermissionService implements PermissionService {
    
    @Override
    public boolean isAdmin() {
        // 自定义管理员判断逻辑
        return false;
    }
    
    @Override
    public boolean hasPermission(String permission) {
        // 自定义权限验证逻辑
        return false;
    }
    
    // 实现其他方法...
}
```

## 安全最佳实践

### 1. 密码安全
- 使用强密码策略
- 定期更换密码
- 使用 BCrypt 加密算法

### 2. Token 安全
- 使用强密钥
- 设置合理的过期时间
- 实施 Token 刷新机制

### 3. 会话管理
- 启用单点登录防止账户共享
- 适时清理过期会话
- 考虑 IP 绑定以增强安全性

### 4. 权限控制
- 实施最小权限原则
- 定期审核权限分配
- 使用角色继承简化管理

## 常见问题

### Q: 如何禁用自动配置？
A: 在配置文件中设置 `basetc.security.auto-configure=false`

### Q: 如何自定义未授权响应？
A: 通过配置 `basetc.security.response.unauthorized` 实现

### Q: 如何实现单点登录？
A: 启用 Redis 并设置 `basetc.security.auth.filter.single-enabled=true`

### Q: 如何添加自定义白名单路径？
A: 在 `basetc.security.auth.white-list` 中添加路径

## 性能优化

### 1. 缓存策略
- 用户信息缓存
- 权限信息缓存
- Token 有效性缓存

### 2. 数据库优化
- 索引优化
- 查询优化
- 连接池配置

### 3. 网络优化
- Token 压缩
- 批量请求支持
- CDN 集成

## 扩展功能

### 1. 多租户支持
- 租户隔离
- 数据权限控制
- 资源隔离

### 2. 审计日志
- 操作日志记录
- 安全日志监控
- 异常行为检测

### 3. 限流控制
- 接口限流
- 用户限流
- IP 限流

---

该安全模块提供了企业级的安全解决方案，通过模块化设计和灵活配置，能够满足各种规模应用的安全需求。
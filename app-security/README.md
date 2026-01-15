# App-Security 安全模块

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-blue.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.2.0-blue.svg)](https://spring.io/projects/spring-security)
[![JWT](https://img.shields.io/badge/JWT-Supported-brightgreen.svg)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📖 概述

App-Security 是基于 Spring Security 构建的企业级安全框架，为微服务架构提供统一的身份认证、授权和安全防护机制。该模块集成了 JWT 认证、细粒度权限控制、会话管理、单点登录等核心安全特性，致力于简化企业级应用的安全开发。

## ✨ 核心功能

### 🔐 认证机制
- **JWT 无状态认证**：基于 JSON Web Token 的无状态身份认证，支持 Token 自动刷新
- **多认证方式支持**：用户名/密码认证、OAuth2 认证、验证码认证
- **单点登录(SSO)**：同一用户多设备登录控制，支持 IP 和 User-Agent 绑定

### 🛡️ 授权机制
- **基于角色的访问控制 (RBAC)**：细粒度的角色和权限管理
- **动态权限验证**：运行时权限检查，支持注解和编程式权限控制
- **权限表达式支持**：`@PreAuthorize("hasPermission('user:create')")` 注解方式

### 🎯 安全防护
- **会话管理**：用户会话生命周期管理，支持并发登录限制
- **CSRF 保护**：可配置的跨站请求伪造防护
- **安全响应**：统一的异常处理和安全响应格式
- **CORS 支持**：灵活的跨域资源共享配置

### ⚙️ 扩展能力
- **可插拔设计**：支持自定义用户详情服务、权限服务
- **事件监听**：登录、登出等安全事件监听机制
- **分布式支持**：Redis 集成实现分布式环境下的安全管理

## 🚀 快速开始

### 🔧 依赖配置

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.basetc</groupId>
    <artifactId>app-security</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 📝 配置示例

在 `application.yml` 中配置安全参数：

```yaml
basetc:
  security:
    # 基础配置
    auto-configure: true
    
    # 认证配置
    auth:
      logout-url: /logout
      csrf-enabled: false
      white-list:
        - /api/public/**
        - /auth/login
        - /swagger-ui/**
      filter:
        single-enabled: true  # 启用单点登录
        ip-enabled: false     # 启用IP绑定
    
    # JWT配置
    jwt:
      header: Authorization
      prefix: Bearer 
      secret: your-jwt-secret-key
      expire-time: 3600      # Token过期时间(秒)
      refresh-time: 300      # Token刷新时间(秒)
    
    # Redis配置
    redis:
      enabled: true
      key-prefix: security:
    
    # CORS配置
    cors:
      enabled: true
      allowed-origin-patterns: ["*"]
      allowed-methods: ["GET", "POST", "PUT", "DELETE"]
```

### 📋 基本使用

#### 用户登录接口

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
            LoginUser loginUser = authenticateService.authenticate(request);
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

#### 权限控制示例

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping
    @PreAuthorize("hasPermission('user:create')")
    public R<Void> createUser(@RequestBody User user) {
        // 只有拥有 user:create 权限的用户才能访问
        return R.success();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('user:view')")
    public R<User> getUser(@PathVariable Long id) {
        // 只有拥有 user:view 权限的用户才能访问
        return R.success(userService.getById(id));
    }
}
```

#### 获取当前用户

```java
@Service
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

## 📖 详细配置文档

### 🔧 配置参考

App-Security 模块支持丰富的配置选项，以下是完整的配置参考：

#### 基础配置

```yaml
basetc:
  security:
    # 是否自动配置安全模块
    auto-configure: true
```

#### 认证配置

```yaml
basetc:
  security:
    auth:
      # 是否自动配置认证模块
      auto-configure: true
      # 登出URL
      logout-url: /logout
      # 是否开启CSRF保护
      csrf-enabled: false
      # 白名单路径
      white-list:
        - /api/public/**
        - /auth/login
        - /swagger-ui/**
        - /v3/api-docs/**
      # 过滤器配置
      filter:
        # 是否启用单账号唯一登录
        single-enabled: false
        # 是否覆盖旧登录
        overwrite-old-auth: false
        # 是否启用IP限制
        ip-enabled: false
        # 是否启用User-Agent限制
        user-agent-enabled: false
      # 未授权响应配置
      un-authorized:
        http-code: 401
        content-type: application/json
        body:
          code: 401
          msg: "当前资源无法访问,请登录"
      # 权限不足响应配置
      access-denied:
        http-code: 403
        content-type: application/json
        body:
          code: 403
          msg: "权限不足,无法访问当前资源"
```

#### JWT 配置

```yaml
basetc:
  security:
    jwt:
      # 令牌头
      header: Authorization
      # 令牌前缀
      prefix: Bearer 
      # 令牌过期时间(分钟)
      expire: 30
      # 令牌刷新间隔(分钟)
      refresh-scope: 15
      # 令牌密钥
      secret: BaseTC:139fc8d7b794540fa52621ec8c211a82
```

#### 权限配置

```yaml
basetc:
  security:
    permissions:
      # 超级管理员角色标识
      super-role: SUPER_ADMIN
      # 所有权限标识
      all-permission: '*'
```

#### Redis 存储配置

```yaml
basetc:
  security:
    redis:
      # 是否启用Redis存储模式
      enable: false
      # Redis键前缀
      redis-key-prefix: basetc:user:
```

#### Session 存储配置

```yaml
basetc:
  security:
    session:
      # 是否启用Session存储模式
      enable: true
      # Session属性前缀
      session-key-prefix: basetc_user
```

#### CORS 配置

```yaml
basetc:
  security:
    cors:
      # 是否启用CORS
      enabled: true
      # 允许的源
      allowed-origin-patterns:
        - http://localhost:3000
        - https://example.com
      # 允许的HTTP方法
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      # 允许的请求头
      allowed-headers: '*'
      # 是否允许携带凭证
      allow-credentials: true
      # 预检请求缓存时间(秒)
      max-age: 3600
```

### 📖 配置说明

#### 1. 认证配置 (`basetc.security.auth`)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| auto-configure | boolean | true | 是否自动配置认证模块 |
| logout-url | string | /logout | 用户登出URL |
| csrf-enabled | boolean | false | 是否开启CSRF保护 |
| white-list | list | [] | 无需认证的白名单路径 |
| filter.single-enabled | boolean | false | 是否启用单点登录 |
| filter.overwrite-old-auth | boolean | false | 是否覆盖旧登录 |
| filter.ip-enabled | boolean | false | 是否启用IP限制 |
| filter.user-agent-enabled | boolean | false | 是否启用User-Agent限制 |

#### 2. JWT 配置 (`basetc.security.jwt`)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| header | string | Authorization | HTTP请求头中的令牌名称 |
| prefix | string | "Bearer " | 令牌前缀 |
| expire | long | 30 | 令牌过期时间(分钟) |
| refresh-scope | long | 15 | 令牌刷新间隔(分钟) |
| secret | string | BaseTC:139fc8d7b794540fa52621ec8c211a82 | 令牌密钥 |

#### 3. 权限配置 (`basetc.security.permissions`)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| super-role | string | SUPER_ADMIN | 超级管理员角色标识 |
| all-permission | string | * | 所有权限标识 |

#### 4. 存储配置

**Redis 模式** (`basetc.security.redis`):

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| enable | boolean | false | 是否启用Redis存储 |
| redis-key-prefix | string | basetc:user: | Redis键前缀 |

**Session 模式** (`basetc.security.session`):

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| enable | boolean | true | 是否启用Session存储 |
| session-key-prefix | string | basetc_user | Session属性前缀 |

#### 5. CORS 配置 (`basetc.security.cors`)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| enabled | boolean | true | 是否启用CORS |
| allowed-origin-patterns | list | [] | 允许的源模式 |
| allowed-methods | list | [] | 允许的HTTP方法 |
| allowed-headers | list | [] | 允许的请求头 |
| allow-credentials | boolean | true | 是否允许携带凭证 |
| max-age | int | 3600 | 预检请求缓存时间(秒) |

### 🎯 配置优先级

配置优先级从高到低如下：

1. **命令行参数**：`--basetc.security.jwt.secret=your-secret`
2. **系统环境变量**：`BASETC_SECURITY_JWT_SECRET=your-secret`
3. **application-{profile}.properties/yml**：环境特定配置
4. **application.properties/yml**：全局配置
5. **默认值**：代码中定义的默认值

### ⚠️ 注意事项

1. **生产环境安全**：
   - 必须修改 `jwt.secret` 为强密钥
   - 建议启用 HTTPS
   - 配置合理的 Token 过期时间

2. **存储模式选择**：
   - 单机应用：Session 模式 (默认)
   - 分布式应用：Redis 模式
   - 不要同时启用两种模式

3. **白名单配置**：
   - 支持 Ant 风格路径匹配 (`/**`)
   - 白名单路径必须以 `/` 开头

4. **CORS 配置**：
   - 生产环境建议明确指定允许的源
   - 带凭证的请求不允许使用通配符源

## 📂 模块架构

```
app-security/
├── annotation/           # 安全注解
│   ├── AnonymousAccess.java  # 匿名访问注解
│   └── Permission.java       # 权限注解
├── autoconfigure/        # 自动配置
│   └── BasetcSecurityAutoConfiguration.java
├── context/              # 安全上下文
│   └── PasswordScoped.java
├── domain/               # 领域模型
│   ├── AuthenticateRequest.java
│   ├── LoginUser.java
│   └── OauthAuthenticateRequest.java
├── enums/                # 安全枚举
│   └── BasetcSecurityAuthFilter.java
├── event/                # 安全事件
│   ├── LoginEvent.java
│   └── LogoutEvent.java
├── filter/               # 安全过滤器
│   ├── SecurityAuthenticationFilter.java
│   └── SecurityAuthenticationFilterImpl.java
├── listener/             # 事件监听器
│   └── SessionManagerListener.java
├── properties/           # 配置属性
│   ├── BasetcSecurityProperties.java
│   ├── BasetcSecurityJwtProperties.java
│   └── ... (更多配置类)
├── service/              # 安全服务
│   ├── impl/            # 服务实现
│   ├── suport/          # 支持服务
│   └── warp/            # 服务包装
└── utils/                # 工具类
    └── SecurityUtils.java
```

## 📚 详细文档

- [完整模块文档](SECURITY-MODULE-README.md)：包含详细的配置说明和使用指南
- [API 参考](javadoc)：完整的 API 文档
- [最佳实践](docs/BEST_PRACTICES.md)：安全开发最佳实践指南

## 🎨 自定义扩展

### 自定义用户详情服务

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

### 自定义权限服务

```java
@Component("ss")  // Bean名称必须是 "ss"
public class CustomPermissionService implements PermissionService {
    
    @Override
    public boolean isAdmin() {
        // 自定义管理员判断逻辑
        return SecurityUtils.hasRole("SUPER_ADMIN");
    }
    
    @Override
    public boolean hasPermission(String permission) {
        // 自定义权限验证逻辑
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && loginUser.getPermissions().contains(permission);
    }
}
```

## 🛡️ 安全最佳实践

1. **密码安全**：使用 BCrypt 等强哈希算法存储密码
2. **Token 管理**：设置合理的 Token 过期时间，使用强密钥
3. **权限控制**：实施最小权限原则，定期审核权限分配
4. **会话安全**：启用单点登录，考虑 IP 绑定增强安全性
5. **安全审计**：记录关键安全操作日志，便于追溯

## ❓ 常见问题

**Q: 如何禁用自动配置？**
A: 在配置文件中设置 `basetc.security.auto-configure=false`

**Q: 如何实现单点登录？**
A: 启用 Redis 并设置 `basetc.security.auth.filter.single-enabled=true`

**Q: 如何自定义未授权响应？**
A: 通过配置 `basetc.security.response.unauthorized` 实现

**Q: 如何添加白名单路径？**
A: 在 `basetc.security.auth.white-list` 中配置不需要认证的路径

## 🤝 贡献指南

我们欢迎社区贡献！如果您有任何问题或建议，请：

1. 查看 [问题列表](https://github.com/basetc/app-security/issues)
2. 提交 [Pull Request](https://github.com/basetc/app-security/pulls)
3. 阅读 [贡献文档](CONTRIBUTING.md)

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

## 📞 联系方式

- **项目主页**：https://github.com/basetc/app-security
- **问题反馈**：https://github.com/basetc/app-security/issues
- **文档网站**：https://basetc.github.io/app-security/

---

**App-Security** - 为企业级应用提供专业、可靠的安全解决方案！
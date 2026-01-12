<div align="center">

# BaseTC Dependencies

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.0-red.svg)](https://central.sonatype.com/)
[![Lombok](https://img.shields.io/badge/Lombok-Supported-red.svg)](https://projectlombok.org/)

**企业级 Spring Boot 基础依赖库，开箱即用的微服务基础设施**

[快速开始](#-快速开始) • [功能特性](#-核心功能) • [架构设计](#-架构设计) • [配置指南](#-配置指南) • [最佳实践](#-最佳实践) • [贡献指南](#-贡献指南) • [更新日志](CHANGELOG.md)

</div>

---

## 📖 项目简介

**BaseTC Dependencies** 是一个基于 **Spring Boot 4.0.1** 和 **Java 25** 的企业级基础依赖库，旨在为微服务架构提供开箱即用的基础设施。项目采用模块化设计，遵循 Spring Boot 生态最佳实践，集成了现代 Java 开发所需的核心功能。

### 核心优势

| 特性 | 说明 | 优势 |
|------|------|------|
| 🚀 **开箱即用** | 基于 Spring Boot AutoConfiguration，零配置启动 | 5分钟即可完成项目搭建，专注业务逻辑开发 |
| 📦 **模块化设计** | 功能模块独立，按需引入依赖 | 降低项目复杂度，减少依赖冲突，优化编译速度 |
| 🔒 **安全可靠** | 集成 Spring Security，支持 JWT 认证 | 企业级安全保障，支持多种认证方式和存储策略 |
| 🎯 **统一规范** | 统一的响应结构、异常处理、日志规范 | 团队协作更高效，代码更规范，维护成本更低 |
| 🛠️ **开发效率** | 内置分页、缓存、字典管理等常用功能 | 减少 80% 的重复代码，提升开发效率 |
| 📈 **可扩展性** | 基于接口设计，支持自定义扩展 | 灵活适配各种业务需求，轻松应对业务变化 |

### 适用场景

- ✅ **企业级微服务架构** - 为微服务提供统一的基础设施和开发规范
- ✅ **快速开发平台** - 快速搭建业务系统，专注于业务逻辑实现
- ✅ **Spring Boot 学习项目** - 学习 Spring Boot 最佳实践和企业级应用架构
- ✅ **单体应用改造** - 为现有应用提供标准化改造方案，提升代码质量

---

## 🎯 核心功能

### 1️⃣ 统一响应与异常处理

提供标准化的API响应结构和全局异常处理机制，确保所有API返回一致的格式，提高前端开发效率。

```java
// 统一响应结构
@GetMapping("/users/{id}")
public R<User> getUser(@PathVariable Long id) {
    User user = userService.getByIdOrThrow(id, () -> {
        throw new BasetcException(404, "用户不存在");
    });
    return R.success(user);
}

// 响应示例: 
// {"code": 200, "msg": "成功", "data": {"id": 1, "name": "张三"}, "timestamp": 1699999999999}
```

**特性**:
- ✅ 统一的响应格式 (`code`, `msg`, `data`, `timestamp`)
- ✅ 全局异常处理器，自动捕获所有异常
- ✅ 支持 `@Valid` 参数校验，自动返回校验失败信息
- ✅ 支持自定义异常类型和错误码
- ✅ 实现了 `Serializable` 接口，支持序列化

### 2️⃣ 智能分页查询

内置分页查询功能，自动从HTTP请求中解析分页参数，支持自定义分页参数名称和默认值。

```java
@Service
public class UserService extends BaseService<UserMapper, User> {

    public PageResult<User> pageUsers(QueryDTO query) {
        // 自动从请求中获取分页参数
        IPage<User> page = PageUtils.getPageRequest();

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(query.getName()),
                    User::getName, query.getName());

        // 分页查询
        IPage<User> result = this.page(page, wrapper);

        // 转换为统一格式
        return PageUtils.coverTableData(result);
    }
}
```

**特性**:
- ✅ 自动从 HTTP 请求中解析分页参数
- ✅ 支持自定义分页参数名称
- ✅ 防止全表更新删除，保护数据安全
- ✅ 分页参数校验，默认值和最大值限制
- ✅ 统一的分页结果格式

### 3️⃣ Redis 缓存与分布式锁

提供强大的Redis缓存操作和分布式锁功能，支持缓存自动加载、防止缓存击穿等高级特性。

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisTemplateClient redisClient;

    // 使用缓存
    public User getUserById(Long userId) {
        String cacheKey = "user:" + userId;

        return redisClient.getCacheObjectOrLoad(
            cacheKey,
            () -> RedisCacheData.of(
                userMapper.selectById(userId), // 缓存加载逻辑
                30, TimeUnit.MINUTES          // 过期时间
            )
        );
    }

    // 使用分布式锁
    public void updateUserStock(Long productId, int quantity) {
        String lockKey = "product:stock:" + productId;

        redisClient.tryLockWhile(
            lockKey,
            RedisLoadWithLock.of(() -> RedisCacheData.of(null, 10, TimeUnit.SECONDS)),
            () -> {
                // 扣减库存操作
                productMapper.decreaseStock(productId, quantity);
            }
        );
    }
}
```

**特性**:
- ✅ 支持缓存加载锁（Cache Aside Pattern）
- ✅ 支持分布式锁，防止并发问题
- ✅ 使用 FastJSON2 序列化，性能优秀
- ✅ 支持反序列化白名单，防止安全漏洞
- ✅ 自动续期机制，防止锁过期

### 4️⃣ JWT 认证与权限管理

集成Spring Security和JWT，提供完整的认证授权解决方案，支持多种存储方式。

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final TokenManager tokenManager;
    private final AuthenticationStorage authStorage;

    // 登录认证
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginDTO dto) {
        // 1. 验证用户名密码
        User user = userService.authenticate(dto);

        // 2. 构建认证对象
        Authentication auth = Authentication.builder()
            .userId(user.getId().toString())
            .username(user.getUsername())
            .roles(user.getRoles())
            .permissions(user.getPermissions())
            .build();

        // 3. 生成 JWT Token
        String token = tokenManager.createAccessToken(auth);

        // 4. 保存到存储（支持 Redis/Session 切换）
        authStorage.save(auth, Duration.ofHours(24));

        return R.success(LoginVO.builder().token(token).build());
    }
}
```

**配置示例**:

```yaml
basetc:
  security:
    auth:
      # 存储类型: REDIS, SESSION, MEMORY
      storage-type: REDIS

      # JWT 配置
      jwt:
        secret: your-256-bit-secret-key-at-least-32-bytes-long
        access-token-expiration: 3600000    # 1小时
        refresh-token-expiration: 604800000 # 7天
```

**特性**:
- ✅ 基于 JWT 的无状态认证
- ✅ 支持 Redis/Session/Memory 多种存储方式
- ✅ 支持 Access Token + Refresh Token 模式
- ✅ 自动续期机制
- ✅ 细粒度权限控制（支持角色和权限）

### 5️⃣ 枚举字典管理

提供枚举字典自动注册和管理功能，支持将枚举类型自动转换为前端友好的字典格式。

```java
// 定义字典枚举
@DictType(value = "user_status", description = "用户状态")
public enum UserStatusEnum implements BaseEnum<Integer> {

    ACTIVE(1, "正常"),
    INACTIVE(0, "禁用"),
    LOCKED(-1, "锁定");

    private final Integer value;
    private final String description;

    // 自动生成 valueOf() 和 getValue() 方法
    // 自动注册到字典扫描器
}

// 启动时自动扫描并注册为字典
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        BasetcApplication.run(Application.class, args);
        // 枚举字典自动初始化，可通过 API 查询
    }
}
```

**特性**:
- ✅ 启动时自动扫描 `@DictType` 注解的枚举
- ✅ 自动生成字典数据，供前端下拉选择使用
- ✅ 支持多级字典（如：省市区三级联动）
- ✅ 自动转换为前端友好的格式
- ✅ 支持动态字典更新

### 6️⃣ 审计日志

提供自动审计日志功能，自动记录数据的创建和修改信息。

```java
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    // 自动填充创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 自动填充创建人
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    // 自动填充更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 自动填充更新人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}

// 所有插入和更新操作，自动填充审计字段
```

**特性**:
- ✅ 自动填充创建时间、创建人、更新时间、更新人
- ✅ 支持自定义填充逻辑
- ✅ 支持函数式接口，灵活扩展
- ✅ 与 MyBatis Plus 深度集成

---

## 🚀 快速开始

### 环境要求

| 环境 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 25+ | 推荐使用 Oracle JDK 或 OpenJDK |
| Spring Boot | 4.0.1+ | 项目基于 Spring Boot 4.0.1 开发 |
| Maven | 3.9+ | 用于构建和依赖管理 |
| Redis | 7.0+ (可选) | 用于缓存和分布式锁 |
| MySQL | 8.0+ (可选) | 用于数据持久化 |

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<properties>
    <basetc.version>1.0.0</basetc.version>
</properties>

<dependencies>
    <!-- 核心模块（必需） -->
    <dependency>
        <groupId>com.basetc</groupId>
        <artifactId>app-base-common</artifactId>
        <version>${basetc.version}</version>
    </dependency>

    <!-- DAO 模块（数据库操作） -->
    <dependency>
        <groupId>com.basetc</groupId>
        <artifactId>app-base-dao</artifactId>
        <version>${basetc.version}</version>
    </dependency>

    <!-- Web 模块（API 接口） -->
    <dependency>
        <groupId>com.basetc</groupId>
        <artifactId>app-base-web</artifactId>
        <version>${basetc.version}</version>
    </dependency>

    <!-- Redis 模块（缓存和分布式锁） -->
    <dependency>
        <groupId>com.basetc</groupId>
        <artifactId>app-redis</artifactId>
        <version>${basetc.version}</version>
    </dependency>

    <!-- 安全模块（认证和授权） -->
    <dependency>
        <groupId>com.basetc</groupId>
        <artifactId>app-security</artifactId>
        <version>${basetc.version}</version>
    </dependency>
</dependencies>
```

### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
basetc:
  # DAO 配置
  dao:
    auto-configure: true
    interceptor:
      auto-configure: true
      optimistic-locker-enabled: true    # 乐观锁
      block-attack-inner-enabled: true   # 防全表更新删除
      pagination-enabled: true           # 分页
      max-page-limit: 100                # 单页最大记录数

  # Redis 配置
  redis:
    auto-configure: true
    auto-type-accept:                    # 反序列化白名单
      - com.basetc.base.common.domain.*
      - com.yourcompany.domain.*
    lock-key-prefix: "app:lock:"         # 分布式锁前缀
    lock-timeout: 3000                   # 获取锁超时时间(ms)
    lock-expire-time: 30000              # 锁过期时间(ms)

  # Web 配置
  web:
    cors:
      auto-configure: true
      allowed-origin-patterns:
        - https://*.yourcompany.com
        - http://localhost:*
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600

  # 安全配置
  security:
    auth:
      storage-type: REDIS                 # 存储类型: REDIS, SESSION, MEMORY
      jwt:
        secret: ${JWT_SECRET:your-secret-key-change-in-production}
        access-token-expiration: 3600000  # 1小时
        refresh-token-expiration: 604800000 # 7天
        issuer: your-application
```

### 3. 创建启动类

```java
package com.example.yourapp;

import com.basetc.base.common.BasetcApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YourApplication {

    public static void main(String[] args) {
        // 使用 BasetcApplication.run() 启动
        BasetcApplication.run(YourApplication.class, args);
    }
}
```

### 4. 创建实体类

```java
package com.example.yourapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String password;
    private String email;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 5. 创建 Mapper

```java
package com.example.yourapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yourapp.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

### 6. 创建 Service

```java
package com.example.yourapp.service;

import com.basetc.base.service.BaseService;
import com.example.yourapp.entity.User;
import com.example.yourapp.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<UserMapper, User> {
    // 继承 BaseService，自动拥有 CRUD 方法
}
```

### 7. 创建 Controller

```java
package com.example.yourapp.controller;

import com.basetc.base.common.response.R;
import com.basetc.base.common.domain.PageResult;
import com.basetc.base.dao.utils.PageUtils;
import com.basetc.base.common.exception.BasetcException;
import com.example.yourapp.entity.User;
import com.example.yourapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 查询用户列表（分页）
    @GetMapping
    public R<PageResult<User>> pageUsers() {
        return R.success(
            PageUtils.coverTableData(userService.page())
        );
    }

    // 查询用户详情
    @GetMapping("/{id}")
    public R<User> getUser(@PathVariable Long id) {
        return R.success(
            userService.getByIdOrThrow(id, () ->
                new BasetcException(404, "用户不存在")
            )
        );
    }

    // 新增用户
    @PostMapping
    public R<Void> addUser(@RequestBody @Valid User user) {
        userService.save(user);
        return R.success();
    }

    // 更新用户
    @PutMapping("/{id}")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        return R.success();
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return R.success();
    }
}
```

### 8. 运行项目

```bash
# 编译项目
mvn clean package

# 运行项目
java -jar target/your-app.jar

# 或使用 Spring Boot Maven 插件
mvn spring-boot:run
```

访问 `http://localhost:8080/api/users` 即可测试接口。

---

## 🏗️ 架构设计

### 分层架构

BaseTC 采用经典的分层架构设计，确保代码的清晰性和可维护性：

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                │
│  (Controller + GlobalExceptionHandler)       │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Business Layer                    │
│     (Service + Domain Models)               │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Infrastructure Layer               │
│  (DAO + Redis + Security + Utils)           │
└─────────────────────────────────────────────┘
```

### 模块依赖关系

BaseTC 采用模块化设计，各模块之间保持清晰的依赖关系：

```
┌─────────────┐
│   app-web   │  (Web层) - 提供API接口支持
└──────┬──────┘
       │
┌──────▼──────┐      ┌─────────────┐
│app-service  │◄─────│app-security │  (安全模块) - 认证授权
└──────┬──────┘      └─────────────┘
       │
┌──────▼──────┐      ┌─────────────┐
│  app-dao    │◄─────│  app-redis  │  (缓存模块) - Redis操作
└──────┬──────┘      └─────────────┘
       │
┌──────▼──────┐
│app-common   │  (核心模块) - 通用功能
└─────────────┘
```

### 核心设计模式

| 设计模式 | 应用场景 | 优势 |
|----------|----------|------|
| **Strategy Pattern** | 存储层抽象（Redis/Session切换） | 运行时灵活切换存储方式，提高系统可配置性 |
| **Template Method** | BaseService 提供通用 CRUD | 子类专注业务逻辑，减少重复代码 |
| **Builder Pattern** | Authentication 对象构建 | 链式调用，代码优雅，易于维护 |
| **Factory Pattern** | TokenProvider 创建 | 支持多种 Token 类型，提高扩展性 |
| **Repository Pattern** | DAO 层数据访问 | 解耦业务逻辑与数据访问，提高可测试性 |
| **AOP** | 全局异常处理、审计日志 | 代码解耦，可维护性高，减少重复代码 |

---

## 📚 配置指南

### 完整配置示例

```yaml
basetc:
  # ========== DAO 配置 ==========
  dao:
    auto-configure: true
    interceptor:
      auto-configure: true
      optimistic-locker-enabled: true      # 乐观锁拦截器
      block-attack-inner-enabled: true     # 防全表更新删除
      pagination-enabled: true             # 分页拦截器
      max-page-limit: 100                  # 单页最大记录数

  # ========== Redis 配置 ==========
  redis:
    auto-configure: true
    auto-type-accept:                      # FastJSON 反序列化白名单
      - com.basetc.base.common.response.R
      - com.basetc.base.common.domain.*
      - com.yourcompany.domain.*
    lock-key-prefix: "app:lock:"
    lock-timeout: 3000                     # 获取锁超时时间(ms)
    lock-expire-time: 30000                # 锁过期时间(ms)
    lock-sleep-time: 10                    # 锁竞争休眠时间(ms)

  # ========== Web 配置 ==========
  web:
    cors:
      auto-configure: true
      allowed-origin-patterns:
        - https://*.example.com
        - http://localhost:*
      allowed-methods: ["*"]
      allowed-headers: ["*"]
      allow-credentials: true
      max-age: 3600

  # ========== 安全配置 ==========
  security:
    auth:
      storage-type: REDIS                   # 存储类型: REDIS, SESSION, MEMORY
      jwt:
        secret: ${JWT_SECRET:your-secret-key-at-least-32-bytes}
        access-token-expiration: 3600000   # 1小时 (ms)
        refresh-token-expiration: 604800000 # 7天 (ms)
        issuer: your-application
        base64-encoding: false
```

### 环境配置

| 环境 | 配置文件 | 说明 |
|------|----------|------|
| 开发环境 | `application-dev.yml` | 使用 H2 内存数据库，日志级别 DEBUG |
| 测试环境 | `application-test.yml` | 使用 MySQL，日志级别 INFO |
| 生产环境 | `application-prod.yml` | 使用 MySQL 集群，日志级别 WARN |

---

## 🎓 最佳实践

### 1. 异常处理规范

```java
// ✅ 推荐: 使用 BasetcException
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public User getUserOrThrow(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BasetcException(404, "用户不存在");
        }
        return user;
    }
}

// ❌ 不推荐: 返回 null
public User getUser(Long userId) {
    return userMapper.selectById(userId); // 可能返回 null
}
```

### 2. 分页查询规范

```java
// ✅ 推荐: 使用 PageUtils 自动解析分页参数
@GetMapping("/users")
public R<PageResult<User>> pageUsers() {
    IPage<User> page = PageUtils.getPageRequest();
    return R.success(
        PageUtils.coverTableData(userService.page(page))
    );
}

// ❌ 不推荐: 手动解析分页参数
@GetMapping("/users")
public R<PageResult<User>> pageUsers(
    @RequestParam(defaultValue = "1") Integer pageNum,
    @RequestParam(defaultValue = "10") Integer pageSize
) {
    IPage<User> page = new Page<>(pageNum, pageSize);
    // ...
}
```

### 3. Redis 缓存使用规范

```java
// ✅ 推荐: 使用缓存加载器
public User getUserById(Long userId) {
    String cacheKey = "user:" + userId;
    return redisClient.getCacheObjectOrLoad(cacheKey, 
        () -> RedisCacheData.of(
            userMapper.selectById(userId),
            30, TimeUnit.MINUTES
        ));
}

// ❌ 不推荐: 手动判断缓存是否存在
public User getUserById(Long userId) {
    String cacheKey = "user:" + userId;
    User user = redisClient.getCacheObject(cacheKey);
    if (user == null) {
        user = userMapper.selectById(userId);
        redisClient.setCacheObject(cacheKey, user, 30, TimeUnit.MINUTES);
    }
    return user;
}
```

### 4. 枚举定义规范

```java
// ✅ 推荐: 实现 BaseEnum 接口
@DictType(value = "user_status", description = "用户状态")
public enum UserStatusEnum implements BaseEnum<Integer> {

    ACTIVE(1, "正常"),
    INACTIVE(0, "禁用");

    private final Integer value;
    private final String description;

    UserStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
```

### 5. Service 层规范

```java
// ✅ 推荐: 继承 BaseService，使用 getByIdOrThrow
@Service
public class UserService extends BaseService<UserMapper, User> {

    public void updateUserRole(Long userId, Long roleId) {
        User user = getByIdOrThrow(userId, () ->
            new BasetcException(404, "用户不存在")
        );

        user.setRoleId(roleId);
        updateById(user);
    }
}

// ❌ 不推荐: 手动判断 null
public void updateUserRole(Long userId, Long roleId) {
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BasetcException(404, "用户不存在");
    }
    // ...
}
```

---

## 🔧 常见问题

### Q1: 如何自定义响应格式？

```java
@Data
public class ApiResponse<T> extends R<T> {
    private String requestId;
    private String traceId;

    public static <T> ApiResponse<T> of(R<T> response) {
        ApiResponse<T> apiResponse = new ApiResponse<>();
        BeanUtils.copyProperties(response, apiResponse);
        apiResponse.setRequestId(UUID.randomUUID().toString());
        return apiResponse;
    }
}
```

### Q2: 如何切换 Token 存储方式？

只需修改配置文件，无需修改代码：

```yaml
basetc:
  security:
    auth:
      storage-type: REDIS   # 改为 SESSION 或 MEMORY
```

### Q3: 如何禁用某个拦截器？

```yaml
basetc:
  dao:
    interceptor:
      pagination-enabled: false  # 禁用分页拦截器
```

### Q4: Redis 反序列化失败怎么办？

将实体类添加到白名单：

```yaml
basetc:
  redis:
    auto-type-accept:
      - com.yourcompany.domain.*
```

### Q5: 如何自定义 JWT 密钥？

使用环境变量，避免硬编码：

```yaml
basetc:
  security:
    auth:
      jwt:
        secret: ${JWT_SECRET:your-default-secret}
```

启动时设置环境变量：

```bash
export JWT_SECRET=your-production-secret-key-at-least-256-bits
java -jar your-app.jar
```

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是代码提交、文档完善、bug报告还是功能建议，都对我们非常重要。

### 开发环境搭建

```bash
# 1. 克隆项目
git clone https://github.com/your-org/basetc-dependencies.git
cd basetc-dependencies

# 2. 安装依赖
mvn clean install

# 3. 运行测试
mvn test
```

### 提交代码规范

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 使用 Lombok 简化代码
- 所有 public 方法必须添加 JavaDoc
- 单元测试覆盖率 ≥ 80%
- 代码提交前请运行 `mvn clean package` 确保编译通过

### 问题反馈

如遇问题，请在 GitHub Issues 中提交：

1. 提供详细的问题描述
2. 提供复现步骤
3. 提供相关代码示例
4. 提供环境信息（JDK版本、Spring Boot版本等）

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 📞 联系方式

- **作者**: Liu,Dongdong
- **邮箱**: your-email@example.com
- **官网**: https://your-website.com
- **文档**: https://docs.your-website.com
- **问题反馈**: https://github.com/your-org/basetc-dependencies/issues

---

## 🙏 致谢

感谢以下开源项目的启发和支持：

- [Spring Boot](https://spring.io/projects/spring-boot) - 简化 Spring 应用开发
- [MyBatis Plus](https://baomidou.com/) - 增强 MyBatis 功能
- [Lombok](https://projectlombok.org/) - 简化 Java 代码
- [Hutool](https://hutool.cn/) - 工具类库

---

<div align="center">

**如果觉得项目不错，请给个 ⭐ Star 支持一下**

Made with ❤️ by BaseTC Team

</div>
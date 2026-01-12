# TC Base Web 模块

TC Base Web 是一个基于 Spring Boot 的 Web 层基础模块，提供统一的异常处理、CORS 跨域配置等功能，简化 Web 层的开发。

## 模块结构

```
src/main/java/com/basetc/base/web/
├── handler/
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── TcBaseWebAutoConfiguration.java # 自动配置类
└── WebCorsProperties.java          # CORS 配置属性类
```

## 核心功能

### 1. 全局异常处理

提供统一的异常处理机制，将所有异常转换为标准的响应格式：

- 支持分类处理不同类型的异常
- 详细记录异常日志
- 提供友好的错误提示
- 避免敏感信息泄露

### 2. CORS 跨域配置

灵活的跨域资源共享配置，支持：

- 自定义允许的源、请求方法和请求头
- 支持预检请求缓存
- 支持带凭证的请求
- 安全的配置选项

## 快速开始

### 依赖配置

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.basetc</groupId>
    <artifactId>app-base-web</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本使用

模块会自动配置，无需额外代码。

## 配置说明

### CORS 配置

在 `application.yml` 或 `application.properties` 中配置跨域参数：

```yaml
basetc:
  web:
    cors:
      auto-configure: true                   # 是否启用自动配置
      allowed-origin-patterns:              # 允许的源模式
        - http://localhost:3000
        - https://example.com
      allowed-methods:                      # 允许的 HTTP 方法
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers:                      # 允许的请求头
        - Authorization
        - Content-Type
        - Accept
      allow-credentials: true               # 是否允许携带凭证
      max-age: 3600                         # 预检请求缓存时间（秒）
```

## 使用示例

### 1. 异常处理示例

在业务层抛出异常：

```java
@Service
public class UserService extends BaseService<UserMapper, User> {

    public User getByIdOrThrow(Long id) {
        return getByIdOrThrow(id, () -> {
            throw new BasetcException(404, "用户不存在");
        });
    }
}
```

在 Controller 中使用：

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        User user = userService.getByIdOrThrow(id);
        return R.success(user);
    }
}
```

前端收到的响应格式：

```json
{
  "code": 404,
  "msg": "用户不存在",
  "data": null
}
```

### 2. 自定义异常处理

继承 `GlobalExceptionHandler` 类自定义异常处理：

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(MyCustomException.class)
    public R<Void> handleMyCustomException(MyCustomException e) {
        // 自定义处理逻辑
        return R.error(400, "自定义错误信息");
    }
}
```

### 3. 开发环境 CORS 配置

```yaml
basetc:
  web:
    cors:
      auto-configure: true
      allowed-origin-patterns: "*"  # 允许所有源（开发环境）
      allowed-methods: "*"           # 允许所有方法
      allowed-headers: "*"           # 允许所有请求头
      allow-credentials: false       # 开发环境不允许携带凭证
      max-age: 3600                  # 缓存 1 小时
```

### 4. 生产环境 CORS 配置

```yaml
basetc:
  web:
    cors:
      auto-configure: true
      allowed-origin-patterns:      # 明确指定允许的域名
        - https://www.example.com
        - https://admin.example.com
        - https://app.example.com
      allowed-methods:              # 只允许必要的方法
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers:              # 只允许必要的请求头
        - Authorization
        - Content-Type
        - Accept
      allow-credentials: true       # 允许携带凭证
      max-age: 1800                 # 缓存 30 分钟
```

## 异常处理优先级

1. 自定义业务异常 (`BasetcException`)
2. 参数校验异常 (Validation)
3. 安全异常 (Spring Security)
4. 数据库异常 (DataAccessException)
5. 系统异常 (Exception)

## 最佳实践

1. **异常处理**：
   - 优先使用自定义业务异常 (`BasetcException`)
   - 合理设置 HTTP 状态码，符合 RESTful 规范
   - 记录详细的异常日志
   - 避免向客户端暴露敏感的技术细节

2. **CORS 配置**：
   - 开发环境可以使用通配符方便调试
   - 生产环境必须明确指定允许的域名
   - 合理设置预检请求缓存时间
   - 带凭证的请求不允许使用通配符

3. **响应格式**：
   - 所有 API 都应使用统一的响应格式
   - 包含状态码、消息和数据
   - 保持简洁和一致

## 注意事项

1. **CORS 配置**：
   - CORS 配置只在浏览器环境生效，服务端调用不受影响
   - 如果使用了 Spring Security，需要确保 CORS 配置在 Security 过滤器之前生效
   - 修改 CORS 配置后可能需要清除浏览器缓存

2. **异常处理**：
   - 全局异常处理器会捕获所有 Controller 层抛出的异常
   - 建议在业务层统一处理业务异常
   - 避免在 Controller 中直接处理异常

## 扩展指南

### 自定义全局异常处理器

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(MyBusinessException.class)
    public R<Void> handleMyBusinessException(MyBusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    public R<Void> handleMultipartException(MultipartException e) {
        log.warn("文件上传异常: {}", e.getMessage(), e);
        return R.error(413, "文件上传失败，请检查文件大小和格式");
    }
}
```

### 自定义响应格式

扩展 `R<T>` 类添加额外的信息：

```java
@Data
public class ApiResponse<T> extends R<T> {
    private String requestId;
    private long timestamp;
    private String path;
    
    public static <T> ApiResponse<T> success(T data, HttpServletRequest request) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMsg("success");
        response.setData(data);
        response.setRequestId(UUID.randomUUID().toString());
        response.setTimestamp(System.currentTimeMillis());
        response.setPath(request.getRequestURI());
        return response;
    }
}
```

## 相关类

- `GlobalExceptionHandler`：全局异常处理器
- `TcBaseWebAutoConfiguration`：自动配置类
- `WebCorsProperties`：CORS 配置属性类
- `BasetcException`：自定义异常类
- `R<T>`：统一响应类

# TC Base Common 模块

TC Base Common 是一个通用功能模块，提供统一的响应结构、异常处理、字典管理、环境配置等基础功能。

## 模块结构

```
src/main/java/com/basetc/base/common/
├── annotation/          # 注解定义
│   └── DictType.java    # 字典类型注解
├── domain/             # 数据模型
│   ├── BaseDict.java   # 字典实体类
│   └── BaseOptions.java # 选项类
├── enums/              # 枚举定义
│   └── BaseEnum.java   # 基础枚举接口
├── exception/          # 异常定义
│   └── BasetcException.java # 自定义异常类
├── response/           # 响应结构
│   ├── R.java          # 统一响应类
│   └── TcResponse.java # 响应接口
├── utils/              # 工具类
│   ├── DictBaseEnumScanner.java # 字典枚举扫描器
│   └── SpringUtils.java         # Spring工具类
├── BasetcApplication.java # 启动类
└── BasetcProfile.java   # 环境工具类
```

## 核心功能

### 1. 统一响应结构

#### R<T> - 统一响应类

提供统一的响应结构，包含状态码、消息和数据。

**主要方法：**

- `success(T data)` - 构建成功响应
- `success()` - 构建无数据的成功响应
- `success(String message, T data)` - 构建带自定义消息的成功响应
- `error(String message)` - 构建错误响应（默认错误码500）
- `error(int code, String message)` - 构建带指定错误码的错误响应

**使用示例：**

```java
// 成功响应
R<User> successResponse = R.success(user);

// 成功响应（带自定义消息）
R<User> customSuccess = R.success("获取用户成功", user);

// 错误响应
R<Void> errorResponse = R.error("用户不存在");

// 错误响应（带自定义错误码）
R<Void> customError = R.error(404, "用户不存在");
```

#### TcResponse<C, T> - 响应接口

定义响应结构规范，包含状态码、消息和数据的获取方法。

### 2. 异常处理

#### BasetcException - 自定义异常类

继承自 `RuntimeException`，用于封装业务异常信息。

**构造函数：**

- `BasetcException(String message)` - 基本异常
- `BasetcException(String message, Throwable cause)` - 带原因的异常
- `BasetcException(int code, String message)` - 指定错误码的异常
- `BasetcException(int code, String message, Throwable cause)` - 指定错误码和原因的异常
- `BasetcException(TcResponse<Integer, Void> response)` - 基于响应对象构建异常
- `BasetcException(TcResponse<Integer, Void> response, Throwable cause)` - 基于响应对象和原因构建异常

**使用示例：**

```java
// 基本异常
throw new BasetcException("业务处理失败");

// 带原因的异常
throw new BasetcException("业务处理失败", cause);

// 指定错误码的异常
throw new BasetcException(1001, "参数验证失败");
```

### 3. 枚举和字典

#### BaseEnum<T> - 基础枚举接口

提供通用的枚举处理方法，继承自 MyBatis-Plus 的 `IEnum` 接口。

**主要方法：**

- `getByValue(T value, Class<E> clazz)` - 根据枚举值获取对应的枚举实例
- `convertOptions(Class<? extends BaseEnum<?>> baseEnum, Class<? extends BaseOptions> clazz)` - 将枚举转换为选项列表
- `getDescription()` - 获取枚举描述
- `getExtMap()` - 获取扩展信息
- `getValue()` - 获取枚举值

**使用示例：**

```java
public enum UserStatus implements BaseEnum<Integer> {
    ACTIVE(1, "活跃"),
    INACTIVE(0, "非活跃");

    private final Integer value;
    private final String description;

    UserStatus(Integer value, String description) {
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

#### DictType - 字典类型注解

用于标记字典类，提供字典类型的标识和描述信息。

**属性：**

- `value()` - 字典类型的标识
- `description()` - 字典类型的描述

**使用示例：**

```java
@DictType(value = "user_status", description = "用户状态")
public enum UserStatusEnum implements BaseEnum<Integer> {
    // 枚举实现
}
```

#### DictBaseEnumScanner - 字典枚举扫描器

扫描指定包路径下带有 `@DictType` 注解的枚举类，并将其转换为 `BaseDict` 对象列表。

**主要方法：**

- `scan(String basePackage)` - 扫描指定包路径下带有 @DictType 注解的枚举类

**使用示例：**

```java
List<BaseDict> dictList = DictBaseEnumScanner.scan("com.example.enums");
```

#### BaseDict - 字典实体类

封装字典数据结构，包含字典的唯一标识、描述信息和数据项列表。

**属性：**

- `key` - 字典的唯一标识键
- `description` - 字典的描述信息
- `data` - 字典的数据项列表

#### BaseOptions - 选项类

表示键值对形式的选项数据。

**属性：**

- `label` - 选项显示名称
- `value` - 选项值
- `extMap` - 扩展字段

### 4. 环境配置

#### BasetcProfile - 环境工具类

提供环境判断功能，支持多种环境（开发、测试、生产、集成、UAT、本地）。

**主要方法：**

- `isDev()` - 判断当前是否为开发环境
- `isProd()` - 判断当前是否为生产环境
- `isTest()` - 判断当前是否为测试环境
- `isItg()` - 判断当前是否为集成环境
- `isUat()` - 判断当前是否为用户验收测试环境
- `isLocal()` - 判断当前是否为本地环境

**使用示例：**

```java
if (BasetcProfile.isDev()) {
    // 开发环境逻辑
}

if (BasetcProfile.isProd()) {
    // 生产环境逻辑
}
```

### 5. 工具类

#### SpringUtils - Spring工具类

提供对Spring应用上下文的便捷访问和操作功能。

**主要方法：**

- `getBean(String name)` - 根据Bean名称获取Bean实例
- `getBean(Class<T> clazz)` - 根据Bean类型获取Bean实例
- `getBean(String name, Class<T> clazz)` - 根据Bean名称和类型获取Bean实例
- `containsBean(String name)` - 判断是否包含指定名称的Bean
- `isSingleton(String name)` - 判断指定名称的Bean是否为单例
- `registerBean(String beanName, Object beanInstance)` - 动态注册Bean到Spring容器中
- `removeBean(String beanName)` - 从Spring容器中移除指定名称的Bean

**使用示例：**

```java
// 获取Bean
UserService userService = SpringUtils.getBean(UserService.class);

// 获取Bean（按名称）
UserService userService = SpringUtils.getBean("userService");

// 动态注册Bean
SpringUtils.registerBean("myBean", new MyBean());

// 移除Bean
SpringUtils.removeBean("myBean");
```

#### BasetcApplication - 启动类

基础启动类，继承自BasetcProfile，提供应用启动功能。

## 使用指南

### 1. 创建自定义枚举

```java
@DictType(value = "order_status", description = "订单状态")
public enum OrderStatusEnum implements BaseEnum<Integer> {
    PENDING(0, "待处理"),
    CONFIRMED(1, "已确认"),
    SHIPPED(2, "已发货"),
    DELIVERED(3, "已送达"),
    CANCELLED(4, "已取消");

    private final Integer value;
    private final String description;

    OrderStatusEnum(Integer value, String description) {
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

    @Override
    public Map<String, Object> getExtMap() {
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("color", getColorByStatus(this));
        return extMap;
    }

    private String getColorByStatus(OrderStatusEnum status) {
        switch (status) {
            case PENDING: return "orange";
            case CONFIRMED: return "blue";
            case SHIPPED: return "yellow";
            case DELIVERED: return "green";
            case CANCELLED: return "red";
            default: return "gray";
        }
    }
}
```

### 2. 字典管理服务

```java
@Service
public class DictService {
    
    @Value("${app.dict.scan-package:com.example.enums}")
    private String scanPackage;

    public List<BaseDict> getAllDicts() {
        return DictBaseEnumScanner.scan(scanPackage);
    }

    public BaseDict getDictByType(String dictType) {
        List<BaseDict> allDicts = getAllDicts();
        return allDicts.stream()
                .filter(dict -> dictType.equals(dict.getKey()))
                .findFirst()
                .orElse(null);
    }

    public List<BaseOptions> getDictOptions(String dictType) {
        BaseDict dict = getDictByType(dictType);
        return dict != null ? dict.getData() : Collections.emptyList();
    }
}
```

### 3. 统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BasetcException.class)
    public R<Void> handleBasetcException(BasetcException e) {
        return e.getErr();
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.error("系统内部错误");
    }
}
```

## 最佳实践

1. **统一响应**: 在所有API端点使用 `R<T>` 类进行响应
2. **异常处理**: 使用 `BasetcException` 进行业务异常处理
3. **枚举定义**: 实现 `BaseEnum` 接口来定义枚举类型
4. **字典管理**: 使用 `@DictType` 注解标记字典枚举类
5. **环境配置**: 合理使用环境配置来管理不同环境的参数
6. **工具使用**: 合理使用 `SpringUtils` 等工具类

## 扩展指南

### 自定义响应结构

可以扩展 `R<T>` 类来满足特定需求：

```java
@Data
public class ApiResponse<T> extends R<T> {
    private String requestId;
    private long timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMsg("success");
        response.setData(data);
        response.setRequestId(UUID.randomUUID().toString());
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
```

### 自定义环境类型

如果需要添加新的环境类型，可以扩展 `BasetcProfile.ProfileEnv` 枚举：

```java
public class CustomProfile extends BasetcProfile {
    public static boolean isStaging() {
        return ProfileEnv.STAGING.name().equalsIgnoreCase(CURRENT);
    }
}
```
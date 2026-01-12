# TC Base Service 模块

TC Base Service 是一个基于 MyBatis-Plus 的基础服务类，提供通用的 CRUD 操作和分页查询功能，简化业务服务层的开发。

## 模块结构

```
src/main/java/com/basetc/base/service/
└── BaseService.java        # 基础服务类，提供通用 CRUD 操作
```

## 核心功能

### 1. 通用 CRUD 操作

继承自 MyBatis-Plus 的 `ServiceImpl`，提供完整的 CRUD 操作：

- `save(T entity)` - 插入数据
- `updateById(T entity)` - 根据 ID 更新数据
- `removeById(Serializable id)` - 根据 ID 删除数据
- `getById(Serializable id)` - 根据 ID 查询数据
- `list()` - 查询所有数据
- `count()` - 统计数据总数

### 2. 分页查询功能

#### pageList(T entity)

根据实体中的非空属性作为条件进行分页查询。

#### pageList(Wrapper<T> queryWrapper)

根据自定义的查询条件进行分页查询。

### 3. 异常处理机制

提供带异常处理的查询、更新、删除方法：

- `getByIdOrThrow(Serializable id, ExThrow ex)` - 根据 ID 查询，如不存在则执行异常处理
- `updateByIdOrThrow(T entity, ExThrow ex)` - 根据 ID 更新，如不存在则执行异常处理
- `removeByIdOrThrow(Serializable id, ExThrow ex)` - 根据 ID 删除，如不存在则执行异常处理

### 4. 条件查询构建

通过重写 `buildQueryWrapper` 方法自定义查询逻辑，支持灵活的条件组合。默认实现使用实体的非空属性作为查询条件。

## 快速开始

### 依赖配置

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.basetc</groupId>
    <artifactId>app-base-service</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本使用

创建自定义 Service 类并继承 `BaseService`：

```java
@Service
public class UserService extends BaseService<UserMapper, User> {
    // 自定义业务方法
}
```

## 使用示例

### 1. 基础 CRUD 操作

```java
@Autowired
private UserService userService;

// 插入数据
User user = new User();
user.setName("张三");
userService.save(user);

// 更新数据
user.setAge(25);
userService.updateById(user);

// 查询数据
User foundUser = userService.getById(1L);

// 删除数据
userService.removeById(1L);

// 查询所有数据
List<User> allUsers = userService.list();
```

### 2. 分页查询

```java
// 根据实体条件分页查询
User queryUser = new User();
queryUser.setStatus(1); // 只查询状态为1的用户
PageResult<User> activeUsers = userService.pageList(queryUser);

// 根据自定义条件分页查询
Wrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
    .ge(User::getAge, 18)
    .orderByDesc(User::getId);
PageResult<User> adultUsers = userService.pageList(queryWrapper);
```

### 3. 异常处理

```java
// 使用 Lambda 表达式抛出异常
User user = userService.getByIdOrThrow(1L, () -> {
    throw new BasetcException(404, "用户不存在");
});

// 使用方法引用抛出异常
userService.updateByIdOrThrow(user, this::throwUserNotFoundException);

// 使用异常处理删除数据
userService.removeByIdOrThrow(1L, () -> {
    throw new BasetcException(404, "用户不存在，无法删除");
});

// 自定义异常处理方法
private void throwUserNotFoundException() {
    throw new BasetcException(404, "用户不存在");
}
```

### 4. 自定义查询条件

重写 `buildQueryWrapper` 方法自定义查询逻辑：

```java
@Service
public class UserService extends BaseService<UserMapper, User> {

    @Override
    protected Wrapper<User> buildQueryWrapper(User entity) {
        // 只查询状态为1的用户，并且按ID降序排列
        return Wrappers.lambdaQuery(entity)
            .eq(User::getStatus, 1) // 固定条件：状态为1
            .like(StringUtils.isNotBlank(entity.getName()), User::getName, entity.getName()) // 动态条件：名称模糊查询
            .ge(entity.getAge() != null, User::getAge, entity.getAge()) // 动态条件：年龄大于等于
            .orderByDesc(User::getId); // 排序
    }
}
```

## 最佳实践

1. **合理使用继承**：只在需要通用 CRUD 操作的 Service 类中继承 `BaseService`
2. **自定义查询条件**：重写 `buildQueryWrapper` 方法统一管理查询逻辑
3. **异常处理**：使用 `OrThrow` 系列方法简化异常处理代码
4. **分页查询**：优先使用 `pageList` 方法进行分页查询，避免手动处理分页逻辑
5. **事务管理**：在需要的方法上添加 `@Transactional` 注解管理事务

## 扩展指南

### 1. 添加自定义通用方法

```java
@Service
public class CustomBaseService<M extends BaseMapper<T>, T> extends BaseService<M, T> {

    /**
     * 根据多个ID查询数据
     */
    public List<T> listByIdsWithLock(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        // 自定义逻辑：加锁查询
        return baseMapper.selectBatchIds(idList);
    }

    /**
     * 批量更新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusBatch(Collection<? extends Serializable> idList, Integer status) {
        if (CollectionUtils.isEmpty(idList) || status == null) {
            return false;
        }
        return lambdaUpdate()
            .in(T::getId, idList)
            .set(T::getStatus, status)
            .update();
    }
}
```

### 2. 结合业务逻辑扩展

```java
@Service
public class UserService extends BaseService<UserMapper, User> {

    // 业务方法：批量导入用户
    @Transactional(rollbackFor = Exception.class)
    public boolean importUsers(List<User> users) {
        // 验证用户数据
        validateUsers(users);
        
        // 批量插入
        return this.saveBatch(users);
    }

    // 业务方法：用户注册
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(User user) {
        // 检查用户名是否已存在
        if (this.lambdaQuery().eq(User::getUsername, user.getUsername()).count() > 0) {
            throw new BasetcException("用户名已存在");
        }
        
        // 设置默认值
        user.setStatus(1);
        user.setCreateTime(new Date());
        
        // 保存用户
        return this.save(user);
    }

    // 验证用户数据
    private void validateUsers(List<User> users) {
        for (User user : users) {
            if (StringUtils.isBlank(user.getUsername())) {
                throw new BasetcException("用户名不能为空");
            }
            if (StringUtils.isBlank(user.getPassword())) {
                throw new BasetcException("密码不能为空");
            }
        }
    }
}
```

## 注意事项

1. **线程安全**：`BaseService` 是线程安全的，可以在多线程环境下使用
2. **事务管理**：默认方法没有事务支持，需要手动添加 `@Transactional` 注解
3. **分页参数**：默认从 HTTP 请求中获取分页参数，参数名为 `current`（页码）和 `size`（页大小）
4. **条件构建**：`buildQueryWrapper` 方法默认使用实体的非空属性作为查询条件
5. **异常处理**：`OrThrow` 系列方法会验证参数的非空性，空参数会抛出 `IllegalArgumentException`

## 相关类

- `BaseMapper`：MyBatis-Plus 的基础 Mapper 接口
- `ServiceImpl`：MyBatis-Plus 的基础服务实现类
- `PageResult`：分页结果封装类
- `PageUtils`：分页工具类
- `BasetcException`：自定义异常类

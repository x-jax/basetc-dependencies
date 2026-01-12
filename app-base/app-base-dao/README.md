# TC Base DAO 模块

TC Base DAO 是一个基于 MyBatis-Plus 的数据访问层模块，提供分页、自动填充、拦截器等数据库访问增强功能。

## 模块结构

```
src/main/java/com/basetc/base/dao/
├── domain/            # 分页结果模型
│   └── PageResult.java # 分页结果封装类
├── fun/               # 函数式接口
│   └── MetaAutoFillFunction.java # 元数据填充函数式接口
├── handler/           # 处理器
│   └── DaoMetaObjectHandler.java # 元对象处理器
├── support/           # 支持类
│   └── MybatisMetaFillSupport.java # 元数据填充支持接口
├── utils/             # 工具类
│   └── PageUtils.java  # 分页工具类
├── BaseDaoProperties.java        # 配置属性
└── BaseDaoAutoConfiguration.java  # 自动配置类
```

## 核心功能

### 1. 配置属性

#### BaseDaoProperties - 配置属性类

配置 MyBatis Plus 拦截器的相关属性。

**主要属性：**

- `autoConfigure` - 是否启用自动配置
- `interceptor` - 拦截器配置属性

**拦截器配置属性：**

- `autoConfigure` - 是否启用拦截器自动配置
- `optimisticLockerEnabled` - 是否启用乐观锁拦截器
- `blockAttackInnerEnabled` - 是否启用阻止全表更新删除拦截器
- `paginationEnabled` - 是否启用分页拦截器
- `maxPageLimit` - 分页最大限制

**配置示例：**

```properties
# 启用自动配置
tc.base.dao.auto-configure=true

# 拦截器配置
tc.base.dao.interceptor.auto-configure=true
tc.base.dao.interceptor.optimistic-locker-enabled=true
tc.base.dao.interceptor.block-attack-inner-enabled=true
tc.base.dao.interceptor.pagination-enabled=true
tc.base.dao.interceptor.max-page-limit=1000
```

### 2. 自动配置

#### BaseDaoAutoConfiguration - 自动配置类

自动配置 MyBatis-Plus 相关组件，包括拦截器和元对象处理器。

### 3. 分页功能

#### PageResult<T> - 分页结果封装类

封装分页查询结果，包含数据列表、总记录数、总页数、当前页码、页面大小等信息。

**属性：**

- `rows` - 分页数据列表
- `total` - 总记录数
- `totalPage` - 总页数
- `current` - 当前页码
- `size` - 每页显示数量

#### PageUtils - 分页工具类

提供分页相关的便捷操作。

**主要方法：**

- `setRequestParams(String pageNoName, String pageSizeName)` - 设置请求参数名
- `getPageRequest()` - 获取分页请求对象，使用默认的页码和页面大小参数名
- `getPageRequest(String pageNumKey, String pageSizeKey)` - 根据指定的参数名从HTTP请求中获取分页信息
- `coverTableData(IPage<T> page)` - 将MyBatis Plus的分页对象转换为分页结果对象

**使用示例：**

```java
// 从HTTP请求获取分页参数
IPage<User> page = PageUtils.getPageRequest();

// 转换分页结果
PageResult<User> result = PageUtils.coverTableData(page);
```

### 4. 自动填充功能

#### MybatisMetaFillSupport - 元数据填充支持接口

定义插入和更新操作时的元数据自动填充功能。

**主要方法：**

- `insertMetaAutoFillFunction(MetaObject metaObject)` - 插入时的元数据自动填充函数
- `updateMetaAutoFillFunction(MetaObject metaObject)` - 更新时的元数据自动填充函数

#### MetaAutoFillFunction - 元数据填充函数式接口

用于填充表字段默认值的函数式接口。

**主要方法：**

- `getFillValue()` - 获取填充值

#### DaoMetaObjectHandler - 元对象处理器

自动填充创建时间和更新时间等字段。

支持自动填充的字段包括：

- 创建时间相关字段：createTime, createdTime, createDate, createdDate
- 更新时间相关字段：updateTime, updatedTime, updateDate, updatedDate
- 其他通用字段：version, dataVersion, enabled, isEnabled, deleted, isDeleted

## 使用指南

### 1. 基础实体类

创建包含通用字段的基础实体类：

```java
@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version = 1;

    @TableField(fill = FieldFill.INSERT)
    private String tenantId;
}
```

### 2. 实现元数据填充支持

创建自定义的元数据填充支持类：

```java
@Component
@Order(1) // 确保优先级
public class CustomMetaFillSupport implements MybatisMetaFillSupport {
    
    @Autowired
    private HttpServletRequest request;

    @Override
    public Map<String, MetaAutoFillFunction> insertMetaAutoFillFunction(MetaObject metaObject) {
        Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
        
        // 设置创建时间
        fillMap.put("createTime", () -> new Date());
        
        // 设置创建人
        fillMap.put("createBy", () -> getCurrentUserId());
        
        // 设置租户ID（多租户场景）
        fillMap.put("tenantId", () -> getCurrentTenantId());
        
        // 设置版本号
        fillMap.put("version", () -> 1);
        
        // 设置启用状态
        fillMap.put("enabled", () -> true);
        
        return fillMap;
    }

    @Override
    public Map<String, MetaAutoFillFunction> updateMetaAutoFillFunction(MetaObject metaObject) {
        Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
        
        // 设置更新时间
        fillMap.put("updateTime", () -> new Date());
        
        // 设置更新人
        fillMap.put("updateBy", () -> getCurrentUserId());
        
        return fillMap;
    }

    private String getCurrentUserId() {
        // 从SecurityContext或Request中获取当前用户ID
        String userId = request.getHeader("X-User-Id");
        return userId != null ? userId : "system";
    }

    private String getCurrentTenantId() {
        // 从请求头或上下文中获取租户ID
        String tenantId = request.getHeader("X-Tenant-Id");
        return tenantId != null ? tenantId : "default";
    }
}
```

### 3. 分页查询示例

```java
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    
    public R<PageResult<User>> pageUsers() {
        IPage<User> page = PageUtils.getPageRequest();
        IPage<User> result = userMapper.selectPage(page, null);
        PageResult<User> pageResult = PageUtils.coverTableData(result);
        return R.success(pageResult);
    }
}
```

### 4. Controller 使用示例

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/page")
    public R<PageResult<User>> getPageUsers() {
        return userService.pageUsers();
    }
}
```

## 拦截器功能

框架集成了多种 MyBatis-Plus 拦截器：

### 1. 乐观锁拦截器

防止并发更新冲突，通过版本号控制实现乐观锁机制。

### 2. 分页拦截器

自动处理分页查询，支持自定义最大分页限制。

### 3. 阻止全表更新删除拦截器

防止误操作导致全表更新或删除，增强数据安全性。

## 扩展指南

### 自定义分页结果类

扩展分页结果类以包含额外信息：

```java
@Data
public class ExtendedPageResult<T> extends PageResult<T> {
    private Map<String, Object> summary; // 汇总信息
    private String filterInfo; // 过滤条件信息
    private long executionTime; // 执行时间

    public ExtendedPageResult(IPage<T> page) {
        setRows(page.getRecords());
        setTotal(page.getTotal());
        setTotalPage(page.getPages());
        setCurrent(page.getCurrent());
        setSize(page.getSize());
    }
}
```

### 自定义分页工具

扩展分页工具类以支持更多功能：

```java
@UtilityClass
public class ExtendedPageUtils extends PageUtils {
    
    /**
     * 从请求参数创建分页对象，并应用默认限制
     */
    public static <T> IPage<T> getPageRequestWithLimit(int maxPageSize) {
        IPage<T> page = getPageRequest();
        if (page.getSize() > maxPageSize) {
            page.setSize(maxPageSize);
        }
        return page;
    }

    /**
     * 创建分页对象，支持排序
     */
    public static <T> IPage<T> getPageRequestWithOrder(String orderField, boolean isAsc) {
        IPage<T> page = getPageRequest();
        OrderItem orderItem = isAsc ? OrderItem.asc(orderField) : OrderItem.desc(orderField);
        page.addOrder(orderItem);
        return page;
    }
}
```

## 配置说明

### 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| tc.base.dao.auto-configure | false | 是否启用DAO自动配置 |
| tc.base.dao.interceptor.auto-configure | true | 是否启用拦截器自动配置 |
| tc.base.dao.interceptor.optimistic-locker-enabled | true | 是否启用乐观锁拦截器 |
| tc.base.dao.interceptor.block-attack-inner-enabled | true | 是否启用阻止全表更新删除拦截器 |
| tc.base.dao.interceptor.pagination-enabled | true | 是否启用分页拦截器 |
| tc.base.dao.interceptor.max-page-limit | 100 | 分页最大限制 |

## 最佳实践

1. **实体类设计**: 使用 `BaseEntity` 作为基础实体类，统一管理通用字段
2. **自动填充**: 实现 `MybatisMetaFillSupport` 接口来管理自动填充逻辑
3. **分页查询**: 使用 `PageUtils` 进行分页查询，避免手动解析分页参数
4. **拦截器配置**: 根据业务需求合理配置拦截器，平衡性能和安全性
5. **分页结果**: 使用 `PageResult` 统一封装分页查询结果
6. **安全性**: 启用阻止全表更新删除拦截器，防止误操作

## 注意事项

1. **线程安全**: `TcDaoMetaObjectHandler` 是单例的，自定义实现时需注意线程安全
2. **性能考虑**: 分页最大限制应根据系统性能合理设置
3. **兼容性**: 自定义元数据填充实现时，需确保字段类型兼容
4. **配置验证**: 启用拦截器前应验证其对系统性能的影响
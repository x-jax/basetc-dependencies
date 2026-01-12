package com.basetc.base.dao.support;

import com.basetc.base.dao.fun.MetaAutoFillFunction;
import com.basetc.base.dao.handler.DaoMetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * MyBatis 元数据填充支持接口,定义了插入和更新操作时的元数据自动填充功能.
 *
 * <p>实现类需要提供插入和更新时需要自动填充的字段及其值的生成函数.
 * 此接口与 {@link DaoMetaObjectHandler} 配合使用,
 * 实现 MyBatis Plus 的自动填充功能.</p>
 *
 * <h3>实现示例</h3>
 * <pre>{@code
 * @Component
 * public class CustomMetaFillSupport implements MybatisMetaFillSupport {
 *
 *     @Override
 *     public Map<String, MetaAutoFillFunction> insertMetaAutoFillFunction(MetaObject metaObject) {
 *         Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
 *
 *         // 填充创建时间
 *         fillMap.put("createTime", () -> new Date());
 *         fillMap.put("createdTime", () -> System.currentTimeMillis());
 *
 *         // 填充创建人 (从 SecurityContext 获取)
 *         fillMap.put("createBy", () -> {
 *             Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *             return auth != null ? auth.getName() : "system";
 *         });
 *
 *         // 填充创建人ID
 *         fillMap.put("createdBy", () -> {
 *             LoginUser user = SecurityUtils.getLoginUser();
 *             return user != null ? user.getId() : -1L;
 *         });
 *
 *         // 填充租户ID
 *         fillMap.put("tenantId", () -> TenantContextHolder.getTenantId());
 *
 *         return fillMap;
 *     }
 *
 *     @Override
 *     public Map<String, MetaAutoFillFunction> updateMetaAutoFillFunction(MetaObject metaObject) {
 *         Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
 *
 *         // 填充更新时间
 *         fillMap.put("updateTime", () -> new Date());
 *         fillMap.put("updatedTime", () -> System.currentTimeMillis());
 *
 *         // 填充更新人
 *         fillMap.put("updateBy", () -> {
 *             Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *             return auth != null ? auth.getName() : "system";
 *         });
 *
 *         // 填充更新人ID
 *         fillMap.put("updatedBy", () -> {
 *             LoginUser user = SecurityUtils.getLoginUser();
 *             return user != null ? user.getId() : -1L;
 *         });
 *
 *         // 填充版本号（乐观锁）
 *         fillMap.put("version", () -> 0);
 *
 *         return fillMap;
 *     }
 * }
 * }</pre>
 *
 * <h3>在实体类中使用</h3>
 * <pre>{@code
 * @Data
 * @TableName("sys_user")
 * public class User {
 *
 *     @TableId(type = IdType.ASSIGN_ID)
 *     private Long id;
 *
 *     private String username;
 *
 *     // 自动填充创建时间
 *     @TableField(fill = FieldFill.INSERT)
 *     private LocalDateTime createTime;
 *
 *     // 自动填充更新时间
 *     @TableField(fill = FieldFill.INSERT_UPDATE)
 *     private LocalDateTime updateTime;
 *
 *     // 自动填充创建人
 *     @TableField(fill = FieldFill.INSERT)
 *     private String createBy;
 *
 *     // 自动填充更新人
 *     @TableField(fill = FieldFill.INSERT_UPDATE)
 *     private String updateBy;
 * }
 * }</pre>
 *
 * <h3>支持的填充类型</h3>
 * <ul>
 *   <li>时间字段: Date, LocalDateTime, Long (时间戳)</li>
 *   <li>数字字段: Integer, Long (用户ID、版本号等)</li>
 *   <li>字符串字段: String (用户名、租户ID等)</li>
 *   <li>布尔字段: Boolean (启用状态、删除标记等)</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>返回的 Map 不能为 null</li>
 *   <li>Map 的 key 和 value 都不能为 null</li>
 *   <li>只有在字段值为 null 时才会执行填充</li>
 *   <li>如果需要覆盖已有值,请配置 TcDaoMetaObjectHandler 的 override 参数</li>
 * </ul>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>可扩展性: 通过接口定义,支持自定义填充逻辑</li>
 *   <li>灵活性: 支持任意字段名的自动填充</li>
 *   <li>安全性: 与安全框架集成,自动获取当前用户信息</li>
 *   <li>高性能: 仅在必要时执行填充操作</li>
 * </ul>
 *
 * @see DaoMetaObjectHandler
 * @see MetaAutoFillFunction
 * @see org.apache.ibatis.reflection.MetaObject
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public interface MybatisMetaFillSupport {

    /**
     * 获取插入操作时的元数据填充函数映射.
     *
     * <p>返回的 Map 中,key 为需要填充的字段名,value 为生成填充值的函数.
     * 只有当字段值为 null 时才会执行填充(除非 override=true).
     *
     * <h3>常见填充字段:</h3>
     * <ul>
     *   <li>createTime, createdTime, createDate, createdDate - 创建时间
     *   <li>createBy, createdBy, creator - 创建人
     *   <li>tenantId, tenantCode - 租户ID
     *   <li>enabled, isEnabled - 启用状态
     * </ul>
     *
     * @param metaObject MyBatis 元对象,包含实体类的元信息
     * @return 字段名到填充函数的映射,不能为 null
     */
    @NonNull Map<@NonNull String, @NonNull MetaAutoFillFunction> insertMetaAutoFillFunction(MetaObject metaObject);

    /**
     * 获取更新操作时的元数据填充函数映射.
     *
     * <p>返回的 Map 中,key 为需要填充的字段名,value 为生成填充值的函数.
     * 只有当字段值为 null 时才会执行填充(除非 override=true).
     *
     * <h3>常见填充字段:</h3>
     * <ul>
     *   <li>updateTime, updatedTime, updateDate, updatedDate - 更新时间
     *   <li>updateBy, updatedBy, updater - 更新人
     *   <li>version, dataVersion, optLock - 版本号
     * </ul>
     *
     * @param metaObject MyBatis 元对象,包含实体类的元信息
     * @return 字段名到填充函数的映射,不能为 null
     */
    @NonNull Map<@NonNull String, @NonNull MetaAutoFillFunction> updateMetaAutoFillFunction(MetaObject metaObject);

}

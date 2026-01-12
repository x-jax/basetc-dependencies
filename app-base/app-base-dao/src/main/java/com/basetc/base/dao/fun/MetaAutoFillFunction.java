package com.basetc.base.dao.fun;

import org.jspecify.annotations.NonNull;

/**
 * 填充表字段默认值的函数式接口.
 *
 * <p>此接口用于定义 MyBatis Plus 插入和更新操作时自动填充字段值的逻辑.
 * 配合 {@link com.basetc.base.dao.support.MybatisMetaFillSupport} 使用,
 * 可以为实体类的字段提供自动填充功能.
 *
 * <p>通常用于填充以下类型的字段:
 * <ul>
 *   <li>创建时间、更新时间
 *   <li>创建人、更新人
 *   <li>版本号
 *   <li>租户ID
 *   <li>删除标记
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Component
 * public class CustomMetaFillSupport implements MybatisMetaFillSupport {
 *
 *     @Override
 *     public Map<String, MetaAutoFillFunction> insertMetaAutoFillFunction(MetaObject metaObject) {
 *         Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
 *
 *         // 填充创建时间 - 使用 Lambda 表达式
 *         fillMap.put("createTime", () -> new Date());
 *
 *         // 填充创建人ID - 使用方法引用
 *         fillMap.put("createBy", this::getCurrentUserId);
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
 *
 *         // 填充更新人
 *         fillMap.put("updateBy", this::getCurrentUserId);
 *
 *         return fillMap;
 *     }
 *
 *     private String getCurrentUserId() {
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         return auth != null ? auth.getName() : "system";
 *     }
 * }
 * }</pre>
 *
 * <h3>在实体类中使用:</h3>
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
 *     // 自动填充创建人
 *     @TableField(fill = FieldFill.INSERT)
 *     private String createBy;
 *
 *     // 自动填充更新时间
 *     @TableField(fill = FieldFill.INSERT_UPDATE)
 *     private LocalDateTime updateTime;
 *
 *     // 自动填充更新人
 *     @TableField(fill = FieldFill.INSERT_UPDATE)
 *     private String updateBy;
 * }
 *
 * // 插入操作,createTime 和 createBy 自动填充
 * userMapper.insert(user);
 *
 * // 更新操作,updateTime 和 updateBy 自动填充
 * userMapper.updateById(user);
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>此方法不能返回 null,否则会抛出 NullPointerException
 *   <li>此方法在每次填充时都会调用,不适合执行耗时操作
 *   <li>如果需要根据实体类不同字段返回不同值,可以从 MetaObject 中获取信息
 * </ul>
 *
 * @see com.basetc.base.dao.support.MybatisMetaFillSupport
 * @see org.apache.ibatis.reflection.MetaObject
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@FunctionalInterface
public interface MetaAutoFillFunction {

    /**
     * 获取填充值.
     *
     * <p>此方法在执行 INSERT 或 UPDATE 操作时被调用,
     * 用于返回需要填充到字段的值.
     *
     * <p><b>重要:</b> 此方法不能返回 null,否则会抛出 NullPointerException.
     * 如果某个字段不需要填充,请不要将其添加到填充 Map 中.
     *
     * <h3>返回值类型要求:</h3>
     * <ul>
     *   <li>时间字段: 返回 {@link java.util.Date} 或 {@link java.time.LocalDateTime}
     *   <li>数字字段: 返回 {@link Integer}、{@link Long} 等
     *   <li>字符串字段: 返回 {@link String}
     *   <li>布尔字段: 返回 {@link Boolean}
     * </ul>
     *
     * <h3>实现建议:</h3>
     * <ul>
     *   <li>使用 Lambda 表达式: {@code () -> new Date()}
     *   <li>使用方法引用: {@code this::getCurrentUserId}
     *   <li>避免在方法中执行耗时操作(如数据库查询)
     *   <li>确保返回值类型与字段类型匹配
     * </ul>
     *
     * @return 填充值,不能为 null
     * @throws NullPointerException 如果返回 null
     */
    @NonNull
    Object getFillValue();

}

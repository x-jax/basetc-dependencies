package com.basetc.base.dao.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.basetc.base.dao.fun.MetaAutoFillFunction;
import com.basetc.base.dao.support.MybatisMetaFillSupport;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Map;
import java.util.Objects;

/**
 * DAO 层元对象处理器,用于自动填充创建时间和更新时间等字段.
 *
 * <p>此类实现了 MyBatis Plus 的 {@link MetaObjectHandler} 接口,
 * 在执行 insert 和 update 操作时自动填充实体类中的通用字段.
 * 提供了灵活的自动填充机制,支持多种字段类型的自动填充,
 * 包括时间戳、用户信息、版本号等常用字段.</p>
 *
 * <p>支持自动填充的字段包括:
 * <ul>
 *   <li>创建时间: createTime, createdTime, createDate, createdDate</li>
 *   <li>更新时间: updateTime, updatedTime, updateDate, updatedDate</li>
 *   <li>创建人: createBy, createdBy</li>
 *   <li>更新人: updateBy, updatedBy</li>
 *   <li>版本号: version, dataVersion, optLock</li>
 *   <li>启用状态: enabled, isEnabled</li>
 *   <li>删除标记: deleted, isDeleted</li>
 *   <li>租户ID: tenantId, tenantCode</li>
 * </ul></p>
 *
 * <h3>使用示例</h3>
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
 *
 * // 插入操作,createTime 和 createBy 自动填充
 * userMapper.insert(user);
 * // SQL: INSERT INTO sys_user (username, create_time, update_time, create_by, update_by)
 * //      VALUES (?, ?, ?, ?, ?)
 *
 * // 更新操作,updateTime 和 updateBy 自动填充
 * userMapper.updateById(user);
 * // SQL: UPDATE sys_user SET username = ?, update_time = ?, update_by = ? WHERE id = ?
 * }</pre>
 *
 * <h3>自定义填充逻辑</h3>
 * <p>通过实现 {@link MybatisMetaFillSupport} 接口自定义填充逻辑:</p>
 * <pre>{@code
 * @Component
 * public class CustomMetaFillSupport implements MybatisMetaFillSupport {
 *
 *     @Override
 *     public Map<String, MetaAutoFillFunction> insertMetaAutoFillFunction(MetaObject metaObject) {
 *         Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
 *         fillMap.put("createTime", () -> new Date());
 *         fillMap.put("createBy", () -> SecurityContextHolder.getContext().getAuthentication().getName());
 *         return fillMap;
 *     }
 *
 *     @Override
 *     public Map<String, MetaAutoFillFunction> updateMetaAutoFillFunction(MetaObject metaObject) {
 *         Map<String, MetaAutoFillFunction> fillMap = new HashMap<>();
 *         fillMap.put("updateTime", () -> new Date());
 *         fillMap.put("updateBy", () -> SecurityContextHolder.getContext().getAuthentication().getName());
 *         return fillMap;
 *     }
 * }
 * }</pre>
 *
 * <h3>设计优势</h3>
 * <ul>
 *   <li>自动化: 减少重复代码,自动处理通用字段的填充</li>
 *   <li>灵活性: 通过接口实现,支持自定义填充逻辑</li>
 *   <li>安全性: 集成安全上下文,自动获取当前用户信息</li>
 *   <li>兼容性: 与MyBatis Plus无缝集成,遵循其设计原则</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see MetaObjectHandler
 * @see MybatisMetaFillSupport
 * @see MetaAutoFillFunction
 */
@RequiredArgsConstructor
public class DaoMetaObjectHandler implements MetaObjectHandler {

    private final MybatisMetaFillSupport mybatisMetaFillSupport;

    @Override
    public void insertFill(MetaObject metaObject) {
        autoFill(metaObject, false, mybatisMetaFillSupport.insertMetaAutoFillFunction(metaObject));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        autoFill(metaObject, true, mybatisMetaFillSupport.updateMetaAutoFillFunction(metaObject));
    }

    protected void autoFill(MetaObject metaObject, boolean override, Map<String, MetaAutoFillFunction> metaAutoMap) {
        metaAutoMap.keySet().forEach(fieldName -> {
            if (metaObject.hasSetter(fieldName)) {
                if (override && Objects.nonNull(metaObject.getValue(fieldName))) {
                    metaObject.setValue(fieldName, metaAutoMap.get(fieldName).getFillValue());
                } else if (Objects.isNull(metaObject.getValue(fieldName))) {
                    metaObject.setValue(fieldName, metaAutoMap.get(fieldName).getFillValue());
                }
            }
        });
    }

}

package com.basetc.base.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.basetc.base.common.domain.BaseOptions;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.*;

/**
 * 基础枚举接口,提供通用的枚举处理方法.
 *
 * <p>此接口定义了系统中所有枚举类的统一规范,提供了枚举值转换、描述获取、扩展信息等功能.
 * 实现此接口的枚举类可以与 MyBatis Plus 无缝集成,支持自动枚举映射和 JSON 序列化.
 *
 * <p>主要功能:
 * <ul>
 *   <li>根据枚举值获取枚举实例 - {@link #getByValue(Serializable, Class)}
 *   <li>将枚举转换为选项列表 - {@link #convertOptions(Class, Class)}
 *   <li>获取枚举描述信息 - {@link #getDescription()}
 *   <li>获取扩展信息 - {@link #getExtMap()}
 *   <li>获取枚举值 - {@link #getValue()}
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 1. 定义基础枚举
 * public enum UserStatusEnum implements BaseEnum<Integer> {
 *
 *     ACTIVE(1, "正常"),
 *     INACTIVE(0, "禁用"),
 *     LOCKED(-1, "锁定");
 *
 *     private final Integer value;
 *     private final String description;
 *
 *     UserStatusEnum(Integer value, String description) {
 *         this.value = value;
 *         this.description = description;
 *     }
 *
 *     @Override
 *     public Integer getValue() {
 *         return value;
 *     }
 *
 *     @Override
 *     public String getDescription() {
 *         return description;
 *     }
 * }
 *
 * // 2. 根据值获取枚举实例
 * UserStatusEnum status = BaseEnum.getByValue(1, UserStatusEnum.class);
 * // 返回: UserStatusEnum.ACTIVE
 *
 * // 3. 将枚举转换为选项列表(用于前端下拉框)
 * List<BaseOptions> options = BaseEnum.convertOptions(UserStatusEnum.class, BaseDict.DictData.class);
 * // 返回: [
 * //   {label: "正常", value: "1"},
 *   {label: "禁用", value: "0"},
 *   {label: "锁定", value: "-1"}
 * // ]
 *
 * // 4. 在实体类中使用
 * @Data
 * @TableName("sys_user")
 * public class User {
 *
 *     @TableField(typeHandler = MybatisEnumTypeHandler.class)
 *     private UserStatusEnum status;
 * }
 *
 * // 5. 存储到数据库时自动转换为枚举值
 * user.setStatus(UserStatusEnum.ACTIVE);
 * userMapper.insert(user);
 * // 数据库中存储: status = 1
 *
 * // 6. 从数据库查询时自动转换为枚举实例
 * User user = userMapper.selectById(1L);
 * // user.getStatus() 返回 UserStatusEnum.ACTIVE
 * }</pre>
 *
 * <h3>与 MyBatis Plus 集成:</h3>
 * <p>此接口继承了 MyBatis Plus 的 {@link IEnum} 接口,支持以下功能:
 * <ul>
 *   <li>自动将枚举值存储到数据库
 *   <li>从数据库查询时自动转换为枚举实例
 *   <li>支持条件构造器中的枚举参数
 * </ul>
 *
 * <h3>与 Jackson 集成:</h3>
 * <p>通过 {@link JsonValue} 注解,JSON 序列化时自动使用枚举值:
 * <pre>{@code
 * // 序列化时
 * User user = new User();
 * user.setStatus(UserStatusEnum.ACTIVE);
 *
 * // JSON 输出:
 * // {
 * //   "status": 1
 * // }
 * }</pre>
 *
 * <h3>扩展信息示例:</h3>
 * <pre>{@code
 * public enum PriorityEnum implements BaseEnum<Integer> {
 *
 *     LOW(1, "低优先级"),
 *     MEDIUM(2, "中优先级"),
 *     HIGH(3, "高优先级"),
 *     URGENT(4, "紧急");
 *
 *     private final Integer value;
 *     private final String description;
 *
 *     // ... 实现 getValue() 和 getDescription()
 *
 *     @Override
 *     public Map<String, Object> getExtMap() {
 *         Map<String, Object> extMap = new HashMap<>();
 *         extMap.put("color", getColor());
 *         extMap.put("level", getLevel());
 *         return extMap;
 *     }
 *
 *     private String getColor() {
 *         return switch (this) {
 *             case LOW -> "green";
 *             case MEDIUM -> "blue";
 *             case HIGH -> "orange";
 *             case URGENT -> "red";
 *         };
 *     }
 *
 *     private int getLevel() {
 *         return value;
 *     }
 * }
 *
 * // 转换后的选项包含扩展信息
 * List<BaseOptions> options = BaseEnum.convertOptions(PriorityEnum.class, BaseDict.DictData.class);
 * // [
 * //   {label: "低优先级", value: "1", extMap: {color: "green", level: 1}},
 * //   {label: "中优先级", value: "2", extMap: {color: "blue", level: 2}},
 * //   ...
 * // ]
 * }</pre>
 *
 * @param <T> 枚举值类型,必须实现 Serializable 接口,通常为 Integer 或 String
 * @see IEnum
 * @see com.basetc.base.common.annotation.DictType
 * @see com.basetc.base.common.utils.DictBaseEnumScanner
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public interface BaseEnum<T extends Serializable> extends IEnum<T> {

    /**
     * 根据枚举值获取对应的枚举实例.
     *
     * <p>此方法会遍历指定枚举类的所有常量,找到值匹配的枚举实例.
     * 如果未找到匹配的枚举,则返回 null.
     *
     * <p><b>注意:</b> 如果枚举值可能不匹配,建议调用后进行 null 检查.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 根据数据库中的值获取枚举
     * Integer statusCode = user.getStatusCode();
     * UserStatusEnum status = BaseEnum.getByValue(statusCode, UserStatusEnum.class);
     *
     * if (status != null) {
     *     log.info("用户状态: {}", status.getDescription());
     * } else {
     *     log.warn("无效的状态码: {}", statusCode);
     * }
     * }</pre>
     *
     * @param value 枚举值,如 1、0、"ACTIVE" 等
     * @param clazz 枚举类型的 Class 对象,如 UserStatusEnum.class
     * @param <E>   枚举类型,必须继承 Enum 并实现 BaseEnum
     * @param <T>   枚举值类型,必须实现 Serializable
     * @return 匹配的枚举实例,如果未找到则返回 null
     * @throws NullPointerException 如果 clazz 为 null
     */
    static <E extends Enum<E> & BaseEnum<T>, T extends Serializable> E getByValue(
            T value, Class<E> clazz) {
        final E[] clazzEnumConstants = clazz.getEnumConstants();
        final Optional<E> first = Arrays.stream(clazzEnumConstants)
                .filter(e -> Objects.equals(e.getValue(), value))
                .findFirst();
        return first.orElse(null);
    }

    /**
     * 将枚举转换为选项列表.
     *
     * <p>此方法会遍历枚举类的所有常量,将其转换为 {@link BaseOptions} 列表.
     * 每个选项包含 label(描述)、value(值)和 extMap(扩展信息).
     *
     * <p>常用于前端下拉框、单选按钮组等场景.
     *
     * <p><b>注意:</b> 选项类必须有无参构造函数,否则会抛出异常.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 转换为选项列表
     * List<BaseDict.DictData> options = BaseEnum.convertOptions(
     *     UserStatusEnum.class,
     *     BaseDict.DictData.class
     * );
     *
     * // 通过接口返回给前端
     * @GetMapping("/api/dict/user-status")
     * public R<List<BaseDict.DictData>> getUserStatusDict() {
     *     return R.success(
     *         BaseEnum.convertOptions(UserStatusEnum.class, BaseDict.DictData.class)
     *     );
     * }
     * }</pre>
     *
     * @param baseEnum 枚举类的 Class 对象,必须实现 BaseEnum 接口
     * @param clazz    选项类的 Class 对象,通常是 BaseDict.DictData.class
     * @return 选项列表,如果参数无效则返回空列表
     * @throws RuntimeException 如果选项类实例化失败
     */
    static List<? extends BaseOptions> convertOptions(
            Class<? extends BaseEnum<?>> baseEnum, Class<? extends BaseOptions> clazz) {
        if (Objects.isNull(baseEnum) || !baseEnum.isEnum() || Objects.isNull(clazz)) {
            return List.of();
        }
        return Arrays.stream(baseEnum.getEnumConstants())
                .map(
                        e -> {
                            try {
                                BaseOptions options = clazz.getDeclaredConstructor().newInstance();
                                options.setValue(Objects.toString(e.getValue(), ""));
                                options.setLabel(e.getDescription());
                                options.setExtMap(e.getExtMap());
                                return options;
                            } catch (Exception ex) {
                                throw new RuntimeException("Failed to create instance of " + clazz.getName(), ex);
                            }
                        })
                .toList();
    }

    /**
     * 获取枚举的描述信息.
     *
     * <p>描述信息用于前端展示,如 "正常"、"禁用" 等.
     *
     * @return 描述信息,如 "正常"、"禁用" 等
     */
    String getDescription();

    /**
     * 获取枚举的扩展信息.
     *
     * <p>扩展信息是一个 Map,可以包含任意键值对,用于存储额外的元数据.
     * 常见的扩展信息包括: 颜色、级别、图标、CSS 类名等.
     *
     * <p>默认实现返回空 Map,子类可以覆盖此方法提供自定义扩展信息.
     *
     * <h3>扩展示例:</h3>
     * <pre>{@code
     * @Override
     * public Map<String, Object> getExtMap() {
     *     Map<String, Object> extMap = new HashMap<>();
     *     extMap.put("color", "red");
     *     extMap.put("icon", "fa-lock");
     *     extMap.put("cssClass", "status-locked");
     *     return extMap;
     * }
     * }</pre>
     *
     * @return 扩展信息 Map,默认为空 Map
     */
    default Map<String, Object> getExtMap() {
        return Map.of();
    }

    /**
     * 获取枚举值.
     *
     * <p>枚举值是存储到数据库中的实际值,通常是整数或字符串.
     *
     * <p>通过 {@link JsonValue} 注解,JSON 序列化时会使用此值.
     *
     * @return 枚举值,如 1、0、"ACTIVE" 等
     */
    @JsonValue
    @Override
    T getValue();

}

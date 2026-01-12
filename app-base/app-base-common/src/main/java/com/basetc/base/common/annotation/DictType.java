package com.basetc.base.common.annotation;

import com.basetc.base.common.domain.BaseDict;
import com.basetc.base.common.enums.BaseEnum;

import java.lang.annotation.*;

/**
 * 字典类型注解,用于标记字典类.
 *
 * <p>此注解用于标记枚举类或其他字典数据结构,提供字典类型的标识和描述信息.
 * 被标记的类会被 {@link com.basetc.base.common.utils.DictBaseEnumScanner} 扫描
 * 并转换为 {@link BaseDict} 字典数据.
 *
 * <p>通常用于标记实现了 {@link BaseEnum} 接口的枚举类,
 * 将枚举值转换为系统中的字典数据,供前端下拉选择等场景使用.
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 1. 简单字典
 * @DictType(value = "gender", description = "性别")
 * public enum GenderEnum implements BaseEnum<Integer> {
 *     MALE(1, "男"),
 *     FEMALE(2, "女");
 *
 *     private final Integer value;
 *     private final String description;
 *
 *     // ... 实现 BaseEnum 接口
 * }
 *
 * // 2. 复杂字典(包含扩展信息)
 * @DictType(value = "user_status", description = "用户状态")
 * public enum UserStatusEnum implements BaseEnum<Integer> {
 *     ACTIVE(1, "正常"),
 *     INACTIVE(0, "禁用"),
 *     LOCKED(-1, "锁定");
 *
 *     // ... 实现 BaseEnum 接口
 *
 *     @Override
 *     public Map<String, Object> getExtMap() {
 *         Map<String, Object> extMap = new HashMap<>();
 *         extMap.put("color", this.value > 0 ? "green" : "red");
 *         extMap.put("level", Math.abs(this.value));
 *         return extMap;
 *     }
 * }
 * }</pre>
 *
 * <h3>前端使用:</h3>
 * <pre>{@code
 * // 前端获取字典数据
 * GET /api/dict/user_status
 *
 * // 返回结果:
 * {
 *   "code": 200,
 *   "data": [
 *     {"label": "正常", "value": "1", "extMap": {"color": "green"}},
 *     {"label": "禁用", "value": "0", "extMap": {"color": "red"}}
 *   ]
 * }
 * }</pre>
 *
 * @see com.basetc.base.common.utils.DictBaseEnumScanner
 * @see BaseEnum
 * @see BaseDict
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictType {

    /**
     * 字典类型标识.
     *
     * <p>用于唯一标识字典类型,如 "gender"、"user_status" 等.
     * 建议使用全小写加下划线的命名方式.
     *
     * @return 字典类型标识
     */
    String value() default "";

    /**
     * 字典类型描述.
     *
     * <p>用于描述字典的用途,如 "性别"、"用户状态" 等.
     * 通常用于前端展示字典名称.
     *
     * @return 字典类型描述
     */
    String description() default "";
}

package com.basetc.base.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 字典实体类,用于封装字典数据结构.
 *
 * <p>此类用于封装系统中的字典数据,包含字典的唯一标识、描述信息和数据项列表.
 * 通常用于系统中固定的选项数据,如性别、状态、类型等字典表数据封装.
 *
 * <p>字典数据通常来源于枚举类,通过 {@link com.basetc.base.common.utils.DictBaseEnumScanner} 扫描
 * 带有 {@link com.basetc.base.common.annotation.DictType} 注解的枚举类并自动转换为字典数据.
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 定义字典枚举
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
 * // 扫描并获取字典数据
 * List<BaseDict> dicts = DictBaseEnumScanner.scan("com.example.enums");
 *
 * // 字典数据格式:
 * // {
 * //   "key": "gender",
 * //   "description": "性别",
 * //   "data": [
 * //     {"label": "男", "value": "1"},
 * //     {"label": "女", "value": "2"}
 * //   ]
 * // }
 * }</pre>
 *
 * @see com.basetc.base.common.annotation.DictType
 * @see com.basetc.base.common.enums.BaseEnum
 * @see com.basetc.base.common.utils.DictBaseEnumScanner
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
public class BaseDict implements Serializable {

    /**
     * 序列化版本号.
     */
    @Serial
    private static final long serialVersionUID = -6503071134019778836L;

    /**
     * 字典的唯一标识键.
     * <p>用于标识字典的唯一键,如 "gender"、"user_status" 等.
     */
    private String key;

    /**
     * 字典的描述信息.
     * <p>用于描述字典的用途,如 "性别"、"用户状态" 等.
     */
    private String description;

    /**
     * 字典的数据项列表.
     * <p>包含字典的所有选项数据,每个选项包含 label 和 value.
     * <p>默认为空列表,不会为 null.
     */
    private List<DictData> data = List.of();

    /**
     * 字典数据项内部类.
     *
     * <p>继承自 {@link BaseOptions},用于封装字典中的具体数据项.
     * 包含选项的显示名称(label)和选项值(value).
     *
     * @see BaseOptions
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DictData extends BaseOptions {

        /**
         * 序列化版本号.
         */
        @Serial
        private static final long serialVersionUID = 5028414955352940992L;
    }

}

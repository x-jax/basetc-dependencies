package com.basetc.base.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 选项类,用于表示键值对形式的选项数据.
 *
 * <p>此类用于封装系统中通用的选项数据,如下拉框选项、单选按钮组等.
 * 每个选项包含显示名称(label)、选项值(value)和扩展字段(extMap).
 *
 * <p>通常与 {@link BaseDict} 配合使用,作为字典数据的具体数据项.
 * 也可以单独使用,用于封装任何键值对形式的选项数据.
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 创建选项
 * BaseOptions option = new BaseOptions();
 * option.setLabel("管理员");
 * option.setValue("1");
 *
 * // 创建选项并设置扩展信息
 * BaseOptions option2 = new BaseOptions();
 * option2.setLabel("普通用户");
 * option2.setValue("2");
 * option2.getExtMap().put("color", "blue");
 * option2.getExtMap().put("level", 1);
 *
 * // 使用构造函数创建
 * BaseOptions option3 = new BaseOptions("VIP用户", "3");
 * }</pre>
 *
 * <h3>JSON 格式:</h3>
 * <pre>{@code
 * {
 *   "label": "管理员",
 *   "value": "1",
 *   "extMap": {
 *     "color": "red",
 *     "level": 99
 *   }
 * }
 * }</pre>
 *
 * @see BaseDict
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BaseOptions implements Serializable {

    /**
     * 序列化版本号.
     */
    @Serial
    private static final long serialVersionUID = -8943399172114564459L;

    /**
     * 选项显示名称.
     * <p>用于在前端展示的文本,如 "管理员"、"正常" 等.
     */
    private String label;

    /**
     * 选项值.
     * <p>选项的实际值,通常是字符串类型,如 "1"、"0" 等.
     */
    private String value;

    /**
     * 扩展字段.
     * <p>用于存储选项的额外信息,如颜色、级别、图标等.
     * <p>默认为空的 HashMap,不会为 null.
     */
    private Map<String, Object> extMap = new HashMap<>();

}

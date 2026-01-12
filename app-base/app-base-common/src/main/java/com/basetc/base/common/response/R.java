package com.basetc.base.common.response;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类.
 *
 * <p>此类实现了标准的API响应格式,包含状态码、消息、数据和时间戳.
 * 支持泛型数据类型,适用于各种业务场景的统一响应封装.
 *
 * <h3>响应格式示例:</h3>
 * <pre>{@code
 * {
 *   "code": 200,
 *   "msg": "操作成功",
 *   "data": { "id": 1, "name": "张三" },
 *   "timestamp": 1699999999999
 * }
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 成功响应(带数据)
 * R<User> response = R.success(user);
 *
 * // 成功响应(无数据)
 * R<Void> response = R.success();
 *
 * // 失败响应
 * R<Void> error = R.error("用户不存在");
 *
 * // 自定义错误码
 * R<Void> error = R.error(404, "资源未找到");
 * }</pre>
 *
 * @param <T> 响应数据类型
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see TcResponse
 */
@Data
@NoArgsConstructor
public class R<T> implements TcResponse<Integer, T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码.
     * <ul>
     *   <li>200: 成功</li>
     *   <li>400: 请求参数错误</li>
     *   <li>401: 未认证</li>
     *   <li>403: 无权限</li>
     *   <li>404: 资源不存在</li>
     *   <li>500: 服务器内部错误</li>
     * </ul>
     */
    private Integer code;

    /**
     * 响应消息.
     * <p>描述操作结果的详细信息,成功或失败的原因等.
     */
    private String msg;

    /**
     * 响应数据.
     * <p>业务数据对象,可以是任意类型的数据,包括实体对象、集合、Map等.
     */
    private T data;

    /**
     * 响应时间戳.
     * <p>服务器生成响应的时间戳(毫秒),用于客户端计算请求耗时和时钟同步.
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 全参数构造函数.
     *
     * @param code 响应状态码
     * @param msg  响应消息
     * @param data 响应数据
     */
    public R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 构建成功响应(带数据).
     *
     * <p>使用默认成功状态码200和默认成功消息"操作成功".
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return R对象
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 构建成功响应(无数据).
     *
     * <p>使用默认成功状态码200和默认成功消息"操作成功",数据字段为null.
     *
     * @param <T> 数据类型
     * @return R对象
     */
    public static <T> R<T> success() {
        return success(null);
    }

    /**
     * 构建成功响应(自定义消息).
     *
     * <p>使用默认成功状态码200,但允许自定义成功消息.
     *
     * @param message 成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return R对象
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 构建失败响应(默认错误码500).
     *
     * <p>使用默认错误码500(服务器内部错误)和自定义错误消息.
     *
     * @param message 错误信息
     * @return R对象
     */
    public static R<Void> error(String message) {
        return error(500, message);
    }

    /**
     * 构建失败响应(自定义错误码).
     *
     * @param code    错误码
     * @param message 错误信息
     * @return R对象
     */
    public static R<Void> error(int code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 判断响应是否成功.
     *
     * <p>成功条件: 状态码为2xx(200-299).
     *
     * @return true=成功 false=失败
     */
    @JsonIgnore
    public boolean isSuccess() {
        return code != null && code >= 200 && code < 300;
    }

    /**
     * 将响应对象转换为JSON字符串.
     *
     * <p>使用FastJSON2进行序列化,注意此方法标记了@JsonIgnore,
     * 不会在JSON序列化时递归调用.
     *
     * @return JSON字符串
     */
    @JsonIgnore
    public String toJsonString() {
        return JSONObject.toJSONString(this);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public T getData() {
        return data;
    }
}

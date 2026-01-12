package com.basetc.base.common.exception;

import com.basetc.base.common.response.R;
import com.basetc.base.common.response.TcResponse;
import lombok.Getter;

import java.io.Serial;

/**
 * 自定义基础异常类,用于封装业务异常信息.
 *
 * <p>此类继承自{@link RuntimeException},是所有业务异常的基类.
 * 每个异常实例都包含一个标准的响应对象{@link R},便于统一异常处理和返回.
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 简单抛出异常(使用默认错误码500)
 * throw new BasetcException("用户不存在");
 *
 * // 指定错误码
 * throw new BasetcException(404, "资源未找到");
 *
 * // 带原因异常
 * throw new BasetcException("数据库操作失败", cause);
 *
 * // 基于响应对象构建异常
 * R<Void> response = R.error(400, "参数错误");
 * throw new BasetcException(response);
 * }</pre>
 *
 * <h3>全局异常处理:</h3>
 * <p>建议配合{@link com.basetc.base.web.handler.GlobalExceptionHandler}使用,
 * 统一捕获并返回标准格式的错误响应.
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see R
 * @see TcResponse
 */
public class BasetcException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3388911838028749155L;

    /**
     * 异常响应对象.
     * <p>包含错误码和错误消息的响应对象,可直接返回给前端.
     */
    @Getter
    private final R<Void> err;

    /**
     * 构造函数,使用默认错误码500和指定的错误消息.
     *
     * @param message 错误消息,不能为null
     * @throws IllegalArgumentException 如果message为null
     */
    public BasetcException(String message) {
        super(message);
        if (message == null) {
            throw new IllegalArgumentException("Error message cannot be null");
        }
        err = R.error(message);
    }

    /**
     * 构造函数,使用默认错误码500、指定的错误消息和原因.
     *
     * @param message 错误消息,不能为null
     * @param cause   异常原因,可为null
     * @throws IllegalArgumentException 如果message为null
     */
    public BasetcException(String message, Throwable cause) {
        super(message, cause);
        if (message == null) {
            throw new IllegalArgumentException("Error message cannot be null");
        }
        err = R.error(message);
    }

    /**
     * 构造函数,使用指定的错误码和错误消息.
     *
     * @param code    错误码,建议使用HTTP标准状态码
     * @param message 错误消息,不能为null
     * @throws IllegalArgumentException 如果message为null
     */
    public BasetcException(int code, String message) {
        super(message);
        if (message == null) {
            throw new IllegalArgumentException("Error message cannot be null");
        }
        err = R.error(code, message);
    }

    /**
     * 构造函数,使用指定的错误码、错误消息和原因.
     *
     * @param code    错误码,建议使用HTTP标准状态码
     * @param message 错误消息,不能为null
     * @param cause   异常原因,可为null
     * @throws IllegalArgumentException 如果message为null
     */
    public BasetcException(int code, String message, Throwable cause) {
        super(message, cause);
        if (message == null) {
            throw new IllegalArgumentException("Error message cannot be null");
        }
        err = R.error(code, message);
    }

    /**
     * 构造函数,基于响应对象构建异常.
     *
     * <p>此构造函数会从响应对象中提取错误码和错误消息,
     * 构建一个包含相同信息的异常对象.
     *
     * @param response 响应对象,不能为null
     * @throws IllegalArgumentException 如果response为null
     */
    public BasetcException(TcResponse<Integer, Void> response) {
        super(response.getMsg());
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        this.err = new R<>(response.getCode(), response.getMsg(), null);
    }

    /**
     * 构造函数,基于响应对象和异常原因构建异常.
     *
     * <p>此构造函数会从响应对象中提取错误码和错误消息,
     * 构建一个包含相同信息和原因的异常对象.
     *
     * @param response 响应对象,不能为null
     * @param cause    异常原因,可为null
     * @throws IllegalArgumentException 如果response为null
     */
    public BasetcException(TcResponse<Integer, Void> response, Throwable cause) {
        super(response.getMsg(), cause);
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        this.err = new R<>(response.getCode(), response.getMsg(), null);
    }

    /**
     * 获取错误码.
     *
     * @return 错误码
     */
    public int getCode() {
        return err.getCode();
    }
}

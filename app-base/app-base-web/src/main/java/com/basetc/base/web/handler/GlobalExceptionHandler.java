package com.basetc.base.web.handler;

import com.basetc.base.common.exception.BasetcException;
import com.basetc.base.common.response.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器,统一处理Web层异常.
 *
 * <p>此类使用Spring MVC的{@link RestControllerAdvice}注解,用于全局拦截和处理Controller层抛出的异常.
 * 所有异常都会被转换为统一的{@link R}响应格式,返回给前端。</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>统一异常处理: 所有异常都会被转换为标准的R格式</li>
 *   <li>分类处理: 针对不同类型的异常提供不同的处理逻辑</li>
 *   <li>详细日志: 记录异常堆栈和请求信息,便于问题排查</li>
 *   <li>友好提示: 对技术异常进行转换,返回用户友好的错误信息</li>
 *   <li>安全防护: 避免敏感信息泄露,保护系统安全</li>
 * </ul>
 *
 * <h3>异常处理优先级</h3>
 * <ol>
 *   <li>自定义业务异常 ({@link BasetcException})</li>
 *   <li>参数校验异常 (Validation)</li>
 *   <li>安全异常 (Spring Security)</li>
 *   <li>数据库异常 (DataAccessException)</li>
 *   <li>系统异常 (Exception)</li>
 * </ol>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在Controller中抛出异常
 * @RestController
 * @RequestMapping("/users")
 * public class UserController {
 *     @GetMapping("/{id}")
 *     public R<User> getById(@PathVariable Long id) {
 *         User user = userService.getByIdOrThrow(id, () -> {
 *             throw new BasetcException(404, "用户不存在");
 *         });
 *         return R.success(user);
 *     }
 * }
 *
 * // 前端会收到统一格式的响应:
 * // {
 * //   "code": 404,
 * //   "msg": "用户不存在",
 * //   "data": null,
 * //   "timestamp": 1699999999999
 * // }
 * }</pre>
 *
 * <h3>自定义异常处理</h3>
 * <p>如需自定义异常处理逻辑,可以继承此类并重写对应方法:</p>
 * <pre>{@code
 * @RestControllerAdvice
 * public class CustomExceptionHandler extends GlobalExceptionHandler {
 *
 *     @ExceptionHandler(MyCustomException.class)
 *     public R<Void> handleMyCustomException(MyCustomException e) {
 *         // 自定义处理逻辑
 *         return R.error(400, "自定义错误信息");
 *     }
 * }
 * }</pre>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>优先使用自定义业务异常,便于统一处理</li>
 *   <li>合理设置HTTP状态码,符合RESTful规范</li>
 *   <li>记录详细的异常日志,便于问题追踪</li>
 *   <li>避免向客户端暴露敏感的技术细节</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see BasetcException
 * @see R
 * @see RestControllerAdvice
 */
@Slf4j
@ConditionalOnWebApplication
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常.
     *
     * <p>拦截{@link BasetcException}及其子类异常,直接返回异常中封装的错误响应对象.
     * 这是业务层推荐使用的异常类型,可以精确控制错误码和错误消息.
     *
     * @param request 当前HTTP请求对象
     * @param e       业务异常对象
     * @return 错误响应对象,包含错误码和错误消息
     */
    @ExceptionHandler(BasetcException.class)
    public R<Void> handleBasetcException(HttpServletRequest request, BasetcException e) {
        log.error("业务异常: {} {}", request.getMethod(), request.getRequestURI(), e);
        return e.getErr();
    }

    /**
     * 处理方法参数校验异常(@Valid注解触发).
     *
     * <p>当Controller方法参数使用@Valid或@Validated注解时,
     * 如果校验失败会抛出此异常.
     *
     * <p>返回第一个字段的错误消息,便于前端定位问题.
     *
     * @param request 当前HTTP请求对象
     * @param e       方法参数校验异常
     * @return 错误响应,包含具体的校验失败信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(
            HttpServletRequest request, MethodArgumentNotValidException e) {
        log.warn("参数校验异常: {} {}", request.getMethod(), request.getRequestURI(), e);

        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult.hasFieldErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().get(0);
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            return R.error(400, String.format("参数校验失败: %s %s", fieldName, errorMessage));
        }
        return R.error(400, "参数校验失败");
    }

    /**
     * 处理绑定异常.
     *
     * <p>处理数据绑定过程中的异常,例如类型转换失败等.
     *
     * @param request 当前HTTP请求对象
     * @param e       绑定异常对象
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(HttpServletRequest request, BindException e) {
        log.warn("参数绑定异常: {} {}", request.getMethod(), request.getRequestURI(), e);

        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return R.error(400, "参数绑定失败: " + errorMsg);
    }

    /**
     * 处理单个参数校验异常(@RequestParam/@PathVariable校验失败).
     *
     * <p>当使用@Validated注解校验简单类型参数(如@RequestParam、@PathVariable)时,
     * 校验失败会抛出此异常.
     *
     * @param request 当前HTTP请求对象
     * @param e       约束违反异常
     * @return 错误响应,包含所有校验失败信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(
            HttpServletRequest request, ConstraintViolationException e) {
        log.warn("参数约束校验异常: {} {}", request.getMethod(), request.getRequestURI(), e);

        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return R.error(400, errorMsg);
    }

    /**
     * 处理缺少请求参数异常.
     *
     * <p>当请求中缺少必需的参数时抛出此异常.
     *
     * @param request 当前HTTP请求对象
     * @param e       缺少参数异常对象
     * @return 错误响应,提示缺少的参数名
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingServletRequestParameterException(
            HttpServletRequest request, MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {} {}", request.getMethod(), request.getRequestURI(), e);

        String parameterName = e.getParameterName();
        String parameterType = e.getParameterType();
        return R.error(400, String.format("缺少必需参数: %s (类型: %s)", parameterName, parameterType));
    }

    /**
     * 处理方法参数类型不匹配异常.
     *
     * <p>当请求参数类型无法转换为目标类型时抛出此异常,
     * 例如: 期望Long类型但传入字符串"abc".
     *
     * @param request 当前HTTP请求对象
     * @param e       类型不匹配异常对象
     * @return 错误响应,包含参数名、传入值和期望类型
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleMethodArgumentTypeMismatchException(
            HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {} {}", request.getMethod(), request.getRequestURI(), e);

        String paramName = e.getName();
        String paramValue = e.getValue() != null ? e.getValue().toString() : "null";
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        return R.error(400, String.format("参数类型错误: %s (值: %s, 期望类型: %s)", paramName, paramValue, requiredType));
    }

    /**
     * 处理非法参数异常.
     *
     * <p>捕获程序中手动抛出的IllegalArgumentException,
     * 通常用于参数校验失败的场景.
     *
     * @param request 当前HTTP请求对象
     * @param e       非法参数异常对象
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(
            HttpServletRequest request, IllegalArgumentException e) {
        log.warn("非法参数异常: {} {}", request.getMethod(), request.getRequestURI(), e);
        return R.error(400, e.getMessage());
    }

    /**
     * 处理访问拒绝异常(权限不足).
     *
     * <p>当用户已认证但权限不足访问资源时抛出此异常,
     * 例如: 普通用户访问管理员接口.
     *
     * @param request 当前HTTP请求对象
     * @param e       访问拒绝异常对象
     * @return 错误响应,HTTP状态码403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        log.warn("访问被拒绝: {} {}", request.getMethod(), request.getRequestURI(), e);
        return R.error(403, "无权限访问");
    }

    /**
     * 处理认证异常(未登录或认证失败).
     *
     * <p>当用户未登录或认证令牌无效时抛出此异常.
     *
     * @param request 当前HTTP请求对象
     * @param e       认证异常对象
     * @return 错误响应,HTTP状态码401
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthenticationException(HttpServletRequest request, AuthenticationException e) {
        log.warn("认证失败: {} {}", request.getMethod(), request.getRequestURI(), e);
        return R.error(401, "认证失败,请重新登录");
    }

    /**
     * 处理数据完整性异常.
     *
     * <p>处理数据库层面的数据完整性约束违反,
     * 例如: 唯一索引冲突、外键约束违反、非空约束违反等.
     *
     * @param request 当前HTTP请求对象
     * @param e       数据完整性异常对象
     * @return 友好的错误提示,避免暴露数据库细节
     */
    @ExceptionHandler({DataIntegrityViolationException.class, DuplicateKeyException.class})
    public R<Void> handleDataIntegrityViolationException(
            HttpServletRequest request, Exception e) {
        log.error("数据完整性异常: {} {}", request.getMethod(), request.getRequestURI(), e);

        String message = e.getMessage();
        if (message != null) {
            if (message.contains("Duplicate entry")) {
                return R.error(409, "数据已存在,请勿重复添加");
            } else if (message.contains("cannot be null")) {
                return R.error(400, "必填字段不能为空");
            } else if (message.contains("foreign key constraint")) {
                return R.error(409, "有关联数据,无法删除");
            }
        }
        return R.error(500, "数据操作失败");
    }

    /**
     * 处理系统异常(兜底处理).
     *
     * <p>捕获所有未被上述方法处理的异常,
     * 作为最后的异常处理防线,避免直接暴露异常堆栈给用户.
     *
     * <p><b>注意:</b> 此方法应该始终放在最后,作为异常处理的兜底方案.
     *
     * @param request 当前HTTP请求对象
     * @param e       系统异常对象
     * @return 通用的服务器错误响应
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(HttpServletRequest request, Exception e) {
        log.error("系统异常: {} {}", request.getMethod(), request.getRequestURI(), e);
        return R.error(500, "服务异常,请稍后再试");
    }
}

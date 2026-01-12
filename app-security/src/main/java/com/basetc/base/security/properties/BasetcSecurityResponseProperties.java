package com.basetc.base.security.properties;

import lombok.Data;

/**
 * 安全响应配置属性类,定义安全相关响应的配置参数.
 *
 * <p>此类用于配置认证失败和权限不足时的响应信息,
 * 包括 HTTP 状态码、内容类型和响应体.
 *
 * <h3>使用场景:</h3>
 * <ul>
 *   <li>用户未登录或 Token 无效时返回 401 响应</li>
 *   <li>用户已登录但权限不足时返回 403 响应</li>
 *   <li>自定义响应格式和消息</li>
 *   <li>统一前后端交互的响应规范</li>
 * </ul>
 *
 * <h3>配置示例:</h3>
 * <pre>{@code
 * basetc:
 *   security:
 *     auth:
 *       un-authorized:
 *         http-code: 401
 *         content-type: application/json
 *         body:
 *           code: 401
 *           msg: "请先登录"
 *       access-denied:
 *         http-code: 403
 *         content-type: application/json
 *         body:
 *           code: 403
 *           msg: "权限不足"
 * }</pre>
 *
 * <h3>响应格式示例:</h3>
 * <pre>{@code
 * # 401 未授权响应
 * HTTP/1.1 401 Unauthorized
 * Content-Type: application/json
 *
 * {
 *   "code": 401,
 *   "msg": "当前资源无法访问,请登录"
 * }
 *
 * # 403 权限不足响应
 * HTTP/1.1 403 Forbidden
 * Content-Type: application/json
 *
 * {
 *   "code": 403,
 *   "msg": "权限不足,无法访问当前资源"
 * }
 * }</pre>
 *
 * <h3>在代码中使用:</h3>
 * <pre>{@code
 * @Component
 * @RequiredArgsConstructor
 * public class SecurityExceptionHandler {
 *
 *     private final BasetcSecurityAuthProperties authProperties;
 *
 *     @ExceptionHandler(AuthenticationException.class)
 *     public ResponseEntity<?> handleAuthenticationException() {
 *         BasetcSecurityResponseProperties response = authProperties.getUnAuthorized();
 *
 *         return ResponseEntity
 *             .status(response.getHttpCode())
 *             .header("Content-Type", response.getContentType())
 *             .body(response.getBody());
 *     }
 *
 *     @ExceptionHandler(AccessDeniedException.class)
 *     public ResponseEntity<?> handleAccessDeniedException() {
 *         BasetcSecurityResponseProperties response = authProperties.getAccessDenied();
 *
 *         return ResponseEntity
 *             .status(response.getHttpCode())
 *             .header("Content-Type", response.getContentType())
 *             .body(response.getBody());
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>HTTP 状态码应该符合 HTTP 规范</li>
 *   <li>业务码可以自定义,建议与 HTTP 状态码保持一致</li>
 *   <li>响应消息应该友好且明确</li>
 *   <li>Content-Type 通常为 application/json</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see BasetcSecurityAuthProperties
 * @see org.springframework.http.ResponseEntity
 */
@Data
public class BasetcSecurityResponseProperties {

    /**
     * HTTP 状态码.
     *
     * <p>指定响应的 HTTP 状态码,用于表示请求的处理结果.
     *
     * <h3>常见状态码:</h3>
     * <ul>
     *   <li>200 - 成功</li>
     *   <li>400 - 请求错误</li>
     *   <li>401 - 未授权 (需要登录)</li>
     *   <li>403 - 禁止访问 (权限不足)</li>
     *   <li>404 - 资源不存在</li>
     *   <li>500 - 服务器内部错误</li>
     * </ul>
     *
     * <h3>状态码分类:</h3>
     * <ul>
     *   <li>2xx - 成功响应</li>
     *   <li>3xx - 重定向</li>
     *   <li>4xx - 客户端错误</li>
     *   <li>5xx - 服务器错误</li>
     * </ul>
     */
    private Integer httpCode;

    /**
     * 内容类型.
     *
     * <p>指定响应的内容类型 (MIME 类型).
     *
     * <h3>常见内容类型:</h3>
     * <ul>
     *   <li>{@code application/json} - JSON 格式 (推荐)</li>
     *   <li>{@code application/xml} - XML 格式</li>
     *   <li>{@code text/html} - HTML 格式</li>
     *   <li>{@code text/plain} - 纯文本格式</li>
     * </ul>
     *
     * <h3>响应头示例:</h3>
     * <pre>{@code
     * Content-Type: application/json;charset=UTF-8
     * }</pre>
     */
    private String contentType;

    /**
     * 响应体.
     *
     * <p>包含业务码和消息的响应体对象.
     *
     * @see Body
     */
    private Body body;

    /**
     * 响应体内部类.
     *
     * <p>定义响应体的业务码和消息字段.
     *
     * @author Liu,Dongdong
     * @since 1.0.0
     */
    @Data
    public static class Body {

        /**
         * 业务码.
         *
         * <p>用于标识具体的业务错误类型.
         * 通常与 HTTP 状态码保持一致,也可以自定义.
         *
         * <h3>业务码示例:</h3>
         * <ul>
         *   <li>401 - 未登录或 Token 无效</li>
         *   <li>403 - 权限不足</li>
         *   <li>1001 - Token 过期</li>
         *   <li>1002 - Token 格式错误</li>
         *   <li>1003 - 用户不存在</li>
         * </ul>
         *
         * <h3>业务码规范:</h3>
         * <pre>{@code
         * # 通用错误码
         * 400 - 请求参数错误
         * 401 - 未授权
         * 403 - 权限不足
         * 404 - 资源不存在
         * 500 - 服务器错误
         *
         * # 业务错误码
         * 1001 - Token 过期
         * 1002 - Token 无效
         * 1003 - 用户不存在
         * 1004 - 密码错误
         * }</pre>
         */
        private Integer code;

        /**
         * 响应消息.
         *
         * <p>用于向客户端返回友好的错误提示信息.
         *
         * <h3>消息示例:</h3>
         * <ul>
         *   <li>"当前资源无法访问,请登录"</li>
         *   <li>"权限不足,无法访问当前资源"</li>
         *   <li>"Token 已过期,请重新登录"</li>
         *   <li>"用户名或密码错误"</li>
         * </ul>
         *
         * <h3>消息规范:</h3>
         * <ul>
         *   <li>使用简洁明了的中文</li>
         *   <li>避免使用技术术语</li>
         *   <li>提供明确的解决方向</li>
         *   <li>避免暴露系统内部信息</li>
         * </ul>
         */
        private String msg;

    }

}

package com.basetc.base.common.response;


/**
 * 统一响应接口,定义标准的 RESTful API 响应结构.
 *
 * <p>此接口约束服务端返回给客户端的数据格式,确保响应的一致性和可预测性。
 * 通过实现此接口,所有 API 响应都遵循统一的格式规范,便于前端处理和错误追踪。
 *
 * <h3>响应结构:</h3>
 * <pre>{@code
 * {
 *   "code": 200,           // 状态码
 *   "msg": "操作成功",     // 响应消息
 *   "data": { ... }        // 业务数据
 * }
 * }</pre>
 *
 * <h3>泛型参数说明:</h3>
 * <ul>
 *   <li>{@code <C>} - 响应状态码的类型 (如 Integer、String 等)</li>
 *   <li>{@code <T>} - 实际业务数据的类型 (可以是任意类型)</li>
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <h4>1. 实现响应接口:</h4>
 * <pre>{@code
 * @Data
 * public class R<T> implements TcResponse<Integer, T> {
 *
 *     private Integer code;  // 状态码
 *     private String msg;    // 响应消息
 *     private T data;        // 业务数据
 *
 *     public static <T> R<T> success() {
 *         R<T> r = new R<>();
 *         r.setCode(200);
 *         r.setMsg("操作成功");
 *         return r;
 *     }
 *
 *     public static <T> R<T> error(Integer code, String msg) {
 *         R<T> r = new R<>();
 *         r.setCode(code);
 *         r.setMsg(msg);
 *         return r;
 *     }
 * }
 * }</pre>
 *
 * <h4>2. 在 Controller 中使用:</h4>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/user")
 * public class UserController {
 *
 *     @GetMapping("/{id}")
 *     public R<User> getUser(@PathVariable Long id) {
 *         User user = userService.findById(id);
 *         return R.success(user);  // 返回成功响应
 *     }
 *
 *     @PostMapping
 *     public R<Void> createUser(@RequestBody UserDTO dto) {
 *         userService.create(dto);
 *         return R.success();  // 返回成功响应
 *     }
 *
 *     @ExceptionHandler(Exception.class)
 *     public R<Void> handleError(Exception e) {
 *         return R.error(500, e.getMessage());  // 返回错误响应
 *     }
 * }
 * }</pre>
 *
 * <h3>状态码规范建议:</h3>
 * <table border="1">
 *   <tr>
 *     <th>状态码</th>
 *     <th>说明</th>
 *     <th>使用场景</th>
 *   </tr>
 *   <tr>
 *     <td>200</td>
 *     <td>操作成功</td>
 *     <td>请求成功处理</td>
 *   </tr>
 *   <tr>
 *     <td>400</td>
 *     <td>请求参数错误</td>
 *     <td>参数校验失败</td>
 *   </tr>
 *   <tr>
 *     <td>401</td>
 *     <td>未认证</td>
 *     <td>用户未登录</td>
 *   </tr>
 *   <tr>
 *     <td>403</td>
 *     <td>权限不足</td>
 *     <td>用户无权限访问</td>
 *   </tr>
 *   <tr>
 *     <td>404</td>
 *     <td>资源不存在</td>
 *     <td>请求的资源不存在</td>
 *   </tr>
 *   <tr>
 *     <td>500</td>
 *     <td>服务器错误</td>
 *     <td>服务器内部错误</td>
 *   </tr>
 * </table>
 *
 * <h3>设计优势:</h3>
 * <ul>
 *   <li>统一性: 所有 API 响应格式一致,前端处理简单</li>
 *   <li>可扩展性: 泛型设计支持任意业务数据类型</li>
 *   <li>类型安全: 编译时类型检查,减少运行时错误</li>
 *   <li>灵活性: 状态码类型可以是 Integer、String 等</li>
 * </ul>
 *
 * <h3>与 R 类的关系:</h3>
 * <p>{@link com.basetc.base.common.response.R} 是此接口的默认实现,提供了便捷的静态方法来创建响应对象。
 *
 * @param <C> 响应状态码的类型
 * @param <T> 实际业务数据的类型
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see com.basetc.base.common.response.R
 */
public interface TcResponse<C, T> {

    /**
     * 获取响应状态码.
     *
     * @return 响应状态码
     */
    C getCode();

    /**
     * 获取响应消息.
     *
     * @return 响应消息
     */
    String getMsg();

    /**
     * 获取响应数据.
     *
     * @return 响应数据
     */
    @SuppressWarnings("unused")
    T getData();
}

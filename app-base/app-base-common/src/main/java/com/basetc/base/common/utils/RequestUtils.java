package com.basetc.base.common.utils;

import cn.hutool.core.convert.Convert;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求工具类,提供获取当前请求上下文相关信息的便捷方法.
 *
 * <p>此类封装了Spring MVC框架中 {@link RequestContextHolder} 的常用操作,包括:
 * <ul>
 *   <li>获取当前线程的 HttpServletRequest、HttpServletResponse、HttpSession 对象</li>
 *   <li>便捷获取请求参数(支持类型转换和默认值)</li>
 *   <li>获取所有请求参数和请求头</li>
 *   <li>URL编码/解码操作</li>
 * </ul>
 *
 * <h3>使用场景:</h3>
 * <p>此类主要用于在非Controller层的代码(如Service、Utils、拦截器等)中访问当前HTTP请求的信息。
 * 在Controller中可以直接使用方法参数获取,而在其他层次需要通过此类获取。
 *
 * <h3>使用示例:</h3>
 * <h4>1. 在Service层获取请求参数:</h4>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     public void login() {
 *         // 获取请求参数
 *         String username = RequestUtils.getParameter("username");
 *         Integer age = RequestUtils.getParameterToInt("age", 0);
 *         Boolean remember = RequestUtils.getParameterToBool("remember", false);
 *
 *         // 获取客户端IP
 *         String ip = IpUtils.getIpAddr(RequestUtils.getRequest());
 *     }
 * }
 * }</pre>
 *
 * <h4>2. 在拦截器中获取请求信息:</h4>
 * <pre>{@code
 * public class LoginInterceptor implements HandlerInterceptor {
 *     @Override
 *     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
 *         // 获取Token
 *         String token = RequestUtils.getHeader("Authorization");
 *
 *         // 获取所有请求参数
 *         Map<String, String> params = RequestUtils.getParamMap(request);
 *
 *         // 获取Session
 *         HttpSession session = RequestUtils.getSession();
 *         return true;
 *     }
 * }
 * }</pre>
 *
 * <h4>3. URL编码/解码:</h4>
 * <pre>{@code
 * // URL编码
 * String encoded = RequestUtils.urlEncode("测试中文");
 * // 返回: "%E6%B5%8B%E8%AF%95%E4%B8%AD%E6%96%87"
 *
 * // URL解码
 * String decoded = RequestUtils.urlDecode("%E6%B5%8B%E8%AF%95%E4%B8%AD%E6%96%87");
 * // 返回: "测试中文"
 * }</pre>
 *
 * <h4>4. 获取请求头:</h4>
 * <pre>{@code
 * // 获取单个请求头
 * String userAgent = RequestUtils.getHeader("User-Agent");
 *
 * // 获取所有请求头
 * Map<String, String> headers = RequestUtils.getHeaders(RequestUtils.getRequest());
 * headers.forEach((name, value) -> log.info("Header: {} = {}", name, value));
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>此类只能在Web请求的线程中使用,依赖于Spring的 RequestContextHolder</li>
 *   <li>在非Web环境或异步线程中调用会抛出 IllegalStateException</li>
 *   <li>获取参数的方法已经进行了类型转换,无需手动转换</li>
 *   <li>所有方法都是静态方法,直接通过类名调用</li>
 *   <li>返回的参数Map是不可修改的,防止意外修改</li>
 * </ul>
 *
 * <h3>类型转换说明:</h3>
 * <p>参数类型转换使用 Hutool 的 Convert 工具类,支持以下类型转换:
 * <ul>
 *   <li>String: 直接获取或使用默认值</li>
 *   <li>Integer: 自动转换,转换失败返回默认值</li>
 *   <li>Boolean: 支持 "true"/"false"、"1"/"0"、"yes"/"no" 等格式</li>
 *   <li>其他类型可通过 Convert.toXxx() 方法扩展</li>
 * </ul>
 *
 * @author ruoyi
 * @since 1.0.0
 * @see RequestContextHolder
 * @see ServletRequestAttributes
 * @see cn.hutool.core.convert.Convert
 */
@UtilityClass
public class RequestUtils {

  /**
   * 获取String类型的请求参数.
   *
   * <p>从当前请求中获取指定名称的参数值,如果参数不存在则返回null.
   *
   * @param name 参数名称
   * @return 参数值,如果参数不存在则返回null
   * @see HttpServletRequest#getParameter(String)
   */
  public static String getParameter(String name) {
    return getRequest().getParameter(name);
  }

  /**
   * 获取String类型的请求参数,支持默认值.
   *
   * <p>从当前请求中获取指定名称的参数值,如果参数不存在或为空字符串,则返回指定的默认值.
   *
   * @param name 参数名称
   * @param defaultValue 默认值
   * @return 参数值,如果参数不存在或为空则返回默认值
   */
  public static String getParameter(String name, String defaultValue) {
    return Convert.toStr(getRequest().getParameter(name), defaultValue);
  }

  /**
   * 获取Integer类型的请求参数.
   *
   * <p>从当前请求中获取指定名称的参数值并转换为Integer类型.
   * 如果参数不存在或转换失败,则返回null.
   *
   * @param name 参数名称
   * @return Integer类型的参数值,如果转换失败则返回null
   */
  public static Integer getParameterToInt(String name) {
    return Convert.toInt(getRequest().getParameter(name));
  }

  /**
   * 获取Integer类型的请求参数,支持默认值.
   *
   * <p>从当前请求中获取指定名称的参数值并转换为Integer类型.
   * 如果参数不存在、为空或转换失败,则返回指定的默认值.
   *
   * @param name 参数名称
   * @param defaultValue 默认值
   * @return Integer类型的参数值,如果转换失败则返回默认值
   */
  public static Integer getParameterToInt(String name, Integer defaultValue) {
    return Convert.toInt(getRequest().getParameter(name), defaultValue);
  }

  /**
   * 获取Boolean类型的请求参数.
   *
   * <p>从当前请求中获取指定名称的参数值并转换为Boolean类型.
   * 支持的布尔值格式包括: "true"/"false"、"1"/"0"、"yes"/"no"、"y"/"n"等.
   * 如果参数不存在或转换失败,则返回null.
   *
   * @param name 参数名称
   * @return Boolean类型的参数值,如果转换失败则返回null
   */
  public static Boolean getParameterToBool(String name) {
    return Convert.toBool(getRequest().getParameter(name));
  }

  /**
   * 获取Boolean类型的请求参数,支持默认值.
   *
   * <p>从当前请求中获取指定名称的参数值并转换为Boolean类型.
   * 支持的布尔值格式包括: "true"/"false"、"1"/"0"、"yes"/"no"、"y"/"n"等.
   * 如果参数不存在、为空或转换失败,则返回指定的默认值.
   *
   * @param name 参数名称
   * @param defaultValue 默认值
   * @return Boolean类型的参数值,如果转换失败则返回默认值
   */
  public static Boolean getParameterToBool(String name, Boolean defaultValue) {
    return Convert.toBool(getRequest().getParameter(name), defaultValue);
  }

  /**
   * 获取请求中的所有参数.
   *
   * <p>返回请求中所有的参数Map,其中key为参数名,value为参数值数组.
   * <p>注意: 返回的Map是不可修改的,任何修改操作都会抛出UnsupportedOperationException.
   *
   * @param request 请求对象{@link ServletRequest}
   * @return 参数Map,key为参数名,value为参数值数组(不可修改)
   */
  public static Map<String, String[]> getParams(ServletRequest request) {
    final Map<String, String[]> map = request.getParameterMap();
    return Collections.unmodifiableMap(map);
  }

  /**
   * 获取请求中的所有参数,并将多值参数合并为字符串.
   *
   * <p>返回请求中所有的参数Map,其中key为参数名,value为参数值字符串.
   * <p>对于多值参数(如多选框),会使用逗号连接所有值.
   * <p>例如: {@code hobby=reading&hobby=gaming} 会转换为 {@code hobby="reading,gaming"}
   *
   * @param request 请求对象{@link ServletRequest}
   * @return 参数Map,key为参数名,value为逗号连接的参数值字符串
   */
  public static Map<String, String> getParamMap(ServletRequest request) {
    Map<String, String> params = new HashMap<>();
    for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
      params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
    }
    return params;
  }

  /**
   * 获取当前线程的HttpServletRequest对象.
   *
   * <p>从Spring的RequestContextHolder中获取当前请求的HttpServletRequest对象.
   * <p>注意: 此方法只能在Web请求的线程中调用,否则会抛出IllegalStateException.
   *
   * @return HttpServletRequest对象
   * @throws IllegalStateException 如果在非Web环境或异步线程中调用
   * @see RequestContextHolder#getRequestAttributes()
   */
  public static HttpServletRequest getRequest() {
    return getRequestAttributes().getRequest();
  }

  /**
   * 获取当前线程的HttpServletResponse对象.
   *
   * <p>从Spring的RequestContextHolder中获取当前请求的HttpServletResponse对象.
   * <p>注意: 此方法只能在Web请求的线程中调用,否则会抛出IllegalStateException.
   *
   * @return HttpServletResponse对象
   * @throws IllegalStateException 如果在非Web环境或异步线程中调用
   * @see RequestContextHolder#getRequestAttributes()
   */
  public static HttpServletResponse getResponse() {
    return getRequestAttributes().getResponse();
  }

  /**
   * 获取当前线程的HttpSession对象.
   *
   * <p>从当前请求中获取HttpSession对象,如果session不存在则会创建新session.
   * <p>等同于调用 {@code getRequest().getSession(true)}.
   *
   * @return HttpSession对象
   * @see HttpServletRequest#getSession()
   */
  public static HttpSession getSession() {
    return getRequest().getSession();
  }

  /**
   * 获取当前线程的ServletRequestAttributes对象.
   *
   * <p>从Spring的RequestContextHolder中获取当前请求的ServletRequestAttributes对象.
   * <p>注意: 此方法只能在Web请求的线程中调用,否则会抛出IllegalStateException.
   *
   * @return ServletRequestAttributes对象
   * @throws IllegalStateException 如果在非Web环境或异步线程中调用
   * @see RequestContextHolder#getRequestAttributes()
   */
  public static ServletRequestAttributes getRequestAttributes() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    return (ServletRequestAttributes) attributes;
  }

  /**
   * 对URL进行UTF-8编码.
   *
   * <p>使用UTF-8字符集对字符串进行URL编码,用于将特殊字符转换为URL安全格式.
   * <p>常用于编码URL参数值、表单数据等.
   *
   * @param str 需要编码的字符串
   * @return URL编码后的字符串
   * @see URLEncoder#encode(String, java.nio.charset.Charset)
   */
  public static String urlEncode(String str) {
    return URLEncoder.encode(str, StandardCharsets.UTF_8);
  }

  /**
   * 对URL进行UTF-8解码.
   *
   * <p>使用UTF-8字符集对URL编码的字符串进行解码,还原为原始字符串.
   * <p>常用于解析URL参数值、表单数据等.
   *
   * @param str 需要解码的字符串
   * @return URL解码后的字符串
   * @see URLDecoder#decode(String, java.nio.charset.Charset)
   */
  public static String urlDecode(String str) {
    return URLDecoder.decode(str, StandardCharsets.UTF_8);
  }

  /**
   * 获取请求中的所有请求头.
   *
   * <p>返回请求中所有的HTTP头信息,包括Content-Type、User-Agent、Authorization等.
   * <p>返回的Map是可修改的,可以用于日志记录、调试等场景.
   *
   * @param request 请求对象
   * @return 请求头Map,key为请求头名称,value为请求头值
   */
  public static Map<String, String> getHeaders(HttpServletRequest request) {
    Map<String, String> map = new HashMap<>();
    final Enumeration<String> names = request.getHeaderNames();
    while (names.hasMoreElements()) {
      final String name = names.nextElement();
      map.put(name, request.getHeader(name));
    }
    return map;
  }

  /**
   * 获取指定名称的请求头值.
   *
   * <p>从当前请求中获取指定名称的HTTP头信息.
   * <p>如果请求头不存在,则返回null.
   *
   * @param name 请求头名称(不区分大小写,但建议使用标准格式,如"User-Agent")
   * @return 请求头值,如果不存在则返回null
   * @see HttpServletRequest#getHeader(String)
   */
  public static String getHeader(String name) {
    return getRequest().getHeader(name);
  }
}

package com.basetc.base.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * IP地址工具类,提供IP地址的获取、验证、转换等功能.
 *
 * <p>此类封装了IP地址相关的常用操作,包括:
 * <ul>
 *   <li>从HTTP请求中获取真实客户端IP地址(支持多级反向代理)</li>
 *   <li>验证IP地址格式和网段范围</li>
 *   <li>判断是否为内网IP地址</li>
 *   <li>支持IP通配符匹配和网段匹配</li>
 *   <li>获取本地主机IP和主机名</li>
 * </ul>
 *
 * <h3>客户端IP获取策略:</h3>
 * <p>通过 {@link #getIpAddr(HttpServletRequest)} 方法获取客户端IP时,会按以下优先级检查请求头:
 * <pre>
 * 1. X-Forwarded-For - 识别通过HTTP代理或负载均衡器转发的原始客户端IP
 * 2. Proxy-Client-IP - 识别通过Squid等代理服务器的客户端IP
 * 3. WL-Proxy-Client-IP - 识别通过WebLogic代理的客户端IP
 * 4. X-Real-IP - Nginx代理设置的客户端真实IP
 * 5. RemoteAddr - 直接连接的客户端IP(无代理时使用)
 * </pre>
 *
 * <h3>使用示例:</h3>
 * <h4>1. 获取客户端IP地址:</h4>
 * <pre>{@code
 * // 在Controller中获取客户端IP
 * @GetMapping("/api/user/info")
 * public R<UserInfo> getUserInfo(HttpServletRequest request) {
 *     String clientIp = IpUtils.getIpAddr(request);
 *     log.info("客户端IP: {}", clientIp);
 *     // ...
 * }
 *
 * // 在任意位置获取(使用RequestUtils)
 * String clientIp = IpUtils.getIpAddr();
 * }</pre>
 *
 * <h4>2. 验证IP地址:</h4>
 * <pre>{@code
 * String ip = "192.168.1.100";
 *
 * // 验证是否为有效IP
 * boolean isValid = IpUtils.isIp(ip);
 * // 返回: true
 *
 * // 验证是否为内网IP
 * boolean isInternal = IpUtils.internalIp(ip);
 * // 返回: true
 * }</pre>
 *
 * <h4>3. IP白名单过滤:</h4>
 * <pre>{@code
 * // 支持单个IP、通配符IP、IP网段
 * String filter = "192.168.1.*;10.10.10.1-10.10.10.100;172.16.0.5";
 * String clientIp = IpUtils.getIpAddr(request);
 *
 * if (IpUtils.isMatchedIp(filter, clientIp)) {
 *     // IP在白名单中,允许访问
 *     return R.success("访问允许");
 * } else {
 *     // IP不在白名单中,拒绝访问
 *     return R.error("访问被拒绝");
 * }
 * }</pre>
 *
 * <h4>4. IP网段判断:</h4>
 * <pre>{@code
 * // 判断IP是否在指定网段
 * String ipArea = "192.168.1.1-192.168.1.100";
 * String targetIp = "192.168.1.50";
 *
 * boolean inRange = IpUtils.ipIsInNetNoCheck(ipArea, targetIp);
 * // 返回: true
 * }</pre>
 *
 * <h4>5. 获取本地主机信息:</h4>
 * <pre>{@code
 * // 获取本地IP地址
 * String localIp = IpUtils.getHostIp();
 * // 返回: "192.168.1.100"
 *
 * // 获取本地主机名
 * String hostName = IpUtils.getHostName();
 * // 返回: "my-server"
 * }</pre>
 *
 * <h3>内网IP范围:</h3>
 * <p>以下IP范围会被识别为内网IP:
 * <ul>
 *   <li>10.0.0.0 - 10.255.255.255 (10.0.0.0/8)</li>
 *   <li>172.16.0.0 - 172.31.255.255 (172.16.0.0/12)</li>
 *   <li>192.168.0.0 - 192.168.255.255 (192.168.0.0/16)</li>
 *   <li>127.0.0.1 (本地回环地址)</li>
 * </ul>
 *
 * <h3>多级反向代理处理:</h3>
 * <p>当应用部署在多级反向代理后,X-Forwarded-For 请求头可能包含多个IP地址,
 * 格式为: {@code 客户端IP, 代理1IP, 代理2IP, ...}
 * <p>此工具会自动提取第一个非unknown的有效IP地址作为真实客户端IP.
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>X-Forwarded-For 请求头可以被伪造,安全场景需谨慎使用</li>
 *   <li>IPv6地址会转换为IPv4本地回环地址(127.0.0.1)</li>
 *   <li>IP地址长度限制为255字符</li>
 *   <li>建议在Nginx等反向代理中配置真实IP传递</li>
 * </ul>
 *
 * <h3>Nginx配置示例:</h3>
 * <pre>{@code
 * # Nginx配置传递真实IP
 * location / {
 *     proxy_set_header X-Real-IP $remote_addr;
 *     proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 *     proxy_set_header Host $host;
 *     proxy_pass http://backend;
 * }
 * }</pre>
 *
 * @author ruoyi
 * @since 1.0.0
 * @see RequestUtils
 */
@SuppressWarnings("all")
@UtilityClass
public class IpUtils {

  public static final String REGX_0_255 = "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
  // 匹配 ip
  public static final String REGX_IP = "((" + REGX_0_255 + "\\.){3}" + REGX_0_255 + ")";
  // 匹配网段
  public static final String REGX_IP_SEG = "(" + REGX_IP + "\\-" + REGX_IP + ")";
  public static final String REGX_IP_WILDCARD =
      "(((\\*\\.){3}\\*)|("
          + REGX_0_255
          + "(\\.\\*){3})|("
          + REGX_0_255
          + "\\."
          + REGX_0_255
          + ")(\\.\\*){2}"
          + "|(("
          + REGX_0_255
          + "\\.){3}\\*))";

  /**
   * 获取客户端IP.
   *
   * @return IP地址
   */
  public static String getIpAddr() {
    return getIpAddr(RequestUtils.getRequest());
  }

  /**
   * 获取客户端IP.
   *
   * @param request 请求对象
   * @return IP地址
   */
  public static String getIpAddr(HttpServletRequest request) {
    if (request == null) {
      return "unknown";
    }
    String ip = request.getHeader("x-forwarded-for");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Forwarded-For");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }

    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
  }

  /**
   * 检查是否为内部IP地址.
   *
   * @param ip IP地址
   * @return 结果
   */
  public static boolean internalIp(String ip) {
    byte[] addr = textToNumericFormatV4(ip);
    return internalIp(addr) || "127.0.0.1".equals(ip);
  }

  /**
   * 检查是否为内部IP地址.
   *
   * @param addr byte地址
   * @return 结果
   */
  private static boolean internalIp(byte[] addr) {
    if (Objects.isNull(addr) || addr.length < 2) {
      return true;
    }
    final byte b0 = addr[0];
    final byte b1 = addr[1];
    // 10.x.x.x/8
    final byte section1 = 0x0A;
    // 172.16.x.x/12
    final byte section2 = (byte) 0xAC;
    final byte section3 = (byte) 0x10;
    final byte section4 = (byte) 0x1F;
    // 192.168.x.x/16
    final byte section5 = (byte) 0xC0;
    final byte section6 = (byte) 0xA8;
    switch (b0) {
      case section1:
        return true;
      case section2:
        if (b1 >= section3 && b1 <= section4) {
          return true;
        }
        break;
      case section5:
        switch (b1) {
          case section6:
            return true;
          default:
            break;
        }
        break;
      default:
        break;
    }
    return false;
  }

  /**
   * 将IPv4地址转换成字节.
   *
   * @param text IPv4地址
   * @return byte 字节
   */
  public static byte[] textToNumericFormatV4(String text) {
    if (text.length() == 0) {
      return null;
    }

    byte[] bytes = new byte[4];
    String[] elements = text.split("\\.", -1);
    try {
      long l;
      int i;
      switch (elements.length) {
        case 1:
          l = Long.parseLong(elements[0]);
          if ((l < 0L) || (l > 4294967295L)) {
            return null;
          }
          bytes[0] = (byte) (int) (l >> 24 & 0xFF);
          bytes[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
          bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
          bytes[3] = (byte) (int) (l & 0xFF);
          break;
        case 2:
          l = Integer.parseInt(elements[0]);
          if ((l < 0L) || (l > 255L)) {
            return null;
          }
          bytes[0] = (byte) (int) (l & 0xFF);
          l = Integer.parseInt(elements[1]);
          if ((l < 0L) || (l > 16777215L)) {
            return null;
          }
          bytes[1] = (byte) (int) (l >> 16 & 0xFF);
          bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
          bytes[3] = (byte) (int) (l & 0xFF);
          break;
        case 3:
          for (i = 0; i < 2; ++i) {
            l = Integer.parseInt(elements[i]);
            if ((l < 0L) || (l > 255L)) {
              return null;
            }
            bytes[i] = (byte) (int) (l & 0xFF);
          }
          l = Integer.parseInt(elements[2]);
          if ((l < 0L) || (l > 65535L)) {
            return null;
          }
          bytes[2] = (byte) (int) (l >> 8 & 0xFF);
          bytes[3] = (byte) (int) (l & 0xFF);
          break;
        case 4:
          for (i = 0; i < 4; ++i) {
            l = Integer.parseInt(elements[i]);
            if ((l < 0L) || (l > 255L)) {
              return null;
            }
            bytes[i] = (byte) (int) (l & 0xFF);
          }
          break;
        default:
          return null;
      }
    } catch (NumberFormatException e) {
      return null;
    }
    return bytes;
  }

  /**
   * 获取IP地址.
   *
   * @return 本地IP地址
   */
  public static String getHostIp() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      // 记录获取IP地址失败的异常
      e.printStackTrace();
    }
    return "127.0.0.1";
  }

  /**
   * 获取主机名.
   *
   * @return 本地主机名
   */
  public static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      // 记录获取主机名失败的异常
      e.printStackTrace();
    }
    return "未知";
  }

  /**
   * 从多级反向代理中获得第一个非unknown IP地址.
   *
   * @param ip 获得的IP地址
   * @return 第一个非unknown IP地址
   */
  public static String getMultistageReverseProxyIp(String ip) {
    // 多级反向代理检测
    if (ip != null && ip.indexOf(",") > 0) {
      final String[] ips = ip.trim().split(",");
      for (String subIp : ips) {
        if (false == isUnknown(subIp)) {
          ip = subIp;
          break;
        }
      }
    }
    return StringUtils.substring(ip, 0, 255);
  }

  /**
   * 检测给定字符串是否为未知，多用于检测HTTP请求相关.
   *
   * @param checkString 被检测的字符串
   * @return 是否未知
   */
  public static boolean isUnknown(String checkString) {
    return StringUtils.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
  }

  /**
   * 是否为IP.
   *
   * @param ip IP地址
   * @return boolean 结果
   */
  public static boolean isIp(String ip) {
    return StringUtils.isNotBlank(ip) && ip.matches(REGX_IP);
  }

  /**
   * 是否为IP，或 *为间隔的通配符地址.
   *
   * @param ip IP地址
   * @return boolean 结果
   */
  public static boolean isIpWildCard(String ip) {
    return StringUtils.isNotBlank(ip) && ip.matches(REGX_IP_WILDCARD);
  }

  /**
   * 检测参数是否在ip通配符里.
   *
   * @param ipWildCard 通配符IP
   * @param ip IP地址
   * @return boolean 结果
   */
  public static boolean ipIsInWildCardNoCheck(String ipWildCard, String ip) {
    String[] s1 = ipWildCard.split("\\.");
    String[] s2 = ip.split("\\.");
    boolean isMatchedSeg = true;
    for (int i = 0; i < s1.length && !s1[i].equals("*"); i++) {
      if (!s1[i].equals(s2[i])) {
        isMatchedSeg = false;
        break;
      }
    }
    return isMatchedSeg;
  }

  /**
   * 是否为特定格式如:“10.10.10.1-10.10.10.99”的ip段字符串.
   *
   * @param ipSeg IP段
   * @return boolean 结果
   */
  public static boolean isIpSegment(String ipSeg) {
    return StringUtils.isNotBlank(ipSeg) && ipSeg.matches(REGX_IP_SEG);
  }

  /**
   * 判断ip是否在指定网段中.
   *
   * @param iparea 网段范围
   * @param ip IP地址
   * @return boolean 结果
   */
  public static boolean ipIsInNetNoCheck(String iparea, String ip) {
    int idx = iparea.indexOf('-');
    String[] sips = iparea.substring(0, idx).split("\\.");
    String[] sipe = iparea.substring(idx + 1).split("\\.");
    String[] sipt = ip.split("\\.");
    long ips = 0L;
    long ipe = 0L;
    long ipt = 0L;
    for (int i = 0; i < 4; ++i) {
      ips = ips << 8 | Integer.parseInt(sips[i]);
      ipe = ipe << 8 | Integer.parseInt(sipe[i]);
      ipt = ipt << 8 | Integer.parseInt(sipt[i]);
    }
    if (ips > ipe) {
      long t = ips;
      ips = ipe;
      ipe = t;
    }
    return ips <= ipt && ipt <= ipe;
  }

  /**
   * 校验ip是否符合过滤串规则.
   *
   * @param filter 过滤IP列表,支持后缀'*'通配,支持网段如:`10.10.10.1-10.10.10.99`
   * @param ip 校验IP地址
   * @return boolean 结果
   */
  public static boolean isMatchedIp(String filter, String ip) {
    if (StringUtils.isEmpty(filter) || StringUtils.isEmpty(ip)) {
      return false;
    }
    String[] ips = filter.split(";");
    for (String ipStr : ips) {
      if (isIp(ipStr) && ipStr.equals(ip)) {
        return true;
      } else if (isIpWildCard(ipStr) && ipIsInWildCardNoCheck(ipStr, ip)) {
        return true;
      } else if (isIpSegment(ipStr) && ipIsInNetNoCheck(ipStr, ip)) {
        return true;
      }
    }
    return false;
  }
}

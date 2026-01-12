package com.basetc.base.common.utils;

import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间计算工具类,提供便捷的日期加减运算方法.
 *
 * <p>此类封装了Java {@link Calendar} 的日期时间操作,提供了简洁的API来执行日期的加减运算。
 * 所有方法都是线程安全的,且不会修改原始日期对象,而是返回新的日期对象.
 *
 * <h3>主要功能:</h3>
 * <ul>
 *   <li>日期加减: 支持天、小时、分钟、秒的加减运算</li>
 *   <li>空值处理: 如果输入日期为null,自动使用当前时间</li>
 *   <li>不可变性: 所有操作返回新的Date对象,不修改原对象</li>
 *   <li>静态工具类: 使用@UtilityClass注解,所有方法都是静态的</li>
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <h4>1. 日期加减运算:</h4>
 * <pre>{@code
 * // 获取当前时间
 * Date now = new Date();
 *
 * // 加7天
 * Date nextWeek = DateAppendUtils.addDays(now, 7);
 *
 * // 减3天
 * Date threeDaysAgo = DateAppendUtils.addDays(now, -3);
 *
 * // 加2小时
 * Date twoHoursLater = DateAppendUtils.addHours(now, 2);
 *
 * // 加30分钟
 * Date thirtyMinutesLater = DateAppendUtils.addMinutes(now, 30);
 *
 * // 加60秒
 * Date oneMinuteLater = DateAppendUtils.addSeconds(now, 60);
 * }</pre>
 *
 * <h4>2. 计算过期时间:</h4>
 * <pre>{@code
 * // Token过期时间: 当前时间 + 7天
 * Date expireTime = DateAppendUtils.addDays(new Date(), 7);
 *
 * // 缓存过期时间: 当前时间 + 30分钟
 * Date cacheExpireTime = DateAppendUtils.addMinutes(new Date(), 30);
 *
 * // 验证码过期时间: 当前时间 + 5分钟
 * Date codeExpireTime = DateAppendUtils.addMinutes(new Date(), 5);
 * }</pre>
 *
 * <h4>3. 时间范围计算:</h4>
 * <pre>{@code
 * // 计算今天开始和结束时间
 * Date now = new Date();
 * Date startOfDay = DateAppendUtils.addDays(now, 0); // 可结合其他工具类使用
 * Date endOfDay = DateAppendUtils.addDays(now, 1);
 * }</pre>
 *
 * <h4>4. 空值处理示例:</h4>
 * <pre>{@code
 * // 如果date为null,自动使用当前时间
 * Date nullDate = null;
 * Date result = DateAppendUtils.addDays(nullDate, 1);
 * // result为当前时间 + 1天
 * }</pre>
 *
 * <h3>典型应用场景:</h3>
 * <ul>
 *   <li><b>Token/Session管理:</b> 计算过期时间,如7天、30天等</li>
 *   <li><b>缓存时效控制:</b> 设置缓存的过期时间,如30分钟、1小时等</li>
 *   <li><b>验证码有效期:</b> 设置短信验证码、邮箱验证码的过期时间,如5分钟</li>
 *   <li><b>订单超时:</b> 计算订单的自动关闭时间,如30分钟</li>
 *   <li><b>定时任务:</b> 计算下次执行时间,如每天、每周等</li>
 *   <li><b>数据统计:</b> 计算时间范围,如昨天、上周、上个月等</li>
 * </ul>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>所有方法都不会修改原始Date对象,而是返回新的Date对象</li>
 *   <li>如果传入的date为null,会自动使用当前时间作为基准</li>
 *   <li>负数表示减去指定的时间,正数表示增加指定的时间</li>
 *   <li>此类只提供常用的时间单位运算,更复杂的运算建议使用Java 8的 LocalDateTime</li>
 *   <li>时区问题: 使用系统默认时区,如需特定时区请使用其他工具类</li>
 * </ul>
 *
 * <h3>性能考虑:</h3>
 * <p>每次调用都会创建新的Calendar对象,在循环或高频调用场景下,
 * 建议使用Java 8的 {@link java.time.LocalDateTime} 或 {@link java.time.Instant},
 * 它们的性能更好且API更加现代化.
 *
 * <h3>替代方案:</h3>
 * <p>对于新项目,建议使用Java 8+的日期时间API:
 * <pre>{@code
 * // Java 8+ 风格 (推荐用于新项目)
 * LocalDateTime now = LocalDateTime.now();
 * LocalDateTime nextWeek = now.plusWeeks(1);
 * LocalDateTime nextDay = now.plusDays(1);
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see Calendar
 * @see Date
 * @see java.time.LocalDateTime
 */
@SuppressWarnings("unused")
@UtilityClass
public class DateAppendUtils {
  /**
   * 给指定日期增加指定的时间量(核心方法).
   *
   * <p>这是所有公共方法的基础实现,通过Calendar进行日期时间的加减运算.
   * <p>如果传入的date为null,会自动使用当前时间作为基准时间.
   *
   * @param date 原始日期,如果为null则使用当前时间
   * @param amount 要增加的数量,正数表示增加,负数表示减少
   * @param calendarField 日历字段,如 {@link Calendar#DAY_OF_MONTH}, {@link Calendar#HOUR_OF_DAY} 等
   * @return 增加时间后的新日期对象
   */
  @SuppressWarnings("all")
  private static Date addTime(Date date, int amount, int calendarField) {
    if (date == null) {
      date = new Date();
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(calendarField, amount);
    return calendar.getTime();
  }

  /**
   * 给指定日期增加指定的天数.
   *
   * <p>对给定日期增加(正数)或减少(负数)指定的天数.
   * <p>如果date为null,则使用当前时间作为基准.
   *
   * <h3>使用示例:</h3>
   * <pre>{@code
   * // 获取明天的日期
   * Date tomorrow = DateAppendUtils.addDays(new Date(), 1);
   *
   * // 获取昨天的日期
   * Date yesterday = DateAppendUtils.addDays(new Date(), -1);
   *
   * // 获取7天后的日期
   * Date nextWeek = DateAppendUtils.addDays(new Date(), 7);
   *
   * // 获取30天前的日期
   * Date lastMonth = DateAppendUtils.addDays(new Date(), -30);
   * }</pre>
   *
   * @param date 原始日期,如果为null则使用当前时间
   * @param days 要增加的天数,正数表示增加,负数表示减少
   * @return 增加天数后的新日期对象
   */
  public static Date addDays(Date date, int days) {
    return addTime(date, days, Calendar.DAY_OF_MONTH);
  }

  /**
   * 给指定日期增加指定的小时数.
   *
   * <p>对给定日期增加(正数)或减少(负数)指定的小时数.
   * <p>如果date为null,则使用当前时间作为基准.
   *
   * <h3>使用示例:</h3>
   * <pre>{@code
   * // 获取2小时后的时间
   * Date twoHoursLater = DateAppendUtils.addHours(new Date(), 2);
   *
   * // 获取12小时前的时间
   * Date halfDayAgo = DateAppendUtils.addHours(new Date(), -12);
   *
   * // 计算Token过期时间: 当前时间 + 24小时
   * Date expireTime = DateAppendUtils.addHours(new Date(), 24);
   * }</pre>
   *
   * @param date 原始日期,如果为null则使用当前时间
   * @param hours 要增加的小时数,正数表示增加,负数表示减少
   * @return 增加小时数后的新日期对象
   */
  public static Date addHours(Date date, int hours) {
    return addTime(date, hours, Calendar.HOUR_OF_DAY);
  }

  /**
   * 给指定日期增加指定的分钟数.
   *
   * <p>对给定日期增加(正数)或减少(负数)指定的分钟数.
   * <p>如果date为null,则使用当前时间作为基准.
   *
   * <h3>使用示例:</h3>
   * <pre>{@code
   * // 获取30分钟后的时间
   * Date thirtyMinutesLater = DateAppendUtils.addMinutes(new Date(), 30);
   *
   * // 获取15分钟前的时间
   * Date quarterAgo = DateAppendUtils.addMinutes(new Date(), -15);
   *
   * // 计算验证码过期时间: 当前时间 + 5分钟
   * Date codeExpireTime = DateAppendUtils.addMinutes(new Date(), 5);
   *
   * // 计算订单超时时间: 当前时间 + 30分钟
   * Date orderTimeout = DateAppendUtils.addMinutes(new Date(), 30);
   * }</pre>
   *
   * @param date 原始日期,如果为null则使用当前时间
   * @param minutes 要增加的分钟数,正数表示增加,负数表示减少
   * @return 增加分钟数后的新日期对象
   */
  public static Date addMinutes(Date date, int minutes) {
    return addTime(date, minutes, Calendar.MINUTE);
  }

  /**
   * 给指定日期增加指定的秒数.
   *
   * <p>对给定日期增加(正数)或减少(负数)指定的秒数.
   * <p>如果date为null,则使用当前时间作为基准.
   *
   * <h3>使用示例:</h3>
   * <pre>{@code
   * // 获取60秒后的时间
   * Date oneMinuteLater = DateAppendUtils.addSeconds(new Date(), 60);
   *
   * // 获取30秒前的时间
   * Date halfMinuteAgo = DateAppendUtils.addSeconds(new Date(), -30);
   *
   * // 计算短期缓存过期时间: 当前时间 + 120秒
   * Date cacheExpire = DateAppendUtils.addSeconds(new Date(), 120);
   * }</pre>
   *
   * @param date 原始日期,如果为null则使用当前时间
   * @param seconds 要增加的秒数,正数表示增加,负数表示减少
   * @return 增加秒数后的新日期对象
   */
  public static Date addSeconds(Date date, int seconds) {
    return addTime(date, seconds, Calendar.SECOND);
  }
}

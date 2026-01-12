package com.basetc.base.common;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * BaseTC应用启动类，是BaseTC框架的入口点。
 *
 * <h3>架构设计</h3>
 * <p>此类是BaseTC框架的核心启动类，继承自{@link BasetcProfile}，提供了以下核心功能：</p>
 * <ul>
 *   <li><strong>应用启动</strong>: 封装Spring Boot应用启动过程，提供统一的启动入口</li>
 *   <li><strong>环境管理</strong>: 集成{@link BasetcProfile}的环境配置管理能力</li>
 *   <li><strong>组件初始化</strong>: 启动后自动初始化框架组件，如枚举字典扫描器</li>
 *   <li><strong>上下文管理</strong>: 提供全局访问Spring应用上下文的能力</li>
 * </ul>
 *
 * <h3>核心价值</h3>
 * <p>使用{@code BasetcApplication.run()}替代标准的{@code SpringApplication.run()}，可以获得：</p>
 * <ul>
 *   <li>自动加载BaseTC框架的所有自动配置</li>
 *   <li>初始化枚举字典扫描器，自动注册{@code @DictType}注解的枚举</li>
 *   <li>提供全局应用上下文访问能力</li>
 *   <li>标准化应用启动流程，确保框架组件正确初始化</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @SpringBootApplication
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         // 使用BaseTC应用启动类启动应用
 *         BasetcApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>扩展能力</h3>
 * <p>子类可以重写run方法来扩展启动逻辑：</p>
 * <pre>{@code
 * public class CustomApplication extends BasetcApplication {
 *     public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
 *         ConfigurableApplicationContext context = super.run(primarySource, args);
 *         // 自定义启动后的初始化逻辑
 *         initCustomComponents(context);
 *         return context;
 *     }
 *     
 *     private static void initCustomComponents(ConfigurableApplicationContext context) {
 *         // 初始化自定义组件
 *     }
 * }
 * }</pre>
 *
 * @see BasetcProfile 环境配置管理类
 * @see com.basetc.base.common.utils.DictBaseEnumScanner 枚举字典扫描器
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public class BasetcApplication extends BasetcProfile {

    /**
     * Spring应用上下文.
     * <p>存储当前运行的Spring Boot应用上下文实例,可通过此类访问ApplicationContext.
     */
    public static ConfigurableApplicationContext CONTEXT = null;

    /**
     * 启动Spring Boot应用程序.
     *
     * <p>此方法会启动Spring Boot应用,并在启动完成后自动初始化枚举字典扫描器.
     * 枚举字典扫描器会扫描所有带有{@link com.basetc.base.common.enums.BaseEnum}注解的枚举类型,
     * 并将其注册为字典数据,供前端使用.
     *
     * @param applicationClass 应用程序主类的Class对象,必须标注有@SpringBootApplication注解
     * @param args             命令行参数,通常直接传递自main方法
     * @throws IllegalArgumentException 如果applicationClass为null
     * @see SpringApplication#run(Class, String...)
     */
    public static void run(Class<?> applicationClass, String... args) {
        if (applicationClass == null) {
            throw new IllegalArgumentException("applicationClass cannot be null");
        }
        CONTEXT = SpringApplication.run(applicationClass, args);

    }
}

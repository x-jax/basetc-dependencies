package com.basetc.base.common;


import java.util.Objects;

/**
 * 环境配置工具类,用于判断和管理应用的运行环境.
 *
 * <p>此类提供了便捷的方法来判断当前应用的运行环境 (开发、测试、生产等),
 * 支持从多个来源获取环境配置,并提供了环境判断的工具方法。
 *
 * <h3>支持的环境类型:</h3>
 * <table border="1">
 *   <tr>
 *     <th>环境</th>
 *     <th>说明</th>
 *     <th>典型用途</th>
 *   </tr>
 *   <tr>
 *     <td>LOCAL</td>
 *     <td>本地环境</td>
 *     <td>开发者本地调试</td>
 *   </tr>
 *   <tr>
 *     <td>DEV</td>
 *     <td>开发环境</td>
 *     <td>开发团队日常开发</td>
 *   </tr>
 *   <tr>
 *     <td>TEST</td>
 *     <td>测试环境</td>
 *     <td>测试团队功能测试</td>
 *   </tr>
 *   <tr>
 *     <td>ITG</td>
 *     <td>集成环境</td>
 *     <td>系统集成测试</td>
 *   </tr>
 *   <tr>
 *     <td>UAT</td>
 *     <td>用户验收测试环境</td>
 *     <td>用户验收测试</td>
 *   </tr>
 *   <tr>
 *     <td>PROD</td>
 *     <td>生产环境</td>
 *     <td>正式线上环境</td>
 *   </tr>
 * </table>
 *
 * <h3>环境配置获取优先级:</h3>
 * <pre>{@code
 * 1. 启动参数: SPRING_APPLICATION_ARGS (环境变量)
 *    示例: SPRING_APPLICATION_ARGS="--spring.profiles.active=dev"
 *
 * 2. 系统属性: spring.profiles.active
 *    示例: java -jar app.jar --spring.profiles.active=dev
 *
 * 3. 环境变量: spring_profiles_active
 *    示例: export spring_profiles_active=dev
 *
 * 4. 默认值: LOCAL
 * }</pre>
 *
 * <h3>使用示例:</h3>
 * <h4>1. 判断当前环境:</h4>
 * <pre>{@code
 * if (BasetcProfile.isDev()) {
 *     // 开发环境特定逻辑
 *     System.out.println("当前是开发环境");
 *     // 启用详细日志
 *     loggingLevel = LogLevel.DEBUG;
 * } else if (BasetcProfile.isProd()) {
 *     // 生产环境特定逻辑
 *     System.out.println("当前是生产环境");
 *     // 关闭详细日志
 *     loggingLevel = LogLevel.ERROR;
 * }
 * }</pre>
 *
 * <h4>2. 根据环境加载不同配置:</h4>
 * <pre>{@code
 * @Configuration
 * public class DatabaseConfig {
 *
 *     @Bean
 *     public DataSource dataSource() {
 *         if (BasetcProfile.isProd()) {
 *             // 生产环境使用主数据库
 *             return createMasterDataSource();
 *         } else {
 *             // 其他环境使用测试数据库
 *             return createTestDataSource();
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h4>3. 条件日志输出:</h4>
 * <pre>{@code
 * public void processPayment(Payment payment) {
 *     if (BasetcProfile.isDev() || BasetcProfile.isTest()) {
 *         // 仅在开发/测试环境输出详细信息
 *         log.debug("处理支付: amount={}, userId={}",
 *             payment.getAmount(), payment.getUserId());
 *     }
 *
 *     // 支付处理逻辑...
 * }
 * }</pre>
 *
 * <h4>4. 环境特定的功能开关:</h4>
 * <pre>{@code
 * @Service
 * public class EmailService {
 *
 *     public void sendEmail(String to, String subject, String content) {
 *         if (BasetcProfile.isDev() || BasetcProfile.isLocal()) {
 *             // 开发环境不发送真实邮件,仅记录日志
 *             log.info("[模拟邮件] 收件人: {}, 主题: {}, 内容: {}",
 *                 to, subject, content);
 *             return;
 *         }
 *
 *         // 生产环境发送真实邮件
 *         emailSender.send(to, subject, content);
 *     }
 * }
 * }</pre>
 *
 * <h4>5. 获取当前环境名称:</h4>
 * <pre>{@code
 * String currentEnv = BasetcProfile.CURRENT;
 * log.info("当前运行环境: {}", currentEnv);
 *
 * // 输出示例: 当前运行环境: dev
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>环境名称不区分大小写,会自动转换为大写进行比较</li>
 *   <li>如果未配置任何环境,默认使用 LOCAL 环境</li>
 *   <li>建议在 application.yml 中明确指定环境: {@code spring.profiles.active: dev}</li>
 *   <li>生产环境部署时,应通过环境变量或启动参数明确指定环境</li>
 * </ul>
 *
 * <h3>最佳实践:</h3>
 * <pre>{@code
 * # application.yml
 * spring:
 *   profiles:
 *     active: ${SPRING_PROFILES_ACTIVE:local}
 *
 * # application-dev.yml
 * logging:
 *   level:
 *     root: DEBUG
 *
 * # application-prod.yml
 * logging:
 *   level:
 *     root: WARN
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public class BasetcProfile {

    /**
     * 获取当前环境
     */
    public static final String CURRENT = getCurrentActive();

    /**
     * 判断当前是否为开发环境.
     *
     * @return true表示是开发环境，false表示不是
     */
    public static boolean isDev() {
        return ProfileEnv.DEV.name().equalsIgnoreCase(CURRENT);
    }

    /**
     * 判断当前是否为生产环境.
     *
     * @return true表示是生产环境，false表示不是
     */
    public static boolean isProd() {
        return ProfileEnv.PROD.name().equalsIgnoreCase(CURRENT);
    }

    /**
     * 判断当前是否为测试环境.
     *
     * @return true表示是测试环境，false表示不是
     */
    public static boolean isTest() {
        return ProfileEnv.TEST.name().equalsIgnoreCase(CURRENT);
    }

    /**
     * 判断当前是否为集成环境.
     *
     * @return true表示是集成环境，false表示不是
     */
    public static boolean isItg() {
        return ProfileEnv.ITG.name().equalsIgnoreCase(CURRENT);
    }

    /**
     * 判断当前是否为用户验收测试环境.
     *
     * @return true表示是UAT环境，false表示不是
     */
    public static boolean isUat() {
        return ProfileEnv.UAT.name().equalsIgnoreCase(CURRENT);
    }

    /**
     * 判断当前是否为本地环境.
     *
     * @return true表示是本地环境，false表示不是
     */
    public static boolean isLocal() {
        return ProfileEnv.LOCAL.name().equalsIgnoreCase(CURRENT);
    }

    /**
     * 获取当前激活的环境配置.
     *
     * <p>优先级顺序如下： 1. 启动参数中的 SPRING_APPLICATION_ARGS 2. 系统属性 spring.profiles.active 3. 环境变量
     * spring_profiles_active 4. 默认使用 LOCAL 环境
     *
     * @return 当前激活的环境名称
     */
    private static String getCurrentActive() {
        // 尝试从启动参数中获取
        final String args = System.getenv("SPRING_APPLICATION_ARGS");
        if (Objects.nonNull(args)) {
            final String[] split = args.split(" ");
            for (String str : split) {
                if (str.toLowerCase().startsWith("--spring.profiles.active=")) {
                    return str.split("=")[1];
                }
            }
        }
        // 尝试从系统属性中获取
        final String systemProperty = System.getProperty("spring.profiles.active");
        if (Objects.nonNull(systemProperty) && !systemProperty.isEmpty()) {
            return systemProperty;
        }
        // 尝试从环境变量中获取
        final String envProperty = System.getenv("spring_profiles_active");
        if (Objects.nonNull(envProperty) && !envProperty.isEmpty()) {
            return envProperty;
        }
        // 设置默认启动环境
        final String localProfile = ProfileEnv.LOCAL.name().toLowerCase();
        System.setProperty("spring.profiles.active", localProfile);
        return localProfile;
    }

    /** 支持的环境枚举. */
    public enum ProfileEnv {
        /** 开发环境. */
        DEV,
        /** 测试环境. */
        TEST,
        /** 生产环境. */
        PROD,
        /** 集成环境. */
        ITG,
        /** 用户验收测试环境. */
        UAT,
        /** 本地环境. */
        LOCAL;
    }

}

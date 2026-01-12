package com.basetc.base.common.utils;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Spring 工具类,提供对 Spring 应用上下文的便捷访问和操作功能.
 *
 * <p>此类实现了 {@link org.springframework.context.ApplicationContextAware} 接口,
 * 在 Spring 容器启动时会自动注入 ApplicationContext,提供静态方法访问 Spring 容器.
 *
 * <p>主要功能包括:
 * <ul>
 *   <li>根据名称或类型获取 Bean 实例
 *   <li>判断 Bean 是否存在
 *   <li>判断 Bean 的作用域
 *   <li>动态注册和移除 Bean
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 1. 根据 Class 获取 Bean
 * UserService userService = SpringUtils.getBean(UserService.class);
 * userService.doSomething();
 *
 * // 2. 根据 Bean 名称获取 Bean
 * UserService userService = SpringUtils.getBean("userService");
 *
 * // 3. 根据名称和类型获取 Bean
 * UserService userService = SpringUtils.getBean("userService", UserService.class);
 *
 * // 4. 判断 Bean 是否存在
 * if (SpringUtils.containsBean("userService")) {
 *     // Bean 存在
 * }
 *
 * // 5. 判断 Bean 是否为单例
 * boolean isSingleton = SpringUtils.isSingleton("userService");
 *
 * // 6. 动态注册 Bean
 * MyBean myBean = new MyBean();
 * SpringUtils.registerBean("myBean", myBean);
 *
 * // 7. 动态移除 Bean
 * SpringUtils.removeBean("myBean");
 * }</pre>
 *
 * <h3>在非 Spring 管理的类中使用:</h3>
 * <pre>{@code
 * // 在工具类或静态方法中获取 Spring Bean
 * public class MyUtil {
 *
 *     public static void doSomething() {
 *         // 直接通过 SpringUtils 获取 Bean
 *         UserService userService = SpringUtils.getBean(UserService.class);
 *         userService.process();
 *     }
 * }
 * }</pre>
 *
 * <h3>动态注册 Bean 的场景:</h3>
 * <pre>{@code
 * // 根据配置动态创建 Bean
 * @Configuration
 * public class DynamicBeanConfig {
 *
 *     @PostConstruct
 *     public void registerDynamicBeans() {
 *         // 根据数据库配置动态注册数据源
 *         List<DataSourceConfig> configs = loadDataSourceConfigs();
 *
 *         for (DataSourceConfig config : configs) {
 *             DataSource dataSource = createDataSource(config);
 *             SpringUtils.registerBean(config.getBeanName(), dataSource);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>此类是线程安全的,可以在多线程环境下使用
 *   <li>动态注册的 Bean 默认为单例,如果需要原型 Bean,请使用 BeanDefinition
 *   <li>动态移除 Bean 后,需要确保没有其他地方引用该 Bean,否则可能导致内存泄漏
 *   <li>在单元测试中,可能需要手动初始化 ApplicationContext
 * </ul>
 *
 * <h3>单元测试中的使用:</h3>
 * <pre>{@code
 * @RunWith(SpringRunner.class)
 * @ContextConfiguration(classes = TestConfig.class)
 * public class MyTest {
 *
 *     @Autowired
 *     private ApplicationContext applicationContext;
 *
 *     @Before
 *     public void setUp() {
 *         // 手动设置 ApplicationContext
 *         SpringUtils.setApplicationContext(applicationContext);
 *     }
 *
 *     @Test
 *     public void testGetBean() {
 *         UserService userService = SpringUtils.getBean(UserService.class);
 *         assertNotNull(userService);
 *     }
 * }
 * }</pre>
 *
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public class SpringUtils implements ApplicationContextAware {

    /**
     * Spring 应用上下文.
     * <p>存储当前运行的 Spring ApplicationContext 实例.
     * 在 Spring 容器启动时自动注入.
     */
    private static ApplicationContext applicationContext;

    /**
     * 设置 ApplicationContext.
     *
     * <p>此方法由 Spring 容器在启动时自动调用,无需手动调用.
     *
     * @param applicationContext Spring 应用上下文,不能为 null
     * @throws BeansException 如果设置失败
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 根据 Bean 名称获取 Bean 实例.
     *
     * <p>此方法通过 Bean 的名称从 Spring 容器中获取对应的 Bean 实例.
     * 需要确保 Bean 名称在容器中存在,否则会抛出异常.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 根据 Bean 名称获取
     * UserService userService = SpringUtils.getBean("userService");
     * }</pre>
     *
     * @param name Bean 的名称,不能为 null
     * @param <T> Bean 类型
     * @return Bean 实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 根据 Bean 类型获取 Bean 实例.
     *
     * <p>此方法通过 Bean 的 Class 类型从 Spring 容器中获取对应的 Bean 实例.
     * 如果容器中存在多个该类型的 Bean,会抛出异常.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 根据 Bean 类型获取
     * UserService userService = SpringUtils.getBean(UserService.class);
     * }</pre>
     *
     * @param clazz Bean 的 Class 对象,不能为 null
     * @param <T> Bean 类型
     * @return Bean 实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果 Bean 不存在
     * @throws org.springframework.beans.factory.NoUniqueBeanDefinitionException 如果存在多个该类型的 Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据 Bean 名称和类型获取 Bean 实例.
     *
     * <p>此方法同时使用 Bean 名称和类型来获取 Bean,比单独使用名称或类型更安全.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 根据名称和类型获取
     * UserService userService = SpringUtils.getBean("userService", UserService.class);
     * }</pre>
     *
     * @param name  Bean 的名称,不能为 null
     * @param clazz Bean 的 Class 对象,不能为 null
     * @param <T>   Bean 类型
     * @return Bean 实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 判断是否包含指定名称的 Bean.
     *
     * <p>此方法可以安全地检查 Bean 是否存在,不会抛出异常.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * if (SpringUtils.containsBean("userService")) {
     *     UserService userService = SpringUtils.getBean("userService", UserService.class);
     * }
     * }</pre>
     *
     * @param name Bean 的名称,不能为 null
     * @return true 如果 Bean 存在,false 如果不存在
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 判断指定名称的 Bean 是否为单例.
     *
     * <p>单例 Bean 在整个容器中只有一个实例,原型 Bean 每次获取都会创建新实例.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * boolean isSingleton = SpringUtils.isSingleton("userService");
     * if (isSingleton) {
     *     log.info("userService 是单例 Bean");
     * }
     * }</pre>
     *
     * @param name Bean 的名称,不能为 null
     * @return true 如果是单例,false 如果不是单例或 Bean 不存在
     */
    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    /**
     * 动态注册 Bean 到 Spring 容器中.
     *
     * <p>此方法用于在运行时动态向 Spring 容器注册 Bean 实例.
     * 注册的 Bean 默认为单例模式.
     *
     * <p><b>注意:</b> 只有 GenericApplicationContext 类型的 ApplicationContext 才支持动态注册.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 创建 Bean 实例
     * MyBean myBean = new MyBean();
     * myBean.setName("test");
     *
     * // 注册到 Spring 容器
     * SpringUtils.registerBean("myBean", myBean);
     *
     * // 之后可以通过 getBean 获取
     * MyBean bean = SpringUtils.getBean("myBean", MyBean.class);
     * }</pre>
     *
     * @param beanName     Bean 的名称,不能为 null
     * @param beanInstance Bean 的实例,不能为 null
     * @throws IllegalStateException 如果 ApplicationContext 不支持动态注册
     */
    public static void registerBean(String beanName, Object beanInstance) {
        if (applicationContext instanceof GenericApplicationContext genericApplicationContext) {
            genericApplicationContext.getBeanFactory().registerSingleton(beanName, beanInstance);
        } else {
            throw new IllegalStateException(
                    "ApplicationContext does not support dynamic bean registration.");
        }
    }

    /**
     * 从 Spring 容器中移除指定名称的 Bean.
     *
     * <p>此方法用于在运行时动态从 Spring 容器中移除 Bean 定义.
     * 移除后,该 Bean 将无法再从容器中获取.
     *
     * <p><b>警告:</b> 移除 Bean 后,需要确保没有其他地方引用该 Bean,否则可能导致内存泄漏或功能异常.
     *
     * <p><b>注意:</b> 只有 DefaultListableBeanFactory 类型的 BeanFactory 才支持动态移除.
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 移除 Bean
     * SpringUtils.removeBean("myBean");
     *
     * // 之后无法通过 getBean 获取
     * // SpringUtils.getBean("myBean"); // 会抛出异常
     * }</pre>
     *
     * @param beanName Bean 的名称,不能为 null
     * @throws IllegalStateException 如果 ApplicationContext 或 BeanFactory 不支持动态移除
     */
    public static void removeBean(String beanName) {
        if (applicationContext instanceof GenericApplicationContext genericApplicationContext) {
            ConfigurableListableBeanFactory beanFactory = genericApplicationContext.getBeanFactory();
            if (beanFactory instanceof DefaultListableBeanFactory) {
                ((DefaultListableBeanFactory) beanFactory).removeBeanDefinition(beanName);
            } else {
                throw new IllegalStateException("BeanFactory does not support dynamic bean removal.");
            }
        } else {
            throw new IllegalStateException("ApplicationContext does not support dynamic bean removal.");
        }
    }
}

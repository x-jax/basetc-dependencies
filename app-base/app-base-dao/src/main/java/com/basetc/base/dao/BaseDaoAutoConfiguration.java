package com.basetc.base.dao;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.basetc.base.dao.handler.DaoMetaObjectHandler;
import com.basetc.base.dao.support.MybatisMetaFillSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Tc Base DAO模块自动配置类,负责配置和初始化MyBatis-Plus相关组件.
 *
 * <p>此类是Spring Boot的自动配置类,通过 {@code @AutoConfiguration} 注解在类路径下存在MyBatis-Plus时自动生效.
 * <p>主要功能包括:
 * <ul>
 *   <li>配置MyBatis-Plus拦截器链(分页、乐观锁、防全表更新删除等)</li>
 *   <li>自动装配元数据处理器,实现创建时间、更新时间等字段的自动填充</li>
 *   <li>提供灵活的配置选项,可通过配置文件控制各功能模块的启用状态</li>
 *   <li>支持条件化装配,根据项目需求选择性启用功能</li>
 * </ul>
 *
 * <h3>自动配置条件:</h3>
 * <p>此配置类在以下条件下自动生效:
 * <ul>
 *   <li>类路径下存在 {@code MybatisConfiguration} 类</li>
 *   <li>配置文件中 {@code basetc.dao.auto-configure} 为 {@code true} 或未配置(默认启用)</li>
 * </ul>
 *
 * <h3>配置示例:</h3>
 * <h4>1. application.yml 配置:</h4>
 * <pre>{@code
 * basetc:
 *   dao:
 *     # 是否启用自动配置(默认true)
 *     auto-configure: true
 *     # 拦截器配置
 *     interceptor:
 *       # 是否启用拦截器自动配置(默认true)
 *       auto-configure: true
 *       # 是否启用分页插件(默认true)
 *       pagination-enabled: true
 *       # 最大单页分页条目限制(默认无限制)
 *       max-page-limit: 1000
 *       # 是否启用乐观锁插件(默认true)
 *       optimistic-locker-enabled: true
 *       # 是否启用防全表更新删除插件(默认true)
 *       block-attack-inner-enabled: true
 * }</pre>
 *
 * <h4>2. 禁用自动配置:</h4>
 * <pre>{@code
 * # 方式1: 通过配置文件禁用
 * basetc:
 *   dao:
 *     auto-configure: false
 *
 * # 方式2: 通过@SpringBootApplication排除
 * @SpringBootApplication(exclude = TcBaseDaoAutoConfiguration.class)
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>拦截器说明:</h3>
 * <p>此配置类会自动配置以下MyBatis-Plus拦截器:
 *
 * <h4>1. 分页拦截器 ({@link PaginationInnerInterceptor}):</h4>
 * <ul>
 *   <li>提供物理分页功能,自动拼接分页SQL</li>
 *   <li>支持多种数据库(MySQL、Oracle、PostgreSQL等)</li>
 *   <li>可通过 {@code max-page-limit} 限制单页最大记录数,防止大数据量查询</li>
 *   <li>配合 {@code Page} 对象使用,自动完成count和分页查询</li>
 * </ul>
 *
 * <h4>2. 乐观锁拦截器 ({@link OptimisticLockerInnerInterceptor}):</h4>
 * <ul>
 *   <li>实现乐观锁功能,防止并发更新冲突</li>
 *   <li>需要在实体类的version字段上标注 {@code @Version} 注解</li>
 *   <li>更新时自动检查并递增版本号,冲突时抛出异常</li>
 * </ul>
 *
 * <h4>3. 防全表更新删除拦截器 ({@link BlockAttackInnerInterceptor}):</h4>
 * <ul>
 *   <li>阻止全表更新和删除操作,提升数据安全性</li>
 *   <li>没有WHERE条件的UPDATE/DELETE语句将被阻止</li>
 *   <li>防止因误操作导致的数据灾难</li>
 * </ul>
 *
 * <h3>元数据处理器:</h3>
 * <p>自动配置 {@link DaoMetaObjectHandler},用于自动填充实体类的元数据字段:
 * <ul>
 *   <li>创建时间: insert操作时自动填充</li>
 *   <li>创建人: insert操作时自动填充</li>
 *   <li>更新时间: insert和update操作时自动填充</li>
 *   <li>更新人: insert和update操作时自动填充</li>
 * </ul>
 * <p>需要自定义 {@link MybatisMetaFillSupport} 接口的实现类来提供填充值.
 *
 * <h3>使用示例:</h3>
 * <h4>1. 使用分页功能:</h4>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     @Autowired
 *     private UserMapper userMapper;
 *
 *     public Page<User> getUserList(int current, int size) {
 *         // 创建分页对象
 *         Page<User> page = new Page<>(current, size);
 *         // 执行分页查询(自动拼接limit语句)
 *         return userMapper.selectPage(page, null);
 *     }
 * }
 * }</pre>
 *
 * <h4>2. 使用乐观锁:</h4>
 * <pre>{@code
 * @Data
 * @TableName("sys_user")
 * public class User {
 *     @TableId
 *     private Long id;
 *     private String username;
 *
 *     // 乐观锁版本号字段
 *     @Version
 *     private Integer version;
 * }
 *
 * // 更新时会自动检查版本号
 * User user = userMapper.selectById(1L);
 * user.setUsername("new name");
 * userMapper.updateById(user); // 自动 WHERE version = ${oldVersion}
 * }</pre>
 *
 * <h4>3. 自定义元数据填充:</h4>
 * <pre>{@code
 * @Component
 * public class MyMetaFillSupport implements MybatisMetaFillSupport {
 *     @Override
 *     public Object getCreateBy() {
 *         // 返回当前登录用户ID
 *         return SecurityUtils.getUserId();
 *     }
 *
 *     @Override
 *     public Object getUpdateBy() {
 *         return SecurityUtils.getUserId();
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项:</h3>
 * <ul>
 *   <li>拦截器的加载顺序: 分页 -> 乐观锁 -> 防全表更新删除</li>
 *   <li>如果自定义了 {@link MetaObjectHandler},则自动配置的处理器将不会生效</li>
 *   <li>分页插件建议配置 {@code max-page-limit} 防止一次性查询过多数据</li>
 *   <li>乐观锁需要在实体类中添加 {@code @Version} 注解的字段</li>
 *   <li>元数据填充需要配合 {@link com.baomidou.mybatisplus.annotation.TableField} 注解使用</li>
 * </ul>
 *
 * <h3>依赖关系:</h3>
 * <ul>
 *   <li>依赖: MyBatis-Plus Core</li>
 *   <li>依赖: Spring Boot AutoConfiguration</li>
 *   <li>被依赖: Service层、Controller层</li>
 * </ul>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see MybatisPlusInterceptor
 * @see PaginationInnerInterceptor
 * @see OptimisticLockerInnerInterceptor
 * @see BlockAttackInnerInterceptor
 * @see DaoMetaObjectHandler
 * @see BaseDaoProperties
 * @see MybatisMetaFillSupport
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(MybatisConfiguration.class)
@EnableConfigurationProperties(BaseDaoProperties.class)
@ConditionalOnProperty(prefix = "basetc.dao", name = "auto-configure", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class BaseDaoAutoConfiguration {

    /**
     * DAO模块配置属性.
     * <p>通过Spring Boot自动注入,包含拦截器、分页等配置项.
     */
    private final BaseDaoProperties baseDaoProperties;

    /**
     * 配置MyBatis-Plus拦截器链.
     *
     * <p>此方法创建并配置MyBatis-Plus的核心拦截器,包括:
     * <ul>
     *   <li>乐观锁拦截器: 当 {@code basetc.dao.interceptor.optimistic-locker-enabled=true} 时启用</li>
     *   <li>分页拦截器: 当 {@code basetc.dao.interceptor.pagination-enabled=true} 时启用</li>
     *   <li>防全表更新删除拦截器: 当 {@code basetc.dao.interceptor.block-attack-inner-enabled=true} 时启用</li>
     * </ul>
     *
     * <p>拦截器的加载顺序非常重要,必须按以下顺序添加:
     * <ol>
     *   <li>乐观锁拦截器 - 必须在分页拦截器之前</li>
     *   <li>分页拦截器 - 必须在最后</li>
     *   <li>防全表更新删除拦截器 - 可以在任意位置</li>
     * </ol>
     *
     * <h3>配置示例:</h3>
     * <pre>{@code
     * # application.yml
     * basetc:
     *   dao:
     *     interceptor:
     *       auto-configure: true  # 启用拦截器自动配置
     *       pagination-enabled: true
     *       max-page-limit: 1000
     *       optimistic-locker-enabled: true
     *       block-attack-inner-enabled: true
     * }</pre>
     *
     * @return 配置好的MyBatis-Plus拦截器对象
     * @see PaginationInnerInterceptor
     * @see OptimisticLockerInnerInterceptor
     * @see BlockAttackInnerInterceptor
     */
    @Bean
    @ConditionalOnProperty(prefix = "basetc.dao.interceptor", name = "auto-configure", havingValue = "true", matchIfMissing = true)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        final MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加乐观锁拦截器
        // 注意: 乐观锁拦截器必须在分页拦截器之前添加
        if (baseDaoProperties.getInterceptor().isOptimisticLockerEnabled()) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }

        // 添加分页拦截器
        // 注意: 分页拦截器必须在最后添加
        if (baseDaoProperties.getInterceptor().isPaginationEnabled()) {
            final PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
            paginationInnerInterceptor.setMaxLimit(baseDaoProperties.getInterceptor().getMaxPageLimit());
            interceptor.addInnerInterceptor(paginationInnerInterceptor);
        }

        // 添加阻止全表更新删除拦截器
        // 此拦截器会阻止没有WHERE条件的UPDATE和DELETE操作
        if (baseDaoProperties.getInterceptor().isBlockAttackInnerEnabled()) {
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        }

        return interceptor;
    }

    /**
     * 配置MyBatis-Plus元数据处理器.
     *
     * <p>此方法创建并配置元数据自动填充处理器,用于在插入和更新操作时自动填充字段值.
     * <p>自动填充的字段包括:
     * <ul>
     *   <li>createTime: 创建时间</li>
     *   <li>createBy: 创建人ID</li>
     *   <li>updateTime: 更新时间</li>
     *   <li>updateBy: 更新人ID</li>
     * </ul>
     *
     * <h3>使用条件:</h3>
     * <ul>
     *   <li>Spring容器中存在 {@link MybatisMetaFillSupport} 类型的Bean</li>
     *   <li>Spring容器中不存在自定义的 {@link MetaObjectHandler} Bean</li>
     * </ul>
     *
     * <h3>使用示例:</h3>
     * <pre>{@code
     * // 1. 实现MybatisMetaFillSupport接口
     * @Component
     * public class MyMetaFillSupport implements MybatisMetaFillSupport {
     *     @Override
     *     public Object getCreateBy() {
     *         return SecurityUtils.getUserId(); // 返回当前用户ID
     *     }
     *
     *     @Override
     *     public Object getUpdateBy() {
     *         return SecurityUtils.getUserId();
     *     }
     * }
     *
     * // 2. 在实体类中使用
     * @Data
     * @TableName("sys_user")
     * public class User {
     *     @TableId
     *     private Long id;
     *
     *     @TableField(fill = FieldFill.INSERT)
     *     private LocalDateTime createTime;
     *
     *     @TableField(fill = FieldFill.INSERT)
     *     private Long createBy;
     *
     *     @TableField(fill = FieldFill.INSERT_UPDATE)
     *     private LocalDateTime updateTime;
     *
     *     @TableField(fill = FieldFill.INSERT_UPDATE)
     *     private Long updateBy;
     * }
     *
     * // 3. 插入和更新时会自动填充
     * userMapper.insert(user);  // 自动填充createTime和createBy
     * userMapper.updateById(user); // 自动填充updateTime和updateBy
     * }</pre>
     *
     * @param mybatisMetaFillSupport 元数据填充支持接口,提供填充值的获取逻辑
     * @return 配置好的元数据处理器
     * @see DaoMetaObjectHandler
     * @see MybatisMetaFillSupport
     */
    @Bean
    @ConditionalOnBean(MybatisMetaFillSupport.class)
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public DaoMetaObjectHandler tcDaoMetaObjectHandler(MybatisMetaFillSupport mybatisMetaFillSupport) {
        return new DaoMetaObjectHandler(mybatisMetaFillSupport);
    }

}

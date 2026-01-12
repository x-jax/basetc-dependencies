package com.basetc.base.dao;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis Plus DAO层配置属性.
 *
 * <p>此类用于配置MyBatis Plus的相关参数,包括自动配置开关和各类拦截器配置.
 * 通过Spring Boot的{@link ConfigurationProperties}机制,支持在application.yml中配置.
 *
 * <h3>配置示例:</h3>
 * <pre>{@code
 * basetc:
 *   dao:
 *     auto-configure: true
 *     interceptor:
 *       auto-configure: true
 *       optimistic-locker-enabled: true
 *       block-attack-inner-enabled: true
 *       pagination-enabled: true
 *       max-page-limit: 1000
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Data
@FieldNameConstants
@ConfigurationProperties(prefix = "basetc.dao")
public class BaseDaoProperties {

    /**
     * 是否启用自动配置.
     * <p>默认为false,需要手动启用以加载DAO层配置.
     */
    private boolean autoConfigure = false;

    /**
     * 拦截器配置属性.
     * <p>配置MyBatis Plus的各种拦截器行为.
     */
    private InterceptorProperties interceptor = new InterceptorProperties();

    /**
     * 拦截器配置属性类.
     *
     * <p>用于配置各种MyBatis Plus拦截器的启用状态和相关参数.
     * 包括乐观锁拦截器、防全表更新删除拦截器、分页拦截器等.
     *
     * @author Liu,Dongdong
     * @since 1.0.0
     */
    @Data
    @FieldNameConstants
    public static class InterceptorProperties {

        /**
         * 是否启用拦截器自动配置.
         * <p>默认为true,启用所有配置的拦截器.
         */
        private boolean autoConfigure = true;

        /**
         * 启用乐观锁拦截器.
         * <p>当更新实体时,根据@Version注解字段进行乐观锁控制.
         * <p>默认值: true
         */
        private boolean optimisticLockerEnabled = true;

        /**
         * 启用全表更新删除拦截器.
         * <p>阻止恶意的全表更新删除操作,提升系统安全性.
         * <p>默认值: true
         */
        private boolean blockAttackInnerEnabled = true;

        /**
         * 启用分页拦截器.
         * <p>自动识别数据库类型并构建分页SQL.
         * <p>默认值: true
         */
        private boolean paginationEnabled = true;

        /**
         * 分页最大限制.
         * <p>单页最大记录数限制,防止一次查询过多数据.
         * <p>默认值: 100
         */
        private Long maxPageLimit = 100L;
    }
}
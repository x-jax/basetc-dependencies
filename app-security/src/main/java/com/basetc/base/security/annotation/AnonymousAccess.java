package com.basetc.base.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 自定义匿名访问注解，用于标记允许匿名访问的控制器或方法
 * 支持指定环境，仅在指定环境下允许匿名访问
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnonymousAccess {
    /**
     * 允许匿名访问的环境列表，默认为所有环境
     */
    String[] value() default {};
}

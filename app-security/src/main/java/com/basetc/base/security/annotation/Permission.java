package com.basetc.base.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 自定义权限注解，用于控制方法访问权限
 * 使用Spring Security的PreAuthorize注解实现
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@ss.hasPermission(#root.annotation.value)")
public @interface Permission {

    /**
     * 权限编码
     */
    String value() default "";
    
    /**
     * 权限描述
     */
    String description() default "";
}

package com.basetc.base.security.enums;


/**
 * 安全认证过滤器枚举.
 *
 * <p>定义了安全认证过程中可用的过滤器类型,用于标识不同的认证过滤器实现.</p>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li>{@link #EXCEPTION}: 异常处理过滤器类型</li>
 *   <li>{@link #NONE}: 无过滤器类型</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <p>主要用于配置和区分不同的安全认证过滤器实现,以便在安全配置中正确应用相应的过滤器.</p>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */

public enum BasetcSecurityAuthFilter {

    EXCEPTION,
    NONE,
    ;

}

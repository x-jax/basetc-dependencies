package com.basetc.base.security.context;


import lombok.experimental.UtilityClass;

/**
 * 密码作用域值工具类
 *
 * <p>提供线程安全的密码存储机制，确保密码在特定作用域内使用，避免密码在内存中长时间暴露
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@UtilityClass
public class PasswordScoped {

    private static final ScopedValue<String> PASSWORD = ScopedValue.newInstance();

    /**
     * 检查当前作用域是否已设置密码.
     *
     * @return 如果已设置返回 true，否则返回 false
     */
    public static boolean isBound() {
        return PASSWORD.isBound();
    }

    /**
     * 在指定密码的作用域内执行操作.
     *
     * @param password 密码
     * @param runnable 要执行的操作
     */
    public static void runWithPassword(String password, Runnable runnable) {
        ScopedValue.where(PASSWORD, password).run(runnable);
    }

    /**
     * 获取当前线程作用域内的密码.
     *
     * @return 密码，如果未设置则返回 null
     */
    public static String get() {
        if (!PASSWORD.isBound()) {
            return null;
        }
        return PASSWORD.get();
    }

    /**
     * 在指定密码的作用域内执行操作并返回结果.
     *
     * <p>注意：由于ScopedValue.call()的类型约束，此方法仅能抛出RuntimeException.
     * 如果需要抛出检查型异常，请在callable内部将其包装为RuntimeException.
     *
     * @param password 密码
     * @param callable 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public static <T> T callWithPassword(String password, CallableOp<T> callable) {
        return ScopedValue.where(PASSWORD, password).call(callable::call);
    }

    /**
     * 函数式接口，用于执行可能抛出异常的操作.
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface CallableOp<T> {
        /**
         * 执行操作.
         *
         * @return 操作结果
         */
        T call();
    }
}

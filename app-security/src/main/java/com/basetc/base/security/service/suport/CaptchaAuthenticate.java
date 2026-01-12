package com.basetc.base.security.service.suport;


/**
 * 验证码认证支持接口
 * 定义验证码验证相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */

public interface CaptchaAuthenticate {

    /**
     * 执行验证码验证
     *
     * @param captcha 用户输入的验证码
     * @param captchaId 验证码ID
     */
    void doCaptcha(String captcha, String captchaId);

}

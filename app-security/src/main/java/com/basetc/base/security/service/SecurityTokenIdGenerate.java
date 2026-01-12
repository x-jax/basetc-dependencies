package com.basetc.base.security.service;


import com.basetc.base.security.domain.LoginUser;

/**
 * 安全令牌ID生成服务接口
 * 定义令牌ID生成相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */

public interface SecurityTokenIdGenerate {


  /**
   * 为登录用户生成令牌ID
   *
   * @param loginUser 登录用户信息
   * @return 生成的令牌ID字符串
   */
  String generate(LoginUser loginUser);

}

package com.basetc.base.security.service.impl;


import cn.hutool.core.util.IdUtil;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.service.SecurityTokenIdGenerate;

/**
 * 安全令牌ID生成服务实现类
 * 实现令牌ID生成相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
public class SecurityTokenIdGenerateImpl implements SecurityTokenIdGenerate {

    /**
     * @param loginUser
     * @return
     */
    @Override
    public String generate(LoginUser loginUser) {
        String uuid = IdUtil.fastUUID();
        loginUser.setTokenId(uuid);
        return uuid;
    }
}

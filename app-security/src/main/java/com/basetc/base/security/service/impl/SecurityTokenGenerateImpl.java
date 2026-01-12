package com.basetc.base.security.service.impl;


import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.properties.BasetcSecurityJwtProperties;
import com.basetc.base.security.service.SecurityTokenGenerate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;

/**
 * 安全令牌生成服务实现类
 * 实现令牌生成和解析相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SecurityTokenGenerateImpl implements SecurityTokenGenerate {

    /**
     * 安全JWT配置属性
     */
    private final BasetcSecurityJwtProperties basetcSecurityJwtProperties;

    enum TokenPlayKey {
        UID,
        TID,
        ;
    }

    public record TokenPayloadImpl(Long userId, String tokenId) {

        public static TokenPayload of(Long userId, String tokenId) {
            return new TokenPayload() {
                @Override
                public Long getUserId() {
                    return userId;
                }

                @Override
                public String getTokenId() {
                    return tokenId;
                }
            };
        }

        public static TokenPayload of(JWTPayload payload) {
            return TokenPayloadImpl.of(
                    (Long) payload.getClaim(TokenPlayKey.UID.name()),
                    (String) payload.getClaim(TokenPlayKey.TID.name())
            );
        }

    }

    /**
     * @param request
     * @return
     */
    @Override
    public TokenPayload parseTokenId(HttpServletRequest request) {
        final String header = request.getHeader(basetcSecurityJwtProperties.getHeader());
        if (Objects.isNull(header) || header.isEmpty() || !header.startsWith(basetcSecurityJwtProperties.getPrefix())) {
            return null;
        }
        String token = header.substring(basetcSecurityJwtProperties.getPrefix().length());
        return validate(token) ? TokenPayloadImpl.of(JWTUtil.parseToken(token).getPayload()) : null;
    }


    /**
     * @param loginUser
     * @return
     */
    @Override
    public String generate(LoginUser loginUser) {
        return JWTUtil.createToken(
                Map.of(TokenPlayKey.UID.name(), loginUser.getUserId(), TokenPlayKey.TID.name(), loginUser.getTokenId()),
                JWTSignerUtil.hs256(basetcSecurityJwtProperties.getSecret().getBytes()));
    }

    /**
     * @param token
     * @return
     */
    private boolean validate(String token) {
        try {
            return JWTUtil.verify(token, basetcSecurityJwtProperties.getSecret().getBytes());
        } catch (Exception e) {
            return false;
        }
    }


}

package com.basetc.base.security.service.impl;


import com.basetc.base.common.exception.BasetcException;
import com.basetc.base.common.utils.RequestUtils;
import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.event.LogoutEvent;
import com.basetc.base.security.listener.SessionManagerListener;
import com.basetc.base.security.properties.BasetcSecurityAuthProperties;
import com.basetc.base.security.properties.BasetcSecuritySessionProperties;
import com.basetc.base.security.service.SecurityAuthenticateUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

/**
 * 基于Session的安全认证用户服务实现类
 * 实现基于Session的用户会话管理和令牌操作相关的方法
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SessionSecurityAuthenticateUserServiceImpl implements SecurityAuthenticateUserService {

    /**
     * 安全Session配置属性
     */
    private final BasetcSecuritySessionProperties basetcSecuritySessionProperties;
    private final BasetcSecurityAuthProperties basetcSecurityAuthProperties;
    private final SessionManagerListener sessionManagerListener;
    private final ApplicationContext applicationContext;

    /**
     * 从HTTP请求中获取当前登录用户信息.
     *
     * <p>通过解析请求头或请求参数中的令牌，获取已登录用户的详细信息. 如果请求中没有有效的令牌或令牌已失效，可能返回 null.
     *
     * @param request HTTP请求对象
     * @return 登录用户信息，如果未登录或令牌无效则返回 null
     */
    @Override
    public LoginUser getLoginUser(HttpServletRequest request) {
        return (LoginUser) request.getSession().getAttribute(basetcSecuritySessionProperties.getSessionKeyPrefix());
    }

    /**
     * 登出当前用户.
     *
     * <p>从HTTP请求中获取当前登录用户信息，并注销当前用户.
     *
     * @param request HTTP请求对象
     */
    @Override
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }

    /**
     * 登出当前用户.
     *
     * <p>从HTTP请求中获取当前登录用户信息，并注销当前用户.
     *
     * @param loginUser 登录用户信息
     */
    @Override
    public void logout(LoginUser loginUser) {
        RequestUtils.getRequest().getSession().invalidate();
    }

    /**
     * 刷新.
     *
     * <p>根据给定的登录用户信息刷新令牌. 具体实现可能基于JWT、Session或其他认证机制.
     *
     * @param loginUser 登录用户信息
     */
    @Override
    public void refreshToken(LoginUser loginUser) {

    }

    /**
     * 创建访问令牌.
     *
     * <p>根据给定的登录用户信息创建访问令牌. 具体实现可能基于JWT、Session或其他认证机制.
     *
     * @param loginUser 登录用户信息
     * @return 创建的访问令牌
     */
    @Override
    public String createToken(LoginUser loginUser) {

        boolean singleEnabled = basetcSecurityAuthProperties.getFilter().isSingleEnabled();
        if (singleEnabled && sessionManagerListener.checkUserExist(loginUser.getUserId())) {
            if (basetcSecurityAuthProperties.getFilter().isOverwriteOldAuth()) {
                LoginUser loginUser1 = sessionManagerListener.getLoginUser(loginUser.getUserId());
                if (loginUser1 != null){
                    sessionManagerListener.logout(loginUser1.getTokenId());
                    applicationContext.publishEvent(new LogoutEvent(loginUser1, "顶号,强制下线"));
                }
            }else {
                throw new BasetcException("当前账号已在其他地点登录, 如不是本人操作请立即修改密码");
            }
        }

        HttpSession session = RequestUtils.getRequest().getSession();
        loginUser.setTokenId(session.getId());
        session.setAttribute(basetcSecuritySessionProperties.getSessionKeyPrefix(), loginUser);
        return session.getId();
    }
}

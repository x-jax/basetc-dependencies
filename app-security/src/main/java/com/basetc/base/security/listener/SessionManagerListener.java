package com.basetc.base.security.listener;


import com.basetc.base.security.domain.LoginUser;
import com.basetc.base.security.properties.BasetcSecuritySessionProperties;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理监听器.
 *
 * <p>实现HttpSessionListener和HttpSessionAttributeListener接口,
 * 用于监听和管理用户会话的创建、销毁以及属性变更事件.</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *   <li>会话生命周期监听: 监听会话的创建和销毁事件</li>
 *   <li>用户会话管理: 维护用户ID与会话的映射关系</li>
 *   <li>令牌会话关联: 维护令牌与用户信息的映射关系</li>
 *   <li>会话属性监听: 监听会话属性的添加和替换事件</li>
 *   <li>用户登出管理: 支持基于令牌的用户登出功能</li>
 * </ul>
 *
 * <h3>设计特点</h3>
 * <ul>
 *   <li>线程安全: 使用ConcurrentHashMap保证并发安全</li>
 *   <li>内存管理: 及时清理无效会话,防止内存泄漏</li>
 *   <li>实时同步: 会话状态变更时实时更新内部映射</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在Spring Boot应用中注册监听器
 * @Bean
 * public SessionManagerListener sessionManagerListener(
 *         BasetcSecuritySessionProperties properties) {
 *     return new SessionManagerListener(properties);
 * }
 * 
 * // 获取所有登录用户
 * List<LoginUser> users = sessionManagerListener.getLoginUsers();
 * 
 * // 登出指定用户
 * sessionManagerListener.logout("token123");
 * }</pre>
 *
 * @author Liu,Dongdong
 * @since 1.0.0
 * @see HttpSessionListener
 * @see HttpSessionAttributeListener
 * @see LoginUser
 * @see BasetcSecuritySessionProperties
 */
@RequiredArgsConstructor
public class SessionManagerListener implements HttpSessionListener, HttpSessionAttributeListener {

    private final BasetcSecuritySessionProperties basetcSecuritySessionProperties;

    private final Map<Long, HttpSession> httpSessionMap = new ConcurrentHashMap<>();
    private final Map<String, LoginUser> httpSessionMapByToken = new ConcurrentHashMap<>();


    protected LoginUser getSessionUser(HttpSession session) {
        Object attribute = session.getAttribute(basetcSecuritySessionProperties.getSessionKeyPrefix());
        if (Objects.nonNull(attribute) && attribute instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /**
     * 监听session创建事件
     *
     * @param se session事件对象
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LoginUser sessionUser = getSessionUser(se.getSession());
        if (Objects.nonNull(sessionUser)) {
            httpSessionMap.put(sessionUser.getUserId(), se.getSession());
            httpSessionMapByToken.put(sessionUser.getTokenId(), sessionUser);
        }
    }

    /**
     * 监听session销毁事件
     *
     * @param se session事件对象
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        LoginUser loginUser = httpSessionMapByToken.get(sessionId);
        httpSessionMapByToken.remove(sessionId);
        if (Objects.nonNull(loginUser)) {
            httpSessionMap.remove(loginUser.getUserId());
        }
    }

    /**
     * 监听Session属性添加事件
     *
     * @param event session事件对象
     */
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (Objects.equals(event.getName(), basetcSecuritySessionProperties.getSessionKeyPrefix())) {
            LoginUser sessionUser = getSessionUser(event.getSession());
            if (Objects.nonNull(sessionUser)) {
                httpSessionMap.put(sessionUser.getUserId(), event.getSession());
                httpSessionMapByToken.put(sessionUser.getTokenId(), sessionUser);
            }
        }
    }

    /**
     * 监听Session属性替换事件
     *
     * @param event session事件对象
     */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        if (Objects.equals(event.getName(), basetcSecuritySessionProperties.getSessionKeyPrefix())) {
            LoginUser sessionUser = getSessionUser(event.getSession());
            if (Objects.nonNull(sessionUser)) {
                httpSessionMap.put(sessionUser.getUserId(), event.getSession());
                httpSessionMapByToken.put(sessionUser.getTokenId(), sessionUser);
            }
        }
    }


    /**
     * 获取所有登录用户
     *
     * @return 登录用户列表
     */
    public List<? extends LoginUser> getLoginUsers() {
        return httpSessionMapByToken.values().stream().toList();
    }

    public LoginUser getLoginUser(Long userId) {
        HttpSession httpSession = httpSessionMap.get(userId);
        return getSessionUser(httpSession);
    }

    /**
     * 登出
     *
     * @param tokenId tokenId
     */
    public void logout(String tokenId) {
        LoginUser loginUser = httpSessionMapByToken.get(tokenId);
        if (Objects.nonNull(loginUser)) {
            HttpSession httpSession = httpSessionMap.get(loginUser.getUserId());
            if (Objects.nonNull(httpSession)) {
                httpSession.invalidate();
            }
        }
    }


    /**
     * 检查用户是否存在活跃会话.
     *
     * <p>根据用户ID检查是否存在对应的活跃会话,用于判断用户是否在线.</p>
     *
     * @param userId 用户ID
     * @return true-用户存在活跃会话, false-用户不存在或会话已失效
     */
    public boolean checkUserExist(Long userId) {
        return httpSessionMap.containsKey(userId);
    }

}

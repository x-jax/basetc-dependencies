package com.basetc.base.security.event;


import com.basetc.base.security.domain.LoginUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * 登录事件.
 *
 * @author Liu,Dongdong
 *
 */
@Getter
public class LoginEvent extends ApplicationEvent {

    private final LoginUser loginUser;
    private final String grantType;
    private final boolean success;
    private final String error;

    @Serial
    private static final long serialVersionUID = -3634087987883016790L;

    public LoginEvent(LoginUser loginUser, String grantType, boolean success, String error) {
        super(loginUser);
        this.loginUser = loginUser;
        this.grantType = grantType;
        this.success = success;
        this.error = error;
    }
}

package com.basetc.base.security.event;


import com.basetc.base.security.domain.LoginUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * 退出事件.
 *
 * @author Liu,Dongdong
 *
 */
@Getter
public class LogoutEvent extends ApplicationEvent {


    @Serial
    private static final long serialVersionUID = 6075985324339149715L;

    private final LoginUser loginUser;
    private final String msg;

    public LogoutEvent(LoginUser loginUser, String msg) {
        super(loginUser);
        this.loginUser = loginUser;
        this.msg = msg;
    }
}

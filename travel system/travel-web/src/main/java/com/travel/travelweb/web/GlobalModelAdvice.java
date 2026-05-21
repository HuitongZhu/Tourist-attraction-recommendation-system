package com.travel.travelweb.web;

import com.travel.travelweb.config.LoginInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("loginUserId")
    public String loginUserId(HttpSession session) {
        Object v = session != null ? session.getAttribute(LoginInterceptor.SESSION_USER_ID) : null;
        return v != null ? v.toString() : null;
    }

    @ModelAttribute("loginUserName")
    public String loginUserName(HttpSession session) {
        Object v = session != null ? session.getAttribute(LoginInterceptor.SESSION_USER_NAME) : null;
        return v != null ? v.toString() : null;
    }
}

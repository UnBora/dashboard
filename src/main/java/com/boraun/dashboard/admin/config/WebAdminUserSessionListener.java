package com.boraun.dashboard.admin.config;

import com.boraun.dashboard.admin.login_history.AdminLoginService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebAdminUserSessionListener implements HttpSessionListener {

    private final AdminLoginService adminLoginService;

    @Autowired
    public WebAdminUserSessionListener(AdminLoginService adminLoginService) {
        this.adminLoginService = adminLoginService;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        log.info(sessionId + " is created");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        log.info(sessionId + " is destroyed");
        adminLoginService.expireNow(sessionId);
    }
}


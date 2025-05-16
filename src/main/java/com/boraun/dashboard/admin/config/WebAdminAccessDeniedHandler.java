package com.boraun.dashboard.admin.config;

import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class WebAdminAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.warn(auth.getName() + " was trying to access protected resource: " + request.getRequestURI());
        } else {
            log.warn("Anonymous was trying to access protected resource: " + request.getRequestURI());
        }
        if (accessDeniedException instanceof MissingCsrfTokenException || accessDeniedException instanceof InvalidCsrfTokenException) {
            log.error("Invalid or Missing CsrfToken");
        }
        if (Utils.isAjaxRequest(request)) {
            log.warn("Request is not AjaxRequest");
            response.sendError(HttpStatus.FORBIDDEN.value());
        } else {
            log.error("No permission to access. Redirect to unauthorized page");
            response.sendRedirect(CoreConstants.WEB_ADMIN_UNAUTHORIZED_ACCESS);
        }
    }
}

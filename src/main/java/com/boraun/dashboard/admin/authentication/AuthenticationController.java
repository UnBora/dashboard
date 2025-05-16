package com.boraun.dashboard.admin.authentication;

import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.email_reset.EmailResetEntity;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.admin.user.AdminUserEntity;
import com.boraun.dashboard.admin.user.AdminUserService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Utils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@Controller
@RequestMapping(value = "/web/admin/authentication")
@Slf4j
public class AuthenticationController{


    private final AdminUserService adminUserService;
    private final MessageUtilities messageUtilities;
    private final ConfigurationService configurationService;

    @Autowired
    public AuthenticationController(AdminUserService adminUserService, MessageUtilities messageUtilities, ConfigurationService configurationService) {
        this.adminUserService = adminUserService;
        this.messageUtilities = messageUtilities;
        this.configurationService = configurationService;
    }

    @RequestMapping(value = "/login")
    public ModelAndView login(ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        modelAndView.setViewName("web/admin/authentication/login");
        modelAndView.addObject("siteKey", configurationService.getGoogleRecaptchaSiteKey());
        modelAndView.addObject("URL", "/web/admin/authentication/login");
        return modelAndView;
    }
    @RequestMapping(value = "/forgot-password", method = RequestMethod.GET)
    public ModelAndView forgotPasswordForm(ModelAndView modelAndView) {
        modelAndView.setViewName("web/admin/authentication/forgot_password");
        return modelAndView;
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public ModelAndView forgotPasswordSubmit(@ModelAttribute("email") String email, ModelAndView modelAndView, HttpServletRequest httpServletRequest) throws MessagingException {
        modelAndView.setViewName("web/admin/authentication/forgot_password");
        if (StringUtils.isBlank(email)) {
            modelAndView.addObject("msgError", messageUtilities.getString("email.required"));
        } else {
            if (adminUserService.findByAdminUserName(email, CoreConstants.Status.Enabled) == null) {
                modelAndView.addObject("msgError", messageUtilities.getString("email.already_exists"));
            } else {
                modelAndView.addObject("msgSuccess", messageUtilities.getString("message.forgot.password.send.email"));
                adminUserService.sendEmailForUserForgotPassword(email, Utils.getBaseUrl(httpServletRequest) != null ? Utils.getBaseUrl(httpServletRequest) : configurationService.getSystemBaseUrl());
            }
        }
        return modelAndView;
    }

    @RequestMapping(value = "/forgot-password/reset", method = RequestMethod.GET)
    public ModelAndView resetPasswordForm(@RequestParam("key") String token, ModelAndView modelAndView) {
        modelAndView.setViewName("web/admin/authentication/reset_password");
        EmailResetEntity entity = adminUserService.findEmailResetByToken(token);
        modelAndView.addObject("reset", entity);
        return modelAndView;
    }

    @RequestMapping(value = "/forgot-password/reset", method = RequestMethod.POST)
    public ModelAndView resetPasswordSubmit(@RequestParam("key") String token,
                                            @ModelAttribute("newPwd") String newPwd,
                                            @ModelAttribute("confirmPwd") String confirmPwd,
                                            ModelAndView modelAndView) {
        modelAndView.setViewName("web/admin/authentication/reset_password");
        EmailResetEntity entity = adminUserService.findEmailResetByToken(token);
        modelAndView.addObject("reset", entity);
        if (StringUtils.isBlank(newPwd) || StringUtils.isBlank(confirmPwd)) {
            modelAndView.addObject("msgError", messageUtilities.getString("password.required"));
        } else if (!confirmPwd.equals(newPwd)) {
            modelAndView.addObject("msgError", messageUtilities.getString("password.not.match"));
        } else {
            if (Objects.nonNull(entity)) {
                AdminUserEntity userEntity = adminUserService.findByAdminUserName(entity.getEmail(), CoreConstants.Status.Enabled);
                if (Objects.nonNull(userEntity)) {
                    userEntity.setPassword(newPwd);
                    adminUserService.resetPassword(userEntity.getId(),userEntity);
                    entity.setStatus(CoreConstants.Status.Enabled);
                    adminUserService.updateEmailReset(entity);
                } else {
                    modelAndView.addObject("msgError", messageUtilities.getString("something.went.wrong"));
                }
            } else {
                log.error("Not found reset link with key: {}" , token);
                modelAndView.addObject("msgError", messageUtilities.getString("something.went.wrong"));
            }
        }
        return modelAndView;
    }


    @RequestMapping(value = "/session-timeout", method = RequestMethod.GET)
    @ResponseBody
    public String getSessionTimeout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) return "false";
        log.info("Continue session - sessionId {}", session.getId());
        return "true";
    }

}

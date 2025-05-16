package com.boraun.dashboard.admin.login_history;

import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.base.WebAdminException;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.Utils;
import com.boraun.dashboard.common.annotation.RootAuthority;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/web/admin/login-history", name = "Login History")
public class AdminLoginController extends WebAdminBaseController<AdminLoginEntity, Long> {
    private final AdminLoginService adminLoginService;

    @Autowired
    protected AdminLoginController(WebAdminBaseService<AdminLoginEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService, AdminLoginService adminLoginService) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
        this.adminLoginService = adminLoginService;
    }

    @RequestMapping(value = "/expire-now/{id}", method = RequestMethod.GET, name = "Expired Now")
    public ModelAndView ExpiredNowForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName(getRootViewPath() + "/expire_now");
                modelAndView.addObject("URL", rootURL + "/expire-now/" + id);
                modelAndView.addObject("entity", adminLoginService.findOne(id));
            }
        } catch (Exception ex) {
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/expire-now/{id}", method = RequestMethod.POST, name = "Expired Now")
    public ModelAndView ExpiredNowSave(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        }
        AdminLoginEntity entity = adminLoginService.findOne(id);
        String objectName = entity.getClass().getSimpleName().toLowerCase().replace("entity", "");
        adminLoginService.expireNow(entity.getSessionId());
        throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".expired.now"));
    }

}

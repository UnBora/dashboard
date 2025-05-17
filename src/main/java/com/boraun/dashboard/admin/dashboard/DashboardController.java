package com.boraun.dashboard.admin.dashboard;

import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.user.AdminUserEntity;
import com.boraun.dashboard.admin.user.AdminUserService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping(value = "/admin", name = "Dashboard")
@AllArgsConstructor
public class DashboardController {
    private final AdminUserService adminUserService;


    @RequestMapping(value = "", name = "view", method = RequestMethod.GET)
    public ModelAndView index(ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        AdminUserEntity userEntity = adminUserService.findByAdminUserName(SecurityContextHolder.getContext().getAuthentication().getName(), CoreConstants.Status.Enabled);
        HttpSession httpSession = httpServletRequest.getSession();
        if (Utils.isUserAuthenticated()) {
            List<AdminAuthorityEntity> authorityEntities = adminUserService.findAdminUserLoggedAuthority(userEntity, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
            httpSession.setAttribute("authorityEntities", authorityEntities);
            if (Utils.isAjaxRequest(httpServletRequest)) {
                modelAndView.setViewName("admin/dashboard");
            } else {
                modelAndView.setViewName("admin/index");
            }
        }
        return modelAndView;
    }

}

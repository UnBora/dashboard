package com.boraun.dashboard.admin.portfolio;


import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.annotation.RootAuthority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/admin/portfolio", name = "Portfolio")
@Slf4j
public class PortfolioController extends WebAdminBaseController<PortfolioEntity, Long> {

    @Autowired
    protected PortfolioController(WebAdminBaseService<PortfolioEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
    }
}

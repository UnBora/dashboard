package com.boraun.dashboard.admin.service_type;

import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.annotation.RootAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RootAuthority(name = "Setting", icon = "glyphicon glyphicon-cog")
@RequestMapping(value = "/admin/service-type", name = "Service Type")
public class ServiceTypeController extends WebAdminBaseController<ServiceTypeEntity, Long> {
    protected ServiceTypeController(WebAdminBaseService<ServiceTypeEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
    }
}

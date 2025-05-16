package com.boraun.dashboard.admin.configuration;


import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.annotation.RootAuthority;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/web/admin/configuration", name = "Configuration")
@Slf4j
public class ConfigurationController extends WebAdminBaseController<ConfigurationEntity, Long> {
    private ConfigurationController(WebAdminBaseService<ConfigurationEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
    }
}

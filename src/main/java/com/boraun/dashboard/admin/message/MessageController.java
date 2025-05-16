package com.boraun.dashboard.admin.message;


import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.common.annotation.RootAuthority;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/web/admin/message", name = "Message")
@Slf4j
public class MessageController extends WebAdminBaseController<MessageEntity, Long> {

    protected MessageController(WebAdminBaseService<MessageEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
    }
}

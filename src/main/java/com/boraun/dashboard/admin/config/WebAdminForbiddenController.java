package com.boraun.dashboard.admin.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/admin/unauthorized", name = "Unauthorized")
public class WebAdminForbiddenController {
    @GetMapping(value = "")
    public ModelAndView unauthorized(ModelAndView modelAndView) {
        modelAndView.setViewName("admin/unauthorized");
        return modelAndView;
    }
}

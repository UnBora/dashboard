package com.boraun.dashboard.admin.auditlog;

import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.base.WebAdminException;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.Utils;
import com.boraun.dashboard.common.annotation.RootAuthority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/admin/audit-log", name = "Audit Log")
public class AuditLogController {

    private String rootURL = "";

    private final MessageUtilities messageUtilities;
    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(MessageUtilities messageUtilities, AuditLogService auditLogService) {
        this.messageUtilities = messageUtilities;
        this.auditLogService = auditLogService;

        if (this.getClass().getAnnotation(RequestMapping.class) != null
                && this.getClass().getAnnotation(RequestMapping.class).value().length > 0) {
            rootURL = this.getClass().getAnnotation(RequestMapping.class).value()[0];
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView index(ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        List<AdminAuthorityEntity> navigationEntities = (List<AdminAuthorityEntity>) httpServletRequest.getSession().getAttribute("authorityEntities");
        if (navigationEntities == null) {
            try {
                httpServletResponse.sendRedirect("/admin/authentication/login?expired");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
//            modelAndView.setViewName(indexUrl.replace("-", "").concat("/index"));
            modelAndView.setViewName("admin/auditlog/index");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/page", name = "View Page", method = RequestMethod.GET)
    public ModelAndView getDataByPage(ModelAndView modelAndView, @RequestParam("pageSize") Optional<Integer> pageSize, @RequestParam("page") Optional<Integer> page, @RequestParam("fSearch") Optional<String> fSearch, @RequestParam("fromDateToDate") Optional<String> fromDateToDate, HttpServletRequest request) throws WebAdminException {
        try {
            if (Utils.isAjaxRequest(request)) {
//                modelAndView.setViewName(indexUrl.replace("-", "").concat("/table"));
                modelAndView.setViewName("admin/auditlog/table");
                modelAndView.addObject("pager", auditLogService.findAll(page, pageSize, fSearch, fromDateToDate));
                return modelAndView;
            } else {
                return new ModelAndView("redirect:" + rootURL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
    }

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET, name = "View Detail")
    public ModelAndView viewForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
//                modelAndView.setViewName(indexUrl.replace("-", "").concat("/view"));
                modelAndView.setViewName("admin/auditlog/view");
                AuditLogEntity entity = auditLogService.findById(id);
                modelAndView.addObject("entity", entity);
                return modelAndView;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
    }
}

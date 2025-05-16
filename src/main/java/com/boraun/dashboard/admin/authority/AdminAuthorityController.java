package com.boraun.dashboard.admin.authority;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.base.WebAdminException;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Utils;
import com.boraun.dashboard.common.annotation.RootAuthority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/web/admin/authority", name = "Authority")
@Slf4j
public class AdminAuthorityController extends WebAdminBaseController<AdminAuthorityEntity, Long> {
    private final AdminAuthorityService adminAuthorityService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    protected AdminAuthorityController(WebAdminBaseService<AdminAuthorityEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService, AdminAuthorityService adminAuthorityService1) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
        this.adminAuthorityService = adminAuthorityService1;
    }

    @RequestMapping(value = "/order", method = RequestMethod.GET, name = "Order")
    public ModelAndView orderForm(ModelAndView modelAndView, HttpServletRequest httpServletRequest) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName("web/admin/authority/order");
                List<AdminAuthorityEntity> adminAuthorityEntities = adminAuthorityService.findAllByStatus(CoreConstants.Status.Enabled, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
                modelAndView.addObject("adminAuthorityEntities", adminAuthorityEntities);
                modelAndView.addObject("URL", rootURL + "/order");
                AdminAuthorityOrderModel adminAuthorityOrderModel = new AdminAuthorityOrderModel();
                modelAndView.addObject("adminAuthorityOrderModel", adminAuthorityOrderModel);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST, name = "Order")
    public ModelAndView orderSave(AdminAuthorityOrderModel navigationOrderModel, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (!Utils.isAjaxRequest(httpServletRequest)){
            return new ModelAndView("redirect:" + rootURL);
        } else {

            List<AdminAuthorityEntity> adminAuthorityEntities = adminAuthorityService.saveNavigationOrder(navigationOrderModel.getAuthorityOrderAsString());
            auditLogService.save(null, adminAuthorityEntities, AuditLogConstants.AuditLogType.UPDATED);
            throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("admin.authority.order.saved"));
        }
    }


    @RequestMapping(value = "/update/{id}", method = RequestMethod.GET, name = "Update")
    public ModelAndView updateForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        List<AdminAuthorityEntity> entities = adminAuthorityService.findAllByMenu(true, Comparator.comparing(AdminAuthorityEntity::getAuthorityName));
        modelAndView.addObject("entities", entities);
        return super.updateForm(id, modelAndView, httpServletRequest, httpServletResponse, otherParameters);
    }
}

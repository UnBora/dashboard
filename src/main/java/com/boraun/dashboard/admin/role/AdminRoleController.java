package com.boraun.dashboard.admin.role;

import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.annotation.RootAuthority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/web/admin/role", name = "Role")
@Slf4j
public class AdminRoleController extends WebAdminBaseController<AdminRoleEntity, Long> {

    @Autowired
    private final AdminRoleService adminRoleService;
    @Autowired
    private final AdminAuthorityService adminAuthorityService;

    protected AdminRoleController(WebAdminBaseService<AdminRoleEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService, AdminRoleService adminRoleService, AdminAuthorityService adminAuthorityService1) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
        this.adminRoleService = adminRoleService;
        this.adminAuthorityService = adminAuthorityService1;
    }

    @Override
    public ModelAndView addForm(ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        List<AdminAuthorityEntity> adminAuthorityEntities = adminAuthorityService.findAllByStatus(CoreConstants.Status.Enabled, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
        otherParameters.put("adminAuthEntities", adminAuthorityEntities);
        return super.addForm(modelAndView, httpServletRequest, httpServletResponse, otherParameters);
    }

    @Override
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET, name = "View Detail")
    public ModelAndView viewForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        AdminRoleEntity adminRoleEntity = adminRoleService.findOne(id);
        List<AdminAuthorityEntity> adminAuthorityEntities = adminAuthorityService.findAllByStatus(CoreConstants.Status.Enabled, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
        adminAuthorityEntities.forEach(adminAuthorityEntity -> {
            if (adminRoleEntity.getAdminAuthorityEntities().stream().anyMatch(x -> x.getId().equals(adminAuthorityEntity.getId()))) {
                adminAuthorityEntity.setCheck(true);
            }
        });
        otherParameters.put("adminAuthEntities", adminAuthorityEntities);
        return super.viewForm(id, modelAndView, httpServletRequest, httpServletResponse, otherParameters);
    }

    @Override
    public ModelAndView updateForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        AdminRoleEntity adminRoleEntity = adminRoleService.findOne(id);
        List<AdminAuthorityEntity> adminAuthorityEntities = adminAuthorityService.findAllByStatus(CoreConstants.Status.Enabled, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
        adminAuthorityEntities.forEach(adminAuthorityEntity -> {
            if (adminRoleEntity.getAdminAuthorityEntities().stream().anyMatch(x -> x.getId().equals(adminAuthorityEntity.getId()))) {
                adminAuthorityEntity.setCheck(true);
            }
        });
        otherParameters.put("adminAuthEntities", adminAuthorityEntities);
        return super.updateForm(id, modelAndView, httpServletRequest, httpServletResponse, otherParameters);
    }

}

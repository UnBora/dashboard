package com.boraun.dashboard.admin.user;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseController;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.base.WebAdminException;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.login_history.AdminLoginEntity;
import com.boraun.dashboard.admin.login_history.AdminLoginService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.admin.role.AdminRoleService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import com.boraun.dashboard.common.annotation.RootAuthority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

@Controller
@RootAuthority(name = "Administrator", icon = "glyphicon glyphicon-user")
@RequestMapping(value = "/admin/user", name = "User")
@Slf4j
public class AdminUserController extends WebAdminBaseController<AdminUserEntity, Long> {

    private final AdminRoleService adminRoleService;
    private final AdminUserService adminUserService;
    private final AdminLoginService adminLoginService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    protected AdminUserController(WebAdminBaseService<AdminUserEntity, Long> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService, AdminRoleService adminRoleService, AdminUserService adminUserService, AdminLoginService adminLoginService) {
        super(baseService, messageUtilities, adminAuthorityService, configurationService);
        this.adminRoleService = adminRoleService;
        this.adminUserService = adminUserService;
        this.adminLoginService = adminLoginService;
    }

    @RequestMapping(value = "/profile", name = "View Profile")
    public ModelAndView profile(ModelAndView modelAndView) {
        modelAndView.setViewName("web/admin/user/profile");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AdminUserEntity entity = adminUserService.findActiveByUsername(username).orElse(null);
        modelAndView.addObject("entity", entity);
        return modelAndView;
    }


    @RequestMapping(value = "/profile/browser-history", name = "Browser History", method = RequestMethod.GET)
    public ModelAndView getDataByPage(
            ModelAndView modelAndView
            , @RequestParam("pageSize") Optional<Integer> pageSize
            , @RequestParam("page") Optional<Integer> page
            , HttpServletRequest request) throws WebAdminException {
        try {
            if (!Utils.isAjaxRequest(request)) {
                return new ModelAndView("redirect:" + rootURL);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        }
        modelAndView.setViewName(getRootViewPath() + "/browser_history_table");

        AdminUserEntity entity = adminUserService.findActiveByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        if(entity != null) {
            Pager<AdminLoginEntity> pager = adminLoginService.findAllHistory(page, pageSize, entity.getUsername());
            modelAndView.addObject("pager", pager);
        }

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/profile/update", name = "Update Profile")
    public ModelAndView editProfileForm(ModelAndView modelAndView, HttpServletRequest httpServletRequest) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName("web/admin/user/profile-update");
                AdminUserEntity entity = adminUserService.findActiveByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
                modelAndView.addObject("URL", rootURL + "/profile/update");
                modelAndView.addObject("entity", entity);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;

    }

    @RequestMapping(method = RequestMethod.POST, value = "/profile/update", name = "Update Profile")
    public ModelAndView editProfileSave(AdminUserEntity adminUserEntity, HttpServletRequest httpServletRequest) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        } else {
            AdminUserEntity db = adminUserService.findActiveByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
            if (db != null) {
                AdminUserEntity logObject = new AdminUserEntity();
                BeanUtils.copyProperties(db, logObject);

                db.setDisplayName(adminUserEntity.getDisplayName());
                db.setPhoneNumber(adminUserEntity.getPhoneNumber());
                AdminUserEntity saveAdmin = adminUserService.saveAndFlush(db);
                auditLogService.save(logObject, saveAdmin, AuditLogConstants.AuditLogType.UPDATED);
            }
            throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message.admin.user.updated"));
        }
    }

    @Override
    public ModelAndView addForm(ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        otherParameters.put("roleEntities", adminRoleService.findAllByStatus(CoreConstants.Status.Enabled));
        return super.addForm(modelAndView, httpServletRequest, httpServletResponse, otherParameters);
    }

    @Override
    public ModelAndView updateForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        otherParameters.put("roleEntities", adminRoleService.findAllByStatus(CoreConstants.Status.Enabled));
        return super.updateForm(id, modelAndView, httpServletRequest, httpServletResponse, otherParameters);
    }

    @RequestMapping(value = "/profile/change-password/{id}", name = "Change Password", method = RequestMethod.GET)
    public ModelAndView changePasswordForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            }
            modelAndView.setViewName("admin/user/profile-change-password");
            ChangePasswordModel entity = new ChangePasswordModel();
            modelAndView.addObject("URL", rootURL + "/profile/change-password/" + id);
            modelAndView.addObject("entity", entity);
            return modelAndView;

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("Something went wrong"));
        }
    }

    @RequestMapping(value = "/profile/change-password/{id}", method = RequestMethod.POST, name = "Change Password")
    public ModelAndView changePasswordSave(@Valid ChangePasswordModel changePasswordModel, BindingResult bindingResult, @PathVariable Long id, HttpServletRequest httpServletRequest) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        } else {
            if (bindingResult.hasErrors()) {
                throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, bindingResult.getFieldErrors().get(0).getDefaultMessage());
            }
            AdminUserEntity entity = adminUserService.findActiveByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
            if (entity != null) {
                adminUserService.verifyPassword(changePasswordModel.getCurrentPassword(), entity);
                entity.setPassword(changePasswordModel.getNewPassword());
            }
            if (!changePasswordModel.getConfirmPassword().contentEquals(changePasswordModel.getNewPassword())) {
                throw new RuntimeException(messageUtilities.getString("Password does not match"));
            }

            AdminUserEntity logObject = new AdminUserEntity();
            AdminUserEntity db = adminUserService.findOne(id);
            BeanUtils.copyProperties(db, logObject);

            AdminUserEntity t = adminUserService.resetPassword(id, entity);
            auditLogService.save(logObject, t, AuditLogConstants.AuditLogType.UPDATED);
            String objectName = t.getClass().getSimpleName().toLowerCase().replace("entity", "");
            throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".reset"));
        }
    }

    @RequestMapping(value = "/profile/browser-history/{id}", name = "Login History", method = RequestMethod.GET)
    public ModelAndView viewBrowserHistoryDetailForm(@PathVariable Long id,
                                                     ModelAndView modelAndView,
                                                     HttpServletRequest httpServletRequest
            , @RequestParam("pageSize") Optional<Integer> pageSize
            , @RequestParam("page") Optional<Integer> page) throws WebAdminException {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            }
            modelAndView.setViewName("admin/user/login-history-table");
            modelAndView.addObject("entity", adminLoginService.findAllHistoryById(page, pageSize, id));
            return modelAndView;

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("Something went wrong"));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/reset/password/{id}", name = "Reset Password")
    public ModelAndView resetPasswordForm(@PathVariable Long id, ModelAndView modelAndView, HttpServletRequest httpServletRequest) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            }
            modelAndView.setViewName("admin/user/reset");
            AdminUserEntity entity = adminUserService.findOne(id);
            modelAndView.addObject("URL", rootURL + "/reset/password/" + id);
            modelAndView.addObject("entity", entity);
            return modelAndView;

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        }
    }

    @RequestMapping(value = "/reset/password/{id}", method = RequestMethod.POST, name = "Reset Password")
    public ModelAndView resetPasswordSave(@Valid AdminUserEntity entity, BindingResult bindingResult, @PathVariable Long id, HttpServletRequest httpServletRequest) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        } else {
            if (bindingResult.hasErrors()) {
                throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, bindingResult.getFieldErrors().get(0).getDefaultMessage());
            }
            AdminUserEntity logObject = new AdminUserEntity();
            AdminUserEntity db = adminUserService.findOne(id);
            BeanUtils.copyProperties(db, logObject);

            AdminUserEntity t = adminUserService.adminResetPassword(id);

            auditLogService.save(logObject, t, AuditLogConstants.AuditLogType.UPDATED);
            String objectName = t.getClass().getSimpleName().toLowerCase().replace("entity", "");
            throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".reset"));
        }
    }


}

package com.boraun.dashboard.admin.base;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ControllerAdvice
@Configuration
public abstract class WebAdminBaseController<T, ID> extends WebAdminExceptionController {
    protected String rootURL = "";
    private final WebAdminBaseService<T, ID> baseService;
    protected final MessageUtilities messageUtilities;
    private final AdminAuthorityService adminAuthorityService;
    private final ConfigurationService configurationService;
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    protected WebAdminBaseController(WebAdminBaseService<T, ID> baseService, MessageUtilities messageUtilities, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService) {
        if (this.getClass().getAnnotation(RequestMapping.class) != null
                && this.getClass().getAnnotation(RequestMapping.class).value().length > 0) {
            rootURL = this.getClass().getAnnotation(RequestMapping.class).value()[0];
        }

        this.baseService = baseService;
        this.messageUtilities = messageUtilities;
        this.adminAuthorityService = adminAuthorityService;
        this.configurationService = configurationService;
    }

    protected String getRootViewPath() {
        if (rootURL.startsWith("/"))
            return rootURL.substring(1);
        return rootURL;
    }

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${project.version}")
    private String applicationVersion;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @ModelAttribute("appName")
    public String getName() {
        return applicationName;
    }

    @ModelAttribute("appVersion")
    public String getVersion() {
        return "Version".concat(StringUtils.SPACE).concat(applicationVersion).concat("-").concat(activeProfile);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, name = "View")
    public ModelAndView index(@NotNull ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        try {
            modelAndView.setViewName(getRootViewPath() + "/index");
            if (otherParameters != null && !otherParameters.isEmpty()) {
                otherParameters.forEach(modelAndView::addObject);
            }
            modelAndView.addObject("status", CoreConstants.Status.values());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/page", name = "View Page", method = RequestMethod.GET)
    public ModelAndView getDataByPage(
            ModelAndView modelAndView
            , @RequestParam("pageSize") Optional<Integer> pageSize
            , @RequestParam("page") Optional<Integer> page
            , @RequestParam("fSearch") Optional<String> fSearch
            , @RequestParam("fStatus[]") Optional<String[]> fStatus
            , @RequestParam("fromDate") Optional<String> fromDate
            , @RequestParam("toDate") Optional<String> toDate
            , HttpServletRequest request
            , Map<String, Object> otherParameters) throws WebAdminException {
        try {
            if (!Utils.isAjaxRequest(request)) {
                return new ModelAndView("redirect:" + rootURL);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        }
        modelAndView.setViewName(getRootViewPath() + "/table");

        otherParameters = new HashMap<>();

        String fParam1 = request.getParameter("fParam1");
        if (Utils.nonNull(fParam1)) {
            otherParameters.put("fParam1", fParam1);
        }

        String fParam2 = request.getParameter("fParam2");
        if (Utils.nonNull(fParam2)) {
            otherParameters.put("fParam2", fParam2);
        }

        String fParam3 = request.getParameter("fParam3");
        if (Utils.nonNull(fParam3)) {
            otherParameters.put("fParam3", fParam3);
        }

        String fParam4 = request.getParameter("fParam4");
        if (Utils.nonNull(fParam4)) {
            otherParameters.put("fParam4", fParam4);
        }
        modelAndView.addObject("status", CoreConstants.Status.values());
        modelAndView.addObject("pager", baseService.findAll(page, pageSize, fSearch, List.of(CoreConstants.Status.getStatus(fStatus)), fromDate, toDate, otherParameters));
        return modelAndView;
    }

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET, name = "View Detail")
    public ModelAndView viewForm(@PathVariable ID id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            }
            modelAndView.setViewName(getRootViewPath() + "/view");
            modelAndView.addObject("entity", baseService.findOne(id));
            if (otherParameters != null && !otherParameters.isEmpty()) {
                otherParameters.forEach(modelAndView::addObject);
            }
            return modelAndView;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET, name = "Add")
    public ModelAndView addForm(ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName(getRootViewPath() + "/add");
                modelAndView.addObject("URL", rootURL + "/add");
                modelAndView.addObject("entity", baseService.newInstance());
                if (otherParameters != null && !otherParameters.isEmpty()) {
                    otherParameters.forEach(modelAndView::addObject);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, name = "Add")
    public ModelAndView addSave(@Valid T entity, BindingResult bindingResult, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        } else {
            if (bindingResult.hasErrors()) {
                throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, bindingResult.getFieldErrors().get(0).getDefaultMessage());
            }
            T t = baseService.add(entity);
            String objectName = t.getClass().getSimpleName().toLowerCase().replace("entity", "");
            throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".added"));
        }
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.GET, name = "Update")
    public ModelAndView updateForm(@PathVariable ID id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map<String, Object> otherParameters) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName(getRootViewPath() + "/update");
                modelAndView.addObject("URL", rootURL + "/update/" + id);
                modelAndView.addObject("entity", baseService.findOne(id));
                if (otherParameters != null && !otherParameters.isEmpty()) {
                    otherParameters.forEach(modelAndView::addObject);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, name = "Update")
    public ModelAndView updateSave(@Valid T entity, BindingResult bindingResult, @PathVariable ID id, @RequestParam("files[]") Optional<MultipartFile[]> files, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        } else {
            if (bindingResult.hasErrors()) {
                throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, bindingResult.getFieldErrors().get(0).getDefaultMessage());
            }
            T t = baseService.update(id, entity);
            String objectName = t.getClass().getSimpleName().toLowerCase().replace("entity", "");
            throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".updated"));
        }
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET, name = "Delete")
    public ModelAndView deleteForm(@PathVariable ID id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName(getRootViewPath() + "/delete");
                modelAndView.addObject("URL", rootURL + "/delete/" + id);
                modelAndView.addObject("entity", baseService.findOne(id));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST, name = "Delete")
    public ModelAndView deleteSave(@PathVariable ID id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        }
        T t = baseService.findOne(id);
        String objectName = t.getClass().getSimpleName().toLowerCase().replace("entity", "");
        baseService.delete(id);
        auditLogService.save(t, String.valueOf(id), AuditLogConstants.AuditLogType.DELETED);
        throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".deleted"));
    }

    @RequestMapping(value = "/status/{id}", method = RequestMethod.GET, name = "Change Status")
    public ModelAndView statusForm(@PathVariable ID id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            if (!Utils.isAjaxRequest(httpServletRequest)) {
                return new ModelAndView("redirect:" + rootURL);
            } else {
                modelAndView.setViewName(getRootViewPath() + "/status");
                modelAndView.addObject("URL", rootURL + "/status/" + id);
                modelAndView.addObject("entity", baseService.findOne(id));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("something.went.wrong"));
        }
        return modelAndView;
    }

    @RequestMapping(value = "/status/{id}", method = RequestMethod.POST, name = "Change Status")
    public ModelAndView statusSave(@PathVariable ID id, ModelAndView modelAndView, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (!Utils.isAjaxRequest(httpServletRequest)) {
            return new ModelAndView("redirect:" + rootURL);
        }

        T t = baseService.toggleStatus(id);
        auditLogService.save(t, String.valueOf(id), AuditLogConstants.AuditLogType.CHANGE_STATUS);
        String objectName = t.getClass().getSimpleName().toLowerCase().replace("entity", "");
        if (objectName.contains("$")) {
            objectName = objectName.split("\\$")[0];
        }
        throw new WebAdminException(HttpStatus.OK, messageUtilities.getString("message." + objectName + ".status.updated"));
    }

    @ModelAttribute("selectedAdminAuthorityEntity")
    public AdminAuthorityEntity getSelectedAdminNavigationEntity(HttpServletRequest httpServletRequest) {
        String requestedUrl = httpServletRequest.getRequestURI();
        return adminAuthorityService.findByEndPointUrl(requestedUrl);
    }

    @ModelAttribute("configuration")
    public WebAdminConfiguration getConfigurationModel() {
        return configurationService.getConfiguration();
    }


}

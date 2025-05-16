package com.boraun.dashboard.admin.configuration;


import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.base.WebAdminConfiguration;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ConfigurationService extends WebAdminBaseService<ConfigurationEntity, Long> {
    private final ConfigurationRepository repository;
    private final AuditLogService auditLogService;

    @Autowired
    public ConfigurationService(ConfigurationRepository repository, ConfigurationRepository configurationRepository, AuditLogService auditLogService) {
        super(ConfigurationEntity.class, configurationRepository);
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    public WebAdminConfiguration getConfiguration() {
        WebAdminConfiguration configureModel = new WebAdminConfiguration();

        configureModel.setHomeUrl(CoreConstants.WEB_ADMIN_HOME);
        configureModel.setMaximumSessionTimeout(getInt("maximum.session.timeout", "1800"));
        configureModel.setMailSmtpHost(getValue("spring.mail.host", "smtp.office365.com"));
        configureModel.setMailSmtpPort(getInt("spring.mail.port", String.valueOf(587)));
        configureModel.setMailSmtpUsername(getValue("spring.mail.username", "xxxxg@xxxx.com.kh"));
        configureModel.setMailSmtpPassword(getValue("spring.mail.password", "xxxxxx"));
        configureModel.setMailSslOption(getValue("spring.mail.ssl.option","TTSLv12"));
        configureModel.setMailAuthenticationEnable(getBoolean("spring.mail.properties.mail.smtp.auth", true));
        configureModel.setMailSendFrom(getValue("spring.mail.smtp.sender.from", "xxxxg@xxxx.com.kh"));



        return configureModel;
    }

    private String getValue(String key, String defaultValue) {
        ConfigurationEntity entity = repository.findFirstByConfigurationCode(key).orElse(null);
        if (entity == null) {
            log.error("No configuration found for " + key);
            entity = new ConfigurationEntity();
            entity.setConfigurationCode(key);
            entity.setConfigurationValue(defaultValue);
            entity.setStatus(CoreConstants.Status.Enabled);
            repository.saveAndFlush(entity);
        } else {
            entity.setLastUsedAt(new Date());
            repository.saveAndFlush(entity);
        }
        return entity.getConfigurationValue();
    }

    protected String getValue(String key) {
        return this.getValue(key, "Edit Me Please");
    }

    private boolean getBoolean(String key) {
        String booleanValue = getValue(key);
        if (booleanValue.equalsIgnoreCase("")) return false;
        else {
            return Boolean.parseBoolean(booleanValue);
        }
    }

    private boolean getBoolean(String key, boolean defaultBoolValue) {
        String booleanValue = getValue(key, String.valueOf(defaultBoolValue));
        if (booleanValue.equalsIgnoreCase("")) return false;
        else {
            return Boolean.parseBoolean(booleanValue);
        }
    }

    private int getInt(String key) {
        String intValue = getValue(key);
        return Integer.parseInt(intValue);
    }

    private int getInt(String key, String defaultValue) {
        String intValue = getValue(key, defaultValue);
        return Integer.parseInt(intValue);
    }

    private Long getLong(String key) {
        String longValue = getValue(key);
        return Long.parseLong(longValue);
    }

    public int getMaximumSessionAllowance() {
        return this.getInt("maximum.session.allowance", "1");
    }


    @Override
    public Pager<ConfigurationEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> statuses, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        Page<ConfigurationEntity> entities = repository.findAllByStatusInAndConfigurationCodeContaining(pageable, statuses, search.orElse(""));
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public ConfigurationEntity update(Long aLong, ConfigurationEntity entity) {
        ConfigurationEntity configuration = repository.findById(aLong).orElse(null);
        ConfigurationEntity oldConfiguration = new ConfigurationEntity();
        if (configuration != null) {
            BeanUtils.copyProperties(configuration, oldConfiguration);
            configuration.setConfigurationValue(entity.getConfigurationValue());
            configuration.setConfigurationCode(entity.getConfigurationCode());
            configuration.setRemark(entity.getRemark());
            configuration = repository.saveAndFlush(configuration);
            auditLogService.save(oldConfiguration, configuration, AuditLogConstants.AuditLogType.UPDATED);
            return configuration;
        }
        return null;
    }

    @Override
    public ConfigurationEntity add(ConfigurationEntity entity) {
        entity.setStatus(CoreConstants.Status.Enabled);
        super.add(entity);
        //audit
        auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);
        return entity;
    }

    @Override
    public void delete(Long aLong) {
        ConfigurationEntity configuration = repository.findById(aLong).orElse(null);
        if (configuration != null) {
            configuration.setStatus(CoreConstants.Status.Deleted);
            repository.saveAndFlush(configuration);
        }
    }

    protected String[] getFileAllowedExtension() {
        String value = getValue("file.upload.allowed.extension");
        return value.split(";");

    }

    public boolean isAllowedFileExtension(String fileExtension) {
        String[] exts = getFileAllowedExtension();
        for (String ext : exts) {
            if (ext.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    public String getSystemBaseUrl() {
        return getValue("system.base.url");
    }

    public String getMinIOEndpoint() {
        return getValue("minio.endpoint");
    }

    public String getMinIOAccessKey() {
        return getValue("minio.access.key");
    }

    public String getMinIOSecretKey() {
        return getValue("minio.secret.key");
    }

    public String getMinIOBucketName() {
        return getValue("minio.bucket.name");
    }

    public String getGoogleRecaptchaSecretKey() {
        return getValue("google.recaptcha.secret.key");
    }

    public String getGoogleRecaptchaSiteKey() {
        if (getGoogleRecaptchaEnable()) {
            return getValue("google.recaptcha.site.key");
        }
        else return null;
    }

    public boolean getGoogleRecaptchaEnable() {
        return getBoolean("google.recaptcha.enable");
    }

    public boolean isAllowedAttachmentFileSize(double size) {
        double allowedSize = getLong("attachment.allowed.file.size");
        return (size < allowedSize);
    }

    public double getAllowedAttachmentFileSize() {
        return getLong("attachment.allowed.file.size");
    }


    public String getForgetPasswordEmailSubject() {
        return getValue("notification.template.forget.password.subject", "Reset Your Account Password");
    }
    public String getForgotPasswordEmailTemplate(){
        return getValue("notification.template.forget.password.body");
    }

    public String getCreateAccountEmailSubject() {
        return getValue("notification.template.create.account.subject", "Your Login Account Details");
    }
    public String getCreateAccountEmailTemplate() {
        return getValue("notification.template.create.account.body");
    }

    public String getAdminResetPasswordEmailSubject() {
        return getValue("notification.template.admin.reset.password.subject", "Your New Account Password");
    }

    public String getAdminResetPasswordTemplate() {
        return getValue("notification.template.admin.reset.password.body");
    }



}

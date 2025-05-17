package com.boraun.dashboard.admin.user;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.authority.AdminAuthorityService;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.base.WebAdminException;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.admin.email_reset.EmailResetEntity;
import com.boraun.dashboard.admin.email_reset.EmailResetRepository;
import com.boraun.dashboard.admin.message.MessageUtilities;
import com.boraun.dashboard.admin.notification.NotificationService;
import com.boraun.dashboard.admin.notification.NotificationTemplateModel;
import com.boraun.dashboard.admin.role.AdminRoleEntity;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.GoogleRecaptchaService;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import com.boraun.dashboard.executor.ExecutorSendEmail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AdminUserService extends WebAdminBaseService<AdminUserEntity, Long> implements UserDetailsService {

    private final AdminUserRepository repository;
    private final AdminAuthorityService adminAuthorityService;
    private final ConfigurationService configurationService;
    private final GoogleRecaptchaService googleRecaptchaService;

    @Lazy
    private final PasswordEncoder passwordEncoder;

    protected final MessageUtilities messageUtilities;
    private final HttpServletRequest httpServletRequest;
    private final EmailResetRepository emailResetRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;


    protected AdminUserService(AdminUserRepository repository, AdminAuthorityService adminAuthorityService, ConfigurationService configurationService, GoogleRecaptchaService googleRecaptchaService, PasswordEncoder passwordEncoder, MessageUtilities messageUtilities, HttpServletRequest httpServletRequest, EmailResetRepository emailResetRepository, NotificationService notificationService, AuditLogService auditLogService) {
        super(AdminUserEntity.class, repository);
        this.repository = repository;
        this.adminAuthorityService = adminAuthorityService;
        this.configurationService = configurationService;
        this.googleRecaptchaService = googleRecaptchaService;
        this.passwordEncoder = passwordEncoder;
        this.messageUtilities = messageUtilities;
        this.httpServletRequest = httpServletRequest;
        this.emailResetRepository = emailResetRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }


    public void addDefaultAdminUser(AdminRoleEntity defaultRole) {
        AdminUserEntity entity = repository.findById(1L).orElse(null);
        if (entity == null) {
            log.warn("Admin user not found. Admin user is automatically created admin@gmail.com and password is 123456");
            entity = new AdminUserEntity();
            entity.setUsername("admin@gmail.com");
            entity.setDisplayName("Administrator");
            entity.setPassword(passwordEncoder.encode("123456"));
            entity.setAdminRoleEntity(defaultRole);
            entity.setCreatedAt(new Date());
            entity.setCreatedBy("System");
            repository.save(entity);
        }
    }

    public void updateUserWhenLoginSuccessful(String username, String clientIpAddress) {
        AdminUserEntity entity = repository.findAdminUserEntityByUsername(username).orElse(null);
        if (entity != null) {
            entity.setLastLoginAt(new Date());
            entity.setLastLoginIPAddress(clientIpAddress);
            repository.save(entity);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AdminUserEntity entity = repository.findAdminUserEntityByUsername(username).orElse(null);
        if (configurationService.getGoogleRecaptchaEnable()) {
            if (entity == null) {
                log.error("User " + username + " failed login. User not found or incorrect credentials");
                throw new UsernameNotFoundException("User not found or incorrect credentials " + username);
            }
            String recaptChaResponse = httpServletRequest.getParameter("g-recaptcha-response");
            if (Objects.isNull(recaptChaResponse)) {
                throw new IllegalStateException("Invalid Captcha");
            }
            if (!googleRecaptchaService.verifyRecaptcha(Utils.getClientIP(httpServletRequest), recaptChaResponse)) {
                throw new IllegalStateException("Invalid Captcha");
            }
        }
        if (entity != null) {
            log.info("loadUserByUsername found with " + username);
            List<GrantedAuthority> authorities = new ArrayList<>();
            List<AdminAuthorityEntity> list = adminAuthorityService.findAuthoritiesByUsername(username, Comparator.comparing(AdminAuthorityEntity::getAuthorityOrder));
            list.forEach(authorityEntity -> authorities.add((GrantedAuthority) authorityEntity::getAuthorityKey));
            return new CustomUserDetails(
                    entity.getDisplayName(),
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.isActive(),
                    true,
                    true,
                    true,
                    authorities
            );
        }
        log.error("User " + username + " failed login. User not found or incorrect credentials");
        throw new UsernameNotFoundException("User not found or incorrect credentials " + username);
    }

    @Override
    public Pager<AdminUserEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        String username = search.orElse(StringUtils.EMPTY);
        Page<AdminUserEntity> entities = repository.findAllByDisplayNameContainingAndStatusIn(pageable, username, status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public AdminUserEntity update(Long id, AdminUserEntity entity) {
        AdminUserEntity adminUserEntity = repository.findById(id).orElse(null);
        AdminUserEntity oldEntity = new AdminUserEntity();
        BeanUtils.copyProperties(entity, oldEntity);
        if (adminUserEntity != null) {
            adminUserEntity.setDisplayName(entity.getDisplayName());
            adminUserEntity.setPhoneNumber(entity.getPhoneNumber());
            adminUserEntity.setAdminRoleEntity(entity.getAdminRoleEntity());
            adminUserEntity = repository.saveAndFlush(adminUserEntity);
            auditLogService.save(oldEntity, adminUserEntity, AuditLogConstants.AuditLogType.UPDATED);
            return adminUserEntity;
        }
        return null;
    }

    public Optional<AdminUserEntity> findActiveByUsername(String username) {
        return repository.findAdminUserEntityByUsername(username);
    }

    @Override
    public AdminUserEntity add(AdminUserEntity entity) {
        AdminUserEntity adminUserEntity = repository.findFirstByUsernameAndStatus(entity.getUsername(), CoreConstants.Status.Enabled).orElse(null);
        if (adminUserEntity != null) {
            log.error("Email or Username already existed.");
            throw new WebAdminException(HttpStatus.INTERNAL_SERVER_ERROR, messageUtilities.getString("username.already.existed"));
        }
        String userPwd = Utils.getRandomSecuredString(8);
        entity.setPassword(entity.isAutoGeneratePassword() ? passwordEncoder.encode(userPwd) : passwordEncoder.encode(entity.getPassword()));
        entity.setStatus(CoreConstants.Status.Enabled);
        AdminUserEntity response = repository.save(entity);
        String systemUrl = Utils.getBaseUrl(httpServletRequest) != null ? Utils.getBaseUrl(httpServletRequest) : configurationService.getSystemBaseUrl();
        this.sendEmailForCreateUserAccount(response.getUsername(), response.getDisplayName(), response.getUsername(), userPwd, systemUrl);
        auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);
        return response;
    }


    @Override
    public void delete(Long aLong) {
        AdminUserEntity adminUserEntity = repository.findById(aLong).orElse(null);
        if (Objects.nonNull(adminUserEntity)) {
            adminUserEntity.setStatus(CoreConstants.Status.Deleted);
            repository.saveAndFlush(adminUserEntity);
        }
    }

    public AdminUserEntity resetPassword(Long aLong, AdminUserEntity adminUserEntity) {
        AdminUserEntity entity = repository.findById(aLong).orElse(null);
        if (Objects.nonNull(entity) && !StringUtils.isEmpty(adminUserEntity.getPassword())) {
            adminUserEntity.setPassword(passwordEncoder.encode(adminUserEntity.getPassword()));
            adminUserEntity.setLastChangePasswordAt(new Date());
            return repository.saveAndFlush(entity);
        }
        return null;
    }

    public AdminUserEntity adminResetPassword(Long aLong) {
        AdminUserEntity entity = repository.findById(aLong).orElse(null);
        if (Objects.nonNull(entity)) {
            String userPwd = Utils.getRandomSecuredString(8);
            entity.setPassword(passwordEncoder.encode(userPwd));
            entity.setLastChangePasswordAt(new Date());
            repository.saveAndFlush(entity);
            String systemUrl = Utils.getBaseUrl(httpServletRequest) != null ? Utils.getBaseUrl(httpServletRequest) : configurationService.getSystemBaseUrl();
            this.sendEmailForAdminResetPassword(entity.getUsername(), systemUrl, userPwd);
            return entity;
        }
        return null;
    }

    public void verifyPassword(String plainText, AdminUserEntity adminUserEntity) {
        if (!passwordEncoder.matches(plainText, adminUserEntity.getPassword())) {
            log.error("The current password does not match.");
            throw new WebAdminException(HttpStatus.BAD_REQUEST,"The current password does not match.");
        }
    }


    public EmailResetEntity findEmailResetByToken(String token) {
        EmailResetEntity entity = emailResetRepository.findByToken(token);
        if (Objects.nonNull(entity)) {
            if (Utils.compareValidDate(entity.getCreatedAt(), 24)) {
                log.info("Token expired. The reset link was sent in over 24h.");
                emailResetRepository.delete(entity);
                return null;
            }
        }
        return entity;
    }

    public void updateEmailReset(EmailResetEntity entity) {
        emailResetRepository.save(entity);
    }


    public AdminUserEntity findByAdminUserName(String name, CoreConstants.Status status) {
        return repository.findFirstByUsernameAndStatus(name, status).orElse(null);
    }

    public List<AdminAuthorityEntity> findAdminUserLoggedAuthority(AdminUserEntity adminUserEntity, Comparator comparator) {
        List<AdminAuthorityEntity> adminAuthorityEntities = adminUserEntity.getAdminRoleEntity().getAdminAuthorityEntities();
        for (AdminAuthorityEntity nav : adminAuthorityEntities) {
            nav.setHasSub(adminAuthorityService.findAllByParentIdAndStatusAndMenuIs(nav.getId(), CoreConstants.Status.Enabled, true).size() != 0);
        }
        if (!Utils.isNull(comparator))
            adminAuthorityEntities.sort(comparator);
        adminAuthorityEntities.removeIf(x -> x.getStatus() != CoreConstants.Status.Enabled);
        return adminAuthorityEntities;
    }

    @Async
    public void sendEmailForCreateUserAccount(String toEmail, String displayName, String username, String rawPassword, String systemUrl) {
        log.info("Sending register email to: " + toEmail);
        NotificationTemplateModel templateEntity = notificationService.createUserAccountTemplate(displayName, username, rawPassword, systemUrl.concat(CoreConstants.WEB_ADMIN_LOGIN));
        if (Objects.nonNull(templateEntity)) {
            new ExecutorSendEmail(notificationService,
                    configurationService.getConfiguration().getMailSmtpHost(),
                    configurationService.getConfiguration().getMailSmtpPort(),
                    configurationService.getConfiguration().isMailAuthenticationEnable(),
                    Utils.SendEmailSecurityOption.valueOf(configurationService.getConfiguration().getMailSslOption()),
                    configurationService.getConfiguration().getMailSmtpUsername(),
                    configurationService.getConfiguration().getMailSmtpPassword(),
                    configurationService.getConfiguration().getMailSendFrom(),
                    toEmail,
                    templateEntity.getSubject(),
                    templateEntity.getHtmlBody(),
                    null
            ).start();
        }
    }

    @Async
    public void sendEmailForUserForgotPassword(String toEmail, String systemUrl) {
        AdminUserEntity userEntity = this.findByAdminUserName(toEmail, CoreConstants.Status.Enabled);
        if (Objects.nonNull(userEntity)) {
            String token = Utils.toBase64(Utils.getRandomSecuredStringForMail(100));
            String restUrlToken = systemUrl.concat("/admin/authentication/forgot-password/reset?key=").concat(token);
            log.info("Sending forget password to: " + toEmail);
            NotificationTemplateModel templateEntity = notificationService.forgetPasswordTemplate(userEntity.getDisplayName(), restUrlToken);
            if (Objects.nonNull(templateEntity)) {
                new ExecutorSendEmail(notificationService,
                        configurationService.getConfiguration().getMailSmtpHost(),
                        configurationService.getConfiguration().getMailSmtpPort(),
                        configurationService.getConfiguration().isMailAuthenticationEnable(),
                        Utils.SendEmailSecurityOption.valueOf(configurationService.getConfiguration().getMailSslOption()),
                        configurationService.getConfiguration().getMailSmtpUsername(),
                        configurationService.getConfiguration().getMailSmtpPassword(),
                        configurationService.getConfiguration().getMailSendFrom(),
                        toEmail,
                        templateEntity.getSubject(),
                        templateEntity.getHtmlBody(),
                        null
                ).start();
            }
            log.info("Reset link has been sent to {}", toEmail);
            EmailResetEntity resetEntity = new EmailResetEntity();
            resetEntity.setEmail(toEmail);
            resetEntity.setToken(token);
            resetEntity.setResetUrlToken(restUrlToken);
            resetEntity.setCreatedAt(new Date());
            resetEntity.setStatus(CoreConstants.Status.Pending);
            emailResetRepository.save(resetEntity);

        } else {
            log.error("Email address not found. {}", toEmail);
        }
    }

    @Async
    public void sendEmailForAdminResetPassword(String toEmail, String systemUrl, String newPassword) {
        AdminUserEntity userEntity = this.findByAdminUserName(toEmail, CoreConstants.Status.Enabled);
        if (Objects.nonNull(userEntity)) {
            log.info("Sending admin reset password to: " + toEmail);
            NotificationTemplateModel templateEntity = notificationService.adminResetPasswordTemplate(userEntity.getDisplayName(), userEntity.getUsername() ,systemUrl.concat(CoreConstants.WEB_ADMIN_LOGIN), newPassword);
            if (Objects.nonNull(templateEntity)) {
                new ExecutorSendEmail(notificationService,
                        configurationService.getConfiguration().getMailSmtpHost(),
                        configurationService.getConfiguration().getMailSmtpPort(),
                        configurationService.getConfiguration().isMailAuthenticationEnable(),
                        Utils.SendEmailSecurityOption.valueOf(configurationService.getConfiguration().getMailSslOption()),
                        configurationService.getConfiguration().getMailSmtpUsername(),
                        configurationService.getConfiguration().getMailSmtpPassword(),
                        configurationService.getConfiguration().getMailSendFrom(),
                        toEmail,
                        templateEntity.getSubject(),
                        templateEntity.getHtmlBody(),
                        null
                ).start();
            }
        } else {
            log.error("Email address not found. {}", toEmail);
        }
    }


}

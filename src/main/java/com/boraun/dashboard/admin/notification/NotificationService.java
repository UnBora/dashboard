package com.boraun.dashboard.admin.notification;


import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.common.Utils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class NotificationService {

    private final ConfigurationService configurationService;

    JavaMailSender mailSender;

    public NotificationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public NotificationTemplateModel forgetPasswordTemplate(String displayName, String resetUrlToken) {
        HashMap<String, String> contentFields = new HashMap<>();
        contentFields.put(":displayName", displayName);
        contentFields.put(":resetUrlToken", resetUrlToken);
        NotificationTemplateModel entity = new NotificationTemplateModel(
                configurationService.getForgetPasswordEmailSubject(),
                configurationService.getForgotPasswordEmailTemplate(),
                "",
                "For User Forget Password"
        );
        return this.replaceNotificationTemplate(entity, null, contentFields, Utils.TemplateBodyType.HTML);
    }

    public NotificationTemplateModel adminResetPasswordTemplate(String displayName, String email, String linkLogin, String newPassword) {
        HashMap<String, String> contentFields = new HashMap<>();
        contentFields.put(":loginUrl", linkLogin);
        contentFields.put(":password", newPassword);
        contentFields.put(":displayName", displayName);
        contentFields.put(":email", email);
        NotificationTemplateModel entity = new NotificationTemplateModel(
                configurationService.getAdminResetPasswordEmailSubject(),
                configurationService.getAdminResetPasswordTemplate(),
                "",
                "For Admin Reset Password"
        );
        return this.replaceNotificationTemplate(entity, null, contentFields, Utils.TemplateBodyType.HTML);
    }

    public NotificationTemplateModel createUserAccountTemplate(String displayName, String username, String rawPassword, String systemPortal) {
        HashMap<String, String> contentFields = new HashMap<>();
        contentFields.put(":displayName", displayName);
        contentFields.put(":username", username);
        contentFields.put(":password", rawPassword);
        contentFields.put(":systemPortal", systemPortal);
        NotificationTemplateModel entity = new NotificationTemplateModel(
                configurationService.getCreateAccountEmailSubject(),
                configurationService.getCreateAccountEmailTemplate(),
                "",
                "For Admin Create User Account"
        );
        return this.replaceNotificationTemplate(entity, null, contentFields, Utils.TemplateBodyType.HTML);
    }

    private NotificationTemplateModel replaceNotificationTemplate(NotificationTemplateModel entityExist, HashMap<String, String> subjectFields, HashMap<String, String> contentFields, Utils.TemplateBodyType templateBodyType) {
        NotificationTemplateModel templateEntity = new NotificationTemplateModel();
        if (Objects.nonNull(entityExist)) {
            BeanUtils.copyProperties(entityExist, templateEntity);
            if (StringUtils.isNotBlank(entityExist.getSubject()) && !CollectionUtils.isEmpty(subjectFields)) {
                String subject = entityExist.getSubject();
                for (Map.Entry<String, String> entry : subjectFields.entrySet()) {
                    subject = subject.replaceAll(entry.getKey(), entry.getValue());
                }
                templateEntity.setSubject(subject);
            }
            if (templateBodyType.equals(Utils.TemplateBodyType.TEXT) && StringUtils.isNotBlank(entityExist.getTextBody()) && !CollectionUtils.isEmpty(contentFields)) {
                String textTemplate = entityExist.getTextBody();
                for (Map.Entry<String, String> entry : contentFields.entrySet()) {
                    textTemplate = textTemplate.replaceAll(entry.getKey(), entry.getValue());
                }
                templateEntity.setTextBody(textTemplate);
            } else if (templateBodyType.equals(Utils.TemplateBodyType.HTML) && StringUtils.isNotBlank(entityExist.getHtmlBody()) && !CollectionUtils.isEmpty(contentFields)) {
                String htmlTemplate = entityExist.getHtmlBody();
                for (Map.Entry<String, String> entry : contentFields.entrySet()) {
                    htmlTemplate = htmlTemplate.replaceAll(entry.getKey(), entry.getValue());
                }
                templateEntity.setHtmlBody(htmlTemplate);
            }
        }
        return templateEntity;
    }
    public boolean sendEmail(String host, int port, String username, String password, boolean authenticationEnable, Utils.SendEmailSecurityOption sslOption, String fromEmailAddress, String toEmailAddressCommaOption, String ccEmailAddressCommaOption, String subject, String body, List<String> attachmentRelativePath) {
        String emailMethod = "Utils.sendEmail";
        boolean result = Utils.sendEmail(
                host,
                port,
                authenticationEnable,
                sslOption,
                username,
                password,
                fromEmailAddress,
                toEmailAddressCommaOption,
                ccEmailAddressCommaOption,
                subject,
                body,
                attachmentRelativePath
        );
        if (!result) {
            log.warn(toEmailAddressCommaOption + " tried with Utils.sendEmail failed, trying SpringBoot.Email");
            emailMethod = "Springboot.Email";
            result = this.sendEmailV1(
                    host,
                    port,
                    authenticationEnable,
                    sslOption,
                    username,
                    password,
                    fromEmailAddress,
                    toEmailAddressCommaOption,
                    ccEmailAddressCommaOption,
                    subject,
                    body,
                    attachmentRelativePath
            );
        }
        if (result) {
            log.info(toEmailAddressCommaOption + " Email sent successfully with " + emailMethod);
        } else {
            log.error(toEmailAddressCommaOption + " Email sent failed with " + emailMethod);
        }
        return result;
    }

    public boolean sendEmailV1(String host, int port, boolean authenticationEnable, Utils.SendEmailSecurityOption sslOption, String username, String password, @NonNull String fromEmailAddress, @NonNull String toEmailAddressCommaOption, String ccEmailAddressCommaOption, @NonNull String subject, @NonNull String body, List<String> attachmentRelativePath) {
        try {
            log.info("Sending email with sendEmailV1" + toEmailAddressCommaOption);
            MimeMessage mimeMsg = this.mailSender.createMimeMessage();
            mimeMsg.setHeader("Content-Type", "text/html; charset=utf-8");
            MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, "UTF-8");
            helper.setFrom(configurationService.getConfiguration().getMailSendFrom());

            helper.setTo(StringUtils.split(toEmailAddressCommaOption, ","));
            helper.setSubject(subject);
            helper.setText(body, true);
            if (Utils.nonNull(ccEmailAddressCommaOption)) {
                helper.setCc(StringUtils.split(ccEmailAddressCommaOption, ","));
            }
            if (Utils.nonNull(attachmentRelativePath)) {
                for (String s : attachmentRelativePath) {
                    File file = new File(s);
                    helper.addAttachment(file.getName(), file);
                }
            }
            mimeMsg.saveChanges();
            this.mailSender.send(mimeMsg);
            log.info("Sending email with sendEmailV1 " + toEmailAddressCommaOption + " => " + true);
            return true;
        } catch (MessagingException e) {
            log.error("Sending email with sendEmailV1 " + toEmailAddressCommaOption + " => " + false);
            e.printStackTrace();
            return false;
        }
    }

}

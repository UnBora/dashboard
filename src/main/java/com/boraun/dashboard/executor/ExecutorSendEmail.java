package com.boraun.dashboard.executor;

import com.boraun.dashboard.admin.notification.NotificationService;
import com.boraun.dashboard.common.Utils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

@Slf4j
public class ExecutorSendEmail extends Thread {

    private NotificationService notificationService;
    private String smtpHost;
    private int smtpPort;
    private boolean smtpEnableAuthentication;
    private Utils.SendEmailSecurityOption sendEmailSecurityOption;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpEmailFrom;
    private String smtpEmailTo;
    private String subject;
    private String body;
    private List<String> attachments;

    public ExecutorSendEmail(NotificationService notificationService, String smtpHost, int smtpPort, boolean smtpEnableAuthentication, Utils.SendEmailSecurityOption sendEmailSecurityOption,
                             String smtpUsername, String smtpPassword, String smtpEmailFrom, String smtpEmailTo, String subject, String body, List<String> attachments) {
        this.notificationService = notificationService;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpEnableAuthentication = smtpEnableAuthentication;
        this.sendEmailSecurityOption = sendEmailSecurityOption;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpEmailFrom = smtpEmailFrom;
        this.smtpEmailTo = smtpEmailTo;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("Sending email to " + this.smtpEmailTo);
        String emailMethod = "Utils.sendEmail";
        boolean result = notificationService.sendEmail(
                smtpHost,
                smtpPort,
                smtpUsername,
                smtpPassword,
                smtpEnableAuthentication,
                sendEmailSecurityOption,
                smtpEmailFrom,
                smtpEmailTo,
                "",
                subject,
                body,
                attachments
        );
        if (result) {
            log.info(smtpEmailTo + " Email sent successfully with " + emailMethod);
        } else {
            log.error(smtpEmailTo + " Email sent error with " + emailMethod);
        }
        log.info("ExecutorSendEmail is sleeping 3 seconds, prevent multiple hits to email server");
        Thread.sleep(3000);
    }
}

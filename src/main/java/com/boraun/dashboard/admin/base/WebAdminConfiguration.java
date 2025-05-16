package com.boraun.dashboard.admin.base;

import lombok.Data;

@Data
public class WebAdminConfiguration {

    private String mailSmtpHost;
    private int mailSmtpPort;
    private String mailSmtpUsername;
    private String mailSmtpPassword;
    private String mailSslOption;
    private boolean mailAuthenticationEnable;
    private String mailSendFrom;

    private String homeUrl;
    private int maximumSessionTimeout;
}

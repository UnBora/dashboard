package com.boraun.dashboard.admin.notification;

import lombok.Data;

@Data
public class NotificationTemplateModel {
    private String subject;
    private String htmlBody;
    private String textBody;
    private String remark;

    public NotificationTemplateModel(String subject, String htmlBody, String textBody, String remark) {
        this.subject = subject;
        this.htmlBody = htmlBody;
        this.textBody = textBody;
        this.remark = remark;
    }
    public NotificationTemplateModel() {

    }
}

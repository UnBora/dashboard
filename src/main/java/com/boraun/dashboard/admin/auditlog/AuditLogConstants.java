package com.boraun.dashboard.admin.auditlog;

public class AuditLogConstants {
    public enum AuditLogType {
        LOG_IN,
        LOG_OUT,
        SAVED,
        UPDATED,
        CHANGE_STATUS,
        DELETED,
        APPROVED,
        REJECTED,
        DUPLICATED
    }
}

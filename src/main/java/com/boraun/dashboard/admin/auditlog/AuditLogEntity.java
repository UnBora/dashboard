package com.boraun.dashboard.admin.auditlog;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLogEntity {
    private static final long serialVersionUID = -1179252837938773184L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditLogId;

    private String logEvent;
    private String logKey;

    @Column(columnDefinition = "TEXT")
    private String oldObject;

    @Column(columnDefinition = "TEXT")
    private String newObject;

    private String username;
    private Date timeStamp;
    private String remoteId;
    private String remark;
}

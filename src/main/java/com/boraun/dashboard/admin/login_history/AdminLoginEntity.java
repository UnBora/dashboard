package com.boraun.dashboard.admin.login_history;

import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admin_login")
@Data
public class AdminLoginEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sessionId;
    private String clientIPAddress;
    private String username;

    @Column(columnDefinition = "TEXT")
    private String message;
    private String browserAgent;
    private String eventName;
}

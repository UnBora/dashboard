package com.boraun.dashboard.admin.email_reset;

import com.boraun.dashboard.common.CoreConstants;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "temp_email_reset")
@Data
public class EmailResetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailId;
    private String email;
    private CoreConstants.Status status;
    private String token;
    private String resetUrlToken;
    private Date createdAt;
}

package com.boraun.dashboard.admin.user;


import com.boraun.dashboard.admin.role.AdminRoleEntity;
import com.boraun.dashboard.common.BaseEntity;
import com.boraun.dashboard.common.CoreConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admin_user")
@Data
public class AdminUserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String displayName;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "admin_role_id")
    @ToString.Exclude
    private AdminRoleEntity adminRoleEntity;

    private String phoneNumber;

    public boolean isActive() {
        return this.getStatus() == CoreConstants.Status.Enabled;
    }

    private boolean forceChangePassword;
    private Date lastChangePasswordAt;
    private Date lastLoginAt;
    private String lastLoginIPAddress;

    @Transient
    @JsonIgnore
    private boolean autoGeneratePassword;
}

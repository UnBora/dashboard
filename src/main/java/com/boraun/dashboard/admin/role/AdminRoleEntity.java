package com.boraun.dashboard.admin.role;

import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.user.AdminUserEntity;
import com.boraun.dashboard.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admin_role")
@Data
public class AdminRoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;

    @JsonIgnore
    @OneToMany(mappedBy = "adminRoleEntity")
    private List<AdminUserEntity> adminUserEntities;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "admin_role_authority",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private List<AdminAuthorityEntity> adminAuthorityEntities;

    @Transient
    private List<Long> selectedAdminAuthorityId;

    @Transient
    private long adminAuthorityCount;

    @Transient
    public long getAdminAuthorityCount() {
        if (this.adminAuthorityEntities == null) return 0;
        return this.adminAuthorityEntities.size();
    }

    @Transient
    private long adminUserCount;

    @Transient
    public long getAdminUserCount() {
        if (this.adminUserEntities == null) return 0;
        return this.adminUserEntities.size();
    }
}

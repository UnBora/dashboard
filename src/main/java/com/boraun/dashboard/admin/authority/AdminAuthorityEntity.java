package com.boraun.dashboard.admin.authority;

import com.boraun.dashboard.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admin_authority")
@NoArgsConstructor
@Data
public class AdminAuthorityEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorityName;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    @JsonBackReference
    private AdminAuthorityEntity parent;

    private String endPointUrl;

    private int authorityOrder;

    private String faIcon;

    private boolean menu;

    @Transient
    private boolean hasSub;

    @Column(unique = true)
    private String authorityKey;

    private Date lastUsed;

    @Transient
    private boolean check;

    public AdminAuthorityEntity(Long navigationId) {
        this.id = navigationId;
    }
    public AdminAuthorityEntity(@NotEmpty  String authorityKey, String authorityName, boolean isMenu, String endPointUrl, String faIcon, AdminAuthorityEntity adminAuthorityEntity) {
        this.authorityKey = authorityKey;
        this.parent = adminAuthorityEntity;
        this.endPointUrl = endPointUrl;
        this.authorityOrder = 0;
//        this.authorityEntities = authorityEntities;
        this.faIcon = faIcon;
        this.menu = isMenu;
        this.authorityName = authorityName;
    }
}

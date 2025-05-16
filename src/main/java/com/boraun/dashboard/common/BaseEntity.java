package com.boraun.dashboard.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
public abstract class BaseEntity implements Serializable {
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(nullable = false)
    private Date createdAt;
    @JsonIgnore
    private String createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    private String updatedBy;

    @Enumerated(EnumType.STRING)
    private CoreConstants.Status status;

    @Transient
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private boolean canUpdate;

    public boolean isCanUpdate() {
        if (this.status == null) return false;
        return switch (this.status) {
            case Enabled, Disabled -> true;
            case Pending, Deleted -> false;
        };
    }

    @PrePersist
    public void initTimeStamps() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        if (updatedAt == null) {
            updatedAt = new Date();
        }
        if (this.status == null) {
            this.status = CoreConstants.Status.Enabled;
        }
        if (Utils.isUserAuthenticated()) {
            this.createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
            this.updatedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        }
    }

    @PreUpdate
    public void updateTimeStamp() {
        updatedAt = new Date();
        if (Utils.isUserAuthenticated()) {
            this.updatedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        }
    }

    public String getCreatedAtAsString() {
        if(Utils.isNull(this.createdAt)) return null;
        return Utils.formatDate(this.createdAt, "dd/MM/yyyy HH:mm:ss");
    }

    public String getUpdatedAtAsString() {
        if(Utils.isNull(this.updatedAt)) return null;
        return Utils.formatDate(this.updatedAt, "dd/MM/yyyy HH:mm:ss");
    }
}

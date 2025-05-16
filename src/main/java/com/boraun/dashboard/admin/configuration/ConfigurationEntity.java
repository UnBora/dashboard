package com.boraun.dashboard.admin.configuration;


import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@Table(name = "admin_configuration")
@Data
public class ConfigurationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String configurationCode;

    @NotNull(message = "{configuration.value.null}")
    @Column(columnDefinition = "TEXT")
    private String configurationValue;

    @Column
    private String remark;

    private Date lastUsedAt;

    public ConfigurationEntity(String configurationCode, String configurationValue) {
        this.configurationCode = configurationCode;
        this.configurationValue = configurationValue;
    }
}

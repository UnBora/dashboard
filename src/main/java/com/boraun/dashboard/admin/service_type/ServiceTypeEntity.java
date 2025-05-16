package com.boraun.dashboard.admin.service_type;

import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "manage_service_type")
@Data
public class ServiceTypeEntity extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private Long id;

    @Column
    private String serviceTypeName;

    @Column
    private String description;
}

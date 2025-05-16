package com.boraun.dashboard.admin.language;

import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admin_language")
@Data
@NoArgsConstructor
public class LanguageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(length = 2)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String flag;

    public LanguageEntity(String code, String name, String flag) {
        this.code = code;
        this.name = name;
        this.flag = flag;
    }

}

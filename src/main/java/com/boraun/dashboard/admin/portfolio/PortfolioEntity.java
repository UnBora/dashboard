package com.boraun.dashboard.admin.portfolio;

import com.boraun.dashboard.admin.category.CategoryEntity;
import com.boraun.dashboard.admin.project.ProjectEntity;
import com.boraun.dashboard.common.BaseEntity;
import com.boraun.dashboard.admin.portfolioHistory.PortfolioHistoryEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "portfolio")
@NoArgsConstructor
public class PortfolioEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    @Lob
    private String description;
    private String phoneNumber;
    private String address;
    @Transient
    private MultipartFile file;
    @Transient
    private String profileUrl;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<ProjectEntity> projects = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<PortfolioHistoryEntity> history = new ArrayList<>();
}

package com.boraun.dashboard.admin.project;

import com.boraun.dashboard.admin.portfolio.PortfolioEntity;
import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "project")
@NoArgsConstructor
public class ProjectEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String image;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private PortfolioEntity portfolio;

//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
//    private List<Media> mediaList = new ArrayList<>();

//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
//    private List<ProjectFile> files = new ArrayList<>();

//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
//    private List<ProjectView> views = new ArrayList<>();

//    @ManyToMany
//    @JoinTable(
//            name = "project_tag",
//            joinColumns = @JoinColumn(name = "project_id"),
//            inverseJoinColumns = @JoinColumn(name = "tag_id")
//    )
//    private Set<Tag> tags = new HashSet<>();
}

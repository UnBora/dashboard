package com.boraun.dashboard.admin.portfolioHistory;

import com.boraun.dashboard.admin.portfolio.PortfolioEntity;
import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "portfolio_history")
public class PortfolioHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private PortfolioEntity portfolio;
}

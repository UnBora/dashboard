package com.boraun.dashboard.admin.portfolio;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Long> {
    Page<PortfolioEntity> findAllByStatusIn(Pageable pageable, Collection<CoreConstants.Status> statuses);

}

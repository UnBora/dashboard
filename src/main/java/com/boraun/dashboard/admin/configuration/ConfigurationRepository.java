package com.boraun.dashboard.admin.configuration;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, Long> {

    List<ConfigurationEntity> findAllByStatus(CoreConstants.Status status);

    Page<ConfigurationEntity> findAllByStatusIn(Pageable pageable, CoreConstants.Status[] statuses);

    Page<ConfigurationEntity> findAllByStatusInAndConfigurationCodeContaining(Pageable pageable, Collection<CoreConstants.Status> statuses, String configurationCode);

    Optional<ConfigurationEntity> findFirstByConfigurationCode(String code);

}

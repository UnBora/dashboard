package com.boraun.dashboard.admin.service_type;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, Long> {
    Page<ServiceTypeEntity> findAllByServiceTypeNameContainingAndStatusIn(Pageable pageable, String serviceTypeName, Collection<CoreConstants.Status> statuses);
}

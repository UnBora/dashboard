package com.boraun.dashboard.admin.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findAllByLogEventContainingAndTimeStampIsBetween(Pageable pageable, String logEvent, Date fromDate, Date toDate);
    Page<AuditLogEntity> findAllByNewObjectContainingAndTimeStampIsBetween(Pageable pageable, String newObject, Date fromDate, Date toDate);
}

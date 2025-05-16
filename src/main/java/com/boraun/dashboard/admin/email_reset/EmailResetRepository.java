package com.boraun.dashboard.admin.email_reset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailResetRepository extends JpaRepository<EmailResetEntity, Long> {
    EmailResetEntity findByToken(String token);

}

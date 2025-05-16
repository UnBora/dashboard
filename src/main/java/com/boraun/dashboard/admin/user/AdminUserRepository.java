package com.boraun.dashboard.admin.user;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUserEntity, Long> {
    Optional<AdminUserEntity> findAdminUserEntityByUsername(String username);

    Optional<AdminUserEntity> findFirstByUsernameAndStatus(String username, CoreConstants.Status status);

    Page<AdminUserEntity> findAllByDisplayNameContainingAndStatusIn(Pageable pageable, String displayName ,Collection<CoreConstants.Status> statuses);

}

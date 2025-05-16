package com.boraun.dashboard.admin.role;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRoleEntity, Long> {

    List<AdminRoleEntity> findAllByStatus(CoreConstants.Status status);

    Page<AdminRoleEntity> findAllByStatusIn(Pageable pageable, CoreConstants.Status[] statuses);

    Page<AdminRoleEntity> findAllByRoleNameContainingAndStatusIn(Pageable pageable, String roleName, Collection<CoreConstants.Status> statuses);
}

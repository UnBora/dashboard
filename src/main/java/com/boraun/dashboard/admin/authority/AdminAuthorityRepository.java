package com.boraun.dashboard.admin.authority;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminAuthorityRepository extends JpaRepository<AdminAuthorityEntity, Long> {
    Optional<AdminAuthorityEntity> findAdminAuthorityEntityByEndPointUrl(String endpointUrl);
    Optional<AdminAuthorityEntity> findFirstByEndPointUrl(String endpointUrl);

    Optional<AdminAuthorityEntity> findAdminAuthorityEntityByAuthorityNameAndMenuIsTrue(String authorityName);

    Optional<AdminAuthorityEntity> findAdminAuthorityEntityByAuthorityName(String authorityName);

    List<AdminAuthorityEntity> findAllByStatus(CoreConstants.Status status);

    List<AdminAuthorityEntity> findAllByParent_IdAndStatus(Long parentId, CoreConstants.Status status);

    Page<AdminAuthorityEntity> findAllByAuthorityNameContainingIgnoreCaseAndStatusIn(Pageable pageable, String authorityName, Collection<CoreConstants.Status> statuses);

    List<AdminAuthorityEntity> findAllByMenuIs(boolean isMenu);

    List<AdminAuthorityEntity> findAllByParent_IdAndStatusAndMenuIs(Long parentId, CoreConstants.Status status, boolean menu);

}

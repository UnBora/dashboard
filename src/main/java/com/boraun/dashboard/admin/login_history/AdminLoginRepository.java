package com.boraun.dashboard.admin.login_history;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AdminLoginRepository extends JpaRepository<AdminLoginEntity, Long> {

    List<AdminLoginEntity> findAdminLoginEntityBySessionIdAndStatus(String sessionId, CoreConstants.Status status);
//
//    Page<AdminLoginEntity> findAllByCreatedAtIsBetweenAndStatusIn(Pageable pageable, Date fromDate, Date toDate, CoreConstants.Status[] statuses);
//
//    Page<AdminLoginEntity> findAllByStatusIn(Pageable pageable, CoreConstants.Status[] statuses);
//
    Page<AdminLoginEntity> findAllByUsernameContainingAndStatusIn(Pageable pageable, String displayName, Collection<CoreConstants.Status> status);

    Page<AdminLoginEntity> findAllById(Pageable pageable, Long id);
//    Page<AdminLoginEntity> findAllByStatusInAndAdminUserEntity_AdminUserId(Pageable pageable, CoreConstants.Status[] statuses, Long adminUserId);
//
//    Page<AdminLoginEntity> findAllByAdminBrowserEntity_BrowserId(Pageable pageable, Long browserId);
//
////    @Query(value = "FROM AdminLoginEntity A WHERE A.adminLoginId IN (SELECT MAX(B.adminLoginId) FROM AdminLoginEntity B GROUP BY B.pushToken) AND A.adminUserEntity.adminUserName =?1")
////    List<AdminLoginEntity> findAllByAdminUsername(String adminUsername);
//
//    Page<AdminLoginEntity> findAllByAdminUserEntity_AdminUserName(Pageable pageable, String adminUsername);
}

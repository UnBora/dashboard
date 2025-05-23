package com.boraun.dashboard.admin.attachment;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
    List<AttachmentEntity> findAllByReferenceResourceAndReferenceIdAndStatus(String referenceResource, Long referenceId, CoreConstants.Status status);

    Optional<AttachmentEntity> findByReferenceIdAndStatus(Long referenceId, CoreConstants.Status status);


}

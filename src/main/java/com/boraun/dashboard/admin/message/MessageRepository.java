package com.boraun.dashboard.admin.message;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    MessageEntity findMessageEntityByKeyAndLocaleAndStatus(String key, String locale, CoreConstants.Status status);
    MessageEntity findFirstByKeyAndLocaleAndStatus(String key, String locale, CoreConstants.Status status);

    Page<MessageEntity> findAllByKeyContainingAndStatusIn(Pageable pageable, String key, Collection<CoreConstants.Status> statuses);
}

package com.boraun.dashboard.admin.language;

import com.boraun.dashboard.common.CoreConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {

    Page<LanguageEntity> findAllByStatusIn(Pageable pageable, Collection<CoreConstants.Status> statuses);

    LanguageEntity findLanguageEntityByCode(String code);

    List<LanguageEntity> findAllByStatus(CoreConstants.Status status);
}

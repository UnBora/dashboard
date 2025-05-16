package com.boraun.dashboard.admin.language;

import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class LanguageService extends WebAdminBaseService<LanguageEntity, Long> {

    private final LanguageRepository repository;

    @Autowired
    protected LanguageService(LanguageRepository repository) {
        super(LanguageEntity.class, repository);
        this.repository = repository;
    }

    @Override
    public Pager<LanguageEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        Page<LanguageEntity> entities = repository.findAllByStatusIn(pageable, status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public LanguageEntity add(LanguageEntity entity) {
        entity.setFlag("https://flagsapi.com/" + StringUtils.upperCase(entity.getCode()) + "/flat/64.png");
        return super.add(entity);
    }

    @Override
    public LanguageEntity update(Long aLong, LanguageEntity entity) {
        return null;
    }

    public List<LanguageEntity> fetchAllAvailableLanguages() {
        List<LanguageEntity> languageEntities = new ArrayList<>();
        String[] languages = Locale.getISOLanguages();
        for (String language : languages) {
            Locale locale = new Locale(language);
            languageEntities.add(new LanguageEntity(language, locale.getDisplayName(), ""));
        }
        languageEntities.sort(Comparator.comparing(LanguageEntity::getName));
        return languageEntities;
    }

}

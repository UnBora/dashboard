package com.boraun.dashboard.admin.login_history;


import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AdminLoginService extends WebAdminBaseService<AdminLoginEntity, Long> {

    private final AdminLoginRepository adminLoginRepository;
    private final SessionRegistry sessionRegistry;

    @Autowired
    public AdminLoginService(AdminLoginRepository adminLoginRepository, @Lazy SessionRegistry sessionRegistry) {
        super(AdminLoginEntity.class, adminLoginRepository);
        this.adminLoginRepository = adminLoginRepository;
        this.sessionRegistry = sessionRegistry;
    }

    public AdminLoginEntity add(String username, String sessionId, String browserAgent, String clientIp, String message, String eventName) {
        AdminLoginEntity entity = new AdminLoginEntity();
        entity.setSessionId(sessionId);
        entity.setStatus(CoreConstants.Status.Enabled);
        entity.setClientIPAddress(clientIp);
        entity.setUsername(username);
        entity.setBrowserAgent(browserAgent);
        entity.setMessage(message);
        entity.setEventName(eventName);
        adminLoginRepository.saveAndFlush(entity);
        return entity;
    }

    @Override
    public Pager<AdminLoginEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        String userName = search.orElse(StringUtils.EMPTY);
        Page<AdminLoginEntity> entities = adminLoginRepository.findAllByUsernameContainingAndStatusIn(pageable, userName, status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public AdminLoginEntity update(Long aLong, AdminLoginEntity entity) {
        return null;
    }

    public void expireNow(String sessionId) {
        try {
            SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
            if (Utils.nonNull(sessionInformation)) {
                if (!sessionRegistry.getSessionInformation(sessionId).isExpired()) {
                    sessionRegistry.getSessionInformation(sessionId).expireNow();
                    log.info(sessionId + " make expired successfully");
                }
            } else {
                log.warn(sessionId + " not found in sessionRegistry");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            List<AdminLoginEntity> entities = adminLoginRepository.findAdminLoginEntityBySessionIdAndStatus(sessionId, CoreConstants.Status.Enabled);
            if (!entities.isEmpty()) {
                entities.forEach(adminLoginEntity -> {
                    adminLoginEntity.setStatus(CoreConstants.Status.Disabled);
                });
                adminLoginRepository.saveAllAndFlush(entities);
                log.info(sessionId + " is updated in database");
            } else {
                log.warn(sessionId + " is not found in database");
            }
        }
    }

    public Pager<AdminLoginEntity> findAllHistory(Optional<Integer> pageNumber, Optional<Integer> pageSize, String userName){
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        Page<AdminLoginEntity> entities = adminLoginRepository.findAllByUsernameContainingAndStatusIn(pageable,userName, Collections.singleton(CoreConstants.Status.Enabled));
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    public Pager<AdminLoginEntity> findAllHistoryById(Optional<Integer> pageNumber, Optional<Integer> pageSize, Long id){
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        Page<AdminLoginEntity> entities = adminLoginRepository.findAllById(pageable, id);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }
}

package com.boraun.dashboard.admin.role;


import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.authority.AdminAuthorityEntity;
import com.boraun.dashboard.admin.authority.AdminAuthorityRepository;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Service
public class AdminRoleService extends WebAdminBaseService<AdminRoleEntity, Long> {
    private final AdminRoleRepository repository;
    private final AdminAuthorityRepository adminAuthorityRepository;
    private final AuditLogService auditLogService;

    @Autowired
    protected AdminRoleService(AdminRoleRepository repository, AdminAuthorityRepository adminAuthorityRepository, AuditLogService auditLogService) {
        super(AdminRoleEntity.class, repository);
        this.repository = repository;
        this.adminAuthorityRepository = adminAuthorityRepository;
        this.auditLogService = auditLogService;
    }

    public AdminRoleEntity addDefaultRole(List<AdminAuthorityEntity> authorityEntities) {
        AdminRoleEntity entity = repository.findById(1L).orElse(null);
        if (entity == null) {
            entity = new AdminRoleEntity();
            entity.setStatus(CoreConstants.Status.Enabled);
            entity.setRoleName("ADMINISTRATOR");
            entity.setAdminAuthorityEntities(authorityEntities);
            entity.setCreatedBy("System");
            entity = repository.save(entity);
        }
        return entity;
    }

    public List<AdminRoleEntity> findAllByStatus(CoreConstants.Status status) {
        return repository.findAllByStatus(status);
    }

    public void save(List<AdminRoleEntity> roleEntities) {
        repository.saveAll(roleEntities);
    }

    public AdminRoleEntity save(AdminRoleEntity roleEntity) {
        return repository.saveAndFlush(roleEntity);
    }

    public List<AdminAuthorityEntity> getSelectedAdminAuthority(List<Long> authorityId, List<AdminAuthorityEntity> adminNavigationEntities) {
        List<AdminAuthorityEntity> selectedAdminAuthorityEntities = new ArrayList<>();
        authorityId.forEach(aLong -> {
            Optional<AdminAuthorityEntity> entity = adminNavigationEntities.stream().filter(x -> x.getId().equals(aLong)).findFirst();
            if (entity.isPresent()) {
                selectedAdminAuthorityEntities.add(entity.get());
                if (Utils.nonNull(entity.get().getParent())) {
                    if (selectedAdminAuthorityEntities.stream().noneMatch(x -> x.getId().equals(entity.get().getParent().getId()))) {
                        selectedAdminAuthorityEntities.add(entity.get().getParent());
                    }
                }
            }
        });
        return selectedAdminAuthorityEntities;
    }

    public List<AdminRoleEntity> findAll() {
        return repository.findAll();
    }

    public Page<AdminRoleEntity> findAllByStatusIn(Pageable pageable, CoreConstants.Status[] selectedStatus) {
        return repository.findAllByStatusIn(pageable, selectedStatus);
    }

    @Override
    public Pager<AdminRoleEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        String roleName = search.orElse(StringUtils.EMPTY);
        Page<AdminRoleEntity> entities = repository.findAllByRoleNameContainingAndStatusIn(pageable, roleName, status);
        return new Pager<>(entities.getTotalPages(), entities.getNumber(), CoreConstants.BUTTONS_TO_SHOW, entities);
    }

    @Override
    public AdminRoleEntity update(Long id, AdminRoleEntity entity) {
        AdminRoleEntity db = this.findOne(id);
        AdminRoleEntity oldEntity = new AdminRoleEntity();
        AdminRoleEntity db002 = new AdminRoleEntity();
        db002.setId(db.getId());
        db002.setRoleName(db.getRoleName());
        db002.setAdminAuthorityEntities(db.getAdminAuthorityEntities());
        db002.setStatus(db.getStatus());
        BeanUtils.copyProperties(db, oldEntity);
        db.setRoleName(entity.getRoleName());
        db.setAdminAuthorityEntities(this.getSelectedAdminAuthority(entity.getSelectedAdminAuthorityId(), adminAuthorityRepository.findAll()));
        db = repository.saveAndFlush(db);

        //audit log
        auditLogService.save(db002, db002, AuditLogConstants.AuditLogType.UPDATED);
        return db;
    }

    @Override
    public AdminRoleEntity add(AdminRoleEntity entity) {
        entity.setStatus(CoreConstants.Status.Enabled);
        entity.setAdminAuthorityEntities(getSelectedAdminAuthority(entity.getSelectedAdminAuthorityId(), adminAuthorityRepository.findAll()));
        entity = repository.saveAndFlush(entity);
        //audit
        auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);
        return entity;
    }

    @Override
    public void delete(Long aLong) {
        AdminRoleEntity entity = this.findOne(aLong);
        if (Objects.nonNull(entity)) {
            entity.setStatus(CoreConstants.Status.Deleted);
            repository.saveAndFlush(entity);
        }
    }
}

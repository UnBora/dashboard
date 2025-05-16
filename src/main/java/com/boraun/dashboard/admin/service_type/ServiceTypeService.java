package com.boraun.dashboard.admin.service_type;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ServiceTypeService extends WebAdminBaseService<ServiceTypeEntity, Long> {

    private final ServiceTypeRepository typeRepository;
    private final AuditLogService auditLogService;

    protected ServiceTypeService(ServiceTypeRepository repository, AuditLogService auditLogService) {
        super(ServiceTypeEntity.class, repository);
        this.typeRepository = repository;
        this.auditLogService = auditLogService;
    }
    @Override
    public Pager<ServiceTypeEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        String key = search.orElse(StringUtils.EMPTY);
        Page<ServiceTypeEntity> entities = typeRepository.findAllByServiceTypeNameContainingAndStatusIn(pageable, key, status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities
        );
    }

    @Override
    public ServiceTypeEntity update(Long aLong, ServiceTypeEntity entity) {
        ServiceTypeEntity serviceTypeEntity = typeRepository.findById(aLong).orElse(null);
        ServiceTypeEntity oldEntity = new ServiceTypeEntity();
        BeanUtils.copyProperties(serviceTypeEntity, oldEntity);
        if (serviceTypeEntity != null) {
            serviceTypeEntity.setServiceTypeName(entity.getServiceTypeName());
            serviceTypeEntity.setDescription(entity.getDescription());
            serviceTypeEntity = typeRepository.saveAndFlush(serviceTypeEntity);
            auditLogService.save(oldEntity, serviceTypeEntity, AuditLogConstants.AuditLogType.UPDATED);
            return serviceTypeEntity;
        }
        return null;
    }

    @Override
    public void delete(Long aLong) {
        ServiceTypeEntity serviceTypeEntity = this.findOne(aLong);
        if (Objects.nonNull(serviceTypeEntity)) {
            serviceTypeEntity.setStatus(CoreConstants.Status.Deleted);
            typeRepository.saveAndFlush(serviceTypeEntity);
        }
    }

    public List<ServiceTypeEntity> serviceTypeEntities(){
        return typeRepository.findAll();
    }
}

package com.boraun.dashboard.admin.portfolio;

import com.boraun.dashboard.admin.attachment.AttachmentService;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PortfolioService extends WebAdminBaseService<PortfolioEntity, Long> {
    private PortfolioRepository portfolioRepository;
    private AttachmentService attachmentService;
    protected PortfolioService(JpaRepository<PortfolioEntity, Long> repository, PortfolioRepository portfolioRepository, AttachmentService attachmentService) {
        super(PortfolioEntity.class, repository);
        this.portfolioRepository = portfolioRepository;
        this.attachmentService = attachmentService;
    }

    @Override
    public Pager<PortfolioEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        String key = search.orElse(StringUtils.EMPTY);
//        String content = otherParameters.containsKey("fParam1") ? otherParameters.get("fParam1").toString() : StringUtils.EMPTY;
        Page<PortfolioEntity> entities = portfolioRepository.findAllByStatusIn(pageable,status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public PortfolioEntity add(PortfolioEntity entity){

            super.add(entity);
            if (entity.getFile() != null && !entity.getFile().isEmpty()) {
                attachmentService.saveAttachment(entity.getFile(), PortfolioEntity.class.getName().toString(),entity.getId(), "Portfolio");
            }
            return entity;

    }

    @Override
    public PortfolioEntity update(Long id, PortfolioEntity updatedEntity) {
        PortfolioEntity existingEntity = portfolioRepository.findById(id).orElse(null);

        if (existingEntity != null) {
            // Optional: Keep a copy of the old entity if you want to use it for audit logs
            PortfolioEntity oldEntity = new PortfolioEntity();
            BeanUtils.copyProperties(existingEntity, oldEntity);

            // Update fields (make sure you do not override the ID or relationships unintentionally)
            existingEntity.setFirstname(updatedEntity.getFirstname());
            existingEntity.setLastname(updatedEntity.getLastname());
            existingEntity.setEmail(updatedEntity.getEmail());
            existingEntity.setDescription(updatedEntity.getDescription());
            existingEntity.setPhoneNumber(updatedEntity.getPhoneNumber());
            existingEntity.setAddress(updatedEntity.getAddress());
            existingEntity.setCategory(updatedEntity.getCategory());

            // You might want to handle projects/history update differently (e.g., add/merge/update)
            existingEntity.setProjects(updatedEntity.getProjects());
            existingEntity.setHistory(updatedEntity.getHistory());

            // Save and return
            PortfolioEntity savedEntity = portfolioRepository.save(existingEntity);

            // Optional: Audit log
            // auditLogService.save(oldEntity, savedEntity, AuditLogConstants.AuditLogType.UPDATED);

            return savedEntity;
        }

        return null;
    }
}

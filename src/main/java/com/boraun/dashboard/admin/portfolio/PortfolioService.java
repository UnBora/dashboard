package com.boraun.dashboard.admin.portfolio;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.base.WebAdminBaseService;
import com.boraun.dashboard.admin.message.MessageEntity;
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

    protected PortfolioService(JpaRepository<PortfolioEntity, Long> repository, PortfolioRepository portfolioRepository) {
        super(PortfolioEntity.class, repository);
        this.portfolioRepository = portfolioRepository;
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
    public PortfolioEntity update(Long aLong, PortfolioEntity entity) {
        PortfolioEntity message = portfolioRepository.findById(aLong).orElse(null);
        PortfolioEntity oldMessage = new PortfolioEntity();
        BeanUtils.copyProperties(message, oldMessage);
//        if (message != null) {
//            message.setContent(entity.getContent());
//            message.setKey(entity.getKey());
//            message = repository.saveAndFlush(message);
//            //audit log
//            auditLogService.save(oldMessage, message, AuditLogConstants.AuditLogType.UPDATED);
//            return message;
//        }
        return null;
    }
}

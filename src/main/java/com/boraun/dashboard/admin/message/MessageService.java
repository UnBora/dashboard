package com.boraun.dashboard.admin.message;

import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MessageService extends WebAdminBaseService<MessageEntity, Long> {

    private final MessageRepository repository;
    private final AuditLogService auditLogService;

    @Autowired
    protected MessageService(MessageRepository repository, AuditLogService auditLogService) {
        super(MessageEntity.class, repository);
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Override
    public Pager<MessageEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> search, Collection<CoreConstants.Status> status, Optional<String> fromDate, Optional<String> toDate, Map<String, Object> otherParameters) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "createdAt");
        String key = search.orElse(StringUtils.EMPTY);
//        String content = otherParameters.containsKey("fParam1") ? otherParameters.get("fParam1").toString() : StringUtils.EMPTY;
        Page<MessageEntity> entities = repository.findAllByKeyContainingAndStatusIn(pageable, key, status);
        return new Pager<>(
                entities.getTotalPages()
                , entities.getNumber()
                , CoreConstants.BUTTONS_TO_SHOW
                , entities);
    }

    @Override
    public MessageEntity update(Long aLong, MessageEntity entity) {
        MessageEntity message = repository.findById(aLong).orElse(null);
        MessageEntity oldMessage = new MessageEntity();
        BeanUtils.copyProperties(message, oldMessage);
        if (message != null) {
            message.setContent(entity.getContent());
            message.setKey(entity.getKey());
            message = repository.saveAndFlush(message);
            //audit log
            auditLogService.save(oldMessage, message, AuditLogConstants.AuditLogType.UPDATED);
            return message;
        }
        return null;
    }
}

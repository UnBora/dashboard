package com.boraun.dashboard.admin.auditlog;

import com.boraun.dashboard.common.BaseEntity;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Pager;
import com.boraun.dashboard.common.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ReferenceChange;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.CollectionChange;
import org.javers.core.metamodel.object.GlobalId;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.javers.core.Javers;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
@Transactional
public class AuditLogService {
    @Autowired
    protected HttpServletRequest httpServletRequest;

    private final Javers javers;

    private final AuditLogRepository repository;

    @Autowired
    public AuditLogService(Javers javers, AuditLogRepository repository) {
        this.javers = javers;
        this.repository = repository;
    }

    public synchronized <T> void save(T object, String id, String duplicateId, AuditLogConstants.AuditLogType auditLogType) {
        AuditLogEntity auditLog = new AuditLogEntity();
        String logKey = object.getClass().getSimpleName().concat("/").concat(id);
        if (auditLogType.equals(AuditLogConstants.AuditLogType.SAVED)) {
            auditLog.setOldObject(new JSONObject().toString());
            auditLog.setNewObject(Utils.toJsonString(object));
            auditLog.setRemark("{}: Save successfully");
        } else if (auditLogType.equals(AuditLogConstants.AuditLogType.CHANGE_STATUS)) {
            BaseEntity status = (BaseEntity) object;
            String oldStatus = status.getStatus() == CoreConstants.Status.Enabled ? CoreConstants.Status.Disabled.name() : CoreConstants.Status.Enabled.name();
            String newStatus = status.getStatus().name();
            auditLog.setOldObject("{\"status\": \"" + oldStatus + "\"}");
            auditLog.setNewObject("{\"status\": \"" + newStatus + "\"}");
            auditLog.setRemark("Status changed: " + oldStatus + " to " + newStatus);
        } else if (auditLogType.equals(AuditLogConstants.AuditLogType.DELETED)) {
            auditLog.setOldObject(Utils.toJsonString(object));
            auditLog.setNewObject(new JSONObject().toString());
            auditLog.setRemark("{}: Delete successfully");
        } else if (auditLogType.equals(AuditLogConstants.AuditLogType.DUPLICATED)) {
            String logDuplicateKey = object.getClass().getSimpleName().concat("/").concat(duplicateId);
            auditLog.setOldObject(new JSONObject().toString());
            auditLog.setNewObject(Utils.toJsonString(object));
            auditLog.setRemark("{}: Duplicate successfully from " + logDuplicateKey);
        }
        auditLog.setLogKey(logKey);
        auditLog.setRemoteId(Utils.getClientIP(httpServletRequest));
        auditLog.setLogEvent(logKey.concat(StringUtils.SPACE).concat(auditLogType.name()));
        auditLog.setUsername(getPrincipleName());
        auditLog.setTimeStamp(new Date());
        repository.save(auditLog);
    }

    public synchronized <T> void save(T object, String id, AuditLogConstants.AuditLogType auditLogType) {
        this.save(object, id, null, auditLogType);
    }

    public synchronized <T> void save(T oldObject, T newObject, AuditLogConstants.AuditLogType auditLogType) {
        Diff diff = javers.compare(oldObject, newObject);
        JSONObject oldJsonObject = new JSONObject();
        JSONObject newJsonObject = new JSONObject();
        GlobalId globalId = null;
        StringBuilder remarkBuilder = new StringBuilder();
        AtomicInteger valueChangeSize = new AtomicInteger();
        List<ValueChange> valueChanges = diff.getChangesByType(ValueChange.class);
        if (!CollectionUtils.isEmpty(valueChanges)) {
            globalId = valueChanges.get(0).getAffectedGlobalId();
            valueChanges.forEach(valueChange -> {
                if (!valueChange.getPropertyName().contains("updatedAt") && !valueChange.getPropertyName().contains("updatedBy")) {
                    valueChangeSize.getAndIncrement();
                    log.debug("Diff value changed: " + valueChange.getPropertyName() + ",  old = " + valueChange.getLeft() + ", new = " + valueChange.getRight());
                    try {
                        oldJsonObject.put(valueChange.getPropertyName(), valueChange.getLeft());
                        newJsonObject.put(valueChange.getPropertyName(), valueChange.getRight());
                    } catch (JSONException e) {
                        log.error("Error set values changed to json string ", e);
                    }
                }
            });
        }
        remarkBuilder.append("Value changed size: ").append(valueChangeSize.get()).append(StringUtils.SPACE);
        List<ReferenceChange> referenceChanges = diff.getChangesByType(ReferenceChange.class);
        if (!CollectionUtils.isEmpty(referenceChanges)) {
            if (globalId == null) {
                globalId = referenceChanges.get(0).getAffectedGlobalId();
            }
            referenceChanges.forEach(referenceChange -> {
                log.debug("Diff value changed: " + referenceChange.getPropertyName() + ",  old = " + referenceChange.getLeft().value() + ", new = " + referenceChange.getRight().value());
                try {
                    oldJsonObject.put(referenceChange.getPropertyName(), referenceChange.getLeft() != null ? referenceChange.getLeft().value().split("/")[1] : null);
                    newJsonObject.put(referenceChange.getPropertyName(), referenceChange.getRight() != null ? referenceChange.getRight().value().split("/")[1] : null);
                } catch (JSONException e) {
                    log.error("Error set reference changed to json string ", e);
                }
            });
        }
        remarkBuilder.append("Reference changed size: ").append(referenceChanges.size()).append(StringUtils.SPACE);
        List<CollectionChange> collectionChanges = diff.getChangesByType(CollectionChange.class);
        if (!CollectionUtils.isEmpty(collectionChanges)) {
            if (globalId == null) {
                globalId = collectionChanges.get(0).getAffectedGlobalId();
            }
            collectionChanges.forEach(collectionChange -> {
                log.debug("Diff value changed: " + collectionChange.getPropertyName() + " {}: " + javers.getJsonConverter().toJson(collectionChange.getChanges()));
                if (!CollectionUtils.isEmpty(collectionChange.getChanges())) {
                    try {
                        newJsonObject.put(collectionChange.getPropertyName(), javers.getJsonConverter().toJson(collectionChange.getChanges()));
                    } catch (JSONException e) {
                        log.error("Error set reference changed to json string ", e);
                    }
                }
            });
            remarkBuilder.append("Collection changed size: ").append(collectionChanges.size()).append(StringUtils.SPACE);
        }
        String globalIdValue = StringUtils.EMPTY;
        if (globalId != null) {
            globalIdValue = this.splitGlobalId(globalId.value());
        }
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setLogKey(globalIdValue);
        auditLog.setOldObject(oldJsonObject.toString());
        auditLog.setNewObject(newJsonObject.toString());
        auditLog.setRemoteId(Utils.getClientIP(httpServletRequest));
        auditLog.setLogEvent(globalIdValue.concat(StringUtils.SPACE).concat(auditLogType.name()));
        auditLog.setUsername(getPrincipleName());
        auditLog.setTimeStamp(new Date());
        auditLog.setRemark(remarkBuilder.toString());
        repository.save(auditLog);
    }

    private String splitGlobalId(String packageNameGlobalId) {
        String[] tmp = packageNameGlobalId.split("\\.");
        return tmp[tmp.length - 1];
    }

    private String getPrincipleName() {
        HttpSession session = httpServletRequest.getSession(true);
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext != null && securityContext.getAuthentication() != null) {
            return securityContext.getAuthentication().getName();
        }
        return "";
    }

    public Pager<AuditLogEntity> findAll(Optional<Integer> pageNumber, Optional<Integer> pageSize, Optional<String> fSearch, Optional<String> fromDateToDate) {
        int selectedPageSize = pageSize.orElse(CoreConstants.INITIAL_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Utils.getPageNumber(pageNumber), selectedPageSize, Sort.Direction.DESC, "timeStamp");
        String logEvent = fSearch.orElse(StringUtils.EMPTY);
        String newObject = fSearch.orElse(StringUtils.EMPTY);
        String defaultFromDateToDate = Utils.formatDate(Utils.getDate(-1), "dd-MM-yyyy") + " to " + Utils.getDate("dd-MM-yyyy");
        Date fromDateAsDate = Utils.getDate(fromDateToDate.orElse(defaultFromDateToDate).split("to")[0], "dd-MM-yyyy");
        Date toDateAsDate = Utils.getDate(fromDateToDate.orElse(defaultFromDateToDate).split("to")[1], "dd-MM-yyyy");
        toDateAsDate.setTime(toDateAsDate.getTime() + (24 * 3600 * 1000) - 1); // time, end of day.
        Page<AuditLogEntity> entities = repository.findAllByLogEventContainingAndTimeStampIsBetween(pageable, logEvent, fromDateAsDate, toDateAsDate);
        if(entities == null){
            entities = repository.findAllByNewObjectContainingAndTimeStampIsBetween(pageable, newObject, fromDateAsDate, toDateAsDate);
        }
        return new Pager<>(
                entities.getTotalPages(),
                entities.getNumber(),
                CoreConstants.BUTTONS_TO_SHOW,
                entities);
    }

    public AuditLogEntity findById(Long id) {
        return repository.findById(id).orElse(null);
    }
}

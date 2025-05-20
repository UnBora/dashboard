package com.boraun.dashboard.admin.attachment;


import com.boraun.dashboard.admin.auditlog.AuditLogConstants;
import com.boraun.dashboard.admin.auditlog.AuditLogService;
import com.boraun.dashboard.admin.configuration.ConfigurationService;
import com.boraun.dashboard.common.CoreConstants;
import com.boraun.dashboard.common.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@Slf4j
public class AttachmentService {
    private final AttachmentRepository repository;

    private final ConfigurationService configurationService;

//    private final MinioService minioService;

    @Autowired
    private AuditLogService auditLogService;

    public AttachmentService(AttachmentRepository repository, ConfigurationService configurationService) {
        this.repository = repository;
        this.configurationService = configurationService;

    }

    public void addAttachments(MultipartFile[] files, Class<?> resource, Long referenceId, String[] remarks) {
        try {
            if (files != null && remarks != null) {
                if (remarks.length == files.length) {
                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].isEmpty()) {
                            log.info("File Size: " + files[i].getSize());
                            String extension = Utils.getExtensionFilename(files[i]);
                            AttachmentEntity entity = new AttachmentEntity();
                            entity.setReferenceResource(getReferenceResource(resource));
                            entity.setReferenceId(referenceId);
                            entity.setExtension(extension);
                            entity.setOriginalFileName(files[i].getOriginalFilename());
                            byte[] contentData = files[i].getBytes();
                            if (configurationService.isAllowedFileExtension(extension)) {
                                if(!StringUtils.equals(extension, "pdf")){
                                    contentData = Utils.compressFileImage(files[i]);
                                }
                            }
                            entity.setContent(contentData);
                            entity.setRemark(remarks[i]);
                            repository.saveAndFlush(entity);
//                            this.moveToMinIO(entity);
                            //Save Log attachment one by one after add
                            auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.error("Handle addAttachment ", e);
        }
    }

    public void addSingleAttachment(MultipartFile files, Class<?> resource, Long referenceId, String remarks) {
        try {
            if (files != null && remarks != null) {
                if (!files.isEmpty()) {
                    log.info("File Size: " + files.getSize());
                    String extension = Utils.getExtensionFilename(files);
                    AttachmentEntity entity = new AttachmentEntity();
                    entity.setReferenceResource(getReferenceResource(resource));
                    entity.setReferenceId(referenceId);
                    entity.setExtension(extension);
                    entity.setOriginalFileName(files.getOriginalFilename());
                    byte[] contentData = files.getBytes();
                    if (configurationService.isAllowedFileExtension(extension)) {
                        if (!StringUtils.equals(extension, "pdf")) {
                            contentData = Utils.compressFileImage(files);
                        }
                    }
                    entity.setContent(contentData);
                    entity.setRemark(remarks);
                    repository.saveAndFlush(entity);
//                    this.moveToMinIO(entity);
                    // Save log attachment one by one after update
                    auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);

                }
            }

        } catch (IOException e) {
            log.error("Handle addAttachment ", e);
        }
    }
    private String getReferenceResource(Class<?> resource) {
        String referenceResource = Utils.getLastPackageName(resource).toLowerCase();
        if (resource.isAnnotationPresent(AttachmentResource.class)) {
            final AttachmentResource attachmentResource = resource.getAnnotation(AttachmentResource.class);
            referenceResource = attachmentResource.name();
        }
        return referenceResource;
    }


    public List<AttachmentEntity> getAttachmentList(Class<?> resource, Long referenceId) {
        log.info("Query attachment by referenceId = {}", referenceId);
        List<AttachmentEntity> entities = repository.findAllByReferenceResourceAndReferenceIdAndStatus(getReferenceResource(resource), referenceId, CoreConstants.Status.Enabled);
        entities.forEach(x -> x.setFullUrl(configurationService.getSystemBaseUrl() + x.getResourceAttachmentUrl()));
        return entities;
    }

    public AttachmentEntity findByReferenceResourceAndAttachmentId(String referenceResource, Long attachmentId) {
        return repository.findFirstByReferenceResourceAndId(referenceResource, attachmentId).orElse(null);
    }

    public void changeAttachmentStatus(long attachmentId, CoreConstants.Status status) {
        repository.findById(attachmentId).ifPresent(attachmentEntity -> attachmentEntity.setStatus(status));
        AttachmentEntity val = repository.findById(attachmentId)
                .map(entity -> {
                    entity.setStatus(status);
                    return repository.save(entity);
                }).orElse(null);
        // Save log attachment one by one after update (remove attachment)
        auditLogService.save(val, String.valueOf(attachmentId), AuditLogConstants.AuditLogType.CHANGE_STATUS);
    }

//    public byte[] moveToMinIO(AttachmentEntity attachmentEntity) {
//        if (attachmentEntity == null) return null;
//        if(Utils.nonNull(attachmentEntity.getReferenceResource())){
//            byte[] data = minioService.getFile(attachmentEntity);
//            attachmentEntity.setContent(null);
//            repository.saveAndFlush(attachmentEntity);
//            return data;
//        }
//        return attachmentEntity.getContent();
//    }

}

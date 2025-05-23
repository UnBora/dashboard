package com.boraun.dashboard.admin.attachment;


import com.boraun.dashboard.common.CoreConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AttachmentService {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public AttachmentEntity saveAttachment(MultipartFile file, String referenceResource, Long referenceId, String remark) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be null or empty");
            }

            logger.info("File Size: " + file.getSize());

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // Read file content into byte array
            byte[] fileContent = file.getBytes();

            AttachmentEntity attachment = new AttachmentEntity();
            attachment.setOriginalFileName(originalFilename);
            attachment.setExtension(extension);
            attachment.setReferenceResource(referenceResource);
            attachment.setReferenceId(referenceId);
            attachment.setRemark(remark);
            attachment.setFileContent(fileContent);
            attachment.setFileSize(file.getSize());
            return attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save attachment", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    // Method to get file content - now simply returns the stored bytes
    public byte[] getAttachmentContent(AttachmentEntity attachment) {
        if (attachment == null || attachment.getFileContent() == null) {
            throw new IllegalArgumentException("Invalid attachment or no file content");
        }
        return attachment.getFileContent();
    }

    // Optional:
    public byte[] getAttachmentContentByReferenceId(Long id) {
        AttachmentEntity attachment = attachmentRepository.findByReferenceIdAndStatus(id, CoreConstants.Status.Enabled)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + id));
        return getAttachmentContent(attachment);
    }

    // Optional: Method to get attachment entity by ID
    public AttachmentEntity getAttachmentByReferenceId(Long ReferenceId) {
        return attachmentRepository.findByReferenceIdAndStatus(ReferenceId, CoreConstants.Status.Enabled)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + ReferenceId));
    }
}

//@Service
//@Transactional
//@Slf4j
//public class AttachmentService {
//    private final AttachmentRepository repository;
//
//    private final ConfigurationService configurationService;
//
////    private final MinioService minioService;
//
//    @Autowired
//    private AuditLogService auditLogService;
//
//    public AttachmentService(AttachmentRepository repository, ConfigurationService configurationService) {
//        this.repository = repository;
//        this.configurationService = configurationService;
//
//    }
//
//    public void addAttachments(MultipartFile[] files, Class<?> resource, Long referenceId, String[] remarks) {
//        try {
//            if (files != null && remarks != null) {
//                if (remarks.length == files.length) {
//                    for (int i = 0; i < files.length; i++) {
//                        if (!files[i].isEmpty()) {
//                            log.info("File Size: " + files[i].getSize());
//                            String extension = Utils.getExtensionFilename(files[i]);
//                            AttachmentEntity entity = new AttachmentEntity();
//                            entity.setReferenceResource(getReferenceResource(resource));
//                            entity.setReferenceId(referenceId);
//                            entity.setExtension(extension);
//                            entity.setOriginalFileName(files[i].getOriginalFilename());
//                            byte[] contentData = files[i].getBytes();
//                            if (configurationService.isAllowedFileExtension(extension)) {
//                                if(!StringUtils.equals(extension, "pdf")){
//                                    contentData = Utils.compressFileImage(files[i]);
//                                }
//                            }
//                            entity.setContent(contentData);
//                            entity.setRemark(remarks[i]);
//                            repository.saveAndFlush(entity);
////                            this.moveToMinIO(entity);
//                            //Save Log attachment one by one after add
//                            auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);
//                        }
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            log.error("Handle addAttachment ", e);
//        }
//    }
//
//    public void addSingleAttachment(MultipartFile files, Class<?> resource, Long referenceId, String remarks) {
//        try {
//            if (files != null && remarks != null) {
//                if (!files.isEmpty()) {
//                    log.info("File Size: " + files.getSize());
//                    String extension = Utils.getExtensionFilename(files);
//                    AttachmentEntity entity = new AttachmentEntity();
//                    entity.setReferenceResource(getReferenceResource(resource));
//                    entity.setReferenceId(referenceId);
//                    entity.setExtension(extension);
//                    entity.setOriginalFileName(files.getOriginalFilename());
//                    byte[] contentData = files.getBytes();
//                    if (configurationService.isAllowedFileExtension(extension)) {
//                        if (!StringUtils.equals(extension, "pdf")) {
//                            contentData = Utils.compressFileImage(files);
//                        }
//                    }
//                    entity.setContent(contentData);
//                    entity.setRemark(remarks);
//                    repository.saveAndFlush(entity);
////                    this.moveToMinIO(entity);
//                    // Save log attachment one by one after update
//                    auditLogService.save(entity, String.valueOf(entity.getId()), AuditLogConstants.AuditLogType.SAVED);
//
//                }
//            }
//
//        } catch (IOException e) {
//            log.error("Handle addAttachment ", e);
//        }
//    }
//    private String getReferenceResource(Class<?> resource) {
//        String referenceResource = Utils.getLastPackageName(resource).toLowerCase();
//        if (resource.isAnnotationPresent(AttachmentResource.class)) {
//            final AttachmentResource attachmentResource = resource.getAnnotation(AttachmentResource.class);
//            referenceResource = attachmentResource.name();
//        }
//        return referenceResource;
//    }
//
//
//    public List<AttachmentEntity> getAttachmentList(Class<?> resource, Long referenceId) {
//        log.info("Query attachment by referenceId = {}", referenceId);
//        List<AttachmentEntity> entities = repository.findAllByReferenceResourceAndReferenceIdAndStatus(getReferenceResource(resource), referenceId, CoreConstants.Status.Enabled);
//        entities.forEach(x -> x.setFullUrl(configurationService.getSystemBaseUrl() + x.getResourceAttachmentUrl()));
//        return entities;
//    }
//
//    public AttachmentEntity findByReferenceResourceAndAttachmentId(String referenceResource, Long attachmentId) {
//        return repository.findFirstByReferenceResourceAndId(referenceResource, attachmentId).orElse(null);
//    }
//
//    public void changeAttachmentStatus(long attachmentId, CoreConstants.Status status) {
//        repository.findById(attachmentId).ifPresent(attachmentEntity -> attachmentEntity.setStatus(status));
//        AttachmentEntity val = repository.findById(attachmentId)
//                .map(entity -> {
//                    entity.setStatus(status);
//                    return repository.save(entity);
//                }).orElse(null);
//        // Save log attachment one by one after update (remove attachment)
//        auditLogService.save(val, String.valueOf(attachmentId), AuditLogConstants.AuditLogType.CHANGE_STATUS);
//    }
//
////    public byte[] moveToMinIO(AttachmentEntity attachmentEntity) {
////        if (attachmentEntity == null) return null;
////        if(Utils.nonNull(attachmentEntity.getReferenceResource())){
////            byte[] data = minioService.getFile(attachmentEntity);
////            attachmentEntity.setContent(null);
////            repository.saveAndFlush(attachmentEntity);
////            return data;
////        }
////        return attachmentEntity.getContent();
////    }
//
//}

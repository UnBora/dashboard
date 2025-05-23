package com.boraun.dashboard.admin.attachment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/resources/attachment")
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;

    @Autowired
    public AttachmentController(AttachmentRepository attachmentRepository, AttachmentService attachmentService) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
    }

    @GetMapping("/{resource}/{id}.{extension}")
    public ResponseEntity<byte[]> getAttachment(
            @PathVariable("resource") String resource,
            @PathVariable("id") Long id,
            @PathVariable("extension") String extension) {

        try {
            Optional<AttachmentEntity> attachmentOpt = attachmentRepository.findById(id);

            if (attachmentOpt.isPresent()) {
                AttachmentEntity attachment = attachmentOpt.get();

                // Validate that this is the correct attachment
                if (!resource.equals(attachment.getReferenceResource()) ||
                        !extension.equals(attachment.getExtension())) {
                    return ResponseEntity.notFound().build();
                }

                // Get file content from database (now stored as bytes)
                byte[] content = attachmentService.getAttachmentContent(attachment);

                if (content == null || content.length == 0) {
                    return ResponseEntity.notFound().build();
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(getMediaType(extension));
                headers.setContentLength(content.length);

                // Optional: Add cache control headers for better performance
                headers.setCacheControl("max-age=3600"); // Cache for 1 hour

                // Add content disposition header for downloads if needed
                // headers.setContentDispositionFormData("attachment", attachment.getOriginalFileName());

                return new ResponseEntity<>(content, headers, HttpStatus.OK);
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Additional endpoint for forced download
    @GetMapping("/{resource}/{id}.{extension}/download")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable("resource") String resource,
            @PathVariable("id") Long id,
            @PathVariable("extension") String extension) {

        try {
            Optional<AttachmentEntity> attachmentOpt = attachmentRepository.findById(id);

            if (attachmentOpt.isPresent()) {
                AttachmentEntity attachment = attachmentOpt.get();

                // Validate that this is the correct attachment
                if (!resource.equals(attachment.getReferenceResource()) ||
                        !extension.equals(attachment.getExtension())) {
                    return ResponseEntity.notFound().build();
                }

                byte[] content = attachmentService.getAttachmentContent(attachment);

                if (content == null || content.length == 0) {
                    return ResponseEntity.notFound().build();
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(content.length);
                headers.setContentDispositionFormData("attachment", attachment.getOriginalFileName());

                return new ResponseEntity<>(content, headers, HttpStatus.OK);
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to get attachment metadata
    @GetMapping("/{resource}/{id}/info")
    public ResponseEntity<AttachmentInfo> getAttachmentInfo(
            @PathVariable("resource") String resource,
            @PathVariable("id") Long id) {

        try {
            Optional<AttachmentEntity> attachmentOpt = attachmentRepository.findById(id);

            if (attachmentOpt.isPresent()) {
                AttachmentEntity attachment = attachmentOpt.get();

                if (!resource.equals(attachment.getReferenceResource())) {
                    return ResponseEntity.notFound().build();
                }

                AttachmentInfo info = new AttachmentInfo();
                info.setId(attachment.getId());
                info.setOriginalFileName(attachment.getOriginalFileName());
                info.setExtension(attachment.getExtension());
                info.setFileSize(attachment.getFileSize());
                info.setReferenceResource(attachment.getReferenceResource());
                info.setReferenceId(attachment.getReferenceId());
                info.setRemark(attachment.getRemark());

                return ResponseEntity.ok(info);
            }

            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType getMediaType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "doc":
            case "docx":
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls":
            case "xlsx":
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "zip":
                return MediaType.valueOf("application/zip");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    // DTO class for attachment info
    public static class AttachmentInfo {
        private Long id;
        private String originalFileName;
        private String extension;
        private Long fileSize;
        private String referenceResource;
        private Long referenceId;
        private String remark;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOriginalFileName() { return originalFileName; }
        public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getReferenceResource() { return referenceResource; }
        public void setReferenceResource(String referenceResource) { this.referenceResource = referenceResource; }

        public Long getReferenceId() { return referenceId; }
        public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
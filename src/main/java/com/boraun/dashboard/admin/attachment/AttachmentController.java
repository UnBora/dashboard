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

import java.io.IOException;
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

        Optional<AttachmentEntity> attachmentOpt = attachmentRepository.findById(id);

        if (attachmentOpt.isPresent()) {
            AttachmentEntity attachment = attachmentOpt.get();

            // Validate that this is the correct attachment
            if (!resource.equals(attachment.getReferenceResource()) ||
                    !extension.equals(attachment.getExtension())) {
                return ResponseEntity.notFound().build();
            }

            try {
                // Get file content from disk
                byte[] content = attachmentService.getAttachmentContent(attachment);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(getMediaType(extension));
                // Add content disposition header for downloads if needed
                // headers.setContentDispositionFormData("attachment", attachment.getOriginalFileName());

                return new ResponseEntity<>(content, headers, HttpStatus.OK);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.notFound().build();
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
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
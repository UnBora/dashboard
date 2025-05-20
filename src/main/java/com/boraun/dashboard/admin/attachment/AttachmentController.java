package com.boraun.dashboard.admin.attachment;

import com.boraun.dashboard.common.Utils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping(value = "/resources", name = "Resources")
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }


    @GetMapping(value = "/attachment/{referenceResource}/{attachmentFileName}")
    public void getAttachmentByReferenceResource(@PathVariable String referenceResource, @PathVariable String attachmentFileName, HttpServletResponse response) throws IOException {
        String attachmentId = attachmentFileName.split("\\.")[0];
        AttachmentEntity entity = attachmentService.findByReferenceResourceAndAttachmentId(referenceResource, Long.parseLong(attachmentId));
        if (Utils.nonNull(entity)) {
//            byte[] data = attachmentService.moveToMinIO(entity);
            switch (entity.getExtension().toLowerCase()) {
                case "jpg", "jpeg" -> response.setContentType(MediaType.IMAGE_JPEG_VALUE);
                case "png" -> response.setContentType(MediaType.IMAGE_PNG_VALUE);
                case "svg" -> response.setContentType("image/svg+xml");
                case "gif" -> response.setContentType(MediaType.IMAGE_GIF_VALUE);
                case "pdf" -> response.setContentType(MediaType.APPLICATION_PDF_VALUE);
                default -> {
                }
            }
//            response.getOutputStream().write(data);
        } else {
            Path path = new ClassPathResource("static/assets/images/default_profile.png").getFile().toPath();
            byte[] data = Files.readAllBytes(path);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            response.getOutputStream().write(data);
        }
    }
}

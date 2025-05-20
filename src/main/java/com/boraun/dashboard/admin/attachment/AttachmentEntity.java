package com.boraun.dashboard.admin.attachment;

import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attachment")
public class AttachmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String originalFileName;
    @Getter
    @Setter
    private String extension;
    @Getter
    @Setter
    @Lob
//    @Column(columnDefinition = "LONGBLOB")
//    @Column(columnDefinition = "BYTEA")
    private byte[] content;
    @Getter
    @Setter
    private Long referenceId;
    @Getter
    @Setter
    private String remark;
    @Getter
    @Setter
    private String referenceResource;

    public String getResourceAttachmentUrl() {
        String fullUrl = "/resources/attachment/" + referenceResource + "/" + id + "." + extension;
        fullUrl = fullUrl.replaceAll("//", "/");
        return fullUrl;
    }

    @Transient
    @Getter
    @Setter
    private String fullUrl;
}

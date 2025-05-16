package com.boraun.dashboard.admin.message;

import com.boraun.dashboard.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "admin_message")
@Getter
@Setter
public class MessageEntity extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private Long id;

    @Column
    private String locale;

    @Column(name = "message_key")
    private String key;

    @Column(name = "message_content")
    private String content;

    @Column
    private String remark;
}

package com.example.FileCRUD.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contextType;

    @Column(nullable = false)
    private long size;

//    @OneToOne//(mappedBy ="attachment", cascade = CascadeType.REMOVE)
//    private AttachmentContent attachmentContent;

    @CreationTimestamp
    @Column(unique = true, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(unique = true)
    private Timestamp updatedAt;
}

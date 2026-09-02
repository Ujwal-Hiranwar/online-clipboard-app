package com.example.database.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "clipboard_entries")
@Getter
@Setter
public class ClipboardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "E_ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user_rid")
    private User user;

    @Column(name = "deleted_by_user", nullable = false)
    private Boolean deletedByUser = false;

    @Column(name = "encrypted_content", columnDefinition = "TEXT")
    private String encryptedContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_kind", nullable = false)
    private ClipboardContentKind contentKind = ClipboardContentKind.TEXT;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_data")
    private byte[] fileData;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_content_type")
    private String fileContentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "encryption_key", nullable = true, columnDefinition = "TEXT")
    private String encryptionKey;

    @Column(name = "otp", unique = true, length = 4)
    private String otp;

    @Column(name = "share_token", unique = true, length = 64)
    private String shareToken;

    @Column(name = "expiry_time", nullable = true)
    private LocalDateTime expiryTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

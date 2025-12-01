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

    @Column(name = "created_user_rid", nullable = true)
    private Integer createdUserRid;

    @Column(name = "deleted_by_user", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean deletedByUser = false;

    @Column(name = "encrypted_content", nullable = false, columnDefinition = "TEXT")
    private String encryptedContent;

    @Column(name = "encryption_key", nullable = true, columnDefinition = "TEXT")
    private String encryptionKey;

    @Column(name = "otp", unique = true, length = 4)
    private String otp;

    @Column(name = "expiry_time", nullable = true)
    private LocalDateTime expiryTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
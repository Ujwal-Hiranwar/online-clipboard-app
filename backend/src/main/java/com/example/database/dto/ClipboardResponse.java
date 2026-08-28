package com.example.database.dto;

import com.example.database.model.ClipboardModel;
import com.example.database.model.ClipboardContentKind;
import java.time.LocalDateTime;

public record ClipboardResponse(Long id, String content, String otp, LocalDateTime expiryTime,
                                LocalDateTime createdAt, boolean encrypted, ClipboardContentKind contentKind,
                                String fileName, String fileContentType, Long fileSize, String shareToken) {
    public static ClipboardResponse from(ClipboardModel entry) {
        return new ClipboardResponse(entry.getId(), entry.getEncryptedContent(), entry.getOtp(),
                entry.getExpiryTime(), entry.getCreatedAt(), entry.getEncryptionKey() != null,
                entry.getContentKind(), entry.getFileName(), entry.getFileContentType(), entry.getFileSize(),
                entry.getShareToken());
    }
}

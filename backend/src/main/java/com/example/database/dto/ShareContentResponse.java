package com.example.database.dto;

import com.example.database.model.ClipboardContentKind;
import java.time.LocalDateTime;

public record ShareContentResponse(String content, ClipboardContentKind contentKind,
                                   String fileName, String fileContentType, Long fileSize,
                                   LocalDateTime expiryTime) {}

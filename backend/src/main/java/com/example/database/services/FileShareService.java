package com.example.database.services;

import com.example.database.model.ClipboardContentKind;
import com.example.database.model.ClipboardModel;
import com.example.database.repository.ClipboardRepository;
import com.example.database.repository.UserRepository;
import com.example.database.security.AESUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileShareService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "txt", "csv", "json", "xml", "jpg", "jpeg", "png", "gif", "webp",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "rar", "7z");
    private final ClipboardRepository entries;
    private final UserRepository users;
    private final SecureRandom random = new SecureRandom();

    public FileShareService(ClipboardRepository entries, UserRepository users) { this.entries = entries; this.users = users; }

    @org.springframework.transaction.annotation.Transactional
    public ClipboardModel save(MultipartFile file, LocalDateTime expiryTime, boolean encrypted, Principal principal) {
        validate(file, expiryTime);
        try {
            byte[] originalBytes = file.getBytes();
            ClipboardModel entry = new ClipboardModel();
            entry.setContentKind(ClipboardContentKind.FILE);
            entry.setFileName(safeFileName(file.getOriginalFilename()));
            entry.setFileContentType(safeContentType(file.getContentType()));
            entry.setFileSize(file.getSize());
            entry.setOtp(nextOtp());
            entry.setShareToken(UUID.randomUUID().toString().replace("-", ""));
            entry.setExpiryTime(expiryTime);
            entry.setCreatedAt(LocalDateTime.now());
            if (encrypted) {
                String key = AESUtil.generateKey();
                entry.setFileData(AESUtil.encrypt(originalBytes, key));
                entry.setEncryptionKey(key);
            } else entry.setFileData(originalBytes);
            if (principal != null) users.findByEmail(principal.getName()).ifPresent(entry::setUser);
            return entries.save(entry);
        } catch (ResponseStatusException exception) { throw exception;
        } catch (Exception exception) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file", exception); }
    }

    public ClipboardModel findFile(String otp) {
        ClipboardModel entry = entries.findByOtp(otp).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        if (entry.getContentKind() != ClipboardContentKind.FILE) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        if (entry.getExpiryTime() != null && !entry.getExpiryTime().isAfter(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.GONE, "File has expired");
        return entry;
    }

    public ClipboardModel findFileByShareToken(String token) {
        ClipboardModel entry = entries.findByShareToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        if (entry.getContentKind() != ClipboardContentKind.FILE) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        if (entry.getExpiryTime() != null && !entry.getExpiryTime().isAfter(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.GONE, "File has expired");
        return entry;
    }

    public byte[] content(ClipboardModel entry) {
        try { return entry.getEncryptionKey() == null ? entry.getFileData() : AESUtil.decrypt(entry.getFileData(), entry.getEncryptionKey()); }
        catch (Exception exception) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not decrypt file", exception); }
    }

    private void validate(MultipartFile file, LocalDateTime expiryTime) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a file to share");
        if (file.getSize() > MAX_FILE_SIZE) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File must be 10 MB or smaller");
        if (expiryTime == null || !expiryTime.isAfter(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry time must be in the future");
        String extension = extension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "This file type is not allowed");
    }
    private String nextOtp() { for (int attempt = 0; attempt < 20; attempt++) { String otp = String.format("%04d", random.nextInt(10_000)); if (entries.findByOtp(otp).isEmpty()) return otp; } throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not create share code"); }
    private String safeFileName(String name) { String value = name == null ? "download" : name.replaceAll("[\\r\\n\\\\/]", "_"); return value.isBlank() ? "download" : value; }
    private String safeContentType(String value) { return value == null || value.isBlank() ? "application/octet-stream" : value; }
    private String extension(String name) { if (name == null) return ""; int index = name.lastIndexOf('.'); return index < 1 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT); }
}

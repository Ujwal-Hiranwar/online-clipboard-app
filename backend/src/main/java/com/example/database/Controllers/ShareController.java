package com.example.database.Controllers;

import com.example.database.dto.ShareContentResponse;
import com.example.database.model.ClipboardContentKind;
import com.example.database.model.ClipboardModel;
import com.example.database.repository.ClipboardRepository;
import com.example.database.security.AESUtil;
import com.example.database.services.FileShareService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final ClipboardRepository entries;
    private final FileShareService files;

    public ShareController(ClipboardRepository entries, FileShareService files) {
        this.entries = entries;
        this.files = files;
    }

    @GetMapping("/{token}")
    public ResponseEntity<ShareContentResponse> get(@PathVariable String token) {
        ClipboardModel entry = findActive(token);
        String content = null;
        if (entry.getContentKind() == ClipboardContentKind.TEXT) {
            try {
                content = entry.getEncryptionKey() == null ? entry.getEncryptedContent()
                        : AESUtil.decrypt(entry.getEncryptedContent(), entry.getEncryptionKey());
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not decrypt shared content", exception);
            }
        }
        return ResponseEntity.ok(new ShareContentResponse(content, entry.getContentKind(), entry.getFileName(),
                entry.getFileContentType(), entry.getFileSize(), entry.getExpiryTime()));
    }

    @GetMapping("/{token}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String token) {
        ClipboardModel entry = files.findFileByShareToken(token);
        MediaType mediaType;
        try { mediaType = MediaType.parseMediaType(entry.getFileContentType()); }
        catch (Exception ignored) { mediaType = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok().contentType(mediaType).contentLength(entry.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(entry.getFileName()).build().toString())
                .body(new ByteArrayResource(files.content(entry)));
    }

    private ClipboardModel findActive(String token) {
        ClipboardModel entry = entries.findByShareToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));
        if (entry.getExpiryTime() != null && !entry.getExpiryTime().isAfter(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.GONE, "Share link has expired");
        return entry;
    }
}

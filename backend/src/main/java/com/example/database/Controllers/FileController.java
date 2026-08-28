package com.example.database.Controllers;

import com.example.database.dto.ClipboardResponse;
import com.example.database.services.FileShareService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileShareService files;
    public FileController(FileShareService files) { this.files = files; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClipboardResponse> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("expiryTime") String expiryTime,
                                                     @RequestParam(value = "encrypted", defaultValue = "false") boolean encrypted,
                                                     Principal principal) {
        LocalDateTime expiry = OffsetDateTime.parse(expiryTime).toLocalDateTime();
        var saved = files.save(file, expiry, encrypted, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClipboardResponse.from(saved));
    }

    @GetMapping("/{otp}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String otp) {
        var entry = files.findFile(otp);
        MediaType mediaType;
        try { mediaType = MediaType.parseMediaType(entry.getFileContentType()); } catch (Exception ignored) { mediaType = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok().contentType(mediaType).contentLength(entry.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(entry.getFileName()).build().toString())
                .body(new ByteArrayResource(files.content(entry)));
    }
}

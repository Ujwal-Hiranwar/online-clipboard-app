package com.example.database.Controllers;
import com.example.database.services.ClipboardEncryptionService;
import com.example.database.dto.ClipboardResponse;
import com.example.database.model.ClipboardModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/encrypted")
public class EncrypterdContentController {
    @Autowired
    private ClipboardEncryptionService clipboardService;

    @CrossOrigin("${app.cors.allowed-origin}")
    @PostMapping("/save")
    public ResponseEntity<ClipboardResponse> sendEncryptedContent(@RequestBody Map<String, String> request, Principal principal) {
        try {
            String content = request.get("content");
            String otp = request.get("otp");
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(request.get("expiryTime"));
            LocalDateTime expiryTime = offsetDateTime.toLocalDateTime();

            ClipboardModel saved = clipboardService.saveClipboardData(content, otp, expiryTime, principal);
            return ResponseEntity.ok(ClipboardResponse.from(saved));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @CrossOrigin("${app.cors.allowed-origin}")
    @GetMapping("/retrieve/{otp}")
    public ResponseEntity<String> retrieveDecryptedContent(@PathVariable String otp) {

        try {

            return ResponseEntity.ok(clipboardService.retrieveDecryptedContent(otp));
        } catch (Exception e) {

            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

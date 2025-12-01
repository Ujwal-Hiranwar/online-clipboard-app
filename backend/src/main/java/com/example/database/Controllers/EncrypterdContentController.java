package com.example.database.Controllers;
import com.example.database.services.ClipboardEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/encrypted")
public class EncrypterdContentController {
    @Autowired
    private ClipboardEncryptionService clipboardService;

    @CrossOrigin("${app.cors.allowed-origin}")
    @PostMapping("/save")
    public ResponseEntity<String> sendEncryptedContent(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String otp = request.get("otp");
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(request.get("expiryTime"));
            LocalDateTime expiryTime = offsetDateTime.toLocalDateTime();

            clipboardService.saveClipboardData(content, otp, expiryTime);
            return ResponseEntity.ok("Dat encrypted and saved successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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

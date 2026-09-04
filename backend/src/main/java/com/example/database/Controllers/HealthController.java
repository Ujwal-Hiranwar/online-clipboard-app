package com.example.database.Controllers;

import com.example.database.model.Heartbeat;
import com.example.database.repository.HeartbeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HeartbeatRepository heartbeatRepository;

    public HealthController(HeartbeatRepository heartbeatRepository) {
        this.heartbeatRepository = heartbeatRepository;
    }

    @GetMapping("/db-ping")
    public ResponseEntity<Map<String, Object>> pingDatabase() {
        try {
            LocalDateTime now = LocalDateTime.now();
            Heartbeat pingRecord = new Heartbeat("SYSTEM_PING", now);
            heartbeatRepository.save(pingRecord);

            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "database", "CONNECTED",
                    "lastPing", now.toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "DOWN",
                    "database", "ERROR",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown database error"
            ));
        }
    }
}

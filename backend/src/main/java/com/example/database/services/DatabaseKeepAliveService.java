package com.example.database.services;

import com.example.database.model.Heartbeat;
import com.example.database.repository.HeartbeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DatabaseKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseKeepAliveService.class);

    private final HeartbeatRepository heartbeatRepository;

    public DatabaseKeepAliveService(HeartbeatRepository heartbeatRepository) {
        this.heartbeatRepository = heartbeatRepository;
    }

    /**
     * Periodically writes a heartbeat timestamp to keep the Supabase database connection warm
     * while the Spring Boot backend instance is actively running.
     * Runs every 4 minutes (240,000 ms) with an initial delay of 15 seconds.
     */
    @Scheduled(initialDelay = 15000, fixedRate = 240000)
    public void keepDatabaseWarm() {
        try {
            heartbeatRepository.save(new Heartbeat("SYSTEM_PING", LocalDateTime.now()));
            log.debug("Database keep-alive ping succeeded.");
        } catch (Exception e) {
            log.warn("Database keep-alive ping failed: {}", e.getMessage());
        }
    }
}

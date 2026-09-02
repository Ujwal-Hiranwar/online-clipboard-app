package com.example.database.services;

import com.example.database.repository.ClipboardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpiredClipboardCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ExpiredClipboardCleanupService.class);

    private final ClipboardRepository clipboardRepository;

    public ExpiredClipboardCleanupService(ClipboardRepository clipboardRepository) {
        this.clipboardRepository = clipboardRepository;
    }

    /**
     * Periodically removes expired clipboard entries (both text and files) from the database.
     * Runs every 60 seconds with an initial 10-second delay after application startup.
     */
    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    public void cleanupExpiredClipboards() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = clipboardRepository.deleteExpiredEntries(now);
            if (deletedCount > 0) {
                log.info("Expired clipboard cleanup executed: removed {} expired entries (text/files).", deletedCount);
            }
        } catch (Exception e) {
            log.error("Failed to clean up expired clipboard entries", e);
        }
    }
}

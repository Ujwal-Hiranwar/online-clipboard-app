package com.example.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_heartbeat")
@Getter
@Setter
public class Heartbeat {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id = "SYSTEM_PING";

    @Column(name = "last_ping_at", nullable = false)
    private LocalDateTime lastPingAt = LocalDateTime.now();

    public Heartbeat() {
    }

    public Heartbeat(String id, LocalDateTime lastPingAt) {
        this.id = id;
        this.lastPingAt = lastPingAt;
    }
}

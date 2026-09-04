package com.example.database.repository;

import com.example.database.model.Heartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartbeatRepository extends JpaRepository<Heartbeat, String> {
}

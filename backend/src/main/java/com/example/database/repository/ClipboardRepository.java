package com.example.database.repository;

import com.example.database.model.ClipboardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClipboardRepository extends JpaRepository<ClipboardModel, Long> {

    Optional<ClipboardModel> findByOtp(String otp);
}


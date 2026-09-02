package com.example.database.repository;

import com.example.database.model.ClipboardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import com.example.database.model.User;

@Repository
public interface ClipboardRepository extends JpaRepository<ClipboardModel, Long> {

    Optional<ClipboardModel> findByOtp(String otp);
    Optional<ClipboardModel> findByShareToken(String shareToken);
    List<ClipboardModel> findByUserOrderByCreatedAtDesc(User user);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM ClipboardModel c WHERE c.expiryTime IS NOT NULL AND c.expiryTime <= :now")
    int deleteExpiredEntries(@org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}


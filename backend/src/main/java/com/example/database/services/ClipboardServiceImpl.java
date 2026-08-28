package com.example.database.services;

import com.example.database.model.ClipboardModel;
import com.example.database.model.User;
import com.example.database.repository.ClipboardRepository;
import com.example.database.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ClipboardServiceImpl implements ClipboardService {
    private final ClipboardRepository clipboardRepository;
    private final UserRepository userRepository;

    public ClipboardServiceImpl(ClipboardRepository clipboardRepository, UserRepository userRepository) {
        this.clipboardRepository = clipboardRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ClipboardModel saveClipboardData(ClipboardModel data, Principal principal) {
        if (data.getShareToken() == null || data.getShareToken().isBlank()) {
            data.setShareToken(UUID.randomUUID().toString().replace("-", ""));
        }
        if (principal != null) {
            Optional<User> userOptional = userRepository.findByEmail(principal.getName());
            userOptional.ifPresent(data::setUser);
        }
        return clipboardRepository.save(data);
    }

    @Override
    public Optional<ClipboardModel> getByOtp(String otp) {
        return clipboardRepository.findByOtp(otp).filter(entry -> entry.getExpiryTime() == null || entry.getExpiryTime().isAfter(LocalDateTime.now()));
    }

    @Override
    public List<ClipboardModel> getAllClipboardData() {
        return clipboardRepository.findAll();
    }

    @Override public List<ClipboardModel> getUserClipboardData(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new SecurityException("User not found"));
        return clipboardRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public void deleteById(Long id) {
        clipboardRepository.deleteById(id);
    }
}

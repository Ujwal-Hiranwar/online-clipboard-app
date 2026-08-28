package com.example.database.services;


import com.example.database.model.ClipboardModel;
import com.example.database.repository.ClipboardRepository;
import com.example.database.security.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.security.Principal;
import com.example.database.model.User;
import com.example.database.repository.UserRepository;
import java.util.UUID;

@Service
public class ClipboardEncryptionService {

    @Autowired
    private ClipboardRepository clipboardRepository;
    @Autowired
    private UserRepository userRepository;

    public ClipboardModel saveClipboardData(String content, String otp, LocalDateTime expiryTime, Principal principal) throws Exception {
        String secretKey = AESUtil.generateKey(); // Generate AES Key
        String encryptedContent = AESUtil.encrypt(content, secretKey); // Encrypt Content

        ClipboardModel clipboard = new ClipboardModel();
        clipboard.setEncryptedContent(encryptedContent);
        clipboard.setEncryptionKey(secretKey);
        clipboard.setOtp(otp);
        clipboard.setShareToken(UUID.randomUUID().toString().replace("-", ""));
        clipboard.setExpiryTime(expiryTime);
        clipboard.setCreatedAt(LocalDateTime.now());
        if (principal != null) userRepository.findByEmail(principal.getName()).ifPresent(clipboard::setUser);

        return clipboardRepository.save(clipboard);
    }

    public String retrieveDecryptedContent(String otp) throws Exception {
        Optional<ClipboardModel> clipboardOptional = clipboardRepository.findByOtp(otp);

        return clipboardOptional
                .map(clipboard -> {
                    if (clipboard.getExpiryTime() != null && !clipboard.getExpiryTime().isAfter(LocalDateTime.now())) throw new IllegalStateException("OTP has expired");
                    try {
                        if(clipboard.getEncryptionKey() == null){
                            return clipboard.getEncryptedContent();
                        }else{
                            return AESUtil.decrypt(clipboard.getEncryptedContent(), clipboard.getEncryptionKey());
                        }

                    } catch (Exception e) {
                        throw new RuntimeException("Decryption failed", e);
                    }
                })
                .orElseThrow(() -> new RuntimeException("Invalid OTP or data not found"));
    }
}

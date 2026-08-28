package com.example.database.services;

import com.example.database.dto.AuthDtos.*;
import com.example.database.model.RefreshToken;
import com.example.database.model.Role;
import com.example.database.model.User;
import com.example.database.repository.RefreshTokenRepository;
import com.example.database.repository.UserRepository;
import com.example.database.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {
    public record RefreshResult(AuthResponse response, String refreshToken) { }
    private final UserRepository users; private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder; private final JwtService jwtService; private final int refreshDays;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder,
                       JwtService jwtService, @Value("${jwt.refresh-ttl-days}") int refreshDays) {
        this.users = users; this.refreshTokens = refreshTokens; this.encoder = encoder;
        this.jwtService = jwtService; this.refreshDays = refreshDays;
    }
    public User register(RegisterRequest request) {
        String email = normaliseEmail(request.email());
        validatePassword(request.password());
        if (users.findByEmail(email).isPresent() || users.findByUsername(email).isPresent()) throw new IllegalArgumentException("An account with this email already exists");
        User user = new User(); user.setEmail(email); user.setUsername(email); user.setPasswordHash(encoder.encode(request.password())); user.setRole(Role.USER);
        return users.save(user);
    }
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(normaliseEmail(request.email())).orElseThrow(() -> new SecurityException("Invalid email or password"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) throw new SecurityException("Invalid email or password");
        return new AuthResponse(jwtService.createAccessToken(user), userResponse(user));
    }
    public String createRefreshToken(User user) {
        byte[] bytes = new byte[48]; random.nextBytes(bytes); String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken token = new RefreshToken(); token.setUser(user); token.setTokenHash(hash(value)); token.setExpiresAt(LocalDateTime.now().plusDays(refreshDays)); refreshTokens.save(token); return value;
    }
    public RefreshResult rotateRefreshToken(String raw) {
        RefreshToken old = refreshTokens.findByTokenHash(hash(raw)).orElseThrow(() -> new SecurityException("Invalid refresh token"));
        if (old.isRevoked() || old.getExpiresAt().isBefore(LocalDateTime.now())) throw new SecurityException("Expired refresh token");
        old.setRevoked(true); refreshTokens.save(old);
        return new RefreshResult(new AuthResponse(jwtService.createAccessToken(old.getUser()), userResponse(old.getUser())), createRefreshToken(old.getUser()));
    }
    public void revoke(String raw) { refreshTokens.findByTokenHash(hash(raw)).ifPresent(t -> { t.setRevoked(true); refreshTokens.save(t); }); }
    public User currentUser(String email) { return users.findByEmail(email).orElseThrow(() -> new SecurityException("User not found")); }
    public UserResponse updateProfile(User user) { return userResponse(users.save(user)); }
    public UserResponse userResponse(User user) { return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getGender(), user.getRole()); }
    private String normaliseEmail(String value) { if (value == null || !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("A valid email is required"); return value.trim().toLowerCase(); }
    private void validatePassword(String value) { if (value == null || value.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters"); }
    private String hash(String value) { try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
}

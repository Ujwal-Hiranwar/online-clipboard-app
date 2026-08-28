package com.example.database.security;

import com.example.database.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTtlSeconds;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-ttl-seconds}") long accessTtlSeconds) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail()).claim("role", user.getRole().name())
                .claim("type", "access").issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds))).signWith(key).compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!"access".equals(claims.get("type", String.class))) throw new IllegalArgumentException("Wrong token type");
        return claims;
    }
}

package com.example.database.Controllers;

import com.example.database.dto.AuthDtos.*;
import com.example.database.model.User;
import com.example.database.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String REFRESH_COOKIE = "syncpaste_refresh";
    private final AuthService auth;
    private final boolean secureCookie;
    private final String sameSite;
    public AuthController(AuthService auth, @Value("${app.cookie-secure:false}") boolean secureCookie, @Value("${app.cookie-same-site:Lax}") String sameSite) { this.auth = auth; this.secureCookie = secureCookie; this.sameSite = sameSite; }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.userResponse(auth.register(request)));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse result = auth.login(request); setRefreshCookie(response, auth.createRefreshToken(auth.currentUser(result.user().email()))); return ResponseEntity.ok(result);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = refreshToken(request); if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        AuthService.RefreshResult result = auth.rotateRefreshToken(token); setRefreshCookie(response, result.refreshToken()); return ResponseEntity.ok(result.response());
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = refreshToken(request); if (token != null) auth.revoke(token); clearRefreshCookie(response); return ResponseEntity.noContent().build();
    }
    @GetMapping("/me")
    public UserResponse me(Principal principal) { return auth.userResponse(auth.currentUser(principal.getName())); }

    private String refreshToken(HttpServletRequest request) { if (request.getCookies() == null) return null; for (var cookie : request.getCookies()) if (REFRESH_COOKIE.equals(cookie.getName())) return cookie.getValue(); return null; }
    private void setRefreshCookie(HttpServletResponse response, String value) { response.addHeader("Set-Cookie", ResponseCookie.from(REFRESH_COOKIE, value).httpOnly(true).secure(secureCookie).sameSite(sameSite).path("/api/auth").maxAge(14 * 24 * 60 * 60).build().toString()); }
    private void clearRefreshCookie(HttpServletResponse response) { response.addHeader("Set-Cookie", ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(secureCookie).sameSite(sameSite).path("/api/auth").maxAge(0).build().toString()); }
}

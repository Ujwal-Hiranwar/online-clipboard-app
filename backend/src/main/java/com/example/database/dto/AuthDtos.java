package com.example.database.dto;

import com.example.database.model.Role;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegisterRequest(String email, String password) { }
    public record LoginRequest(String email, String password) { }
    public record ProfileUpdateRequest(String name, String gender) { }
    public record UserResponse(Long id, String email, String name, String gender, Role role) { }
    public record AuthResponse(String accessToken, UserResponse user) { }
}

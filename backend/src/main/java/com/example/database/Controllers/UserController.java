package com.example.database.Controllers;

import com.example.database.dto.AuthDtos.ProfileUpdateRequest;
import com.example.database.dto.AuthDtos.UserResponse;
import com.example.database.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    public UserController(AuthService authService) { this.authService = authService; }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateUserProfile(@RequestBody ProfileUpdateRequest request, Principal principal) {
        var user = authService.currentUser(principal.getName());
        user.setName(request.name()); user.setGender(request.gender());
        // Current JPA transaction is handled by repository save in a subsequent auth-service extension.
        // Save directly through the existing managed repository-backed service operation.
        return ResponseEntity.ok(authService.updateProfile(user));
    }
}

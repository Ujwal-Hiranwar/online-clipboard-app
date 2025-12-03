package com.example.database.Controllers;

import com.example.database.model.User;
import com.example.database.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        boolean success = userService.registerUser(user);
        if (success) {
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("User registration failed", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        boolean success = userService.loginUser(user);
        if (success) {
            return new ResponseEntity<>("User logged in successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateUserProfile(@RequestBody User user, Principal principal) {
        if (principal == null || !principal.getName().equals(user.getUsername())) {
            return new ResponseEntity<>("You can only update your own profile.", HttpStatus.FORBIDDEN);
        }

        boolean success = userService.updateUserProfile(user);
        if (success) {
            return new ResponseEntity<>("Profile updated successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Profile update failed", HttpStatus.BAD_REQUEST);
    }
}

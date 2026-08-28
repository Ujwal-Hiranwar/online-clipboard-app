package com.example.database.config;

import com.example.database.model.Role;
import com.example.database.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository users;
    private final String adminEmail;
    public AdminBootstrap(UserRepository users, @Value("${app.bootstrap-admin-email:}") String adminEmail) { this.users = users; this.adminEmail = adminEmail; }
    @Override public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()) return;
        users.findByEmail(adminEmail.trim().toLowerCase()).ifPresent(user -> { if (user.getRole() != Role.ADMIN) { user.setRole(Role.ADMIN); users.save(user); } });
    }
}

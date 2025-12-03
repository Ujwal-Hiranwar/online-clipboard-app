package com.example.database.services;

import com.example.database.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, email, salt) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, bCryptPasswordEncoder.encode(user.getPassword()));
            ps.setString(3, user.getEmail());
            ps.setString(4, ""); // salt is not used
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error registering user", e);
            return false;
        }
    }

    @Override
    public boolean loginUser(User user) {
        logger.info("Attempting to log in user: {}", user.getUsername());
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.info("User found: {}", user.getUsername());
                    String storedPasswordHash = rs.getString("password_hash");
                    boolean passwordMatches = bCryptPasswordEncoder.matches(user.getPassword(), storedPasswordHash);
                    if (passwordMatches) {
                        logger.info("Password matches for user: {}", user.getUsername());
                    } else {
                        logger.info("Password does not match for user: {}", user.getUsername());
                    }
                    return passwordMatches;
                } else {
                    logger.info("User not found: {}", user.getUsername());
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.error("Error logging in user", e);
            return false;
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, email FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error getting all users", e);
        }
        return users;
    }

    @Override
    public boolean updateUserProfile(User user) {
        // The user needs to execute this SQL statement on their database:
        // ALTER TABLE users ADD COLUMN name VARCHAR(255), ADD COLUMN gender VARCHAR(255);
        String sql = "UPDATE users SET name = ?, gender = ? WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getGender());
            ps.setString(3, user.getUsername());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating user profile", e);
            return false;
        }
    }
}
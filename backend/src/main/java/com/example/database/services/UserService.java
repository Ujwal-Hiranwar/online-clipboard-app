package com.example.database.services;

import com.example.database.model.User;
import java.util.List;

public interface UserService {
    boolean registerUser(User user);
    boolean loginUser(User user);
    List<User> getAllUsers();
    boolean updateUserProfile(User user);
}

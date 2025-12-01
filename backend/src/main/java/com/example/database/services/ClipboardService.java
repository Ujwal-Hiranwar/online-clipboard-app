package com.example.database.services;

import com.example.database.model.ClipboardModel;

import java.util.List;
import java.util.Optional;

public interface ClipboardService {

    ClipboardModel saveClipboardData(ClipboardModel data);

    Optional<ClipboardModel> getByOtp(String otp);

    List<ClipboardModel> getAllClipboardData();

    void deleteById(Long id);
}

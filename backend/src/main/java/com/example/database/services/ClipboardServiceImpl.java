package com.example.database.services;

import com.example.database.model.ClipboardModel;
import com.example.database.repository.ClipboardRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClipboardServiceImpl implements ClipboardService{
    private final ClipboardRepository repository;

    public ClipboardServiceImpl(ClipboardRepository repository) {
        this.repository = repository;
    }

    @Override
    public ClipboardModel saveClipboardData(ClipboardModel data) {
        return repository.save(data);
    }

    @Override
    public Optional<ClipboardModel> getByOtp(String otp) {
        return repository.findByOtp(otp);
    }

    @Override
    public List<ClipboardModel> getAllClipboardData() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}


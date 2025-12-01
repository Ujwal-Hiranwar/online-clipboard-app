package com.example.database.Controllers;
import com.example.database.model.ClipboardModel;
import com.example.database.services.ClipboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api")
public class AppController {
    private final ClipboardService service;

    public AppController(ClipboardService service) {
        this.service = service;
    }
    @CrossOrigin(origins = "${app.cors.allowed-origin}")
    @PostMapping("/post/text")
    public ResponseEntity<ClipboardModel> saveClipboardData(@RequestBody ClipboardModel data) {
        ClipboardModel savedData = service.saveClipboardData(data);
        System.out.println(savedData);
        return ResponseEntity.ok(savedData);
    }

    @CrossOrigin(origins = "${app.cors.allowed-origin}")
    @GetMapping("/get/text/{otp}")
    public ResponseEntity<ClipboardModel> getByOtp(@PathVariable String otp) {
        Optional<ClipboardModel> data = service.getByOtp(otp);
        return data.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}



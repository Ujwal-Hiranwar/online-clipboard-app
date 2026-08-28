package com.example.database.Controllers;
import com.example.database.model.ClipboardModel;
import com.example.database.dto.ClipboardResponse;
import com.example.database.services.ClipboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<ClipboardResponse> saveClipboardData(@RequestBody ClipboardModel data, Principal principal) {
        ClipboardModel savedData = service.saveClipboardData(data, principal);
        return ResponseEntity.ok(ClipboardResponse.from(savedData));
    }

    @CrossOrigin(origins = "${app.cors.allowed-origin}")
    @GetMapping("/get/text/{otp}")
    public ResponseEntity<ClipboardResponse> getByOtp(@PathVariable String otp) {
        Optional<ClipboardModel> data = service.getByOtp(otp);
        return data.map(entry -> ResponseEntity.ok(ClipboardResponse.from(entry))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clipboards/mine")
    public List<ClipboardResponse> mine(Principal principal) {
        return service.getUserClipboardData(principal.getName()).stream().map(ClipboardResponse::from).toList();
    }
}



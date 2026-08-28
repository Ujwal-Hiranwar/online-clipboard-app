package com.example.database.Controllers;

import io.jsonwebtoken.JwtException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<Map<String, String>> invalid(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    @ExceptionHandler({SecurityException.class, JwtException.class})
    ResponseEntity<Map<String, String>> unauthorized(RuntimeException e) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication failed")); }
}

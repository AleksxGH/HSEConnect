package org.example.hseconnect.controller;

import org.example.hseconnect.model.LoginRequest;
import org.example.hseconnect.model.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final Map<String, String> users = new HashMap<>();

    public AuthController() {
        users.put("test@hse.ru", "12345");
        users.put("admin@hse.ru", "admin");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String correctPassword = users.get(request.getEmail());

        if (correctPassword == null) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }

        if (!correctPassword.equals(request.getPassword())) {
            return ResponseEntity.status(401).body("Неверный пароль");
        }

        return ResponseEntity.ok(
                new LoginResponse("1", request.getEmail())
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (users.containsKey(request.getEmail())) {
            return ResponseEntity.status(409).body("Пользователь уже существует");
        }

        users.put(request.getEmail(), request.getPassword());

        String userId = "1";

        return ResponseEntity.ok(
                new LoginResponse(userId, request.getEmail())
        );
    }
}
package org.example.hseconnect.controller;

import org.example.hseconnect.model.LoginRequest;
import org.example.hseconnect.model.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            validateAuthRequest(request);

            String email = normalizeEmail(request.getEmail());

            List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                    SELECT user_id, email, password_hash, is_active
                    FROM app.users
                    WHERE lower(email) = lower(?)
                    LIMIT 1
                    """, email);

            if (users.isEmpty()) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            Map<String, Object> user = users.get(0);

            Boolean isActive = (Boolean) user.get("is_active");
            if (Boolean.FALSE.equals(isActive)) {
                return ResponseEntity.status(403).body("Пользователь заблокирован");
            }

            String passwordHash = Objects.toString(user.get("password_hash"), "");

            // Сейчас пароль сравнивается как plain text, потому что фронт отправляет обычный password.
            // Позже лучше заменить на BCryptPasswordEncoder.
            if (!passwordHash.equals(request.getPassword())) {
                return ResponseEntity.status(401).body("Неверный пароль");
            }

            Long userId = ((Number) user.get("user_id")).longValue();

            return ResponseEntity.ok(new LoginResponse(userId, Objects.toString(user.get("email"))));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        try {
            validateAuthRequest(request);

            String email = normalizeEmail(request.getEmail());

            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM app.users WHERE lower(email) = lower(?)",
                    Integer.class,
                    email
            );

            if (existingCount != null && existingCount > 0) {
                return ResponseEntity.status(409).body("Пользователь уже существует");
            }

            LocalDateTime now = LocalDateTime.now();
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO app.users
                        (email, password_hash, is_active, is_email_verified, created_at, updated_at)
                        VALUES (?, ?, true, false, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, email);
                ps.setString(2, request.getPassword());
                ps.setTimestamp(3, Timestamp.valueOf(now));
                ps.setTimestamp(4, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);

            Long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();

            return ResponseEntity.ok(new LoginResponse(userId, email));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    private void validateAuthRequest(LoginRequest request) {
        if (request == null) {
            throw new RuntimeException("Пустой запрос");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email обязателен");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Пароль обязателен");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

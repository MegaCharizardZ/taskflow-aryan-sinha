package com.example.taskflow.controller;

import com.example.taskflow.models.LoginRequest;
import com.example.taskflow.models.LoginResponse;
import com.example.taskflow.models.RegisterRequest;
import com.example.taskflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API.
 * <ul>
 *   <li>Passwords: hash with bcrypt, strength (cost) &ge; 12.</li>
 *   <li>JWT: expires in 24 hours; claims include {@code user_id} and {@code email} (login uses email + password).</li>
 *   <li>All other API routes require {@code Authorization: Bearer &lt;token&gt;} (enforced via security config, not this controller).</li>
 * </ul>
 */
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

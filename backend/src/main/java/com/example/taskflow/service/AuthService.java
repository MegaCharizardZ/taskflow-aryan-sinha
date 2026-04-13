package com.example.taskflow.service;

import com.example.taskflow.entity.User;
import com.example.taskflow.models.LoginRequest;
import com.example.taskflow.models.LoginResponse;
import com.example.taskflow.models.RegisterRequest;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public void register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Registration rejected — email already registered: {}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        try {
            userRepository.save(user);
            log.info("User registered successfully: email={}", normalizedEmail);
        } catch (DataIntegrityViolationException ex) {
            // Race condition: another request registered the same email between the check and the insert
            log.warn("Registration race condition for email={}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed — no account found for email={}", normalizedEmail);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed — wrong password for email={}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("User logged in: userId={} email={}", user.getId(), normalizedEmail);
        return new LoginResponse(jwtTokenService.createAccessToken(user));
    }
}

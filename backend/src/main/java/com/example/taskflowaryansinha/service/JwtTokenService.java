package com.example.taskflowaryansinha.service;

import com.example.taskflowaryansinha.config.JwtProperties;
import com.example.taskflowaryansinha.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public String createAccessToken(User user) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + jwtProperties.getExpirationMs());
        String userId = user.getId().toString();

        return Jwts.builder()
                .subject(userId)
                .claim("user_id", userId)
                .claim("email", user.getEmail())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

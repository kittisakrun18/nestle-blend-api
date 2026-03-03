package com.nestle.blend.api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private SecretKey key;

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.issuer}")
    private String issuer;
    @Value("${app.jwt.audience}")
    private String audience;
    @Value("${app.jwt.access-ttl-minutes}")
    private int accessTtlMinutes;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String userId, String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlMinutes * 60L);

        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(
                        "username", username,
                        "role", role
                ))
                .signWith(key)
                .compact();
    }

    public io.jsonwebtoken.Claims parseAndValidate(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
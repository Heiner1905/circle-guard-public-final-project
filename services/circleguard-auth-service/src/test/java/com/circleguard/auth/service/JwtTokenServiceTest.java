package com.circleguard.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SECRET = "jwt-unit-test-secret-key-32-characters-long";

    @Test
    void generateTokenIncludesAnonymousIdAndAuthorities() {
        JwtTokenService service = new JwtTokenService(SECRET, 60_000);
        UUID anonymousId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "student",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"), new SimpleGrantedAuthority("gate:enter"))
        );

        String token = service.generateToken(anonymousId, auth);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(anonymousId.toString(), claims.getSubject());
        assertTrue(claims.get("permissions", List.class).contains("ROLE_STUDENT"));
        assertTrue(claims.get("permissions", List.class).contains("gate:enter"));
        assertNotNull(claims.getExpiration());
    }
}

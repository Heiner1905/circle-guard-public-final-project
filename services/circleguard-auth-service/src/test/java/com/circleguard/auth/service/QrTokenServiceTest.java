package com.circleguard.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QrTokenServiceTest {

    private static final String SECRET = "qr-unit-test-secret-key-32-characters-long";

    @Test
    void generateQrTokenUsesAnonymousIdAsSubject() {
        QrTokenService service = new QrTokenService(SECRET, 60_000);
        UUID anonymousId = UUID.randomUUID();

        String token = service.generateQrToken(anonymousId);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(anonymousId.toString(), claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}

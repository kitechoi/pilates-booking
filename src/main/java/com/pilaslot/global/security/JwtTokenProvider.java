package com.pilaslot.global.security;

import com.pilaslot.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final int MINIMUM_SECRET_LENGTH_BYTES = 32;

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties properties, Clock clock) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MINIMUM_SECRET_LENGTH_BYTES) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(clock.instant()))
                .build();
        this.properties = properties;
        this.clock = clock;
    }

    public String createAccessToken(Long memberId) {
        Instant now = clock.instant();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(properties.expiration()));
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Long getMemberId(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException exception) {
            throw new JwtAuthenticationException(ErrorCode.EXPIRED_TOKEN, exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN, exception);
        }
    }
}

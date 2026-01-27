package com.example.mikusmoneybackend.auth;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.example.mikusmoneybackend.config.JwtProperties;
import com.example.mikusmoneybackend.miku.Miku;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for JWT token operations including generation, validation, and claim extraction.
 */
@Slf4j
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final Key signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = createSigningKey();
    }

    /**
     * Generates an access token for the given user.
     */
    public String generateAccessToken(Miku miku) {
        return generateToken(miku, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Generates a refresh token for the given user.
     */
    public String generateRefreshToken(Miku miku) {
        return generateToken(miku, jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use {@link #generateAccessToken(Miku)} instead.
     */
    @Deprecated
    public String getToken(Miku miku) {
        return generateAccessToken(miku);
    }

    /**
     * Extracts the user ID from the token.
     */
    public Long getUserIdFromToken(String token) {
        String subject = getClaim(token, Claims::getSubject);
        return Long.parseLong(subject);
    }

    /**
     * Validates if the token is valid for the given user ID.
     */
    public boolean isTokenValid(String token, Long userId) {
        try {
            Long tokenUserId = getUserIdFromToken(token);
            return tokenUserId.equals(userId) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.debug("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extracts a specific claim from the token.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts custom claim by name.
     */
    public String getCustomClaim(String token, String claimName) {
        return getClaim(token, claims -> claims.get(claimName, String.class));
    }

    // Private methods

    private String generateToken(Miku miku, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", miku.getName());
        claims.put("lastName", miku.getLastName());
        claims.put("publicCode", miku.getPublicCode());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        log.debug("Generating token for user ID: {}, expires at: {}", miku.getId(), expiryDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(miku.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key createSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
            
            if (keyBytes.length < 32) {
                log.warn("JWT secret key is shorter than recommended 256 bits. Current: {} bytes", keyBytes.length);
            }
            
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid JWT secret key configuration. Ensure it's valid Base64.");
            throw new IllegalStateException("Invalid JWT secret key configuration", e);
        }
    }
}

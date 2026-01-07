package com.trash.ecommerce.service;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.trash.ecommerce.dto.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private String secretKey = "Banana";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    public JwtService() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGenerator.generateKey();
            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public Token generateToken(String email, Long id) {
        Map<String, Object> claims = new HashMap<>();
        Token token = new Token();
        claims.put("id", id);
        token.setAccess(
                Jwts.builder()
                        .claims()
                        .add(claims)
                        .subject(email)
                        .issuedAt(new Date(System.currentTimeMillis()))
                        .expiration(new Date(System.currentTimeMillis() + 3 * 60 * 60 * 1000))
                        .and()
                        .signWith(getKey())
                        .compact()
        );
        token.setRefresh(
                Jwts.builder()
                        .claims()
                        .add(claims)
                        .subject(email)
                        .issuedAt(new Date(System.currentTimeMillis()))
                        .expiration(new Date(System.currentTimeMillis() + 3 * 60 * 60 * 1000))
                        .and()
                        .signWith(getKey())
                        .compact()
        );
        String refreshTokenValue = token.getRefresh();
        if (refreshTokenValue != null) {
            redisTemplate.opsForValue().set("refresh:" + String.valueOf(id), refreshTokenValue, 3 * 60 * 60, TimeUnit.SECONDS);
        }
        return token;
    }

    private SecretKey getKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String removeBearerPrefix(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    public Long extractId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        token = removeBearerPrefix(token);
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid token format");
        }
        Claims claims = extractAllClaims(token);
        Long id = claims.get("id", Long.class);
        if (id == null) {
            throw new IllegalArgumentException("Token does not contain user ID");
        }
        return id;
    }
    
    public String extractUsername(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        token = removeBearerPrefix(token);
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function <Claims, T> claimResolve) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        token = removeBearerPrefix(token);
        Claims claims = extractAllClaims(token);
        return claimResolve.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        try {
            return Jwts.parser()
                        .verifyWith(getKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
        }
    }

    public boolean validationTokenCheck(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            return false;
        }
        try {
            String username = extractUsername(token);
            return username != null && username.equals(userDetails.getUsername()) && !isExpiration(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true; // Nếu không parse được, coi như đã hết hạn
        }
    }

    public Date extractExpiration(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        token = removeBearerPrefix(token);
        return extractClaim(token, Claims::getExpiration);
    }

    public Token refreshToken(String oldRefreshToken) {
        if (oldRefreshToken == null || oldRefreshToken.trim().isEmpty()) {
            return null;
        }
        
        // Xử lý "Bearer " prefix trước khi extract
        String cleanToken = removeBearerPrefix(oldRefreshToken);
        if (cleanToken == null || cleanToken.trim().isEmpty()) {
            return null;
        }
        
        try {
            Long userId = extractId(oldRefreshToken); // extractId sẽ tự xử lý prefix
            String storedToken = (String) redisTemplate.opsForValue().get("refresh:" + userId);
            
            if (storedToken == null || !storedToken.equals(cleanToken)) {
                return null;
            }
            
            if (isExpiration(storedToken)) {
                return null;
            }
            
            redisTemplate.delete("refresh:" + userId);
            String username = extractUsername(storedToken);
            Token token = generateToken(username, userId);
            String newRefreshToken = token.getRefresh();
            if (newRefreshToken != null) {
                redisTemplate.opsForValue().set(
                        "refresh:" + userId,
                        newRefreshToken,
                        3, TimeUnit.HOURS
                );
            }
            return token;
        } catch (Exception e) {
            return null;
        }
    }

}

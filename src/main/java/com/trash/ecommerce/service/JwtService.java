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
        redisTemplate.opsForValue().set("refresh:" + String.valueOf(id), token.getRefresh(), 3 * 60 * 60, TimeUnit.SECONDS);
        return token;
    }

    private SecretKey getKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long extractId(String token) {
        token = token.substring(7);
        System.out.println(token);
        Claims claims = extractAllClaims(token);
        Long id = claims.get("id", Long.class);
        return id;
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExspiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function <Claims, T> claimResolve) {
        Claims claims = extractAllClaims(token);
        return claimResolve.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }

    public boolean validationTokenCheck(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpiration(token);
    }

    public boolean isExpiration(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Token refreshToken(String oldRefreshToken) {
        Long userId = extractId(oldRefreshToken);
        String storedToken = (String) redisTemplate.opsForValue().get("refresh:" + userId);
        if (storedToken == null || !storedToken.equals(oldRefreshToken) || isExpiration(storedToken)) {
            return null;
        }
        redisTemplate.delete("refresh:" + userId);
        Token token = generateToken(extractUsername(storedToken), userId);
        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                token.getRefresh(),
                3, TimeUnit.HOURS
        );
        return token;
    }

}

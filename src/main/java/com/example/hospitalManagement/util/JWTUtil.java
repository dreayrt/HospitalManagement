package com.example.hospitalManagement.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JWTUtil {
    private final String secret="kho-doan-lam-doan-kho-jwt-secret-key-123456";
    private final Long experationTime=30L * 60 * 1000;
    private final Long longRefreshTimeToken=7L*24*60*1000;
    public String generateToken(String username, String role){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+experationTime))
                .claim("type","access_token")
                .claim("role",role)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public String generateRefreshToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+longRefreshTimeToken))
                .claim("type","refresh_token")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }
    public String extractUsername(String token){
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String extractType(String token){
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type", String.class);

    }
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey()) //lay secret-key
                    .build()//Tạo JwtParser hoàn chỉnh từ các cấu hình đã khai báo
                    .parseClaimsJws(token);//parse jwt xem co bi chinh sua khong(doc, kiem tra, xac thuc)
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


}

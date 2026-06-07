package com.example.parking.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String email,String password, long userId) {
        return Jwts.builder()
                .setSubject(email) // ה-Subject הראשי יישאר המייל (בשביל השליפה האוטומטית)
                .claim("password", password) // מוסיפים את ה-ID של המשתמש בתוך הטוקן!
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // שעה
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserEmail(String token) { return extractClaims(token).getSubject(); }
    public String getUserpassword(String token) { return extractClaims(token).getSubject(); }


    // פונקציה חדשה אם תרצי לשלוף בעתיד את ה-ID ישירות מהטוקן
    public Long getUserId(String token) { return extractClaims(token).get("userId", Long.class); }
}
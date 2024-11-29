package com.org.education_management.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JWTUtil {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Secure key
    private static final long EXPIRATION_TIME = 3600_000; // 1 hour in milliseconds
    private static final String ISSUER_NAME = "ERP_1";

    public static String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(ISSUER_NAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
package com.back.postpilot.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTService {
    public static final String SECRET = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";

    public String generateToken(String username, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 min
                .sign(Algorithm.HMAC256(SECRET));
    }

    public String extractUserName(String token){
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getSubject();
    }

    public String extractRole(String token){
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getClaim("role").asString();
    }

    public boolean validateToken(String token){
        try {
            JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
            return true;
        }catch (Exception ex){
            return false;
        }

    }
}

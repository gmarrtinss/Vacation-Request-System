package com.rh.vacationbackend.service;

import com.rh.vacationbackend.config.JwtProperties; // Importe a classe de propriedades
import com.rh.vacationbackend.model.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {

    private final JwtProperties jwtProperties;

    public TokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.expiration());
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());

        return Jwts.builder()
                .issuer("Vacation Request API")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }


    public boolean isTokenValid(String token) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubjectFromToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
}
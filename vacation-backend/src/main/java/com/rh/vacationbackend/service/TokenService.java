package com.rh.vacationbackend.service;

import com.rh.vacationbackend.config.JwtProperties;
import com.rh.vacationbackend.model.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders; // 1. Importe o Decoders
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

        // --- LOG DE DEPURAÇÃO ADICIONADO ---
        String secretFromProperties = jwtProperties.secret();
        System.out.println("=============================================");
        System.out.println("DEBUGGING JWT SECRET:");
        System.out.println("Secret Key being used: '" + secretFromProperties + "'");
        System.out.println("Secret Length (characters): " + secretFromProperties.length());
        System.out.println("=============================================");
        // --- FIM DO LOG DE DEPURAÇÃO ---

        byte[] keyBytes = Decoders.BASE64.decode(secretFromProperties);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .issuer("Vacation Request API")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    // O mesmo ajuste é necessário para os métodos de validação

    public boolean isTokenValid(String token) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
            SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

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
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
}
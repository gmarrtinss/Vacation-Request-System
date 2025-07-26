package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.dto.LoginRequestDTO;
import com.rh.vacationbackend.dto.LoginResponseDTO;
import com.rh.vacationbackend.service.TokenBlacklistService;
import com.rh.vacationbackend.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final TokenBlacklistService blacklistService;

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService, TokenBlacklistService blacklistService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequest) {
        UsernamePasswordAuthenticationToken usernamePassword =
                new UsernamePasswordAuthenticationToken(loginRequest.login(), loginRequest.password());

        Authentication auth = authenticationManager.authenticate(usernamePassword);
        String token = tokenService.generateToken(auth);
        
        return new LoginResponseDTO(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token.isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            blacklistService.addToBlacklist(token);
        }
        return ResponseEntity.ok().build();
    }
}
package com.rh.vacationbackend.config;

import com.rh.vacationbackend.repository.UsersRepository;
import com.rh.vacationbackend.service.TokenBlacklistService;
import com.rh.vacationbackend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsersRepository usersRepository;
    private final TokenBlacklistService blacklistService;
    
    public TokenFilter(TokenService tokenService, UsersRepository usersRepository, TokenBlacklistService blacklistService) {
        this.tokenService = tokenService;
        this.usersRepository = usersRepository;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String token = recoverToken(request);
        
        // Verifique se o token está na blacklist ANTES de qualquer outra coisa
        if (token != null && !blacklistService.isBlacklisted(token) && tokenService.isTokenValid(token)) {

            String subject = tokenService.getSubjectFromToken(token);
            UserDetails user = usersRepository.findByCpf(subject).orElseThrow(() -> new RuntimeException("User not found"));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
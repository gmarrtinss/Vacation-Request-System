package com.rh.vacationbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rh.vacationbackend.dto.LoginRequestDTO;
import com.rh.vacationbackend.exception.GlobalExceptionHandler;
import com.rh.vacationbackend.service.AuthenticationService;
import com.rh.vacationbackend.service.TokenBlacklistService;
import com.rh.vacationbackend.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Use a anotação padrão do Mockito
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {


    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;
    
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_ShouldReturn200AndToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO("12345678901", "password123");
        Authentication authMock = mock(Authentication.class);
        String fakeToken = "fake.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(tokenService.generateToken(authMock)).thenReturn(fakeToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk()) // Agora deve esperar 200 OK
                .andExpect(jsonPath("$.token").value(fakeToken));
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO("12345678901", "wrong_password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized()); 
    }


    String fakeToken = "valid.token.to.blacklist";
    @Test
    void logout_ShouldReturn200_WhenTokenIsProvided() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        // Add the Authorization header to simulate an authenticated user
                        .header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().isOk());

        // Verify that the blacklist service was called with the correct token
        verify(tokenBlacklistService, times(1)).addToBlacklist(fakeToken);
    }

    @Test
    void logout_ShouldReturn400_WhenNoTokenIsProvided() throws Exception {
        // Act & Assert
        // Perform the request without the Authorization header
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + "" ))
            .andExpect(status().isBadRequest());

        // Verify that the blacklist service was NEVER called
        verify(tokenBlacklistService, never()).addToBlacklist(anyString());
    }
}
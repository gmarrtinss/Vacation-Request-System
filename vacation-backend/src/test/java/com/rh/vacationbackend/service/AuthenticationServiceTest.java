package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenFoundByCpf() {
        // Arrange
        String userIdentifier = "12345678901";
        Users user = Users.builder().cpf(userIdentifier).build();
        when(usersRepository.findByCpf(userIdentifier)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = authenticationService.loadUserByUsername(userIdentifier);

        // Assert
        assertNotNull(userDetails);
        assertEquals(userIdentifier, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenFoundByEmail() {
        // Arrange
        String userIdentifier = "user@example.com";
        Users user = Users.builder().cpf("11122233344").email(userIdentifier).build();
        when(usersRepository.findByCpf(userIdentifier)).thenReturn(Optional.empty());
        when(usersRepository.findByEmail(userIdentifier)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = authenticationService.loadUserByUsername(userIdentifier);

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getCpf(), userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String userIdentifier = "notfound";
        when(usersRepository.findByCpf(userIdentifier)).thenReturn(Optional.empty());
        when(usersRepository.findByEmail(userIdentifier)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.loadUserByUsername(userIdentifier);
        });
    }
}
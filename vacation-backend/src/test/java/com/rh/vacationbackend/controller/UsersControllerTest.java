package com.rh.vacationbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rh.vacationbackend.dto.UsersCreateDTO;
import com.rh.vacationbackend.dto.UsersUpdateDTO;
import com.rh.vacationbackend.exception.GlobalExceptionHandler;
import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.model.UsersRole;
import com.rh.vacationbackend.service.UsersService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private UsersController usersController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(usersController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // -- Begin create tests -- 

    @Test
    void create_ShouldReturn201_WhenDataIsValid() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "Utilizador Válido", "valido@email.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, UUID.randomUUID());
        when(usersService.create(any(UsersCreateDTO.class))).thenReturn(new Users());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_ShouldReturn400_WhenNameIsNull() throws Exception {
        UsersCreateDTO dtoWithNullName = new UsersCreateDTO(
            "12345678901",
            null, // Supposed to not work cause name can't be null
            "null.name@email.com",
            "senha123",
            UsersRole.EMPLOYEE,
            LocalDate.now(),
            1L,
            1L,
            UUID.randomUUID()
        );

        when(usersService.create(any(UsersCreateDTO.class)))
            .thenThrow(new IllegalArgumentException("Field 'name' cannot be null."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithNullName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn400_WhenPasswordIsProvidedForEmployee() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "Func Com Senha", "func.senha@email.com", "senha123", UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, UUID.randomUUID());
        when(usersService.create(any(UsersCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Password field should not be provided for EMPLOYEE role."));
        
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn400_WhenPasswordIsNullForManager() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("11122233301", "Gestor Sem Senha", "gestor.semsenha@email.com", null, UsersRole.MANAGER, LocalDate.now(), 1L, 1L, null);
        when(usersService.create(any(UsersCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Password is required for managers."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn400_WhenAdmissionDateIsInTheFuture() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "Viajante do Tempo", "futuro@email.com", null, UsersRole.EMPLOYEE, LocalDate.now().plusDays(1), 1L, 1L, UUID.randomUUID());
        when(usersService.create(any(UsersCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Admission date cannot be in the future."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturn404_WhenSectorDoesNotExist() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "Func no Setor", "no.sector@email.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 99L, 1L, UUID.randomUUID());
        when(usersService.create(any(UsersCreateDTO.class)))
                .thenThrow(new EntityNotFoundException("Sector not found with ID: 99"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn404_WhenPositionDoesNotExist() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "Func no Position", "no.position@email.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 99L, UUID.randomUUID());
        when(usersService.create(any(UsersCreateDTO.class)))
                .thenThrow(new EntityNotFoundException("Sector not found with ID: 99"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturn409_WhenManagerIsNotFromSameSector() throws Exception {
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "Func Setor Errado", "setor.errado@email.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, UUID.randomUUID());
        when(usersService.create(any(UsersCreateDTO.class)))
                .thenThrow(new IllegalStateException("Manager must be from the same sector as the employee."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict()); // 409 Conflict é apropriado para falha de regra de negócio
    }

    // -- End of create tests --
/*  --------------------------------------------------- */
    // -- Begin read tests --

    private UUID testUserIdRead;
    private Users testUserRead;

    @BeforeEach
    void setUpRead() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(usersController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testUserIdRead = UUID.randomUUID();
        testUserRead = Users.builder()
                .id(testUserIdRead)
                .name("Utilizador de Teste")
                .cpf("11122233344")
                .email("teste@email.com")
                .role(UsersRole.EMPLOYEE)
                .admissionDate(LocalDate.now())
                .build();
    }


    @Test
    void getById_ShouldReturn200_WhenUserExists() throws Exception {
        when(usersService.findById(testUserIdRead)).thenReturn(testUserRead);

        mockMvc.perform(get("/api/users/{id}", testUserIdRead))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserIdRead.toString()));
    }

    /**
     * Alternative Path 1: Tests that a 404 Not Found is returned
     * when the requested user ID does not exist in the database.
     */
    @Test
    void getById_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        // Teach the mock service to throw the exception that the real service would
        when(usersService.findById(nonExistentId))
                .thenThrow(new EntityNotFoundException("User not found with ID: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound()); // We expect a 404 status
    }

    /**
     * Alternative Path 2: Tests that a 400 Bad Request is returned
     * when the provided ID is not in a valid UUID format.
     */
    @Test
    void getById_ShouldReturn400_WhenIdIsInvalidFormat() throws Exception {
        // Arrange
        String invalidId = "this-is-not-a-uuid";

        // Act & Assert
        // We don't need to mock the service because the error happens before
        // the controller method is even called (during URL parsing).
        mockMvc.perform(get("/api/users/{id}", invalidId))
                .andExpect(status().isBadRequest()); // We expect a 400 status
    }

    @Test
    void getAll_ShouldReturn200AndListOfUsers() throws Exception {
        // Arrange
        when(usersService.findAll()).thenReturn(Collections.singletonList(testUserRead));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Utilizador de Teste"));
    }

    @Test
    void getByCpf_ShouldReturn200_WhenUserExists() throws Exception {
        // Arrange
        String existingCpf = "11122233344";
        when(usersService.findByCpf(existingCpf)).thenReturn(testUserRead);

        // Act & Assert
        mockMvc.perform(get("/api/users/by-cpf/{cpf}", existingCpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(existingCpf));
    }


    
    @Test
    void getByCpf_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        // Arrange
        String nonExistentCpf = "00000000000";
        when(usersService.findByCpf(nonExistentCpf))
                .thenThrow(new EntityNotFoundException("User not found with CPF: " + nonExistentCpf));

        // Act & Assert
        mockMvc.perform(get("/api/users/by-cpf/{cpf}", nonExistentCpf))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByCpf_ShouldReturn400_WhenCpfIsInvalidFormat() throws Exception {
        // Arrange
        String invalidCpfFormat = "this-is-not-cpf";

        // Act & Assert
        // We don't need to mock the service because the error happens before
        // the controller method is even called (during URL parsing).
        mockMvc.perform(get("/api/users/by-cpf/{cpf}", invalidCpfFormat))
                .andExpect(status().isBadRequest());
    }


    // End of read tests
    /*  --------------------------------------------------- */
     // --- Begin delete tests ---

    /**
     * Happy Path: Tests that a 204 No Content is returned when a user is successfully deleted.
     */
    @Test
    void delete_ShouldReturn204_WhenUserIsDeleted() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        // We configure the mock to do nothing when the delete method is called,
        // which is the expected behavior for a void method.
        doNothing().when(usersService).delete(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent()); // 204 No Content is the standard for successful deletions.

        // Verify that the service's delete method was called exactly once.
        verify(usersService, times(1)).delete(userId);
    }

    /**
     * Alternative Path: Tests that a 404 Not Found is returned when trying to delete a user that does not exist.
     */
    @Test
    void delete_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        // Teach the mock service to throw the exception that the real service would
        // when the user is not found.
        doThrow(new EntityNotFoundException("User not found with ID: " + nonExistentId))
                .when(usersService).delete(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound()); // We expect a 404 status from our GlobalExceptionHandler
    }

    // --- End of delete tests ---
    /*  --------------------------------------------------- */
    // --- Begin update tests ---

    private UUID testUserIdUpdate;
    private Users testUserUpdate;


    @BeforeEach
    void setUpUpdate() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(usersController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        
        testUserIdUpdate = UUID.randomUUID();
        testUserUpdate = Users.builder()
                .id(testUserIdUpdate)
                .name("Test User")
                .cpf("11122233344")
                .email("test@example.com")
                .role(UsersRole.EMPLOYEE)
                .admissionDate(LocalDate.now())
                .build();
    }

    /**
     * Happy Path: Tests that a 200 OK is returned with the updated user data
     * when the input is valid.
     */
    @Test
    void update_ShouldReturn200_WhenDataIsValid() throws Exception {
        // Arrange
        UsersUpdateDTO updateDTO = new UsersUpdateDTO("Name Updated", "new.email@company.com");
        
        Users updatedUser = Users.builder()
                .id(testUserIdUpdate)
                .name("Name Updated")
                .email("new.email@company.com")
                .build();


        // Teach the mock service to return the updated user object
        when(usersService.update(eq(testUserIdUpdate), any(UsersUpdateDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", testUserIdUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Name Updated"))
                .andExpect(jsonPath("$.email").value("new.email@company.com"));
    }

    /**
     * Alternative Path: Tests that a 404 Not Found is returned when trying to update
     * a user that does not exist.
     */
    @Test
    void update_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        UsersUpdateDTO updateDTO = new UsersUpdateDTO("Any Name", "any.email@company.com");
        
        // Teach the mock service to throw the exception that the real service would
        when(usersService.update(eq(nonExistentId), any(UsersUpdateDTO.class)))
                .thenThrow(new EntityNotFoundException("User not found with ID: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    /**
     * Alternative Path: Tests that a 400 Bad Request is returned when trying to update
     * with an email that is already in use by another user.
     */
    @Test
    void update_ShouldReturn400_WhenEmailIsAlreadyInUse() throws Exception {
        // Arrange
        UsersUpdateDTO updateDTO = new UsersUpdateDTO("Any Name", testUserRead.getEmail());
        
        // Specific Arrange for this test: what the service should throw
        when(usersService.update(eq(testUserIdUpdate), any(UsersUpdateDTO.class)))
                .thenThrow(new IllegalArgumentException("Email already exists in the system."));

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", testUserIdUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }


    // --- End of update tests ---

}

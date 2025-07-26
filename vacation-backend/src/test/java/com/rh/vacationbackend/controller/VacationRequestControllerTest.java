package com.rh.vacationbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rh.vacationbackend.dto.VacationDateUpdateDTO;
import com.rh.vacationbackend.dto.VacationRequestCreateDTO;
import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.model.VacationRequestStatus;
import com.rh.vacationbackend.service.VacationRequestService;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VacationRequestControllerTest {

    @Mock
    private VacationRequestService vacationRequestService;

    @InjectMocks
    private VacationRequestController vacationRequestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID employeeId;
    private UUID managerId;
    private UUID requestId;
    private Users employee;
    private Users manager;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(vacationRequestController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new com.rh.vacationbackend.exception.GlobalExceptionHandler())
                .build();

        employeeId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        manager = Users.builder().id(managerId).name("O Gestor").build();
        employee = Users.builder().id(employeeId).name("O Funcionário").manager(manager).build();
    }

    @Test
    void createRequest_ShouldReturn201_WhenRequestIsValid() throws Exception {
        VacationRequestCreateDTO createDTO = new VacationRequestCreateDTO(employeeId, "Férias", LocalDate.now().plusDays(30), LocalDate.now().plusDays(45));
        VacationRequest createdRequest = VacationRequest.builder().id(requestId).user(employee).manager(manager).description("Férias").status(VacationRequestStatus.PENDING).createdAt(LocalDateTime.now()).build();
        when(vacationRequestService.create(any(VacationRequestCreateDTO.class), eq(employeeId))).thenReturn(createdRequest);

        mockMvc.perform(post("/api/vacations")
                        .header("X-User-Id", employeeId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getRequestById_ShouldReturn200AndRequest_WhenFound() throws Exception {
        VacationRequest request = VacationRequest.builder().id(requestId).user(employee).manager(manager).status(VacationRequestStatus.APPROVED).createdAt(LocalDateTime.now()).build();
        when(vacationRequestService.findById(requestId)).thenReturn(request);

        mockMvc.perform(get("/api/vacations/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()));
    }

     @Test
    void getRequestById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(vacationRequestService.findById(nonExistentId)).thenThrow(new RuntimeException("Vacation request not found"));

        mockMvc.perform(get("/api/vacations/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateVacationDates_ShouldReturn200AndUpdatedRequest() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 9, 11);
        VacationDateUpdateDTO dateDTO = new VacationDateUpdateDTO(startDate, startDate.plusDays(10));
        VacationRequest updatedRequest = VacationRequest.builder().id(requestId).user(employee).manager(manager).startDate(dateDTO.startDate()).endDate(dateDTO.endDate()).createdAt(LocalDateTime.now()).build();
        when(vacationRequestService.updateDates(eq(requestId), eq(employeeId), any(VacationDateUpdateDTO.class))).thenReturn(updatedRequest);

        mockMvc.perform(patch("/api/vacations/{id}/dates", requestId)
                        .header("X-User-Id", employeeId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(startDate.toString()));
    }

    @Test
    void cancelRequest_ShouldReturn204_WhenSuccessful() throws Exception {
        doNothing().when(vacationRequestService).cancel(requestId, managerId);

        mockMvc.perform(delete("/api/vacations/{id}", requestId)
                        .header("X-Manager-Id", managerId.toString()))
                .andExpect(status().isNoContent());

        verify(vacationRequestService, times(1)).cancel(requestId, managerId);
    }

    @Test
    void createRequest_ShouldReturn400_WhenServiceThrowsException() throws Exception {
        VacationRequestCreateDTO dto = new VacationRequestCreateDTO(employeeId, "Data inválida", LocalDate.now().plusDays(20), LocalDate.now().plusDays(10));
        when(vacationRequestService.create(any(VacationRequestCreateDTO.class), eq(employeeId)))
                .thenThrow(new IllegalArgumentException("The end date must be after the start date."));

        mockMvc.perform(post("/api/vacations")
                        .header("X-User-Id", employeeId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

}
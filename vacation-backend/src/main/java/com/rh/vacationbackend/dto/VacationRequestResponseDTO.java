package com.rh.vacationbackend.dto;

import com.rh.vacationbackend.model.VacationRequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VacationRequestResponseDTO(
    UUID requestId,
    UUID userId, // --- MUDANÇA AQUI ---
    String userName, // --- MUDANÇA AQUI ---
    UUID managerId,
    String managerName,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    VacationRequestStatus status,
    LocalDateTime createdAt
) {}
package com.rh.vacationbackend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record VacationRequestCreateDTO(
    UUID userId, // --- MUDANÇA AQUI ---
    String description,
    LocalDate startDate,
    LocalDate endDate
) {}
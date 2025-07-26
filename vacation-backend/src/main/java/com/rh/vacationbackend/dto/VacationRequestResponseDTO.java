package com.rh.vacationbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rh.vacationbackend.model.VacationRequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VacationRequestResponseDTO(
        UUID id,
        UUID userId,
        UUID managerId,
        String description,

        // ✅ Correção: Garante que a data seja convertida para uma String "yyyy-MM-dd"
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,

        // ✅ Correção: Garante que a data seja convertida para uma String "yyyy-MM-dd"
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,

        VacationRequestStatus status,

        // ✨ Bônus: É uma boa prática formatar o LocalDateTime também
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
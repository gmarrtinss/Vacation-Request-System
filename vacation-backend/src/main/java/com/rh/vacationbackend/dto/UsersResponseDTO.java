package com.rh.vacationbackend.dto;

import com.rh.vacationbackend.model.UsersRole;
import java.time.LocalDate;
import java.util.UUID;

public record UsersResponseDTO(
    UUID id,
    String cpf,
    String name,
    String email,
    UsersRole role,
    LocalDate admissionDate,
    UUID managerId
) {}
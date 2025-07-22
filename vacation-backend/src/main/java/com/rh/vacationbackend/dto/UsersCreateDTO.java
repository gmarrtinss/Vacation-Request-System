package com.rh.vacationbackend.dto;

import com.rh.vacationbackend.model.UsersRole;
import java.time.LocalDate;
import java.util.UUID;

public record UsersCreateDTO(
    String cpf,
    String name,
    String email,
    String password,
    UsersRole role,
    LocalDate admissionDate,
    Long sectorId,
    Long positionId,
    UUID managerId
) {}
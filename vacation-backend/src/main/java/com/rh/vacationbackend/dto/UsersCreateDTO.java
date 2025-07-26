package com.rh.vacationbackend.dto;

import com.rh.vacationbackend.model.UsersRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UsersCreateDTO(
    @NotBlank(message = "CPF cannot be blank")
    @Size(min = 11, max = 11, message = "CPF must have 11 characters")
    String cpf,

    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email,

    String password,

    @NotNull(message = "Role cannot be null")
    UsersRole role,

    @NotNull(message = "Admission date cannot be null")
    LocalDate admissionDate,

    @NotNull(message = "Sector ID cannot be null")
    Long sectorId,

    @NotNull(message = "Position ID cannot be null")
    Long positionId,

    UUID managerId
) {}
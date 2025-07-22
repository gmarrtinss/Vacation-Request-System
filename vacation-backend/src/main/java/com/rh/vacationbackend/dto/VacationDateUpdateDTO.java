package com.rh.vacationbackend.dto;

import java.time.LocalDate;

// DTO para receber exclusivamente a atualização das datas de uma solicitação.
public record VacationDateUpdateDTO(
    LocalDate startDate,
    LocalDate endDate
) {}
package com.rh.vacationbackend.dto;

import com.rh.vacationbackend.model.VacationRequestStatus;
import java.time.LocalDate;

public record PublicVacationStatusDTO(
    LocalDate startDate,
    LocalDate endDate,
    VacationRequestStatus status,
    String description
) {

}
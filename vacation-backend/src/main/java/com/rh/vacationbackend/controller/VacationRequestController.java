package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.dto.VacationDateUpdateDTO;
import com.rh.vacationbackend.dto.VacationRequestCreateDTO;
import com.rh.vacationbackend.dto.VacationRequestResponseDTO;
import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.service.VacationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vacations")
public class VacationRequestController {

    private final VacationRequestService vacationRequestService;

    public VacationRequestController(VacationRequestService vacationRequestService) {
        this.vacationRequestService = vacationRequestService;
    }

    @PostMapping
    public ResponseEntity<VacationRequestResponseDTO> createRequest(@RequestBody VacationRequestCreateDTO createDTO, @RequestHeader("X-User-Id") UUID userId) {
        VacationRequest createdRequest = vacationRequestService.create(createDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(createdRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacationRequestResponseDTO> getRequestById(@PathVariable UUID id) {
        VacationRequest request = vacationRequestService.findById(id);
        return ResponseEntity.ok(convertToResponseDto(request));
    }

    @PatchMapping("/{id}/dates")
    public ResponseEntity<VacationRequestResponseDTO> updateVacationDates(@PathVariable UUID id, @RequestHeader("X-User-Id") UUID userId, @RequestBody VacationDateUpdateDTO dateDTO) {
        VacationRequest updatedRequest = vacationRequestService.updateDates(id, userId, dateDTO);
        return ResponseEntity.ok(convertToResponseDto(updatedRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelRequest(@PathVariable UUID id, @RequestHeader("X-Manager-Id") UUID managerId) {
        vacationRequestService.cancel(id, managerId);
        return ResponseEntity.noContent().build();
    }



    private VacationRequestResponseDTO convertToResponseDto(VacationRequest request) {
        if (request == null) {
            return null;
        }
        UUID managerId = (request.getManager() != null) ? request.getManager().getId() : null;
        UUID userId = (request.getUser() != null) ? request.getUser().getId() : null;

        return new VacationRequestResponseDTO(
                request.getId(),
                userId,
                managerId,
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}

    // @PatchMapping("/{id}/approve")
    // public ResponseEntity<VacationRequestResponseDTO> approveRequest(@PathVariable UUID id, @RequestHeader("X-Manager-Id") UUID managerId) {
    //     VacationRequest approvedRequest = vacationRequestService.approve(id, managerId);
    //     return ResponseEntity.ok(convertToResponseDto(approvedRequest));
    // }

    // @PatchMapping("/{id}/reject")
    // public ResponseEntity<VacationRequestResponseDTO> rejectRequest(@PathVariable UUID id, @RequestHeader("X-Manager-Id") UUID managerId) {
    //     VacationRequest rejectedRequest = vacationRequestService.reject(id, managerId);
    //     return ResponseEntity.ok(convertToResponseDto(rejectedRequest));
    // }
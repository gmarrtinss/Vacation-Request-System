package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.service.VacationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// DTOs que vamos criar a seguir
import com.rh.vacationbackend.dto.VacationRequestCreateDTO;
import com.rh.vacationbackend.dto.VacationRequestResponseDTO;

@RestController
@RequestMapping("/api/vacations")
public class VacationRequestController {

    private final VacationRequestService vacationRequestService;

    public VacationRequestController(VacationRequestService vacationRequestService) {
        this.vacationRequestService = vacationRequestService;
    }

    @PostMapping
    public ResponseEntity<VacationRequestResponseDTO> createRequest(@RequestBody VacationRequestCreateDTO createDTO) {
        VacationRequest createdRequest = vacationRequestService.create(createDTO, createDTO.employeeId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(createdRequest));
    }

    

    @GetMapping("/{id}")
    public ResponseEntity<VacationRequestResponseDTO> getRequestById(@PathVariable UUID id) {
        VacationRequest request = vacationRequestService.findById(id);
        
        VacationRequestResponseDTO responseDTO = convertToResponseDto(request);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/employee/by-cpf/{cpf}")
    public ResponseEntity<List<VacationRequestResponseDTO>> getRequestsByEmployeeCpf(@PathVariable String cpf) {
        List<VacationRequest> requests = vacationRequestService.findRequestsByEmployeeCpf(cpf);

        List<VacationRequestResponseDTO> responseDTOs = requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID managerId) {
        
        vacationRequestService.cancel(requestId, managerId);
        
        return ResponseEntity.noContent().build();
    }

    private VacationRequestResponseDTO convertToResponseDto(VacationRequest request) {
        return new VacationRequestResponseDTO(
            request.getId(),
            request.getEmployee().getId(),
            request.getEmployee().getName(),
            request.getManager().getId(),
            request.getManager().getName(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate(),
            request.getStatus(),
            request.getCreatedAt()
        );
    }
}

// @PutMapping("/{requestId}/approve")
//     public ResponseEntity<VacationRequestResponseDTO> approveRequest(
//             @PathVariable UUID requestId,
//             @RequestParam UUID managerId) {
        
//         VacationRequest approvedRequest = vacationRequestService.approve(requestId, managerId);
//         return ResponseEntity.ok(convertToResponseDto(approvedRequest));
//     }

//     @PutMapping("/{requestId}/reject")
//     public ResponseEntity<VacationRequestResponseDTO> rejectRequest(
//             @PathVariable UUID requestId,
//             @RequestParam UUID managerId) {

//         VacationRequest rejectedRequest = vacationRequestService.reject(requestId, managerId);
//         return ResponseEntity.ok(convertToResponseDto(rejectedRequest));
//     }
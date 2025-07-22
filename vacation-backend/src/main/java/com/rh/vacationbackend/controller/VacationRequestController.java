package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.service.VacationRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rh.vacationbackend.dto.VacationDateUpdateDTO;
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
        VacationRequest createdRequest = vacationRequestService.create(createDTO, createDTO.userId()); // --- MUDANÇA AQUI ---
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(createdRequest));
    }

    @GetMapping
    public ResponseEntity<List<VacationRequestResponseDTO>> getAll() {
        List<VacationRequest> list = vacationRequestService.findAll();
        List<VacationRequestResponseDTO> dtoList = list.stream().map(this::convertToResponseDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<VacationRequestResponseDTO> getRequestById(@PathVariable UUID id) {
        VacationRequest request = vacationRequestService.findById(id);
        
        VacationRequestResponseDTO responseDTO = convertToResponseDto(request);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/employee/by-cpf/{cpf}")
    public ResponseEntity<List<VacationRequestResponseDTO>> getRequestsByUserCpf(@PathVariable String cpf) {
        List<VacationRequest> requests = vacationRequestService.findRequestsByUserCpf(cpf);

        List<VacationRequestResponseDTO> responseDTOs = requests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @DeleteMapping("/{requestId}/managerId")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID managerId) {
        
        vacationRequestService.cancel(requestId, managerId);
        
        return ResponseEntity.noContent().build();
    }


    
    @PatchMapping("/{requestId}/dates")
    public ResponseEntity<VacationRequestResponseDTO> updateVacationDates(
            @PathVariable UUID requestId,
            @RequestBody VacationDateUpdateDTO dateDto,
            @RequestParam UUID userId) {// tirar o userId, já tô usando o requestId não tem sentido eu usar mais uma informação

        VacationRequest updatedRequest = vacationRequestService.updateDates(requestId, userId, dateDto);
        
        return ResponseEntity.ok(convertToResponseDto(updatedRequest));
    }



    private VacationRequestResponseDTO convertToResponseDto(VacationRequest request) {
        return new VacationRequestResponseDTO(
            request.getId(),
            request.getUser().getId(),      
            request.getUser().getName(),    
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


/* 
 * Abaixo fica as alterações pra quando for usuário duplo(employee e manager)
 * Só faz sentido approve e request quando se tem o employee pedindo férias
 * Quando tiver solicitação de férias pelo employee, terá o approve ou reject do manager
*/

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
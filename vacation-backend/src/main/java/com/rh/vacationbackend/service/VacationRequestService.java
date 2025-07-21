package com.rh.vacationbackend.service;

import com.rh.vacationbackend.dto.PublicVacationStatusDTO;
import com.rh.vacationbackend.dto.VacationDateUpdateDTO;
import com.rh.vacationbackend.dto.VacationRequestCreateDTO;
import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.model.UsersRole;
import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.model.VacationRequestStatus;
import com.rh.vacationbackend.repository.UsersRepository;
import com.rh.vacationbackend.repository.VacationRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final UsersRepository usersRepository;

    public VacationRequestService(VacationRequestRepository vacationRequestRepository, UsersRepository usersRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public VacationRequest create(VacationRequestCreateDTO requestDTO, UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Requesting user not found."));

        if (user.getManager() == null) {
            throw new IllegalStateException("Cannot request vacation. No manager associated with this user.");
        }

        LocalDate today = LocalDate.now();
        Period periodOfService = Period.between(user.getAdmissionDate(), today);

        if (periodOfService.getYears() < 1) {
            throw new IllegalStateException("User must have at least one year of service to request vacation.");
        }

        if (requestDTO.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The start date of the vacation cannot be in the past.");
        }
        if (requestDTO.endDate().isBefore(requestDTO.startDate())) {
            throw new IllegalArgumentException("The end date must be after the start date.");
        }

        VacationRequest newRequest = new VacationRequest();
        newRequest.setDescription(requestDTO.description());
        newRequest.setStartDate(requestDTO.startDate());
        newRequest.setEndDate(requestDTO.endDate());
        newRequest.setUser(user);
        newRequest.setManager(user.getManager());
        newRequest.setStatus(VacationRequestStatus.PENDING);

        return vacationRequestRepository.save(newRequest);
    }

    @Transactional(readOnly = true)
    public VacationRequest findById(UUID id) {
        return vacationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacation request not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findByUserId(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        return vacationRequestRepository.findByUser(user);
    }


    @Transactional(readOnly = true)
    public List<VacationRequest> findAll() {
        return vacationRequestRepository.findAll();
    }

    @Transactional
    public void cancel(UUID requestId, UUID managerId) {
        VacationRequest request = findById(requestId);

        Users manager = usersRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));

        if (manager.getRole() != UsersRole.MANAGER) {
            throw new SecurityException("Access denied. Only managers can cancel requests.");
        }

        if (!request.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access denied. You are not the manager responsible for this request.");
        }
        
        if (request.getStatus() != VacationRequestStatus.PENDING) {
            throw new IllegalStateException("Only requests with PENDING status can be canceled.");
        }

        vacationRequestRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findAllRequestsByManager(UUID managerId) {
        Users manager = usersRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));
        
        if (manager.getRole() != UsersRole.MANAGER) {
            throw new SecurityException("Only managers can view team requests.");
        }
        
        return vacationRequestRepository.findByManager(manager);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findRequestsByUserForManager(UUID userId, UUID managerId) {
        Users manager = usersRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));
        
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getManager() == null || !user.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access denied. You are not the manager of this User.");
        }

        return vacationRequestRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findRequestsByUserCpf(String cpf) {
        Users user = usersRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("User not found with CPF: " + cpf));
        
        return vacationRequestRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<PublicVacationStatusDTO> findRequestsByCpfForPublicView(String cpf) {
        Optional<Users> usersOpt = usersRepository.findByCpf(cpf);
        if (usersOpt.isEmpty()) {
            return List.of();
        }

        List<VacationRequest> requests = vacationRequestRepository.findByUser(usersOpt.get());

        return requests.stream()
                .map(request -> new PublicVacationStatusDTO(
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus(),
                        request.getDescription()))
                .toList();
    }

     /**
     * UPDATE (Partial): Updates only the start and end dates of a vacation request.
     * Business logic ensures the request is still PENDING and the new dates are valid.
     */
    @Transactional
    public VacationRequest updateDates(UUID requestId, UUID userId, VacationDateUpdateDTO dateDto) {
        VacationRequest request = findById(requestId);

        if (!request.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied. You can only edit your own vacation requests.");
        }

        if (request.getStatus() != VacationRequestStatus.PENDING) {
            throw new IllegalStateException("Only requests with PENDING status can be modified.");
        }

        if (dateDto.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The start date of the vacation cannot be in the past.");
        }
        if (dateDto.endDate().isBefore(dateDto.startDate())) {
            throw new IllegalArgumentException("The end date must be after the start date.");
        }

        request.setStartDate(dateDto.startDate());
        request.setEndDate(dateDto.endDate());

        return vacationRequestRepository.save(request);
    }

    // Métodos approve e reject comentados, conforme o seu ficheiro original.
    // Você pode descomentá-los quando a lógica de autenticação do gestor estiver pronta.
    /*
    @Transactional
    public VacationRequest approve(UUID requestId, UUID managerId) {
        VacationRequest request = findById(requestId);
        Users manager = usersRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));

        if (!request.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access denied. You are not the manager responsible for this request.");
        }

        if (request.getStatus() != VacationRequestStatus.PENDING) {
            throw new IllegalStateException("Only requests with PENDING status can be approved.");
        }

        request.setStatus(VacationRequestStatus.APPROVED);
        return vacationRequestRepository.save(request);
    }

    @Transactional
    public VacationRequest reject(UUID requestId, UUID managerId) {
        VacationRequest request = findById(requestId);
        Users manager = usersRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));

        if (!request.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access denied. You are not the manager responsible for this request.");
        }
        
        if (request.getStatus() != VacationRequestStatus.PENDING) {
            throw new IllegalStateException("Only requests with PENDING status can be declined.");
        }

        request.setStatus(VacationRequestStatus.DECLINED);
        return vacationRequestRepository.save(request);
    }
    */
}
package com.rh.vacationbackend.service;

import com.rh.vacationbackend.dto.PublicVacationStatusDTO;
import com.rh.vacationbackend.dto.VacationRequestCreateDTO;
import com.rh.vacationbackend.model.Employee;
import com.rh.vacationbackend.model.EmployeeRole;
import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.model.VacationRequestStatus;
import com.rh.vacationbackend.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;

    public VacationRequestService(VacationRequestRepository vacationRequestRepository, EmployeeRepository employeeRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public VacationRequest create(VacationRequestCreateDTO requestDTO, UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Requesting employee not found."));

        if (employee.getManager() == null) {
            throw new IllegalStateException("Cannot request vacation. No manager associated with this employee.");
        }

        LocalDate today = LocalDate.now();
        LocalDate admissionDate = employee.getAdmissionDate();
        Period periodOfService = Period.between(admissionDate, today);

        if (periodOfService.getYears() < 1) {
            throw new IllegalStateException("Employee must have at least one year of service to request vacation.");
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
        newRequest.setEmployee(employee);
        newRequest.setManager(employee.getManager());
        newRequest.setStatus(VacationRequestStatus.PENDING);

        return vacationRequestRepository.save(newRequest);
    }

    @Transactional(readOnly = true)
    public VacationRequest findById(UUID id) {
        return vacationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacation request not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findByEmployeeId(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found."));
        return vacationRequestRepository.findByEmployee(employee);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findPendingRequestsByManager(UUID managerId) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));
        return vacationRequestRepository.findByManager(manager);
    }

    @Transactional
    public VacationRequest approve(UUID requestId, UUID managerId) {
        VacationRequest request = findById(requestId);
        Employee manager = employeeRepository.findById(managerId)
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
        Employee manager = employeeRepository.findById(managerId)
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

    /**
     * DELETE: Cancels a vacation request.
     * New business rule: Only the responsible MANAGER can cancel a request,
     * and only if the status is still PENDING.
     */
    @Transactional
    public void cancel(UUID requestId, UUID managerId) {
        VacationRequest request = findById(requestId);

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));

        if (manager.getRole() != EmployeeRole.MANAGER) {
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
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));
        
        if (manager.getRole() != EmployeeRole.MANAGER) {
            throw new SecurityException("Only managers can view team requests.");
        }
        
        return vacationRequestRepository.findByManager(manager);
    }

    @Transactional(readOnly = true)
    public List<VacationRequest> findRequestsByEmployeeForManager(UUID employeeId, UUID managerId) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found."));
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found."));

        if (employee.getManager() == null || !employee.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Access denied. You are not the manager of this employee.");
        }

        return vacationRequestRepository.findByEmployee(employee);
    }

    /**
     * READ: Busca todas as solicitações de um funcionário específico, usando o CPF como chave.
     */
    @Transactional(readOnly = true)
    public List<VacationRequest> findRequestsByEmployeeCpf(String cpf) {

        Employee employee = employeeRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Employee not found with CPF: " + cpf));


        return vacationRequestRepository.findByEmployee(employee);
    }


    @Transactional(readOnly = true)
    public List<PublicVacationStatusDTO> findRequestsByCpfForPublicView(String cpf) {
        Optional<Employee> employeeOpt = employeeRepository.findByCpf(cpf);
        if (employeeOpt.isEmpty()) {
            return List.of();
        }

        List<VacationRequest> requests = vacationRequestRepository.findByEmployee(employeeOpt.get());

        return requests.stream()
                .map(request -> new PublicVacationStatusDTO(
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus(),
                        request.getDescription()))
                .toList();
    }
}
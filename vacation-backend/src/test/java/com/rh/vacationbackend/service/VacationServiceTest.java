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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationServiceTest {

    @Mock
    private VacationRequestRepository vacationRequestRepository;
    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private VacationRequestService vacationService;

    // Common test data
    private Users employee;
    private Users manager;
    private VacationRequest request;
    private UUID employeeId;
    private UUID managerId;
    private UUID requestId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        manager = Users.builder().id(managerId).name("Manager").role(UsersRole.MANAGER).build();
        employee = Users.builder().id(employeeId).name("Employee").admissionDate(LocalDate.now().minusYears(2)).manager(manager).build();
        request = VacationRequest.builder().id(requestId).user(employee).manager(manager).startDate(LocalDate.now().plusDays(30)).endDate(LocalDate.now().plusDays(45)).status(VacationRequestStatus.PENDING).build();
    }

    // --- `create` Method Tests ---

    @Test
    void create_ShouldSucceed_WhenDataIsValid() {
        VacationRequestCreateDTO dto = new VacationRequestCreateDTO(employeeId, "Valid vacation", LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.save(any(VacationRequest.class))).thenAnswer(i -> i.getArgument(0));

        VacationRequest result = vacationService.create(dto, employeeId);

        assertNotNull(result);
        assertEquals(VacationRequestStatus.PENDING, result.getStatus());
    }

    @Test
    void create_ShouldFail_WhenEmployeeHasLessThanOneYearOfService() {
        employee.setAdmissionDate(LocalDate.now().minusMonths(11));
        VacationRequestCreateDTO dto = new VacationRequestCreateDTO(employeeId, "Too soon", LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(IllegalStateException.class, () -> vacationService.create(dto, employeeId));
    }

    @Test
    void create_ShouldThrowException_WhenStartDateIsInThePast() {
        VacationRequestCreateDTO dto = new VacationRequestCreateDTO(employeeId, "Férias", LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> vacationService.create(dto, employeeId), "The start date cannot be in the past.");
    }

    @Test
    void create_ShouldThrowException_WhenEndDateIsBeforeStartDate() {
        VacationRequestCreateDTO dto = new VacationRequestCreateDTO(employeeId, "Férias", LocalDate.now().plusDays(10), LocalDate.now().plusDays(5));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> vacationService.create(dto, employeeId), "The end date must be after the start date.");
    }

    // ✅ NEW TEST
    @Test
    void create_ShouldThrowException_WhenUserHasNoManager() {
        employee.setManager(null); // Set manager to null
        VacationRequestCreateDTO dto = new VacationRequestCreateDTO(employeeId, "No manager", LocalDate.now().plusDays(10), LocalDate.now().plusDays(20));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        var exception = assertThrows(IllegalStateException.class, () -> vacationService.create(dto, employeeId));
        assertEquals("Cannot request vacation. No manager associated with this user.", exception.getMessage());
    }


    // --- `cancel` Method Tests ---

    @Test
    void cancel_ShouldSucceed_WhenManagerIsCorrectAndStatusIsPending() {
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(usersRepository.findById(managerId)).thenReturn(Optional.of(manager));
        doNothing().when(vacationRequestRepository).delete(request);

        vacationService.cancel(requestId, managerId);

        verify(vacationRequestRepository).delete(request);
    }

    @Test
    void cancel_ShouldFail_WhenUserIsNotTheCorrectManager() {
        Users otherManager = Users.builder().id(UUID.randomUUID()).name("Other Manager").role(UsersRole.MANAGER).build();
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(usersRepository.findById(otherManager.getId())).thenReturn(Optional.of(otherManager));

        assertThrows(SecurityException.class, () -> vacationService.cancel(requestId, otherManager.getId()));
    }

    @Test
    void cancel_ShouldThrowSecurityException_WhenUserIsNotManager() {
        employee.setRole(UsersRole.EMPLOYEE);
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(SecurityException.class, () -> vacationService.cancel(requestId, employeeId));
    }

    @Test
    void cancel_ShouldFail_WhenRequestStatusIsNotPending() {
        request.setStatus(VacationRequestStatus.APPROVED);
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        // FIX: Mock added to prevent test from failing with "Manager not found"
        when(usersRepository.findById(managerId)).thenReturn(Optional.of(manager));

        assertThrows(IllegalStateException.class, () -> vacationService.cancel(requestId, managerId));
    }

    // --- `updateDates` Method Tests ---

    @Test
    void updateDates_ShouldSucceed_WhenUserIsOwnerAndStatusIsPending() {
        VacationDateUpdateDTO dto = new VacationDateUpdateDTO(LocalDate.now().plusDays(5), LocalDate.now().plusDays(15));
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(vacationRequestRepository.save(any(VacationRequest.class))).thenReturn(request);

        VacationRequest result = vacationService.updateDates(requestId, employeeId, dto);

        assertEquals(dto.startDate(), result.getStartDate());
    }
    
    @Test
    void updateDates_ShouldThrowException_WhenStatusIsNotPending() {
        request.setStatus(VacationRequestStatus.APPROVED);
        VacationDateUpdateDTO dto = new VacationDateUpdateDTO(LocalDate.now().plusDays(5), LocalDate.now().plusDays(15));
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        
        assertThrows(IllegalStateException.class, () -> vacationService.updateDates(requestId, employeeId, dto));
    }

    @Test
    void updateDates_ShouldThrowException_WhenUserIsNotTheOwner() {
        UUID anotherUserId = UUID.randomUUID();
        VacationDateUpdateDTO dto = new VacationDateUpdateDTO(LocalDate.now().plusDays(50), LocalDate.now().plusDays(60));
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(SecurityException.class, () -> vacationService.updateDates(requestId, anotherUserId, dto));
    }

    @Test
    void updateDates_ShouldThrowException_WhenStartDateIsInThePast() {
        VacationDateUpdateDTO dto = new VacationDateUpdateDTO(LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () -> {
            vacationService.updateDates(requestId, employeeId, dto);
        });
    }

    @Test
    void updateDates_ShouldThrowException_WhenEndDateIsBeforeStartDate() {
        VacationDateUpdateDTO dto = new VacationDateUpdateDTO(LocalDate.now().plusDays(10), LocalDate.now().plusDays(5));
        when(vacationRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () -> {
            vacationService.updateDates(requestId, employeeId, dto);
        });
    }

     @Test
    void findByUserId_ShouldReturnListOfRequests() {
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.findByUser(employee)).thenReturn(Collections.singletonList(request));

        List<VacationRequest> result = vacationService.findByUserId(employeeId);

        assertEquals(1, result.size());
        assertEquals(request, result.get(0));
    }

    @Test
    void findAllRequestsByManager_ShouldReturnRequests() {
        when(usersRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(vacationRequestRepository.findByManager(manager)).thenReturn(Collections.singletonList(request));

        List<VacationRequest> result = vacationService.findAllRequestsByManager(managerId);

        assertEquals(1, result.size());
    }

    @Test
    void findRequestsByUserForManager_ShouldReturnRequests() {
        when(usersRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.findByUser(employee)).thenReturn(Collections.singletonList(request));

        List<VacationRequest> result = vacationService.findRequestsByUserForManager(employeeId, managerId);

        assertEquals(1, result.size());
    }

    @Test
    void findRequestsByUserForManager_ShouldThrowSecurityException_WhenNotManager() {
        Users otherManager = Users.builder().id(UUID.randomUUID()).name("Another Manager").build();
        when(usersRepository.findById(otherManager.getId())).thenReturn(Optional.of(otherManager));
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        assertThrows(SecurityException.class, () -> vacationService.findRequestsByUserForManager(employeeId, otherManager.getId()));
    }

    @Test
    void findRequestsByUserCpf_ShouldReturnRequests() {
        when(usersRepository.findByCpf("12345678901")).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.findByUser(employee)).thenReturn(Collections.singletonList(request));

        List<VacationRequest> result = vacationService.findRequestsByUserCpf("12345678901");
        assertEquals(1, result.size());
    }

     // --- Read Method Tests ---
    
    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(vacationRequestRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vacationService.findById(nonExistentId));
    }
    
    @Test
    void findByUserId_ShouldThrowException_WhenUserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vacationService.findByUserId(nonExistentUserId));
    }
    
    @Test
    void findAll_ShouldReturnAllRequests() {
        when(vacationRequestRepository.findAll()).thenReturn(Collections.singletonList(request));
        List<VacationRequest> result = vacationService.findAll();
        assertEquals(1, result.size());
        verify(vacationRequestRepository, times(1)).findAll();
    }

    // --- Public View Method Tests ---
    @Test
    void findRequestsByCpfForPublicView_ShouldReturnEmptyList_WhenUserNotFound() {
        when(usersRepository.findByCpf("nonexistentcpf")).thenReturn(Optional.empty());
        List<PublicVacationStatusDTO> result = vacationService.findRequestsByCpfForPublicView("nonexistentcpf");
        assertTrue(result.isEmpty());
    }

    @Test
    void findRequestsByCpfForPublicView_ShouldReturnListOfDtos_WhenUserFound() {
        when(usersRepository.findByCpf(employee.getCpf())).thenReturn(Optional.of(employee));
        when(vacationRequestRepository.findByUser(employee)).thenReturn(Collections.singletonList(request));
        
        List<PublicVacationStatusDTO> result = vacationService.findRequestsByCpfForPublicView(employee.getCpf());
        
        assertEquals(1, result.size());
        assertEquals(request.getDescription(), result.get(0).description());
        assertEquals(request.getStatus(), result.get(0).status());
    }
}
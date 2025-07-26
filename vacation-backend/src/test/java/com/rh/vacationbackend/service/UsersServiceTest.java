package com.rh.vacationbackend.service;

import com.rh.vacationbackend.dto.UsersCreateDTO;
import com.rh.vacationbackend.dto.UsersUpdateDTO;
import com.rh.vacationbackend.model.Position;
import com.rh.vacationbackend.model.Sector;
import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.model.UsersRole;
import com.rh.vacationbackend.repository.PositionRepository;
import com.rh.vacationbackend.repository.SectorRepository;
import com.rh.vacationbackend.repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private SectorRepository sectorRepository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersService usersService;

    // Common test data
    private Users testEmployee;
    private Users testManager;
    private Sector testSector;
    private Position testPosition;
    private UUID employeeId;
    private UUID managerId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        testSector = new Sector("Technology");
        testSector.setId(1L);
        testPosition = new Position("Developer");
        testPosition.setId(1L);

        testManager = Users.builder()
                .id(managerId)
                .name("Test Manager")
                .role(UsersRole.MANAGER)
                .sector(testSector)
                .build();

        testEmployee = Users.builder()
                .id(employeeId)
                .name("Test User")
                .cpf("12345678901")
                .email("test@example.com")
                .role(UsersRole.EMPLOYEE)
                .admissionDate(LocalDate.now().minusYears(1))
                .sector(testSector)
                .position(testPosition)
                .manager(testManager)
                .build();
    }

    // --- Create Method Tests ---

    @Test
    void create_ShouldSucceed_ForEmployeeWithManager() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("44455566602", "New Employee", "new@emp.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, managerId);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(testSector));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(testPosition));
        when(usersRepository.findById(managerId)).thenReturn(Optional.of(testManager));
        when(usersRepository.save(any(Users.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Users result = usersService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("New Employee", result.getName());
        assertEquals(testManager, result.getManager());
        assertNull(result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void create_ShouldSucceed_ForManagerWithoutManager() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("98765432100", "New Manager", "manager.new@email.com", "password123", UsersRole.MANAGER, LocalDate.now(), 1L, 1L, null);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(testSector));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(testPosition));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Users result = usersService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("New Manager", result.getName());
        assertEquals(UsersRole.MANAGER, result.getRole());
        assertNull(result.getManager());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void create_ShouldThrowException_WhenCpfAlreadyExists() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("12345678901", "New User", "new@email.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, managerId);
        when(usersRepository.findByCpf("12345678901")).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> usersService.create(dto));
        verify(usersRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenManagerIsNotFromSameSector() {
        // Arrange
        Sector differentSector = new Sector("Marketing");
        differentSector.setId(2L);
        testManager.setSector(differentSector); // Manager is in a different sector

        UsersCreateDTO dto = new UsersCreateDTO("44455566602", "New Employee", "new@emp.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, managerId);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(testSector));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(testPosition));
        when(usersRepository.findById(managerId)).thenReturn(Optional.of(testManager));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> usersService.create(dto));
    }
    
    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("11122233344", "Another User", "test@example.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, managerId);
        when(usersRepository.findByCpf(dto.cpf())).thenReturn(Optional.empty());
        when(usersRepository.findByEmail(dto.email())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> usersService.create(dto));
        assertEquals("Email already exists in the system.", exception.getMessage());
    }
    
    @Test
    void create_ShouldThrowException_WhenAdmissionDateIsInTheFuture() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("11122233344", "Future Employee", "future@emp.com", null, UsersRole.EMPLOYEE, LocalDate.now().plusDays(1), 1L, 1L, managerId);
        
        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> usersService.create(dto));
        assertEquals("Admission date cannot be in the future.", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenEmployeeIsCreatedWithoutManager() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("11122233344", "Lonesome Employee", "lonesome@emp.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, null);

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> usersService.create(dto));
        assertEquals("Manager ID is required for EMPLOYEE role.", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenManagerPasswordIsBlank() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("98765432100", "Manager NoPass", "nopass@mgr.com", " ", UsersRole.MANAGER, LocalDate.now(), 1L, 1L, null);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(testSector));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(testPosition));
        
        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> usersService.create(dto));
        assertEquals("Password is required for managers.", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenEmployeeHasPassword() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("11122233344", "Emp WithPass", "pass@emp.com", "somepass", UsersRole.EMPLOYEE, LocalDate.now(), 1L, 1L, managerId);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(testSector));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(testPosition));
        
        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> usersService.create(dto));
        assertEquals("Password field should not be provided for EMPLOYEE role.", exception.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenSectorNotFound() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("11122233344", "No Sector Emp", "nosector@emp.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 99L, 1L, managerId);
        when(sectorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> usersService.create(dto));
    }

    @Test
    void create_ShouldThrowException_WhenPositionNotFound() {
        // Arrange
        UsersCreateDTO dto = new UsersCreateDTO("11122233344", "No Position Emp", "noposition@emp.com", null, UsersRole.EMPLOYEE, LocalDate.now(), 1L, 99L, managerId);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(testSector));
        when(positionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> usersService.create(dto));
    }

    // --- Read Method Tests ---

    @Test
    void findByCpf_ShouldReturnUser_WhenCpfExists() {
        // Arrange
        when(usersRepository.findByCpf("12345678901")).thenReturn(Optional.of(testEmployee));

        // Act
        Users result = usersService.findByCpf("12345678901");

        // Assert
        assertNotNull(result);
        assertEquals("12345678901", result.getCpf());
    }
    
    @Test
    void findByCpf_ShouldThrowException_WhenCpfIsNull() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> usersService.findByCpf(null));
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(usersRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> usersService.findById(nonExistentId));
    }

    @Test
    void findByIdForManager_ShouldReturnUser_WhenManagerIsCorrect() {
        // Arrange
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        
        // Act
        Users result = usersService.findByIdForManager(employeeId, testManager);

        // Assert
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
    }

    @Test
    void findByIdForManager_ShouldThrowSecurityException_WhenManagerIsIncorrect() {
        // Arrange
        Users anotherManager = Users.builder().id(UUID.randomUUID()).build();
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(SecurityException.class, () -> usersService.findByIdForManager(employeeId, anotherManager));
    }
    
    @Test
    void findByIdForManager_ShouldThrowSecurityException_WhenUserHasNoManager() {
        // Arrange
        testEmployee.setManager(null);
        when(usersRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(SecurityException.class, () -> usersService.findByIdForManager(employeeId, testManager));
    }

    @Test
    void findManagedEmployeeByCpf_ShouldThrowException_WhenCpfNotFound() {
        // Arrange
        String nonExistentCpf = "00000000000";
        when(usersRepository.findByCpf(nonExistentCpf)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usersService.findManagedEmployeeByCpf(nonExistentCpf));
    }

    // --- Update Method Tests ---

    @Test
    void update_ShouldSucceed_WhenDataIsValid() {
        // Arrange
        UsersUpdateDTO dto = new UsersUpdateDTO("Updated Name", "updated@example.com");
        UUID userId = testEmployee.getId();
        when(usersRepository.findById(userId)).thenReturn(Optional.of(testEmployee));
        when(usersRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(usersRepository.save(any(Users.class))).thenReturn(testEmployee);

        // Act
        Users result = usersService.update(userId, dto);

        // Assert
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void update_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        UsersUpdateDTO dto = new UsersUpdateDTO("Random Name", "existing.email@company.com");
        UUID userId = testEmployee.getId();
        Users anotherUserWithEmail = new Users();
        
        when(usersRepository.findById(userId)).thenReturn(Optional.of(testEmployee));
        when(usersRepository.findByEmail("existing.email@company.com")).thenReturn(Optional.of(anotherUserWithEmail));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> usersService.update(userId, dto));
    }

    // --- Delete Method Tests ---

    @Test
    void delete_ShouldSucceed_WhenUserExists() {
        // Arrange
        UUID userId = testEmployee.getId();
        when(usersRepository.findById(userId)).thenReturn(Optional.of(testEmployee));
        doNothing().when(usersRepository).deleteById(userId);

        // Act
        usersService.delete(userId);

        // Assert
        verify(usersRepository, times(1)).deleteById(userId);
    }

    @Test
    void delete_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        when(usersRepository.findById(nonExistentUserId)).thenThrow(new EntityNotFoundException("User not found with ID: " + nonExistentUserId));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> usersService.delete(nonExistentUserId));
    }
}
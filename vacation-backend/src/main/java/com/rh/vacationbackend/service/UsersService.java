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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final SectorRepository sectorRepository;
    private final PositionRepository positionRepository;

    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder,
                        SectorRepository sectorRepository, PositionRepository positionRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.sectorRepository = sectorRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public Users create(UsersCreateDTO dto) {
        if (usersRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new IllegalArgumentException("CPF already exists in the system.");
        }
        if (usersRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists in the system.");
        }

        Users newUser = new Users();
        newUser.setCpf(dto.cpf());
        newUser.setName(dto.name());
        newUser.setEmail(dto.email());
        newUser.setRole(dto.role());
        newUser.setAdmissionDate(dto.admissionDate());

        if (dto.role() == UsersRole.MANAGER) {
            if (dto.password() == null || dto.password().isBlank()) {
                throw new IllegalArgumentException("Password is required for managers.");
            }
            newUser.setPassword(passwordEncoder.encode(dto.password()));
        }

        if (dto.sectorId() != null) {
            Sector sector = sectorRepository.findById(dto.sectorId())
                    .orElseThrow(() -> new EntityNotFoundException("Sector not found with ID: " + dto.sectorId()));
            newUser.setSector(sector);
        }

        if (dto.positionId() != null) {
            Position position = positionRepository.findById(dto.positionId())
                    .orElseThrow(() -> new EntityNotFoundException("Position not found with ID: " + dto.positionId()));
            newUser.setPosition(position);
        }

        if (dto.managerId() != null) {
            Users manager = findById(dto.managerId());
            newUser.setManager(manager);
        }

        return usersRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Users findById(UUID id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Users findManagedEmployeeByCpf(String cpf) {
        Users usersToFind = usersRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com o CPF: " + cpf));
        return usersToFind;
    }

    /**
     * UPDATE: Atualiza os dados de um utilizador existente a partir de um DTO.
     */
    @Transactional
    public Users update(UUID id, UsersUpdateDTO dto) {
        // 1. Busca a entidade existente no banco para garantir que ela existe.
        Users existingUser = findById(id);

        // 2. Validação de negócio: Verifica se o novo email já está a ser usado por outro utilizador.
        if (dto.email() != null && !dto.email().equals(existingUser.getEmail())) {
            if (usersRepository.findByEmail(dto.email()).isPresent()) {
                throw new IllegalArgumentException("Email already exists in the system.");
            }
        }

        // 3. Atualiza os campos da entidade encontrada com os novos valores do DTO.
        if (dto.name() != null) {
            existingUser.setName(dto.name());
        }
        if (dto.email() != null) {
            existingUser.setEmail(dto.email());
        }
        
        return usersRepository.save(existingUser);
    }
    /**
     * DELETE: Remove um funcionário do banco de dados.
     */
    @Transactional
    public void delete(UUID id) {
        findById(id);
        usersRepository.deleteById(id);
    }

    // --- MÉTODOS DE NEGÓCIO ESPECÍFICOS ---

    /**
     * READ (Específico): Lista todos os funcionários gerenciados por um gestor.
     * Este método seria chamado por um endpoint protegido, acessível apenas por gestores.
     */
    @Transactional(readOnly = true)
    public List<Users> findEmployeesByManager(Users manager) {
        if (manager.getRole() != UsersRole.MANAGER) {
            throw new SecurityException("Only managers can acess their employees.");
        }
        return usersRepository.findByManager(manager);
    }

    public Users findByCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF cannot be null or empty.");
        }
        return usersRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("User not found with CPF: " + cpf));
    }
}
package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.dto.UsersCreateDTO;
import com.rh.vacationbackend.dto.UsersResponseDTO;
import com.rh.vacationbackend.dto.UsersUpdateDTO;
import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.service.UsersService;

import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Validated
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseEntity<UsersResponseDTO> create(@RequestBody UsersCreateDTO dto) {
        Users savedUser = usersService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedUser));
    }

    @GetMapping
    public ResponseEntity<List<UsersResponseDTO>> getAll() {
        List<Users> list = usersService.findAll();
        List<UsersResponseDTO> dtoList = list.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsersResponseDTO> getById(@PathVariable UUID id) {
        Users user = usersService.findById(id);
        return ResponseEntity.ok(convertToDto(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsersResponseDTO> delete(@PathVariable UUID id) {
        usersService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsersResponseDTO> update(@PathVariable UUID id, @RequestBody UsersUpdateDTO dto) {
        Users updatedUser = usersService.update(id, dto);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }




    @GetMapping("/by-cpf/{cpf}")
    public ResponseEntity<UsersResponseDTO> getByCpf( @PathVariable @Pattern(regexp = "[0-9]{11}", message = "CPF must contain exactly 11 digits.") String cpf) {
        
        Users user = usersService.findByCpf(cpf); 
        return ResponseEntity.ok(convertToDto(user));
    }

    private UsersResponseDTO convertToDto(Users user) {
        UUID managerId = (user.getManager() != null) ? user.getManager().getId() : null;
        return new UsersResponseDTO(
            user.getId(),
            user.getCpf(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getAdmissionDate(),
            managerId
        );
    }
}
package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.dto.PositionDTO;
import com.rh.vacationbackend.model.Position;
import com.rh.vacationbackend.service.PositionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    public ResponseEntity<PositionDTO> createPosition(@RequestBody PositionDTO dto) {
        Position createdPosition = positionService.create(dto.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdPosition));
    }

    @GetMapping
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        List<Position> positions = positionService.findAll();
        List<PositionDTO> dtos = positions.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionDTO> getPositionById(@PathVariable Long id) {
        Position position = positionService.findById(id);
        return ResponseEntity.ok(convertToDto(position));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PositionDTO convertToDto(Position position) {
        return new PositionDTO(position.getId(), position.getName());
    }
}
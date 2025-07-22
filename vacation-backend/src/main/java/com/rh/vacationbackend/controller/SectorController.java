package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.dto.SectorDTO;
import com.rh.vacationbackend.model.Sector;
import com.rh.vacationbackend.service.SectorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sectors")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @PostMapping
    public ResponseEntity<SectorDTO> createSector(@RequestBody SectorDTO dto) {
        Sector createdSector = sectorService.create(dto.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdSector));
    }

    @GetMapping
    public ResponseEntity<List<SectorDTO>> getAllSectors() {
        List<Sector> sectors = sectorService.findAll();
        List<SectorDTO> dtos = sectors.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectorDTO> getSectorById(@PathVariable Long id) {
        Sector sector = sectorService.findById(id);
        return ResponseEntity.ok(convertToDto(sector));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSector(@PathVariable Long id) {
        sectorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SectorDTO convertToDto(Sector sector) {
        return new SectorDTO(sector.getId(), sector.getName());
    }
}
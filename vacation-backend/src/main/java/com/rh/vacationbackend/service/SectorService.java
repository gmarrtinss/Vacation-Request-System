package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Sector;
import com.rh.vacationbackend.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;

    public SectorService(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    @Transactional
    public Sector create(String sectorName) {
        Sector newSector = new Sector(sectorName);
        return sectorRepository.save(newSector);
    }

    @Transactional(readOnly = true)
    public List<Sector> findAll() {
        return sectorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sector findById(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sector not found with ID: " + id));
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        sectorRepository.deleteById(id);
    }
}
package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Position;
import com.rh.vacationbackend.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Transactional
    public Position create(String positionName) {
        Position newPosition = new Position(positionName);
        return positionRepository.save(newPosition);
    }

    @Transactional(readOnly = true)
    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Position findById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found with ID: " + id));
    }

    @Transactional
    public void delete(Long id) {
        findById(id); // Verifica se existe antes de deletar
        positionRepository.deleteById(id);
    }
}
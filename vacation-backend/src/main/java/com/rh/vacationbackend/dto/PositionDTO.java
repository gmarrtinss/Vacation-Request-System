package com.rh.vacationbackend.dto;

// Usaremos este DTO tanto para criar/atualizar quanto para responder,
// pois a entidade Position é bem simples.
public record PositionDTO(
    Long id,
    String name
) {}
package com.rh.vacationbackend.dto;

// O campo foi renomeado de 'cpf' para 'login' para ser mais genérico
public record LoginRequestDTO(
    String login,
    String password
) {}
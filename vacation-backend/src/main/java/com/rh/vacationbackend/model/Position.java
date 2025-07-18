package com.rh.vacationbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Exemplo: "Gerente", "Desenvolvedor", "Estagiário"

    // Construtor padrão (necessário para o JPA)
    public Position() {
    }

    // Construtor com argumento (opcional, útil para testes ou seeds)
    public Position(String name) {
        this.name = name;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

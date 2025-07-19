package com.rh.vacationbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue; // Importe esta anotação
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    @Column(length = 11, unique = true, nullable = false)
    private String cpf;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 256)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsersRole role;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Users manager;

    // highlight-start
    // --- MÉTODO DE VALIDAÇÃO CONDICIONAL ---

    @AssertTrue(message = "A senha é obrigatória para a role 'GESTOR'")
    private boolean isPasswordValid() {
        // Se a role não for MANAGER, a validação não se aplica e o método retorna verdadeiro.
        if (this.role != UsersRole.MANAGER) {
            return true;
        }
        return this.password != null && !this.password.isBlank();
    }
}
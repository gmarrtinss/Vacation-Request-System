package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Position;
import com.rh.vacationbackend.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private PositionService positionService;

    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position("Developer");
        position.setId(1L);
    }

    @Test
    void create_ShouldReturnSavedPosition() {
        // Arrange: Configura o mock do save para retornar o cargo criado.
        when(positionRepository.save(any(Position.class))).thenReturn(position);

        // Act: Cria um novo cargo.
        Position result = positionService.create("Developer");

        // Assert: Verifica se o cargo retornado não é nulo e tem o nome correto.
        assertNotNull(result);
        assertEquals("Developer", result.getName());
        verify(positionRepository).save(any(Position.class));
    }

    @Test
    void findAll_ShouldReturnAllPositions() {
        // Arrange: Simula o retorno de uma lista de cargos.
        when(positionRepository.findAll()).thenReturn(Collections.singletonList(position));

        // Act: Busca todos os cargos.
        List<Position> result = positionService.findAll();

        // Assert: Confere o tamanho da lista.
        assertEquals(1, result.size());
        verify(positionRepository).findAll();
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Arrange: Simula que o cargo com ID 1L não foi encontrado.
        when(positionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Garante que uma exceção é lançada.
        assertThrows(RuntimeException.class, () -> positionService.findById(1L));
    }

    @Test
    void delete_ShouldSucceed_WhenPositionExists() {
        // Arrange: Simula que o cargo a ser deletado existe.
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        // Configura o mock para não fazer nada (void) ao chamar deleteById.
        doNothing().when(positionRepository).deleteById(1L);

        // Act: Executa a deleção.
        positionService.delete(1L);

        // Assert: Verifica se o método deleteById foi chamado no repositório.
        verify(positionRepository).deleteById(1L);
    }
}
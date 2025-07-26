package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Sector;
import com.rh.vacationbackend.repository.SectorRepository;
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
class SectorServiceTest {

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private SectorService sectorService;

    private Sector sector;

    @BeforeEach
    void setUp() {
        sector = new Sector("Human Resources");
        sector.setId(1L);
    }

    @Test
    void create_ShouldReturnSavedSector() {
        // Arrange: Configura o mock para retornar o setor salvo.
        when(sectorRepository.save(any(Sector.class))).thenReturn(sector);

        // Act: Cria um novo setor.
        Sector result = sectorService.create("Human Resources");

        // Assert: Verifica se o resultado é o esperado.
        assertNotNull(result);
        assertEquals("Human Resources", result.getName());
        verify(sectorRepository).save(any(Sector.class));
    }

    @Test
    void findAll_ShouldReturnAllSectors() {
        // Arrange: Simula o retorno de uma lista com um setor.
        when(sectorRepository.findAll()).thenReturn(Collections.singletonList(sector));

        // Act: Busca todos os setores.
        List<Sector> result = sectorService.findAll();

        // Assert: Verifica o tamanho da lista retornada.
        assertEquals(1, result.size());
        verify(sectorRepository).findAll();
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Arrange: Simula que o setor não foi encontrado.
        when(sectorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Garante que uma exceção é lançada ao buscar por um ID inexistente.
        assertThrows(RuntimeException.class, () -> sectorService.findById(1L));
    }

    @Test
    void delete_ShouldSucceed_WhenSectorExists() {
        // Arrange: Simula a existência do setor para que ele possa ser deletado.
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        doNothing().when(sectorRepository).deleteById(1L);

        // Act: Executa a deleção.
        sectorService.delete(1L);

        // Assert: Verifica se o método de deleção do repositório foi chamado.
        verify(sectorRepository).deleteById(1L);
    }
}
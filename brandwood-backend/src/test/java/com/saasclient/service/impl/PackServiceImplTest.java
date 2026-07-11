package com.saasclient.service.impl;

import com.saasclient.dto.PackRequest;
import com.saasclient.entity.Pack;
import com.saasclient.exception.BusinessException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.PackMapper;
import com.saasclient.repository.PackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackServiceImplTest {

    @Mock private PackRepository packRepository;
    private PackServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PackServiceImpl(packRepository, new PackMapper());
    }

    private PackRequest req() {
        return PackRequest.builder().nom("Pro").description("desc").prix(10.0)
            .nbProjetsMax(5).nbClientsMax(5).dureeMois(1).actif(true).build();
    }

    @Test
    void creer_ok() {
        when(packRepository.existsByNomIgnoreCase("Pro")).thenReturn(false);
        when(packRepository.save(any(Pack.class))).thenAnswer(i -> {
            Pack p = i.getArgument(0); p.setId(1L); return p;
        });

        var res = service.creer(req());

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getNom()).isEqualTo("Pro");
        verify(packRepository).save(any(Pack.class));
    }

    @Test
    void creer_nomDuplique_leveException() {
        when(packRepository.existsByNomIgnoreCase("Pro")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(req()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("nom");
        verify(packRepository, never()).save(any());
    }

    @Test
    void obtenir_introuvable_leveException() {
        when(packRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenir(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}

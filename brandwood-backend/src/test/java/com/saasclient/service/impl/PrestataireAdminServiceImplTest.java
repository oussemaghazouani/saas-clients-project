package com.saasclient.service.impl;

import com.saasclient.dto.ActionStatut;
import com.saasclient.entity.*;
import com.saasclient.mapper.PrestataireMapper;
import com.saasclient.repository.AbonnementRepository;
import com.saasclient.repository.ExpertRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestataireAdminServiceImplTest {

    @Mock private ExpertRepository expertRepository;
    @Mock private StartupRepository startupRepository;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private UserRepository userRepository;
    private PrestataireAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PrestataireAdminServiceImpl(expertRepository, startupRepository,
            abonnementRepository, userRepository, new PrestataireMapper());
    }

    private Expert expert() {
        User u = User.builder().id(1L).firstName("Paul").lastName("Presta")
            .email("p@x.co").enabled(true).userType(UserType.PRESTATAIRE).build();
        return Expert.builder().id(10L).user(u).statutCompte(StatutCompte.ACTIF)
            .competences(new ArrayList<>()).build();
    }

    @Test
    void changerStatut_desactiver_metCompteDesactiveEtDesactiveLogin() {
        Expert e = expert();
        when(expertRepository.findById(10L)).thenReturn(Optional.of(e));
        when(expertRepository.save(any(Expert.class))).thenAnswer(i -> i.getArgument(0));
        when(startupRepository.countByPrestataireIdAndActifTrue(10L)).thenReturn(2L);

        var res = service.changerStatut(10L, ActionStatut.DESACTIVER);

        assertThat(res.getStatutCompte()).isEqualTo(StatutCompte.DESACTIVE);
        assertThat(e.getUser().isEnabled()).isFalse();
        assertThat(res.getNbClients()).isEqualTo(2L);
        verify(userRepository).save(e.getUser());
    }

    @Test
    void supprimer_detacheClients_supprimeAbonnementsExpertEtUser() {
        Expert e = expert();
        Startup s = Startup.builder().id(7L).prestataire(e).actif(true).build();
        when(expertRepository.findById(10L)).thenReturn(Optional.of(e));
        when(startupRepository.findByPrestataireId(10L)).thenReturn(List.of(s));

        service.supprimer(10L);

        assertThat(s.getPrestataire()).isNull();
        verify(startupRepository).saveAll(anyList());
        verify(abonnementRepository).deleteByExpertId(10L);
        verify(expertRepository).delete(e);
        verify(userRepository).deleteById(1L);
    }
}

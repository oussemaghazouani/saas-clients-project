package com.saasclient.service.impl;

import com.saasclient.dto.ProjetRequest;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.mapper.JalonMapper;
import com.saasclient.mapper.MembreMapper;
import com.saasclient.mapper.ProjetMapper;
import com.saasclient.repository.JalonRepository;
import com.saasclient.repository.MembreRepository;
import com.saasclient.repository.ProjetRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetServiceImplTest {

    @Mock private ProjetRepository projetRepository;
    @Mock private JalonRepository jalonRepository;
    @Mock private StartupRepository startupRepository;
    @Mock private UserRepository userRepository;
    @Mock private ExpertProfileService expertProfileService;
    @Mock private MembreRepository membreRepository;
    @Mock private com.saasclient.service.NotificationService notificationService;
    private ProjetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjetServiceImpl(projetRepository, jalonRepository, startupRepository,
            userRepository, expertProfileService, new ProjetMapper(), new JalonMapper(),
            membreRepository, new MembreMapper(), notificationService);
    }

    private User prestaUser() {
        return User.builder().id(1L).firstName("Max").lastName("Dev").userType(UserType.PRESTATAIRE).build();
    }

    private Expert expert(Long id, User u) {
        return Expert.builder().id(id).user(u).statutCompte(StatutCompte.ACTIF)
            .competences(new ArrayList<>()).build();
    }

    @Test
    void creer_ok_clientDuPrestataire() {
        Expert e = expert(10L, prestaUser());
        User clientUser = User.builder().id(2L).firstName("Lea").lastName("Cli").userType(UserType.CLIENT).build();
        Startup s = Startup.builder().id(5L).user(clientUser).prestataire(e).identifiantUnique("cli@x").build();
        when(expertProfileService.getOrCreate(1L)).thenReturn(e);
        when(startupRepository.findById(5L)).thenReturn(Optional.of(s));
        when(projetRepository.save(any(Projet.class))).thenAnswer(i -> { Projet p = i.getArgument(0); p.setId(7L); return p; });
        when(jalonRepository.countByProjetId(7L)).thenReturn(0L);
        when(jalonRepository.countByProjetIdAndStatut(7L, StatutJalon.ATTEINT)).thenReturn(0L);

        var res = service.creer(1L, ProjetRequest.builder().nom("Site").startupId(5L)
            .statut(StatutProjet.EN_COURS).build());

        assertThat(res.getId()).isEqualTo(7L);
        assertThat(res.getClientNom()).isEqualTo("Lea Cli");
        assertThat(res.getExpertId()).isEqualTo(10L);
        assertThat(res.getProgression()).isEqualTo(0.0);
    }

    @Test
    void creer_clientNonPossede_leveForbidden() {
        Expert e = expert(10L, prestaUser());
        Expert autre = expert(99L, User.builder().id(50L).build());
        Startup s = Startup.builder().id(5L).user(User.builder().id(2L).userType(UserType.CLIENT).build())
            .prestataire(autre).identifiantUnique("cli@x").build();
        when(expertProfileService.getOrCreate(1L)).thenReturn(e);
        when(startupRepository.findById(5L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> service.creer(1L, ProjetRequest.builder().nom("Site").startupId(5L).build()))
            .isInstanceOf(AccessForbiddenException.class);
        verify(projetRepository, never()).save(any());
    }

    @Test
    void changerStatutJalon_recalculeProgression() {
        Expert e = expert(10L, prestaUser());
        Projet projet = Projet.builder().id(7L).nom("P").expert(e)
            .startup(Startup.builder().id(5L).build())
            .technologies(new ArrayList<>()).progression(0.0).statut(StatutProjet.EN_COURS).build();
        Jalon jalon = Jalon.builder().id(1L).nom("Cadrage").statut(StatutJalon.A_VENIR).projet(projet).build();
        when(jalonRepository.findById(1L)).thenReturn(Optional.of(jalon));
        when(expertProfileService.getByUserId(1L)).thenReturn(e);
        when(jalonRepository.save(any(Jalon.class))).thenAnswer(i -> i.getArgument(0));
        when(jalonRepository.countByProjetId(7L)).thenReturn(2L);
        when(jalonRepository.countByProjetIdAndStatut(7L, StatutJalon.ATTEINT)).thenReturn(1L);

        var res = service.changerStatutJalon(1L, 1L, StatutJalon.ATTEINT);

        assertThat(res.getStatut()).isEqualTo("ATTEINT");
        assertThat(projet.getProgression()).isEqualTo(50.0);
        verify(projetRepository).save(projet);
    }

    @Test
    void obtenir_clientNonProprietaire_leveForbidden() {
        Expert e = expert(10L, User.builder().id(1L).build());
        Projet projet = Projet.builder().id(7L).expert(e)
            .startup(Startup.builder().id(5L).user(User.builder().id(99L).build()).build())
            .technologies(new ArrayList<>()).build();
        when(projetRepository.findById(7L)).thenReturn(Optional.of(projet));
        when(userRepository.findById(2L)).thenReturn(Optional.of(
            User.builder().id(2L).userType(UserType.CLIENT).build()));
        when(startupRepository.findByUserId(2L)).thenReturn(Optional.of(Startup.builder().id(8L).build()));

        assertThatThrownBy(() -> service.obtenir(2L, 7L))
            .isInstanceOf(AccessForbiddenException.class);
    }
}

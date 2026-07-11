package com.saasclient.service.impl;

import com.saasclient.dto.AnalyseRisqueResponse;
import com.saasclient.dto.GenerationTachesResponse;
import com.saasclient.entity.*;
import com.saasclient.mapper.TacheMapper;
import com.saasclient.repository.*;
import com.saasclient.service.ExpertProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TacheServiceImplTest {

    @Mock private TacheRepository tacheRepository;
    @Mock private ProjetRepository projetRepository;
    @Mock private MembreRepository membreRepository;
    @Mock private JalonRepository jalonRepository;
    @Mock private UserRepository userRepository;
    @Mock private StartupRepository startupRepository;
    @Mock private ExpertProfileService expertProfileService;
    private TacheServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TacheServiceImpl(tacheRepository, projetRepository, membreRepository,
            jalonRepository, userRepository, startupRepository, expertProfileService, new TacheMapper());
    }

    private User prestaUser() {
        return User.builder().id(1L).firstName("Tom").lastName("Lead").userType(UserType.PRESTATAIRE).build();
    }

    private Expert expert() {
        return Expert.builder().id(10L).user(prestaUser()).statutCompte(StatutCompte.ACTIF)
            .competences(new ArrayList<>()).build();
    }

    private Projet projet(Expert e, List<String> techs, Double progression, LocalDate dateFin) {
        return Projet.builder().id(5L).nom("App").expert(e)
            .startup(Startup.builder().id(8L).user(User.builder().id(2L).build()).build())
            .technologies(techs).progression(progression).dateFin(dateFin)
            .statut(StatutProjet.EN_COURS).build();
    }

    @Test
    void genererTaches_creeDecoupageSelonTechnologies() {
        Expert e = expert();
        when(projetRepository.findById(5L)).thenReturn(Optional.of(projet(e, List.of("Angular", "Spring"), 0.0, null)));
        when(expertProfileService.getByUserId(1L)).thenReturn(e);
        when(tacheRepository.countByProjetId(5L)).thenReturn(0L);
        when(tacheRepository.save(any(Tache.class))).thenAnswer(i -> { Tache t = i.getArgument(0); t.setId(1L); return t; });

        GenerationTachesResponse res = service.genererTaches(1L, 5L);

        // Cadrage + Conception + Front (Angular) + Back (Spring) + Tests + Déploiement = 6
        assertThat(res.getNbCreees()).isEqualTo(6);
        assertThat(res.getTaches()).extracting("titre")
            .anyMatch(t -> t.toString().contains("front-end"));
    }

    @Test
    void analyserRisque_tachesEnRetard_niveauEleve() {
        Expert e = expert();
        when(projetRepository.findById(5L)).thenReturn(Optional.of(projet(e, List.of(), 10.0, LocalDate.now().plusDays(5))));
        when(userRepository.findById(1L)).thenReturn(Optional.of(prestaUser()));
        when(expertProfileService.getByUserId(1L)).thenReturn(e);
        when(tacheRepository.countByProjetId(5L)).thenReturn(3L);
        LocalDate hier = LocalDate.now().minusDays(2);
        List<Tache> taches = List.of(
            Tache.builder().statut(StatutTache.A_FAIRE).dateEcheance(hier).build(),
            Tache.builder().statut(StatutTache.A_FAIRE).dateEcheance(hier).build(),
            Tache.builder().statut(StatutTache.EN_COURS).dateEcheance(hier).build());
        when(tacheRepository.findByProjetIdOrderByPositionAsc(5L)).thenReturn(taches);
        when(jalonRepository.findByProjetIdOrderByDatePrevueAsc(5L)).thenReturn(List.of());

        AnalyseRisqueResponse res = service.analyserRisque(1L, 5L);

        assertThat(res.getNiveau()).isEqualTo("ELEVE");
        assertThat(res.getScore()).isGreaterThanOrEqualTo(60);
        assertThat(res.getFacteurs()).anyMatch(f -> f.contains("retard"));
    }
}

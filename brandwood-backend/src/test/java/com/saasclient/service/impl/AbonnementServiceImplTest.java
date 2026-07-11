package com.saasclient.service.impl;

import com.saasclient.dto.SouscriptionRequest;
import com.saasclient.entity.*;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.mapper.AbonnementMapper;
import com.saasclient.repository.AbonnementRepository;
import com.saasclient.repository.ExpertRepository;
import com.saasclient.repository.PackRepository;
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
class AbonnementServiceImplTest {

    @Mock private ExpertProfileService expertProfileService;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private PackRepository packRepository;
    @Mock private ExpertRepository expertRepository;
    @Mock private com.saasclient.service.NotificationService notificationService;
    private AbonnementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AbonnementServiceImpl(expertProfileService, abonnementRepository,
            packRepository, expertRepository, new AbonnementMapper(), notificationService);
    }

    private Expert expert() {
        User u = User.builder().id(1L).firstName("Paul").lastName("Presta")
            .userType(UserType.PRESTATAIRE).build();
        return Expert.builder().id(10L).user(u).statutCompte(StatutCompte.ACTIF)
            .competences(new ArrayList<>()).build();
    }

    private Pack pack() {
        return Pack.builder().id(2L).nom("Business").prix(99.0)
            .nbProjetsMax(15).nbClientsMax(25).dureeMois(1).actif(true).build();
    }

    @Test
    void souscrire_virement_creeEnAttente() {
        when(expertProfileService.getOrCreate(1L)).thenReturn(expert());
        when(packRepository.findById(2L)).thenReturn(Optional.of(pack()));
        when(abonnementRepository.save(any(Abonnement.class)))
            .thenAnswer(i -> { Abonnement a = i.getArgument(0); a.setId(3L); return a; });

        var res = service.souscrire(1L,
            SouscriptionRequest.builder().packId(2L).methodePaiement(MethodePaiement.VIREMENT).build());

        assertThat(res.getStatut()).isEqualTo("EN_ATTENTE");
        assertThat(res.getMethodePaiement()).isEqualTo("VIREMENT");
    }

    @Test
    void souscrire_carteBancaire_refusee() {
        when(expertProfileService.getOrCreate(1L)).thenReturn(expert());
        when(packRepository.findById(2L)).thenReturn(Optional.of(pack()));

        assertThatThrownBy(() -> service.souscrire(1L,
            SouscriptionRequest.builder().packId(2L).methodePaiement(MethodePaiement.CARTE_BANCAIRE).build()))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("carte");
        verify(abonnementRepository, never()).save(any());
    }

    @Test
    void validerVirement_activeAbonnementEtCompte() {
        Expert e = expert();
        Pack p = pack();
        Abonnement a = Abonnement.builder().id(3L).expert(e).pack(p)
            .statut(StatutAbonnement.EN_ATTENTE).methodePaiement(MethodePaiement.VIREMENT).build();
        when(abonnementRepository.findById(3L)).thenReturn(Optional.of(a));
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(i -> i.getArgument(0));

        var res = service.validerVirement(3L);

        assertThat(res.getStatut()).isEqualTo("ACTIF");
        assertThat(e.getPack()).isEqualTo(p);
        assertThat(e.getStatutCompte()).isEqualTo(StatutCompte.ACTIF);
        verify(expertRepository).save(e);
    }

    @Test
    void validerVirement_dejaTraite_leveException() {
        Abonnement a = Abonnement.builder().id(3L).statut(StatutAbonnement.ACTIF).build();
        when(abonnementRepository.findById(3L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.validerVirement(3L))
            .isInstanceOf(BusinessRuleException.class);
    }
}

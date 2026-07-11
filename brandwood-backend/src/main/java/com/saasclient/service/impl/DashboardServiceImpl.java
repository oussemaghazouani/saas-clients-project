package com.saasclient.service.impl;

import com.saasclient.dto.DashboardResponse;
import com.saasclient.dto.KpiDto;
import com.saasclient.dto.ProjetResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.repository.*;
import com.saasclient.service.DashboardService;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.ProjetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final PackRepository packRepository;
    private final StartupRepository startupRepository;
    private final AbonnementRepository abonnementRepository;
    private final ProjetRepository projetRepository;
    private final ExpertProfileService expertProfileService;
    private final ProjetService projetService;

    private static final String VIOLET = "#6C63FF";
    private static final String BLEU   = "#3B82F6";
    private static final String VERT   = "#10B981";
    private static final String AMBRE  = "#F59E0B";

    @Override
    @Transactional
    public DashboardResponse charger(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        return switch (user.getUserType()) {
            case SUPER_ADMIN -> admin(user);
            case PRESTATAIRE -> prestataire(user);
            case CLIENT -> client(user);
        };
    }

    private DashboardResponse admin(User user) {
        long prestataires = expertRepository.count();
        long actifs = expertRepository.countByStatutCompte(StatutCompte.ACTIF);
        long clients = startupRepository.count();
        long packs = packRepository.count();
        long enAttente = abonnementRepository.countByStatut(StatutAbonnement.EN_ATTENTE);
        double revenus = abonnementRepository.findByStatut(StatutAbonnement.ACTIF, Pageable.unpaged())
            .stream().filter(a -> a.getPack() != null).mapToDouble(a -> a.getPack().getPrix()).sum();

        List<KpiDto> kpis = List.of(
            kpi("Prestataires", String.valueOf(prestataires), actifs + " actifs", "bi-people", VIOLET),
            kpi("Clients", String.valueOf(clients), "comptes provisionnés", "bi-person-badge", BLEU),
            kpi("Packs", String.valueOf(packs), "offres commerciales", "bi-box-seam", VERT),
            kpi("Abonnements en attente", String.valueOf(enAttente), "à valider", "bi-hourglass-split", AMBRE),
            kpi("Revenus mensuels", String.format("%.0f €", revenus), "abonnements actifs", "bi-cash-coin", VERT)
        );
        return DashboardResponse.builder()
            .userType("SUPER_ADMIN").prenom(user.getFirstName()).kpis(kpis).projetsRecents(List.of()).build();
    }

    private DashboardResponse prestataire(User user) {
        Expert e = expertProfileService.getOrCreate(user.getId());
        long clients = startupRepository.countByPrestataireIdAndActifTrue(e.getId());
        List<Projet> projets = projetRepository.findByExpertId(e.getId(), Pageable.unpaged()).getContent();
        long enCours = projets.stream().filter(p -> p.getStatut() == StatutProjet.EN_COURS).count();
        double progMoy = moyenneProgression(projets);

        List<KpiDto> kpis = List.of(
            kpi("Mes clients", String.valueOf(clients),
                e.getPack() != null ? "pack " + e.getPack().getNom() : "essai gratuit", "bi-people", VIOLET),
            kpi("Mes projets", String.valueOf(projets.size()), enCours + " en cours", "bi-kanban", BLEU),
            kpi("Progression moy.", String.format("%.0f%%", progMoy), "tous projets confondus", "bi-graph-up-arrow", VERT),
            kpi("Statut du compte", libelleStatut(e.getStatutCompte()), "votre accès",
                "bi-shield-check", e.getStatutCompte() == StatutCompte.ACTIF ? VERT : AMBRE)
        );
        return DashboardResponse.builder()
            .userType("PRESTATAIRE").prenom(user.getFirstName()).kpis(kpis)
            .projetsRecents(projetsRecents(user.getId())).build();
    }

    private DashboardResponse client(User user) {
        List<Projet> projets = projetRepository.findByStartup_User_Id(user.getId(), Pageable.unpaged()).getContent();
        long enCours = projets.stream().filter(p -> p.getStatut() == StatutProjet.EN_COURS).count();
        long termines = projets.stream().filter(p -> p.getStatut() == StatutProjet.TERMINE).count();
        double progMoy = moyenneProgression(projets);

        List<KpiDto> kpis = List.of(
            kpi("Mes projets", String.valueOf(projets.size()), enCours + " en cours", "bi-kanban", VIOLET),
            kpi("Projets terminés", String.valueOf(termines), "livrés", "bi-check2-circle", VERT),
            kpi("Progression moy.", String.format("%.0f%%", progMoy), "avancement global", "bi-graph-up-arrow", BLEU)
        );
        return DashboardResponse.builder()
            .userType("CLIENT").prenom(user.getFirstName()).kpis(kpis)
            .projetsRecents(projetsRecents(user.getId())).build();
    }

    private List<ProjetResponse> projetsRecents(Long userId) {
        return projetService.lister(userId,
            PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    private double moyenneProgression(List<Projet> projets) {
        return projets.isEmpty() ? 0.0
            : projets.stream().mapToDouble(p -> p.getProgression() != null ? p.getProgression() : 0.0).average().orElse(0.0);
    }

    private String libelleStatut(StatutCompte s) {
        return switch (s) {
            case ACTIF -> "Actif";
            case DESACTIVE -> "Désactivé";
            case SUSPENDU -> "Suspendu";
            case SUPPRIME -> "Supprimé";
        };
    }

    private KpiDto kpi(String label, String valeur, String sousLabel, String icone, String couleur) {
        return KpiDto.builder().label(label).valeur(valeur).sousLabel(sousLabel).icone(icone).couleur(couleur).build();
    }
}

package com.saasclient.service.impl;

import com.saasclient.dto.RapportProjetResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.MembreMapper;
import com.saasclient.repository.*;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {

    private final ProjetRepository projetRepository;
    private final JalonRepository jalonRepository;
    private final TacheRepository tacheRepository;
    private final MembreRepository membreRepository;
    private final ReunionRepository reunionRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final ExpertProfileService expertProfileService;
    private final MembreMapper membreMapper;

    @Override
    @Transactional(readOnly = true)
    public RapportProjetResponse genererRapportProjet(Long userId, Long projetId) {
        Projet p = projetRepository.findById(projetId)
            .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable."));
        assertReadAccess(userId, p);

        LocalDate today = LocalDate.now();

        long jalonsTotal = jalonRepository.countByProjetId(projetId);
        long jalonsAtteints = jalonRepository.countByProjetIdAndStatut(projetId, StatutJalon.ATTEINT);

        List<Tache> taches = tacheRepository.findByProjetIdOrderByPositionAsc(projetId);
        long tachesTerminees = taches.stream().filter(t -> t.getStatut() == StatutTache.TERMINE).count();
        long tachesEnCours = taches.stream().filter(t -> t.getStatut() == StatutTache.EN_COURS).count();
        long tachesEnRetard = taches.stream().filter(t -> t.getStatut() != StatutTache.TERMINE
            && t.getDateEcheance() != null && t.getDateEcheance().isBefore(today)).count();

        List<Membre> membres = membreRepository.findByProjetIdOrderByCreatedAtAsc(projetId);
        long nbReunions = reunionRepository.countByProjetId(projetId);

        return RapportProjetResponse.builder()
            .generatedAt(LocalDateTime.now())
            .projetId(p.getId())
            .nom(p.getNom())
            .description(p.getDescription())
            .statut(p.getStatut().name())
            .progression(p.getProgression())
            .budget(p.getBudget())
            .dateDebut(p.getDateDebut())
            .dateFin(p.getDateFin())
            .technologies(new ArrayList<>(p.getTechnologies()))
            .clientNom(p.getStartup().getUser().getFirstName() + " " + p.getStartup().getUser().getLastName())
            .prestataireNom(p.getExpert().getUser().getFirstName() + " " + p.getExpert().getUser().getLastName())
            .jalonsTotal(jalonsTotal)
            .jalonsAtteints(jalonsAtteints)
            .tachesTotal(taches.size())
            .tachesTerminees(tachesTerminees)
            .tachesEnCours(tachesEnCours)
            .tachesEnRetard(tachesEnRetard)
            .nbMembres(membres.size())
            .nbReunions(nbReunions)
            .membres(membres.stream().map(membreMapper::toResponse).collect(Collectors.toList()))
            .build();
    }

    private void assertReadAccess(Long userId, Projet projet) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        switch (user.getUserType()) {
            case PRESTATAIRE -> {
                Expert e = expertProfileService.getByUserId(userId);
                if (!projet.getExpert().getId().equals(e.getId())) {
                    throw new AccessForbiddenException("Ce projet ne vous est pas assigné.");
                }
            }
            case CLIENT -> {
                Startup s = startupRepository.findByUserId(userId)
                    .orElseThrow(() -> new AccessForbiddenException("Profil client introuvable."));
                if (!projet.getStartup().getId().equals(s.getId())) {
                    throw new AccessForbiddenException("Ce projet ne vous appartient pas.");
                }
            }
            default -> throw new AccessForbiddenException("Accès non autorisé à ce projet.");
        }
    }
}

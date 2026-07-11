package com.saasclient.service;

import com.saasclient.entity.Abonnement;
import com.saasclient.entity.Expert;
import com.saasclient.entity.StatutAbonnement;
import com.saasclient.entity.StatutCompte;
import com.saasclient.entity.User;
import com.saasclient.entity.UserType;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.repository.AbonnementRepository;
import com.saasclient.repository.ExpertRepository;
import com.saasclient.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Garantit l'existence d'un profil {@link Expert} pour un compte PRESTATAIRE.
 * Les prestataires sont créés par le module Auth (User) ; leur profil métier Expert
 * est provisionné à la demande, sans modifier le module Auth.
 */
@Service
@RequiredArgsConstructor
public class ExpertProfileService {

    private final ExpertRepository expertRepository;
    private final UserRepository userRepository;
    private final AbonnementRepository abonnementRepository;

    @Transactional
    public Expert getOrCreate(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        if (user.getUserType() != UserType.PRESTATAIRE) {
            throw new AccessForbiddenException("Seul un prestataire dispose d'un profil Expert.");
        }
        return expertRepository.findByUserId(userId)
            .orElseGet(() -> expertRepository.save(Expert.builder()
                .user(user)
                .disponibilite(true)
                .competences(new ArrayList<>())
                .statutCompte(StatutCompte.ACTIF)
                .build()));
    }

    @Transactional(readOnly = true)
    public Expert getByUserId(Long userId) {
        return expertRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profil prestataire introuvable."));
    }

    /**
     * Applique l'expiration des abonnements : tout abonnement ACTIF dont la date de fin est
     * dépassée passe en EXPIRE ; si c'était le pack courant de l'expert, ce dernier perd son
     * pack (retour aux limites de l'essai gratuit).
     */
    @Transactional
    public void appliquerExpirations(Expert expert) {
        LocalDate aujourdHui = LocalDate.now();
        List<Abonnement> actifs = abonnementRepository.findByExpertIdAndStatut(
            expert.getId(), StatutAbonnement.ACTIF);
        for (Abonnement a : actifs) {
            if (a.getDateFin() != null && a.getDateFin().isBefore(aujourdHui)) {
                a.setStatut(StatutAbonnement.EXPIRE);
                abonnementRepository.save(a);
                if (expert.getPack() != null && a.getPack() != null
                        && a.getPack().getId().equals(expert.getPack().getId())) {
                    expert.setPack(null);
                    expertRepository.save(expert);
                }
            }
        }
    }
}

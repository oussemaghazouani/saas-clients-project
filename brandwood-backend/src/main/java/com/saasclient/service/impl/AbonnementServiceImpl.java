package com.saasclient.service.impl;

import com.saasclient.dto.AbonnementResponse;
import com.saasclient.dto.SouscriptionRequest;
import com.saasclient.entity.*;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.AbonnementMapper;
import com.saasclient.repository.AbonnementRepository;
import com.saasclient.repository.ExpertRepository;
import com.saasclient.repository.PackRepository;
import com.saasclient.service.AbonnementService;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AbonnementServiceImpl implements AbonnementService {

    private final ExpertProfileService expertProfileService;
    private final AbonnementRepository abonnementRepository;
    private final PackRepository packRepository;
    private final ExpertRepository expertRepository;
    private final AbonnementMapper abonnementMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AbonnementResponse souscrire(Long prestataireUserId, SouscriptionRequest req) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);
        Pack pack = packRepository.findById(req.getPackId())
            .orElseThrow(() -> new ResourceNotFoundException("Pack introuvable."));
        if (!pack.isActif()) {
            throw new BusinessRuleException("Ce pack n'est plus disponible.");
        }
        if (req.getMethodePaiement() == MethodePaiement.CARTE_BANCAIRE) {
            throw new BusinessRuleException(
                "Le paiement par carte bancaire sera bientôt disponible. Choisissez le virement.");
        }
        if (abonnementRepository.existsByExpertIdAndStatut(expert.getId(), StatutAbonnement.EN_ATTENTE)) {
            throw new BusinessRuleException(
                "Vous avez déjà une demande d'abonnement en attente de validation.");
        }

        LocalDate debut = LocalDate.now();
        Abonnement abonnement = Abonnement.builder()
            .expert(expert)
            .pack(pack)
            .dateDebut(debut)
            .dateFin(debut.plusMonths(pack.getDureeMois()))
            .statut(StatutAbonnement.EN_ATTENTE)
            .methodePaiement(MethodePaiement.VIREMENT)
            .essaiGratuit(false)
            .build();
        return abonnementMapper.toResponse(abonnementRepository.save(abonnement));
    }

    @Override
    @Transactional
    public List<AbonnementResponse> mesAbonnements(Long prestataireUserId) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);
        expertProfileService.appliquerExpirations(expert);
        return abonnementRepository.findByExpertIdOrderByCreatedAtDesc(expert.getId())
            .stream()
            .map(abonnementMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonnementResponse> listerParStatut(StatutAbonnement statut, Pageable pageable) {
        Page<Abonnement> page = (statut == null)
            ? abonnementRepository.findAll(pageable)
            : abonnementRepository.findByStatut(statut, pageable);
        return page.map(abonnementMapper::toResponse);
    }

    @Override
    @Transactional
    public AbonnementResponse validerVirement(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
            .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable."));
        if (abonnement.getStatut() != StatutAbonnement.EN_ATTENTE) {
            throw new BusinessRuleException("Cet abonnement a déjà été traité.");
        }

        Pack pack = abonnement.getPack();
        LocalDate debut = LocalDate.now();
        abonnement.setDateDebut(debut);
        abonnement.setDateFin(debut.plusMonths(pack.getDureeMois()));
        abonnement.setStatut(StatutAbonnement.ACTIF);

        Expert expert = abonnement.getExpert();
        expert.setPack(pack);
        expert.setStatutCompte(StatutCompte.ACTIF);
        expertRepository.save(expert);

        notificationService.notifier(expert.getUser().getId(),
            "Abonnement validé",
            "Votre pack " + pack.getNom() + " est désormais actif.", "/profil");

        return abonnementMapper.toResponse(abonnementRepository.save(abonnement));
    }
}

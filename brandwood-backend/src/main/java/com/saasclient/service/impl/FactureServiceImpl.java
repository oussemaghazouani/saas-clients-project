package com.saasclient.service.impl;

import com.saasclient.dto.FactureRequest;
import com.saasclient.dto.FactureResponse;
import com.saasclient.dto.LigneRequest;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.FactureMapper;
import com.saasclient.repository.FactureRepository;
import com.saasclient.repository.ProjetRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.FactureService;
import com.saasclient.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final StartupRepository startupRepository;
    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final ExpertProfileService expertProfileService;
    private final FactureMapper factureMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public List<FactureResponse> lister(Long userId) {
        User user = getUser(userId);
        List<Facture> factures = switch (user.getUserType()) {
            case PRESTATAIRE -> factureRepository.findByExpertIdOrderByDateEmissionDesc(
                expertProfileService.getOrCreate(userId).getId());
            case CLIENT -> factureRepository.findByStartup_User_IdOrderByDateEmissionDesc(userId);
            default -> throw new AccessForbiddenException("La facturation n'est pas accessible à ce rôle.");
        };
        return factures.stream().map(factureMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FactureResponse obtenir(Long userId, Long id) {
        Facture facture = getFacture(id);
        assertReadAccess(userId, facture);
        return factureMapper.toResponse(facture);
    }

    @Override
    @Transactional
    public FactureResponse creer(Long prestataireUserId, FactureRequest req) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);

        Startup startup = startupRepository.findById(req.getStartupId())
            .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));
        if (startup.getPrestataire() == null || !startup.getPrestataire().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce client ne fait pas partie de votre portefeuille.");
        }

        Projet projet = null;
        if (req.getProjetId() != null) {
            projet = projetRepository.findById(req.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable."));
            if (!projet.getExpert().getId().equals(expert.getId())) {
                throw new AccessForbiddenException("Ce projet ne vous est pas assigné.");
            }
        }

        double tauxTva = req.getTauxTva() != null ? req.getTauxTva() : 0.0;
        LocalDate emission = LocalDate.now();

        Facture facture = Facture.builder()
            .numero(genererNumero())
            .startup(startup)
            .expert(expert)
            .projet(projet)
            .statut(StatutFacture.BROUILLON)
            .tauxTva(tauxTva)
            .dateEmission(emission)
            .dateEcheance(req.getDateEcheance() != null ? req.getDateEcheance() : emission.plusDays(30))
            .lignes(new ArrayList<>())
            .build();

        double totalHt = 0.0;
        for (LigneRequest lr : req.getLignes()) {
            double montant = arrondi(lr.getQuantite() * lr.getPrixUnitaire());
            totalHt += montant;
            facture.getLignes().add(LigneFacture.builder()
                .facture(facture)
                .description(lr.getDescription())
                .quantite(lr.getQuantite())
                .prixUnitaire(lr.getPrixUnitaire())
                .montant(montant)
                .build());
        }
        totalHt = arrondi(totalHt);
        facture.setMontantHt(totalHt);
        facture.setMontantTtc(arrondi(totalHt * (1 + tauxTva / 100.0)));

        return factureMapper.toResponse(factureRepository.save(facture));
    }

    @Override
    @Transactional
    public FactureResponse changerStatut(Long prestataireUserId, Long id, StatutFacture statut) {
        Facture facture = getFacture(id);
        assertPrestataireOwns(prestataireUserId, facture);
        facture.setStatut(statut);
        facture = factureRepository.save(facture);

        if (statut == StatutFacture.ENVOYEE) {
            notificationService.notifier(facture.getStartup().getUser().getId(),
                "Nouvelle facture",
                "Facture " + facture.getNumero() + " — " + facture.getMontantTtc() + " € TTC.",
                "/factures");
        }
        return factureMapper.toResponse(facture);
    }

    @Override
    @Transactional
    public void supprimer(Long prestataireUserId, Long id) {
        Facture facture = getFacture(id);
        assertPrestataireOwns(prestataireUserId, facture);
        factureRepository.delete(facture);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private String genererNumero() {
        String base = "FAC-" + Year.now().getValue() + "-";
        long n = factureRepository.count() + 1;
        String numero;
        do {
            numero = base + String.format("%04d", n);
            n++;
        } while (factureRepository.existsByNumero(numero));
        return numero;
    }

    private double arrondi(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private Facture getFacture(Long id) {
        return factureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable."));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private void assertPrestataireOwns(Long prestataireUserId, Facture facture) {
        Expert expert = expertProfileService.getByUserId(prestataireUserId);
        if (!facture.getExpert().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Cette facture ne vous appartient pas.");
        }
    }

    private void assertReadAccess(Long userId, Facture facture) {
        User user = getUser(userId);
        switch (user.getUserType()) {
            case PRESTATAIRE -> {
                Expert expert = expertProfileService.getByUserId(userId);
                if (!facture.getExpert().getId().equals(expert.getId())) {
                    throw new AccessForbiddenException("Cette facture ne vous appartient pas.");
                }
            }
            case CLIENT -> {
                Startup startup = startupRepository.findByUserId(userId)
                    .orElseThrow(() -> new AccessForbiddenException("Profil client introuvable."));
                if (!facture.getStartup().getId().equals(startup.getId())) {
                    throw new AccessForbiddenException("Cette facture ne vous est pas destinée.");
                }
            }
            default -> throw new AccessForbiddenException("Accès non autorisé à cette facture.");
        }
    }
}

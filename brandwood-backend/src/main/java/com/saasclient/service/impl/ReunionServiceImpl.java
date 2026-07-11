package com.saasclient.service.impl;

import com.saasclient.dto.ReunionRequest;
import com.saasclient.dto.ReunionResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.ReunionMapper;
import com.saasclient.repository.ProjetRepository;
import com.saasclient.repository.ReunionRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.ReunionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReunionServiceImpl implements ReunionService {

    private final ReunionRepository reunionRepository;
    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final ExpertProfileService expertProfileService;
    private final ReunionMapper reunionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReunionResponse> mesReunions(Long userId) {
        User user = getUser(userId);
        List<Reunion> list = switch (user.getUserType()) {
            case PRESTATAIRE -> reunionRepository.findByProjet_ExpertIdOrderByDateHeureAsc(
                expertProfileService.getOrCreate(userId).getId());
            case CLIENT -> reunionRepository.findByProjet_Startup_User_IdOrderByDateHeureAsc(userId);
            default -> throw new AccessForbiddenException("Les réunions ne sont pas accessibles à ce rôle.");
        };
        return list.stream().map(reunionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReunionResponse> listerParProjet(Long userId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertReadAccess(userId, projet);
        return reunionRepository.findByProjetIdOrderByDateHeureAsc(projetId)
            .stream().map(reunionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReunionResponse creer(Long prestataireUserId, Long projetId, ReunionRequest req) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        Reunion r = Reunion.builder()
            .titre(req.getTitre())
            .description(req.getDescription())
            .dateHeure(req.getDateHeure())
            .dureeMinutes(req.getDureeMinutes() != null ? req.getDureeMinutes() : 60)
            .lien(req.getLien())
            .statut(req.getStatut() != null ? req.getStatut() : StatutReunion.PLANIFIEE)
            .projet(projet)
            .build();
        return reunionMapper.toResponse(reunionRepository.save(r));
    }

    @Override
    @Transactional
    public ReunionResponse modifier(Long prestataireUserId, Long reunionId, ReunionRequest req) {
        Reunion r = getReunion(reunionId);
        assertPrestataireOwns(prestataireUserId, r.getProjet());
        r.setTitre(req.getTitre());
        r.setDescription(req.getDescription());
        r.setDateHeure(req.getDateHeure());
        if (req.getDureeMinutes() != null) r.setDureeMinutes(req.getDureeMinutes());
        r.setLien(req.getLien());
        if (req.getStatut() != null) r.setStatut(req.getStatut());
        return reunionMapper.toResponse(reunionRepository.save(r));
    }

    @Override
    @Transactional
    public void supprimer(Long prestataireUserId, Long reunionId) {
        Reunion r = getReunion(reunionId);
        assertPrestataireOwns(prestataireUserId, r.getProjet());
        reunionRepository.delete(r);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────

    private Reunion getReunion(Long id) {
        return reunionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Réunion introuvable."));
    }

    private Projet getProjet(Long id) {
        return projetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable."));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private Projet getProjetOwnedBy(Long prestataireUserId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertPrestataireOwns(prestataireUserId, projet);
        return projet;
    }

    private void assertPrestataireOwns(Long prestataireUserId, Projet projet) {
        Expert expert = expertProfileService.getByUserId(prestataireUserId);
        if (!projet.getExpert().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce projet ne vous est pas assigné.");
        }
    }

    private void assertReadAccess(Long userId, Projet projet) {
        User user = getUser(userId);
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

package com.saasclient.service.impl;

import com.saasclient.dto.JalonRequest;
import com.saasclient.dto.JalonResponse;
import com.saasclient.dto.MembreRequest;
import com.saasclient.dto.MembreResponse;
import com.saasclient.dto.ProjetRequest;
import com.saasclient.dto.ProjetResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.JalonMapper;
import com.saasclient.mapper.MembreMapper;
import com.saasclient.mapper.ProjetMapper;
import com.saasclient.repository.JalonRepository;
import com.saasclient.repository.MembreRepository;
import com.saasclient.repository.ProjetRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.NotificationService;
import com.saasclient.service.ProjetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetServiceImpl implements ProjetService {

    private final ProjetRepository projetRepository;
    private final JalonRepository jalonRepository;
    private final StartupRepository startupRepository;
    private final UserRepository userRepository;
    private final ExpertProfileService expertProfileService;
    private final ProjetMapper projetMapper;
    private final JalonMapper jalonMapper;
    private final MembreRepository membreRepository;
    private final MembreMapper membreMapper;
    private final NotificationService notificationService;

    // ── Projets ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProjetResponse> lister(Long userId, Pageable pageable) {
        User user = getUser(userId);
        Page<Projet> page = switch (user.getUserType()) {
            case PRESTATAIRE -> projetRepository.findByExpertId(
                expertProfileService.getOrCreate(userId).getId(), pageable);
            case CLIENT -> projetRepository.findByStartup_User_Id(userId, pageable);
            default -> throw new AccessForbiddenException("Les projets ne sont pas accessibles à ce rôle.");
        };
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjetResponse obtenir(Long userId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertReadAccess(userId, projet);
        return toResponse(projet);
    }

    @Override
    @Transactional
    public ProjetResponse creer(Long prestataireUserId, ProjetRequest req) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);
        if (req.getStartupId() == null) {
            throw new BusinessRuleException("Le client (startupId) est obligatoire pour créer un projet.");
        }
        Startup startup = startupRepository.findById(req.getStartupId())
            .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));
        if (startup.getPrestataire() == null || !startup.getPrestataire().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce client ne fait pas partie de votre portefeuille.");
        }

        Projet projet = Projet.builder()
            .nom(req.getNom())
            .description(req.getDescription())
            .dateDebut(req.getDateDebut())
            .dateFin(req.getDateFin())
            .budget(req.getBudget())
            .statut(req.getStatut() != null ? req.getStatut() : StatutProjet.PLANIFIE)
            .progression(0.0)
            .technologies(req.getTechnologies() != null ? new ArrayList<>(req.getTechnologies()) : new ArrayList<>())
            .startup(startup)
            .expert(expert)
            .build();
        Projet saved = projetRepository.save(projet);
        notificationService.notifier(startup.getUser().getId(),
            "Nouveau projet", "Le projet « " + saved.getNom() + " » vous a été assigné.", "/projets");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjetResponse modifier(Long prestataireUserId, Long projetId, ProjetRequest req) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        projet.setNom(req.getNom());
        projet.setDescription(req.getDescription());
        projet.setDateDebut(req.getDateDebut());
        projet.setDateFin(req.getDateFin());
        projet.setBudget(req.getBudget());
        if (req.getStatut() != null) {
            projet.setStatut(req.getStatut());
        }
        if (req.getTechnologies() != null) {
            projet.getTechnologies().clear();
            projet.getTechnologies().addAll(req.getTechnologies());
        }
        return toResponse(projetRepository.save(projet));
    }

    @Override
    @Transactional
    public void supprimer(Long prestataireUserId, Long projetId) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        projetRepository.delete(projet);
    }

    // ── Jalons ──────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<JalonResponse> listerJalons(Long userId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertReadAccess(userId, projet);
        return jalonRepository.findByProjetIdOrderByDatePrevueAsc(projetId)
            .stream().map(jalonMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JalonResponse ajouterJalon(Long prestataireUserId, Long projetId, JalonRequest req) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        Jalon jalon = Jalon.builder()
            .nom(req.getNom())
            .datePrevue(req.getDatePrevue())
            .description(req.getDescription())
            .statut(req.getStatut() != null ? req.getStatut() : StatutJalon.A_VENIR)
            .projet(projet)
            .build();
        jalon = jalonRepository.save(jalon);
        recalculerProgression(projet);
        return jalonMapper.toResponse(jalon);
    }

    @Override
    @Transactional
    public JalonResponse modifierJalon(Long prestataireUserId, Long jalonId, JalonRequest req) {
        Jalon jalon = getJalon(jalonId);
        assertPrestataireOwns(prestataireUserId, jalon.getProjet());
        jalon.setNom(req.getNom());
        jalon.setDatePrevue(req.getDatePrevue());
        jalon.setDescription(req.getDescription());
        if (req.getStatut() != null) {
            jalon.setStatut(req.getStatut());
        }
        jalon = jalonRepository.save(jalon);
        recalculerProgression(jalon.getProjet());
        return jalonMapper.toResponse(jalon);
    }

    @Override
    @Transactional
    public JalonResponse changerStatutJalon(Long prestataireUserId, Long jalonId, StatutJalon statut) {
        Jalon jalon = getJalon(jalonId);
        assertPrestataireOwns(prestataireUserId, jalon.getProjet());
        jalon.setStatut(statut);
        jalon = jalonRepository.save(jalon);
        recalculerProgression(jalon.getProjet());
        return jalonMapper.toResponse(jalon);
    }

    @Override
    @Transactional
    public void supprimerJalon(Long prestataireUserId, Long jalonId) {
        Jalon jalon = getJalon(jalonId);
        Projet projet = jalon.getProjet();
        assertPrestataireOwns(prestataireUserId, projet);
        jalonRepository.delete(jalon);
        recalculerProgression(projet);
    }

    // ── Membres (équipe) ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MembreResponse> listerMembres(Long userId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertReadAccess(userId, projet);
        return membreRepository.findByProjetIdOrderByCreatedAtAsc(projetId)
            .stream().map(membreMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MembreResponse ajouterMembre(Long prestataireUserId, Long projetId, MembreRequest req) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        Membre membre = Membre.builder()
            .nom(req.getNom())
            .prenom(req.getPrenom())
            .email(req.getEmail())
            .roleProjet(req.getRoleProjet())
            .projet(projet)
            .build();
        return membreMapper.toResponse(membreRepository.save(membre));
    }

    @Override
    @Transactional
    public MembreResponse modifierMembre(Long prestataireUserId, Long membreId, MembreRequest req) {
        Membre membre = getMembre(membreId);
        assertPrestataireOwns(prestataireUserId, membre.getProjet());
        membre.setNom(req.getNom());
        membre.setPrenom(req.getPrenom());
        membre.setEmail(req.getEmail());
        membre.setRoleProjet(req.getRoleProjet());
        return membreMapper.toResponse(membreRepository.save(membre));
    }

    @Override
    @Transactional
    public void supprimerMembre(Long prestataireUserId, Long membreId) {
        Membre membre = getMembre(membreId);
        assertPrestataireOwns(prestataireUserId, membre.getProjet());
        membreRepository.delete(membre);
    }

    private Membre getMembre(Long id) {
        return membreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Membre introuvable."));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /** Progression = jalons ATTEINT / total * 100 (méthode appelée à chaque changement de jalon). */
    private void recalculerProgression(Projet projet) {
        long total = jalonRepository.countByProjetId(projet.getId());
        long atteints = jalonRepository.countByProjetIdAndStatut(projet.getId(), StatutJalon.ATTEINT);
        double progression = (total == 0) ? 0.0 : Math.round((100.0 * atteints) / total);
        projet.setProgression(progression);
        projetRepository.save(projet);
    }

    private ProjetResponse toResponse(Projet projet) {
        long total = jalonRepository.countByProjetId(projet.getId());
        long atteints = jalonRepository.countByProjetIdAndStatut(projet.getId(), StatutJalon.ATTEINT);
        ProjetResponse response = projetMapper.toResponse(projet, total, atteints);
        response.setNbMembres(membreRepository.countByProjetId(projet.getId()));
        return response;
    }

    private Projet getProjet(Long id) {
        return projetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable."));
    }

    private Jalon getJalon(Long id) {
        return jalonRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Jalon introuvable."));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    /** Vérifie que le projet appartient bien au prestataire authentifié. */
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

    /** Lecture autorisée : prestataire assigné OU client propriétaire. */
    private void assertReadAccess(Long userId, Projet projet) {
        User user = getUser(userId);
        switch (user.getUserType()) {
            case PRESTATAIRE -> {
                Expert expert = expertProfileService.getByUserId(userId);
                if (!projet.getExpert().getId().equals(expert.getId())) {
                    throw new AccessForbiddenException("Ce projet ne vous est pas assigné.");
                }
            }
            case CLIENT -> {
                Startup startup = startupRepository.findByUserId(userId)
                    .orElseThrow(() -> new AccessForbiddenException("Profil client introuvable."));
                if (!projet.getStartup().getId().equals(startup.getId())) {
                    throw new AccessForbiddenException("Ce projet ne vous appartient pas.");
                }
            }
            default -> throw new AccessForbiddenException("Accès non autorisé à ce projet.");
        }
    }
}

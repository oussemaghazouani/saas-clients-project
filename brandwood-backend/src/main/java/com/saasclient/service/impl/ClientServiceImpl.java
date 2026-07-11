package com.saasclient.service.impl;

import com.saasclient.dto.ClientResponse;
import com.saasclient.dto.ProvisionClientRequest;
import com.saasclient.dto.ProvisionClientResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.ClientMapper;
import com.saasclient.repository.RoleRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ClientService;
import com.saasclient.service.ExpertProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ExpertProfileService expertProfileService;
    private final StartupRepository startupRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientMapper clientMapper;

    private static final SecureRandom RANDOM = new SecureRandom();
    /** Essai gratuit : 1 client pendant 1 semaine tant qu'aucun pack n'est actif. */
    private static final int TRIAL_MAX_CLIENTS = 1;
    private static final int TRIAL_DUREE_JOURS = 7;

    @Override
    @Transactional
    public ProvisionClientResponse provisionner(Long prestataireUserId, ProvisionClientRequest req) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);
        if (expert.getStatutCompte() != StatutCompte.ACTIF) {
            throw new AccessForbiddenException(
                "Votre compte prestataire n'est pas actif. Contactez l'administrateur.");
        }

        // Cycle de vie abonnement : un abonnement expiré retire le pack (retour à l'essai).
        expertProfileService.appliquerExpirations(expert);

        int limite;
        if (expert.getPack() != null) {
            limite = expert.getPack().getNbClientsMax();
        } else {
            // Essai gratuit : 1 client ET 1 semaine.
            LocalDate aujourdHui = LocalDate.now();
            if (expert.getDateDebutEssai() == null) {
                expert.setDateDebutEssai(aujourdHui);
            } else if (aujourdHui.isAfter(expert.getDateDebutEssai().plusDays(TRIAL_DUREE_JOURS))) {
                throw new BusinessRuleException(
                    "Votre essai gratuit (1 semaine) est terminé. Souscrivez un pack pour continuer.",
                    HttpStatus.CONFLICT);
            }
            limite = TRIAL_MAX_CLIENTS;
        }

        long actuels = startupRepository.countByPrestataireIdAndActifTrue(expert.getId());
        if (actuels >= limite) {
            String motif = (expert.getPack() != null)
                ? "Limite de clients du pack atteinte (" + limite + "). Souscrivez un pack supérieur."
                : "Limite de l'essai gratuit atteinte (" + TRIAL_MAX_CLIENTS
                    + " client). Souscrivez un pack pour en ajouter davantage.";
            throw new BusinessRuleException(motif, HttpStatus.CONFLICT);
        }

        String identifiant = genererIdentifiantUnique();
        Role role = roleRepository.findByName("ROLE_CLIENT")
            .orElseThrow(() -> new BusinessRuleException("Rôle client introuvable.",
                HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(identifiant)
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .userType(UserType.CLIENT)
            .enabled(true)
            .build();
        user.getRoles().add(role);
        user = userRepository.save(user);

        Startup startup = Startup.builder()
            .user(user)
            .domaineActivite(req.getDomaineActivite())
            .siret(req.getSiret())
            .adresse(req.getAdresse())
            .nombreEmployes(req.getNombreEmployes())
            .identifiantUnique(identifiant)
            .prestataire(expert)
            .actif(true)
            .build();
        startup = startupRepository.save(startup);

        return ProvisionClientResponse.builder()
            .startupId(startup.getId())
            .userId(user.getId())
            .identifiantUnique(identifiant)
            .message("Client provisionné. Communiquez l'identifiant de connexion au client.")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> listerMesClients(Long prestataireUserId, Pageable pageable) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);
        return startupRepository.findByPrestataireId(expert.getId(), pageable)
            .map(clientMapper::toResponse);
    }

    @Override
    @Transactional
    public ClientResponse desactiver(Long prestataireUserId, Long startupId) {
        Startup startup = getOwnedStartup(prestataireUserId, startupId);
        startup.setActif(false);
        startup.getUser().setEnabled(false);
        userRepository.save(startup.getUser());
        return clientMapper.toResponse(startupRepository.save(startup));
    }

    @Override
    @Transactional
    public ClientResponse reactiver(Long prestataireUserId, Long startupId) {
        Startup startup = getOwnedStartup(prestataireUserId, startupId);
        startup.setActif(true);
        startup.getUser().setEnabled(true);
        userRepository.save(startup.getUser());
        return clientMapper.toResponse(startupRepository.save(startup));
    }

    @Override
    @Transactional
    public void supprimer(Long prestataireUserId, Long startupId) {
        Startup startup = getOwnedStartup(prestataireUserId, startupId);
        Long userId = startup.getUser().getId();
        startupRepository.delete(startup);
        userRepository.deleteById(userId);
    }

    private Startup getOwnedStartup(Long prestataireUserId, Long startupId) {
        Expert expert = expertProfileService.getByUserId(prestataireUserId);
        Startup startup = startupRepository.findById(startupId)
            .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));
        if (startup.getPrestataire() == null
                || !startup.getPrestataire().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce client ne fait pas partie de votre portefeuille.");
        }
        return startup;
    }

    private String genererIdentifiantUnique() {
        String identifiant;
        do {
            String suffix = Long.toString(Math.abs(RANDOM.nextLong()), 36);
            if (suffix.length() > 8) {
                suffix = suffix.substring(0, 8);
            }
            identifiant = "cli-" + suffix + "@saas-client.app";
        } while (startupRepository.existsByIdentifiantUnique(identifiant)
            || userRepository.existsByEmail(identifiant));
        return identifiant;
    }
}

package com.saasclient.service.impl;

import com.saasclient.dto.ContactDto;
import com.saasclient.dto.EnvoiMessageRequest;
import com.saasclient.dto.MessagePriveDto;
import com.saasclient.entity.Expert;
import com.saasclient.entity.MessagePrive;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.repository.MessagePriveRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.MessagerieService;
import com.saasclient.websocket.MessagerieWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessagerieServiceImpl implements MessagerieService {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final ExpertProfileService expertProfileService;
    private final MessagePriveRepository messageRepository;
    private final MessagerieWebSocketHandler wsHandler;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    @Transactional
    public List<ContactDto> contacts(Long userId) {
        User moi = getUser(userId);
        List<User> correspondants = correspondantsAutorises(moi);
        List<ContactDto> result = new ArrayList<>();
        for (User u : correspondants) {
            List<MessagePrive> fil = messageRepository.fil(userId, u.getId());
            MessagePrive dernier = fil.isEmpty() ? null : fil.get(fil.size() - 1);
            result.add(ContactDto.builder()
                .userId(u.getId())
                .nom(nom(u))
                .role(u.getUserType().name())
                .dernierMessage(dernier != null ? dernier.getContenu() : null)
                .dernierMessageDate(dernier != null ? dernier.getCreatedAt().format(ISO) : null)
                .nonLus(messageRepository.countByExpediteurIdAndDestinataireIdAndLuFalse(u.getId(), userId))
                .build());
        }
        // les conversations avec activite en premier
        result.sort((a, b) -> {
            String da = a.getDernierMessageDate(), db = b.getDernierMessageDate();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        return result;
    }

    @Override
    @Transactional
    public List<MessagePriveDto> fil(Long userId, Long autreUserId) {
        getUser(userId);
        List<MessagePrive> messages = messageRepository.fil(userId, autreUserId);
        messageRepository.marquerLus(userId, autreUserId);
        List<MessagePriveDto> dtos = new ArrayList<>();
        for (MessagePrive m : messages) { dtos.add(toDto(m)); }
        return dtos;
    }

    @Override
    @Transactional
    public MessagePriveDto envoyer(Long expediteurUserId, EnvoiMessageRequest req) {
        User expediteur = getUser(expediteurUserId);
        User destinataire = userRepository.findById(req.getDestinataireId())
            .orElseThrow(() -> new ResourceNotFoundException("Destinataire introuvable."));

        boolean autorise = correspondantsAutorises(expediteur).stream()
            .anyMatch(u -> u.getId().equals(destinataire.getId()));
        if (!autorise) {
            throw new AccessForbiddenException("Vous ne pouvez pas contacter cet utilisateur.");
        }

        MessagePrive message = messageRepository.save(MessagePrive.builder()
            .expediteur(expediteur)
            .destinataire(destinataire)
            .contenu(req.getContenu())
            .lu(false)
            .build());

        MessagePriveDto dto = toDto(message);
        // Push temps reel au destinataire (s'il est connecte).
        wsHandler.envoyerAUtilisateur(destinataire.getId(), dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public long nonLus(Long userId) {
        return messageRepository.countByDestinataireIdAndLuFalse(userId);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────

    /** Liste des utilisateurs que l'on a le droit de contacter selon son role. */
    private List<User> correspondantsAutorises(User moi) {
        List<User> liste = new ArrayList<>();
        switch (moi.getUserType()) {
            case PRESTATAIRE -> {
                Expert expert = expertProfileService.getOrCreate(moi.getId());
                for (Startup s : startupRepository.findByPrestataireId(expert.getId())) {
                    if (s.getUser() != null) { liste.add(s.getUser()); }
                }
            }
            case CLIENT -> startupRepository.findByUserId(moi.getId()).ifPresent(s -> {
                if (s.getPrestataire() != null && s.getPrestataire().getUser() != null) {
                    liste.add(s.getPrestataire().getUser());
                }
            });
            case SUPER_ADMIN -> {
                for (User u : userRepository.findAll()) {
                    if (!u.getId().equals(moi.getId())) { liste.add(u); }
                }
            }
        }
        // deduplique par id en conservant l'ordre
        Map<Long, User> uniques = new LinkedHashMap<>();
        for (User u : liste) { uniques.putIfAbsent(u.getId(), u); }
        return new ArrayList<>(uniques.values());
    }

    private MessagePriveDto toDto(MessagePrive m) {
        return MessagePriveDto.builder()
            .id(m.getId())
            .expediteurId(m.getExpediteur().getId())
            .expediteurNom(nom(m.getExpediteur()))
            .destinataireId(m.getDestinataire().getId())
            .contenu(m.getContenu())
            .lu(m.isLu())
            .dateEnvoi(m.getCreatedAt() != null ? m.getCreatedAt().format(ISO) : null)
            .build();
    }

    private String nom(User u) {
        String n = ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
            + (u.getLastName() == null ? "" : u.getLastName())).trim();
        return n.isEmpty() ? u.getEmail() : n;
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }
}

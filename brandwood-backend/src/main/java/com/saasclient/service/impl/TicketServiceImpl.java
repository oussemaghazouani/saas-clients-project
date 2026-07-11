package com.saasclient.service.impl;

import com.saasclient.dto.*;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.MessageTicketMapper;
import com.saasclient.mapper.TicketMapper;
import com.saasclient.repository.MessageTicketRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.TicketRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.AiClient;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.NotificationService;
import com.saasclient.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final MessageTicketRepository messageRepository;
    private final StartupRepository startupRepository;
    private final UserRepository userRepository;
    private final ExpertProfileService expertProfileService;
    private final TicketMapper ticketMapper;
    private final MessageTicketMapper messageMapper;
    private final NotificationService notificationService;
    private final AiClient aiClient;

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> lister(Long userId) {
        User user = getUser(userId);
        List<Ticket> tickets = switch (user.getUserType()) {
            case CLIENT -> ticketRepository.findByStartup_User_IdOrderByCreatedAtDesc(userId);
            case PRESTATAIRE -> ticketRepository.findByExpertIdOrderByCreatedAtDesc(
                expertProfileService.getByUserId(userId).getId());
            default -> throw new AccessForbiddenException("Le support n'est pas accessible à ce rôle.");
        };
        return tickets.stream()
            .map(t -> ticketMapper.toResponse(t, messageRepository.countByTicketId(t.getId()), null))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse obtenir(Long userId, Long ticketId) {
        Ticket ticket = getTicket(ticketId);
        assertAccess(userId, ticket);
        List<MessageTicket> messages = messageRepository.findByTicketIdOrderByDateEnvoiAsc(ticketId);
        return ticketMapper.toResponse(ticket, messages.size(), messages);
    }

    @Override
    @Transactional
    public TicketResponse creer(Long clientUserId, TicketRequest req) {
        Startup startup = startupRepository.findByUserId(clientUserId)
            .orElseThrow(() -> new AccessForbiddenException("Seul un client peut ouvrir un ticket."));
        if (startup.getPrestataire() == null) {
            throw new BusinessRuleException("Aucun prestataire ne vous est rattaché pour le moment.");
        }
        Ticket ticket = Ticket.builder()
            .sujet(req.getSujet())
            .description(req.getDescription())
            .priorite(req.getPriorite() != null ? req.getPriorite() : PrioriteTicket.MOYENNE)
            .statut(StatutTicket.OUVERT)
            .startup(startup)
            .expert(startup.getPrestataire())
            .build();
        ticket = ticketRepository.save(ticket);
        notificationService.notifier(ticket.getExpert().getUser().getId(),
            "Nouveau ticket de support",
            ticket.getStartup().getUser().getFirstName() + " : " + ticket.getSujet(), "/support");
        return ticketMapper.toResponse(ticket, 0, List.of());
    }

    @Override
    @Transactional
    public TicketResponse changerStatut(Long prestataireUserId, Long ticketId, StatutTicket statut) {
        Ticket ticket = getTicket(ticketId);
        Expert expert = expertProfileService.getByUserId(prestataireUserId);
        if (!ticket.getExpert().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce ticket ne vous est pas adressé.");
        }
        ticket.setStatut(statut);
        ticket = ticketRepository.save(ticket);
        notificationService.notifier(ticket.getStartup().getUser().getId(),
            "Ticket mis à jour", "« " + ticket.getSujet() + " » → " + statut.name(), "/support");
        return ticketMapper.toResponse(ticket, messageRepository.countByTicketId(ticketId), null);
    }

    @Override
    @Transactional
    public MessageResponse ajouterMessage(Long userId, Long ticketId, MessageRequest req) {
        Ticket ticket = getTicket(ticketId);
        User user = assertAccess(userId, ticket);

        MessageTicket message = MessageTicket.builder()
            .ticket(ticket)
            .auteurNom(user.getFirstName() + " " + user.getLastName())
            .auteurRole(user.getUserType().name())
            .contenu(req.getContenu())
            .build();
        message = messageRepository.save(message);

        // Quand le prestataire répond à un ticket encore OUVERT, il passe EN_COURS.
        if (user.getUserType() == UserType.PRESTATAIRE && ticket.getStatut() == StatutTicket.OUVERT) {
            ticket.setStatut(StatutTicket.EN_COURS);
            ticketRepository.save(ticket);
        }

        Long destinataire = (user.getUserType() == UserType.CLIENT)
            ? ticket.getExpert().getUser().getId()
            : ticket.getStartup().getUser().getId();
        notificationService.notifier(destinataire, "Nouveau message",
            message.getAuteurNom() + " a répondu à « " + ticket.getSujet() + " »", "/support");

        return messageMapper.toResponse(message);
    }

    // ── IA : analyse et réponse suggérée ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TicketIaResponse analyser(Long prestataireUserId, Long ticketId) {
        Ticket ticket = getTicket(ticketId);
        Expert expert = expertProfileService.getByUserId(prestataireUserId);
        if (!ticket.getExpert().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce ticket ne vous est pas adressé.");
        }

        StringBuilder fil = new StringBuilder(ticket.getSujet()).append(". ")
            .append(ticket.getDescription() == null ? "" : ticket.getDescription());
        messageRepository.findByTicketIdOrderByDateEnvoiAsc(ticketId)
            .forEach(msg -> fil.append(" ").append(msg.getContenu()));
        String texte = fil.toString();
        String prenomClient = ticket.getStartup().getUser().getFirstName();

        String categorie = categoriser(texte);
        String priorite = prioriser(texte);
        String sentiment = analyserSentiment(texte);
        String resume = resumer(ticket.getSujet(), texte);

        // 1) Réponse suggérée par IA générative (si clé configurée).
        String reponse = null;
        String source = "heuristique";
        String systeme = "Tu es un agent de support client francophone, courtois et professionnel. "
            + "Rédige une réponse claire (3-5 phrases) au client, proposant une solution ou une prochaine étape. "
            + "Ne mentionne pas que tu es une IA. Signe « L'équipe support ».";
        String prompt = "Sujet : " + ticket.getSujet() + "\nCatégorie : " + categorie
            + "\nMessage du client : " + texte + "\nPrénom du client : " + prenomClient;
        var ia = aiClient.generer(systeme, prompt, 400);
        if (ia.isPresent()) {
            reponse = ia.get();
            source = "ia";
        } else {
            reponse = reponseTemplate(prenomClient, categorie);
        }

        return TicketIaResponse.builder()
            .prioriteSuggeree(priorite)
            .categorie(categorie)
            .sentiment(sentiment)
            .resume(resume)
            .reponseSuggeree(reponse)
            .source(source)
            .build();
    }

    private String categoriser(String texte) {
        String t = texte.toLowerCase();
        if (contient(t, "facture", "paiement", "virement", "prix", "abonnement", "rembours")) return "Facturation";
        if (contient(t, "bug", "erreur", "plante", "marche pas", "ne fonctionne", "500", "crash", "lent")) return "Technique";
        if (contient(t, "connexion", "mot de passe", "login", "compte", "accès", "acces")) return "Compte / Accès";
        if (contient(t, "projet", "jalon", "tâche", "tache", "livraison", "délai", "delai")) return "Suivi de projet";
        if (contient(t, "fonction", "feature", "amélior", "amelior", "suggestion", "idée", "idee")) return "Demande d'évolution";
        return "Général";
    }

    private String prioriser(String texte) {
        String t = texte.toLowerCase();
        if (contient(t, "urgent", "bloqu", "critique", "immédiat", "immediat", "production", "impossible", "perte")) return "URGENTE";
        if (contient(t, "rapidement", "important", "erreur", "bug", "plante", "500", "crash")) return "HAUTE";
        if (contient(t, "question", "info", "renseign", "comment", "quand")) return "BASSE";
        return "MOYENNE";
    }

    private String analyserSentiment(String texte) {
        String t = texte.toLowerCase();
        int neg = compter(t, "inacceptable", "déçu", "decu", "mécontent", "mecontent", "scandaleux", "nul",
            "jamais", "toujours pas", "colère", "colere", "furieux", "honte", "!!!");
        int pos = compter(t, "merci", "super", "génial", "genial", "parfait", "excellent", "bravo", "content");
        if (neg > pos && neg > 0) return "Négatif";
        if (pos > neg && pos > 0) return "Positif";
        return "Neutre";
    }

    private String resumer(String sujet, String texte) {
        String plat = texte.replaceAll("\\s+", " ").trim();
        String court = plat.length() > 160 ? plat.substring(0, 157) + "…" : plat;
        return "Le client signale : " + court;
    }

    private String reponseTemplate(String prenom, String categorie) {
        String coeur = switch (categorie) {
            case "Facturation" -> "Nous avons bien noté votre demande concernant la facturation. "
                + "Nous vérifions votre dossier et revenons vers vous avec le détail sous 24 h.";
            case "Technique" -> "Merci pour ce signalement. Notre équipe technique reproduit le problème "
                + "et vous tient informé de la correction dans les meilleurs délais.";
            case "Compte / Accès" -> "Nous prenons en charge votre souci d'accès. "
                + "Nous réinitialisons ce qui est nécessaire et vous confirmons la marche à suivre.";
            case "Suivi de projet" -> "Merci pour votre message. Nous faisons le point sur l'avancement "
                + "de votre projet et vous transmettons une mise à jour détaillée.";
            case "Demande d'évolution" -> "Excellente suggestion, merci ! Nous l'ajoutons à notre backlog "
                + "et revenons vers vous sur sa faisabilité.";
            default -> "Merci pour votre message. Nous l'analysons et revenons rapidement vers vous.";
        };
        return "Bonjour " + prenom + ",\n\n" + coeur + "\n\nBien cordialement,\nL'équipe support.";
    }

    private boolean contient(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.contains(mot)) return true;
        }
        return false;
    }

    private int compter(String texte, String... mots) {
        int n = 0;
        for (String mot : mots) {
            if (texte.contains(mot)) n++;
        }
        return n;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────

    private User assertAccess(Long userId, Ticket ticket) {
        User user = getUser(userId);
        switch (user.getUserType()) {
            case CLIENT -> {
                Startup startup = startupRepository.findByUserId(userId)
                    .orElseThrow(() -> new AccessForbiddenException("Profil client introuvable."));
                if (!ticket.getStartup().getId().equals(startup.getId())) {
                    throw new AccessForbiddenException("Ce ticket ne vous appartient pas.");
                }
            }
            case PRESTATAIRE -> {
                Expert expert = expertProfileService.getByUserId(userId);
                if (!ticket.getExpert().getId().equals(expert.getId())) {
                    throw new AccessForbiddenException("Ce ticket ne vous est pas adressé.");
                }
            }
            default -> throw new AccessForbiddenException("Accès non autorisé à ce ticket.");
        }
        return user;
    }

    private Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable."));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }
}

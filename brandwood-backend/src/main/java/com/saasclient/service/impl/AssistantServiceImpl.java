package com.saasclient.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasclient.dto.ChatResponse;
import com.saasclient.entity.Expert;
import com.saasclient.entity.StatutProjet;
import com.saasclient.entity.User;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.repository.*;
import com.saasclient.service.AssistantService;
import com.saasclient.service.ExpertProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Assistant virtuel de la plateforme. Toujours fonctionnel grâce à un moteur local
 * « data-aware » (réponses basées sur les données réelles de l'utilisateur). Si une clé
 * {@code ANTHROPIC_API_KEY} est fournie, les réponses sont générées par le modèle Claude.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final UserRepository userRepository;
    private final ProjetRepository projetRepository;
    private final StartupRepository startupRepository;
    private final PackRepository packRepository;
    private final ExpertRepository expertRepository;
    private final ExpertProfileService expertProfileService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key:}")
    private String apiKey;

    @Value("${anthropic.model:claude-haiku-4-5-20251001}")
    private String model;

    @Override
    @Transactional
    public ChatResponse repondre(Long userId, String message) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

        // 1) Si une clé Claude est configurée, on tente une réponse générée par l'IA.
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String reply = appelerClaude(construireContexte(user), message);
                if (reply != null && !reply.isBlank()) {
                    return ChatResponse.builder().reply(reply.trim()).source("ia").build();
                }
            } catch (Exception e) {
                log.warn("Claude indisponible, repli sur l'assistant local : {}", e.getMessage());
            }
        }

        // 2) Repli toujours disponible : assistant local data-aware.
        return ChatResponse.builder().reply(reponseLocale(user, message)).source("assistant").build();
    }

    // ── Contexte (données réelles de l'utilisateur) ──────────────────────────────────

    private String construireContexte(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("Utilisateur : ").append(user.getFirstName()).append(" ").append(user.getLastName())
          .append(" (rôle ").append(user.getUserType()).append("). ");
        switch (user.getUserType()) {
            case PRESTATAIRE -> {
                Expert e = expertProfileService.getOrCreate(user.getId());
                sb.append("Pack : ").append(e.getPack() != null ? e.getPack().getNom() : "aucun (essai gratuit)")
                  .append(", statut compte : ").append(e.getStatutCompte())
                  .append(", projets gérés : ").append(projetRepository.countByExpertId(e.getId()))
                  .append(", clients actifs : ").append(startupRepository.countByPrestataireIdAndActifTrue(e.getId()))
                  .append(".");
            }
            case CLIENT -> sb.append("Projets suivis : ")
                .append(projetRepository.countByStartup_User_Id(user.getId())).append(".");
            case SUPER_ADMIN -> sb.append("Prestataires : ").append(expertRepository.count())
                .append(", packs : ").append(packRepository.count()).append(".");
        }
        return sb.toString();
    }

    // ── Assistant local (intentions + données) ───────────────────────────────────────

    private String reponseLocale(User user, String message) {
        String m = message == null ? "" : message.toLowerCase().trim();
        String prenom = user.getFirstName();

        if (contient(m, "bonjour", "salut", "hello", "coucou", "bonsoir")) {
            return "Bonjour " + prenom + " ! Je suis l'assistant SaaS Client. Je peux vous renseigner sur "
                + "vos projets, vos clients, vos packs/abonnements et le fonctionnement de la plateforme. "
                + "Que voulez-vous savoir ?";
        }
        if (contient(m, "aide", "help", "que peux", "capac", "fonction")) {
            return "Je peux répondre sur : vos projets et leur progression, vos clients (prestataire), "
                + "votre pack / abonnement, les jalons et l'équipe d'un projet. Posez votre question en langage naturel.";
        }

        switch (user.getUserType()) {
            case PRESTATAIRE -> {
                Expert e = expertProfileService.getOrCreate(user.getId());
                if (contient(m, "projet")) {
                    long n = projetRepository.countByExpertId(e.getId());
                    long enCours = projetRepository.countByExpertIdAndStatut(e.getId(), StatutProjet.EN_COURS);
                    return "Vous gérez " + n + " projet(s), dont " + enCours + " en cours. "
                        + "Ouvrez le menu « Projets » pour les détails et les jalons.";
                }
                if (contient(m, "client")) {
                    long c = startupRepository.countByPrestataireIdAndActifTrue(e.getId());
                    String limite = e.getPack() != null
                        ? "votre pack " + e.getPack().getNom() + " autorise " + e.getPack().getNbClientsMax() + " clients"
                        : "l'essai gratuit autorise 1 client pendant 1 semaine";
                    return "Vous avez " + c + " client(s) actif(s). Pour rappel, " + limite + ".";
                }
                if (contient(m, "pack", "abonnement", "paiement", "facture")) {
                    return e.getPack() != null
                        ? "Votre pack actuel est « " + e.getPack().getNom() + " » (statut compte : "
                            + e.getStatutCompte() + "). La souscription se gère depuis « Mon profil »."
                        : "Vous êtes en essai gratuit (1 client / 1 semaine). Souscrivez un pack depuis "
                            + "« Mon profil » (paiement par virement, validé par l'administrateur).";
                }
                if (contient(m, "jalon", "progression", "avanc")) {
                    return "La progression d'un projet = jalons atteints / total. Ajoutez et marquez les jalons "
                        + "dans l'onglet « Jalons » du détail du projet ; la progression se recalcule automatiquement.";
                }
                if (contient(m, "membre", "equipe", "équipe")) {
                    return "Gérez l'équipe dans l'onglet « Membres » du détail d'un projet : ajoutez des membres "
                        + "avec un rôle (Chef de projet, Développeur, Designer, Testeur, Analyste).";
                }
            }
            case CLIENT -> {
                if (contient(m, "projet", "jalon", "progression", "avanc")) {
                    long n = projetRepository.countByStartup_User_Id(user.getId());
                    return "Vous suivez " + n + " projet(s). Ouvrez le menu « Projets » pour consulter "
                        + "l'avancement, les jalons et l'équipe (lecture seule).";
                }
                if (contient(m, "profil", "compte", "entreprise")) {
                    return "Vous pouvez mettre à jour vos informations d'entreprise dans « Mon profil ».";
                }
            }
            case SUPER_ADMIN -> {
                if (contient(m, "prestataire")) {
                    return "Vous administrez " + expertRepository.count() + " prestataire(s) depuis le menu "
                        + "« Prestataires » (activer / désactiver / suspendre / supprimer).";
                }
                if (contient(m, "pack")) {
                    return "Vous gérez " + packRepository.count() + " pack(s) depuis le menu « Packs ».";
                }
                if (contient(m, "abonnement", "virement", "paiement")) {
                    return "Validez les paiements par virement dans « Abonnements » (filtre EN_ATTENTE) → bouton Valider.";
                }
            }
        }
        return "Je n'ai pas saisi votre demande, " + prenom + ". Essayez : « combien de projets ai-je ? », "
            + "« quel est mon pack ? », « comment fonctionne la progression ? ». Tapez « aide » pour la liste.";
    }

    private boolean contient(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.contains(mot)) {
                return true;
            }
        }
        return false;
    }

    // ── Appel Claude (activé uniquement si ANTHROPIC_API_KEY est défini) ──────────────

    private String appelerClaude(String contexte, String message) throws Exception {
        String systeme = "Tu es l'assistant virtuel de la plateforme SaaS Client (gestion de projets, clients, "
            + "packs et abonnements). Réponds en français, de façon concise et utile. "
            + "Contexte de l'utilisateur connecté : " + contexte;

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 512,
            "system", systeme,
            "messages", new Object[]{ Map.of("role", "user", "content", message) }
        );

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .timeout(Duration.ofSeconds(30))
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("Claude HTTP " + resp.statusCode());
        }
        JsonNode content = objectMapper.readTree(resp.body()).path("content");
        return (content.isArray() && content.size() > 0) ? content.get(0).path("text").asText() : null;
    }
}

package com.saasclient.service.impl;

import com.saasclient.dto.CampagneIaRequest;
import com.saasclient.dto.CampagneIaResponse;
import com.saasclient.dto.CampagneRequest;
import com.saasclient.dto.CampagneResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.CampagneMapper;
import com.saasclient.repository.CampagneRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.AiClient;
import com.saasclient.service.CampagneService;
import com.saasclient.service.ExpertProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampagneServiceImpl implements CampagneService {

    private final CampagneRepository campagneRepository;
    private final StartupRepository startupRepository;
    private final UserRepository userRepository;
    private final ExpertProfileService expertProfileService;
    private final CampagneMapper campagneMapper;
    private final AiClient aiClient;

    @Override
    @Transactional(readOnly = true)
    public Page<CampagneResponse> lister(Long userId, Pageable pageable) {
        User user = getUser(userId);
        Page<Campagne> page = switch (user.getUserType()) {
            case PRESTATAIRE -> campagneRepository.findByExpertId(
                expertProfileService.getOrCreate(userId).getId(), pageable);
            case CLIENT -> campagneRepository.findByStartup_User_Id(userId, pageable);
            default -> throw new AccessForbiddenException("Les campagnes ne sont pas accessibles à ce rôle.");
        };
        return page.map(campagneMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CampagneResponse obtenir(Long userId, Long id) {
        Campagne c = getCampagne(id);
        assertReadAccess(userId, c);
        return campagneMapper.toResponse(c);
    }

    @Override
    @Transactional
    public CampagneResponse creer(Long prestataireUserId, CampagneRequest req) {
        Expert expert = expertProfileService.getOrCreate(prestataireUserId);
        if (req.getStartupId() == null) {
            throw new BusinessRuleException("Le client (startupId) est obligatoire pour créer une campagne.");
        }
        Startup startup = startupRepository.findById(req.getStartupId())
            .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));
        if (startup.getPrestataire() == null || !startup.getPrestataire().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Ce client ne fait pas partie de votre portefeuille.");
        }

        Campagne c = Campagne.builder()
            .nom(req.getNom())
            .description(req.getDescription())
            .canal(req.getCanal())
            .statut(req.getStatut() != null ? req.getStatut() : StatutCampagne.BROUILLON)
            .budget(req.getBudget())
            .dateDebut(req.getDateDebut())
            .dateFin(req.getDateFin())
            .impressions(req.getImpressions() != null ? req.getImpressions() : 0)
            .clics(req.getClics() != null ? req.getClics() : 0)
            .conversions(req.getConversions() != null ? req.getConversions() : 0)
            .startup(startup)
            .expert(expert)
            .build();
        return campagneMapper.toResponse(campagneRepository.save(c));
    }

    @Override
    @Transactional
    public CampagneResponse modifier(Long prestataireUserId, Long id, CampagneRequest req) {
        Campagne c = getCampagne(id);
        assertPrestataireOwns(prestataireUserId, c);
        c.setNom(req.getNom());
        c.setDescription(req.getDescription());
        c.setCanal(req.getCanal());
        if (req.getStatut() != null) c.setStatut(req.getStatut());
        c.setBudget(req.getBudget());
        c.setDateDebut(req.getDateDebut());
        c.setDateFin(req.getDateFin());
        if (req.getImpressions() != null) c.setImpressions(req.getImpressions());
        if (req.getClics() != null) c.setClics(req.getClics());
        if (req.getConversions() != null) c.setConversions(req.getConversions());
        return campagneMapper.toResponse(campagneRepository.save(c));
    }

    @Override
    @Transactional
    public void supprimer(Long prestataireUserId, Long id) {
        Campagne c = getCampagne(id);
        assertPrestataireOwns(prestataireUserId, c);
        campagneRepository.delete(c);
    }

    // ── IA : génération de contenu de campagne ────────────────────────────────────────

    @Override
    @Transactional
    public CampagneIaResponse genererContenu(Long prestataireUserId, CampagneIaRequest req) {
        // Réservé aux prestataires (garantit un profil expert).
        expertProfileService.getOrCreate(prestataireUserId);

        String theme = req.getTheme() == null ? "" : req.getTheme().trim();
        CanalCampagne canal = req.getCanal() != null ? req.getCanal() : canalRecommande(theme);
        String cible = (req.getCible() != null && !req.getCible().isBlank())
            ? req.getCible().trim() : audienceParDefaut(theme);

        // 1) Tentative IA générative (si ANTHROPIC_API_KEY configurée).
        String systeme = "Tu es un expert en marketing digital. À partir d'un thème, tu rédiges en français "
            + "le contenu d'une campagne pour le canal indiqué. Réponds STRICTEMENT en JSON avec les clés : "
            + "nom, accroche, description, audience, hashtags (tableau de 3 à 5 mots-clés sans #). Rien d'autre.";
        String prompt = "Thème : " + theme + "\nCanal : " + canal + "\nAudience : " + cible;
        var ia = aiClient.generer(systeme, prompt, 600);
        if (ia.isPresent()) {
            CampagneIaResponse depuisIa = parserJsonIa(ia.get(), canal);
            if (depuisIa != null) {
                return depuisIa;
            }
        }

        // 2) Repli heuristique local (toujours disponible).
        return heuristique(theme, canal, cible);
    }

    /** Moteur local : compose un contenu marketing cohérent à partir du thème et du canal. */
    private CampagneIaResponse heuristique(String theme, CanalCampagne canal, String cible) {
        String t = theme.isBlank() ? "votre offre" : theme;
        String accroche = switch (canal) {
            case EMAIL -> "Découvrez " + t + " — offre exclusive réservée à nos abonnés";
            case RESEAUX_SOCIAUX -> t + " arrive : ne passez pas à côté ✨";
            case SEO -> t + " : le guide complet pour bien choisir";
            case SEA -> t + " au meilleur prix — cliquez maintenant";
            case EVENEMENT -> "Rejoignez-nous pour découvrir " + t + " en avant-première";
            case AUTRE -> "Faites la différence avec " + t;
        };
        String description = "Campagne " + libelleCanal(canal) + " autour de « " + t + " ». "
            + "Objectif : capter l'attention de " + cible + " et générer des conversions qualifiées. "
            + "Message clé : mettre en avant la valeur concrète et un appel à l'action clair "
            + "(essai, démo ou remise limitée dans le temps). Ton : professionnel, direct et engageant.";
        List<String> hashtags = motsCles(t, canal);
        return CampagneIaResponse.builder()
            .nom("Campagne " + capitaliser(t))
            .accroche(accroche)
            .description(description)
            .audience(cible)
            .canalRecommande(canal.name())
            .hashtags(hashtags)
            .source("heuristique")
            .build();
    }

    private CampagneIaResponse parserJsonIa(String texte, CanalCampagne canal) {
        try {
            int deb = texte.indexOf('{');
            int fin = texte.lastIndexOf('}');
            if (deb < 0 || fin <= deb) return null;
            com.fasterxml.jackson.databind.JsonNode n =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(texte.substring(deb, fin + 1));
            java.util.List<String> tags = new java.util.ArrayList<>();
            n.path("hashtags").forEach(x -> tags.add(x.asText()));
            return CampagneIaResponse.builder()
                .nom(n.path("nom").asText(""))
                .accroche(n.path("accroche").asText(""))
                .description(n.path("description").asText(""))
                .audience(n.path("audience").asText(""))
                .canalRecommande(canal.name())
                .hashtags(tags)
                .source("ia")
                .build();
        } catch (Exception e) {
            return null;
        }
    }

    private CanalCampagne canalRecommande(String theme) {
        String t = theme.toLowerCase();
        if (t.contains("event") || t.contains("évén") || t.contains("salon") || t.contains("conf")) return CanalCampagne.EVENEMENT;
        if (t.contains("promo") || t.contains("solde") || t.contains("vente") || t.contains("prix")) return CanalCampagne.SEA;
        if (t.contains("blog") || t.contains("article") || t.contains("guide") || t.contains("contenu")) return CanalCampagne.SEO;
        if (t.contains("insta") || t.contains("tiktok") || t.contains("social") || t.contains("commun")) return CanalCampagne.RESEAUX_SOCIAUX;
        return CanalCampagne.EMAIL;
    }

    private String audienceParDefaut(String theme) {
        return "les professionnels et prospects intéressés par " + (theme.isBlank() ? "votre secteur" : theme);
    }

    private List<String> motsCles(String theme, CanalCampagne canal) {
        String base = theme.toLowerCase().replaceAll("[^a-zàâçéèêëîïôûùüÿñ0-9 ]", "").trim();
        List<String> tags = new java.util.ArrayList<>();
        for (String mot : base.split("\\s+")) {
            if (mot.length() > 3 && tags.size() < 3) tags.add(mot);
        }
        tags.add(libelleCanal(canal).toLowerCase().replace(" ", ""));
        tags.add("marketing");
        return tags.stream().distinct().limit(5).toList();
    }

    private String libelleCanal(CanalCampagne c) {
        return switch (c) {
            case EMAIL -> "e-mailing";
            case RESEAUX_SOCIAUX -> "réseaux sociaux";
            case SEO -> "référencement naturel";
            case SEA -> "publicité payante";
            case EVENEMENT -> "événementiel";
            case AUTRE -> "multicanal";
        };
    }

    private String capitaliser(String s) {
        s = s.trim();
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────

    private Campagne getCampagne(Long id) {
        return campagneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    private void assertPrestataireOwns(Long prestataireUserId, Campagne c) {
        Expert expert = expertProfileService.getByUserId(prestataireUserId);
        if (!c.getExpert().getId().equals(expert.getId())) {
            throw new AccessForbiddenException("Cette campagne ne vous est pas assignée.");
        }
    }

    private void assertReadAccess(Long userId, Campagne c) {
        User user = getUser(userId);
        switch (user.getUserType()) {
            case PRESTATAIRE -> {
                Expert e = expertProfileService.getByUserId(userId);
                if (!c.getExpert().getId().equals(e.getId())) {
                    throw new AccessForbiddenException("Cette campagne ne vous est pas assignée.");
                }
            }
            case CLIENT -> {
                Startup s = startupRepository.findByUserId(userId)
                    .orElseThrow(() -> new AccessForbiddenException("Profil client introuvable."));
                if (!c.getStartup().getId().equals(s.getId())) {
                    throw new AccessForbiddenException("Cette campagne ne vous appartient pas.");
                }
            }
            default -> throw new AccessForbiddenException("Accès non autorisé à cette campagne.");
        }
    }
}

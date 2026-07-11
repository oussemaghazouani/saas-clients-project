package com.saasclient.service.impl;

import com.saasclient.dto.*;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.TacheMapper;
import com.saasclient.repository.*;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.TacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TacheServiceImpl implements TacheService {

    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;
    private final MembreRepository membreRepository;
    private final JalonRepository jalonRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final ExpertProfileService expertProfileService;
    private final TacheMapper tacheMapper;

    // ── CRUD / Kanban ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TacheResponse> lister(Long userId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertReadAccess(userId, projet);
        return tacheRepository.findByProjetIdOrderByPositionAsc(projetId)
            .stream().map(tacheMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TacheResponse creer(Long prestataireUserId, Long projetId, TacheRequest req) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        Tache tache = construire(projet, req, (int) tacheRepository.countByProjetId(projetId));
        return tacheMapper.toResponse(tacheRepository.save(tache));
    }

    @Override
    @Transactional
    public TacheResponse modifier(Long prestataireUserId, Long tacheId, TacheRequest req) {
        Tache tache = getTache(tacheId);
        assertPrestataireOwns(prestataireUserId, tache.getProjet());
        tache.setTitre(req.getTitre());
        tache.setDescription(req.getDescription());
        if (req.getPriorite() != null) tache.setPriorite(req.getPriorite());
        if (req.getStatut() != null)   tache.setStatut(req.getStatut());
        tache.setDateEcheance(req.getDateEcheance());
        tache.setMembre(resoudreMembre(tache.getProjet(), req.getMembreId()));
        return tacheMapper.toResponse(tacheRepository.save(tache));
    }

    @Override
    @Transactional
    public TacheResponse changerStatut(Long prestataireUserId, Long tacheId, StatutTache statut) {
        Tache tache = getTache(tacheId);
        assertPrestataireOwns(prestataireUserId, tache.getProjet());
        tache.setStatut(statut);
        return tacheMapper.toResponse(tacheRepository.save(tache));
    }

    @Override
    @Transactional
    public void supprimer(Long prestataireUserId, Long tacheId) {
        Tache tache = getTache(tacheId);
        assertPrestataireOwns(prestataireUserId, tache.getProjet());
        tacheRepository.delete(tache);
    }

    // ── IA : génération intelligente de tâches ──────────────────────────────────────

    @Override
    @Transactional
    public GenerationTachesResponse genererTaches(Long prestataireUserId, Long projetId) {
        Projet projet = getProjetOwnedBy(prestataireUserId, projetId);
        List<TacheRequest> suggestions = genererSuggestions(projet);
        int base = (int) tacheRepository.countByProjetId(projetId);
        List<TacheResponse> creees = new ArrayList<>();
        for (int i = 0; i < suggestions.size(); i++) {
            Tache t = construire(projet, suggestions.get(i), base + i);
            creees.add(tacheMapper.toResponse(tacheRepository.save(t)));
        }
        return GenerationTachesResponse.builder()
            .taches(creees)
            .nbCreees(creees.size())
            .source("intelligent")
            .message(creees.size() + " tâche(s) générée(s) à partir du projet et de ses technologies.")
            .build();
    }

    /** Découpage SDLC adapté aux technologies déclarées du projet. */
    private List<TacheRequest> genererSuggestions(Projet projet) {
        List<TacheRequest> l = new ArrayList<>();
        LocalDate base = projet.getDateDebut() != null ? projet.getDateDebut() : LocalDate.now();
        String tech = projet.getTechnologies() == null ? "" : String.join(" ", projet.getTechnologies()).toLowerCase();

        l.add(tr("Cadrage et recueil des besoins", "Ateliers, user stories, définition du périmètre", PrioriteTache.HAUTE, base.plusDays(5)));
        l.add(tr("Conception de l'architecture", "Modèle de données, schémas, choix techniques", PrioriteTache.HAUTE, base.plusDays(10)));
        if (contient(tech, "angular", "react", "vue", "front")) {
            l.add(tr("Développement de l'interface (front-end)", "Écrans, composants, intégration du design", PrioriteTache.MOYENNE, base.plusDays(22)));
        }
        if (contient(tech, "spring", "node", "java", "api", "back")) {
            l.add(tr("Développement de l'API (back-end)", "Endpoints, services métier, persistance", PrioriteTache.MOYENNE, base.plusDays(22)));
        }
        if (contient(tech, "mysql", "postgres", "mongo", "sql", "oracle")) {
            l.add(tr("Mise en place de la base de données", "Schéma, migrations, jeux de données", PrioriteTache.MOYENNE, base.plusDays(12)));
        }
        l.add(tr("Tests et recette", "Tests unitaires, intégration, recette fonctionnelle", PrioriteTache.HAUTE, base.plusDays(28)));
        l.add(tr("Déploiement et livraison", "Mise en production et documentation", PrioriteTache.CRITIQUE, base.plusDays(32)));
        return l;
    }

    // ── IA : analyse de risque (prédiction de retard) ───────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AnalyseRisqueResponse analyserRisque(Long userId, Long projetId) {
        Projet projet = getProjet(projetId);
        assertReadAccess(userId, projet);
        LocalDate today = LocalDate.now();

        long totalT = tacheRepository.countByProjetId(projetId);
        long tachesRetard = tacheRepository.findByProjetIdOrderByPositionAsc(projetId).stream()
            .filter(t -> t.getStatut() != StatutTache.TERMINE
                && t.getDateEcheance() != null && t.getDateEcheance().isBefore(today))
            .count();
        long jalonsRetard = jalonRepository.findByProjetIdOrderByDatePrevueAsc(projetId).stream()
            .filter(j -> j.getStatut() != StatutJalon.ATTEINT
                && j.getDatePrevue() != null && j.getDatePrevue().isBefore(today))
            .count();

        int score = 0;
        List<String> facteurs = new ArrayList<>();
        if (tachesRetard > 0) { score += (int) Math.min(50, tachesRetard * 15); facteurs.add(tachesRetard + " tâche(s) en retard"); }
        if (jalonsRetard > 0) { score += (int) Math.min(30, jalonsRetard * 15); facteurs.add(jalonsRetard + " jalon(s) en retard"); }
        if (projet.getProgression() != null && projet.getProgression() < 30
                && projet.getDateFin() != null && projet.getDateFin().isBefore(today.plusDays(14))) {
            score += 20; facteurs.add("Progression faible alors que l'échéance approche");
        }
        if (totalT == 0) { score += 10; facteurs.add("Aucune tâche définie"); }
        score = Math.min(100, score);

        String niveau = score >= 60 ? "ELEVE" : (score >= 30 ? "MOYEN" : "FAIBLE");
        if (facteurs.isEmpty()) { facteurs.add("Aucun facteur de risque détecté"); }
        String message = switch (niveau) {
            case "ELEVE" -> "Risque de retard élevé : une action corrective est recommandée.";
            case "MOYEN" -> "Risque modéré : surveillez les éléments en retard.";
            default -> "Projet sous contrôle.";
        };
        return AnalyseRisqueResponse.builder()
            .niveau(niveau).score(score).message(message).facteurs(facteurs).source("heuristique").build();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────

    private Tache construire(Projet projet, TacheRequest req, int position) {
        return Tache.builder()
            .titre(req.getTitre())
            .description(req.getDescription())
            .priorite(req.getPriorite() != null ? req.getPriorite() : PrioriteTache.MOYENNE)
            .statut(req.getStatut() != null ? req.getStatut() : StatutTache.A_FAIRE)
            .dateEcheance(req.getDateEcheance())
            .position(position)
            .projet(projet)
            .membre(resoudreMembre(projet, req.getMembreId()))
            .build();
    }

    private TacheRequest tr(String titre, String desc, PrioriteTache p, LocalDate d) {
        return TacheRequest.builder().titre(titre).description(desc)
            .priorite(p).dateEcheance(d).statut(StatutTache.A_FAIRE).build();
    }

    private boolean contient(String texte, String... mots) {
        for (String mot : mots) { if (texte.contains(mot)) return true; }
        return false;
    }

    private Membre resoudreMembre(Projet projet, Long membreId) {
        if (membreId == null) return null;
        Membre m = membreRepository.findById(membreId)
            .orElseThrow(() -> new ResourceNotFoundException("Membre introuvable."));
        if (!m.getProjet().getId().equals(projet.getId())) {
            throw new BusinessRuleException("Ce membre n'appartient pas au projet.");
        }
        return m;
    }

    private Projet getProjet(Long id) {
        return projetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable."));
    }

    private Tache getTache(Long id) {
        return tacheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable."));
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

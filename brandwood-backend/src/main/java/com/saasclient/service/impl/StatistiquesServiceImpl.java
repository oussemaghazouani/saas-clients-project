package com.saasclient.service.impl;

import com.saasclient.dto.ChartData;
import com.saasclient.dto.DataPoint;
import com.saasclient.dto.InsightsResponse;
import com.saasclient.dto.KpiDto;
import com.saasclient.dto.StatistiquesResponse;
import com.saasclient.entity.*;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.repository.*;
import com.saasclient.service.AiClient;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatistiquesServiceImpl implements StatistiquesService {

    private final UserRepository userRepository;
    private final ExpertProfileService expertProfileService;
    private final StartupRepository startupRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final FactureRepository factureRepository;
    private final ExpertRepository expertRepository;
    private final PackRepository packRepository;
    private final AbonnementRepository abonnementRepository;
    private final AiClient aiClient;

    private static final String GREY = "#9CA3AF", BLUE = "#3B82F6", AMBER = "#F59E0B",
        GREEN = "#10B981", RED = "#EF4444", VIOLET = "#6C63FF";

    @Override
    @Transactional
    public StatistiquesResponse charger(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        return switch (user.getUserType()) {
            case SUPER_ADMIN -> admin();
            case PRESTATAIRE -> prestataire(userId);
            case CLIENT -> client(userId);
        };
    }

    // ── IA : insights en langage naturel ──────────────────────────────────────────────

    @Override
    @Transactional
    public InsightsResponse insights(Long userId) {
        StatistiquesResponse stats = charger(userId);

        List<String> obs = new ArrayList<>();
        for (KpiDto k : stats.getKpis()) {
            obs.add(k.getLabel() + " : " + k.getValeur() + " — " + k.getSousLabel() + ".");
        }
        for (ChartData c : stats.getCharts()) {
            double tot = c.getPoints().stream().mapToDouble(DataPoint::getValeur).sum();
            DataPoint dom = c.getPoints().stream().max((a, b) -> Double.compare(a.getValeur(), b.getValeur())).orElse(null);
            if (dom != null && tot > 0 && !"Aucun".equals(dom.getLabel())) {
                int pct = (int) Math.round(dom.getValeur() / tot * 100);
                obs.add(c.getTitre() + " : « " + dom.getLabel() + " » domine (" + pct + "%).");
            }
        }
        String reco = recommander(stats);

        // 1) Analyse IA générative (si clé configurée).
        if (aiClient.estActif()) {
            String systeme = "Tu es analyste de données pour une plateforme SaaS. À partir d'indicateurs, "
                + "produis un JSON {\"insights\":[3 à 5 observations concises en français], "
                + "\"recommandation\":\"un conseil d'action\"}. Rien d'autre.";
            String prompt = "Profil : " + stats.getUserType() + "\nIndicateurs :\n" + String.join("\n", obs);
            var ia = aiClient.generer(systeme, prompt, 500);
            if (ia.isPresent()) {
                InsightsResponse depuisIa = parserInsightsIa(ia.get());
                if (depuisIa != null) {
                    return depuisIa;
                }
            }
        }

        // 2) Repli heuristique local.
        return InsightsResponse.builder().insights(obs).recommandation(reco).source("heuristique").build();
    }

    private String recommander(StatistiquesResponse stats) {
        return switch (stats.getUserType()) {
            case "PRESTATAIRE" -> "Concentrez-vous sur les projets « en cours » et relancez les factures « envoyées » "
                + "non réglées pour améliorer votre trésorerie.";
            case "SUPER_ADMIN" -> "Traitez en priorité les abonnements « en attente » et surveillez le taux de comptes actifs.";
            case "CLIENT" -> "Suivez l'avancement de vos projets et réglez les factures en attente pour éviter les retards.";
            default -> "Analysez régulièrement vos indicateurs pour anticiper les tendances.";
        };
    }

    private InsightsResponse parserInsightsIa(String texte) {
        try {
            int deb = texte.indexOf('{');
            int fin = texte.lastIndexOf('}');
            if (deb < 0 || fin <= deb) return null;
            com.fasterxml.jackson.databind.JsonNode n =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(texte.substring(deb, fin + 1));
            List<String> list = new ArrayList<>();
            n.path("insights").forEach(x -> list.add(x.asText()));
            if (list.isEmpty()) return null;
            return InsightsResponse.builder()
                .insights(list)
                .recommandation(n.path("recommandation").asText(""))
                .source("ia")
                .build();
        } catch (Exception e) {
            return null;
        }
    }

    private StatistiquesResponse prestataire(Long userId) {
        Expert e = expertProfileService.getOrCreate(userId);
        List<Projet> projets = projetRepository.findByExpertId(e.getId(), Pageable.unpaged()).getContent();
        List<Facture> factures = factureRepository.findByExpertIdOrderByDateEmissionDesc(e.getId());
        long clients = startupRepository.countByPrestataireIdAndActifTrue(e.getId());
        double caPaye = factures.stream().filter(f -> f.getStatut() == StatutFacture.PAYEE)
            .mapToDouble(Facture::getMontantTtc).sum();

        List<KpiDto> kpis = List.of(
            kpi("Projets", String.valueOf(projets.size()), "total", "bi-kanban", VIOLET),
            kpi("Clients actifs", String.valueOf(clients), "portefeuille", "bi-people", BLUE),
            kpi("CA encaissé", String.format("%.0f €", caPaye), "factures payées", "bi-cash-coin", GREEN),
            kpi("Factures", String.valueOf(factures.size()), "émises", "bi-receipt", AMBER)
        );

        List<DataPoint> projParStatut = new ArrayList<>();
        for (StatutProjet s : StatutProjet.values()) {
            long n = projets.stream().filter(p -> p.getStatut() == s).count();
            if (n > 0) { projParStatut.add(dp(s.name(), n, couleurProjet(s))); }
        }

        long[] taches = new long[StatutTache.values().length];
        for (Projet p : projets) {
            for (Tache t : tacheRepository.findByProjetIdOrderByPositionAsc(p.getId())) {
                taches[t.getStatut().ordinal()]++;
            }
        }
        List<DataPoint> tacheParStatut = new ArrayList<>();
        for (StatutTache s : StatutTache.values()) {
            tacheParStatut.add(dp(s.name(), taches[s.ordinal()], couleurTache(s)));
        }

        List<DataPoint> factParStatut = new ArrayList<>();
        for (StatutFacture s : StatutFacture.values()) {
            double somme = factures.stream().filter(f -> f.getStatut() == s).mapToDouble(Facture::getMontantTtc).sum();
            factParStatut.add(dp(s.name(), Math.round(somme), couleurFacture(s)));
        }

        return StatistiquesResponse.builder().userType("PRESTATAIRE").kpis(kpis)
            .charts(List.of(
                chart("donut", "Projets par statut", orDefault(projParStatut)),
                chart("bar", "Tâches par statut", tacheParStatut),
                chart("bar", "Montant facturé par statut (€)", factParStatut)
            )).build();
    }

    private StatistiquesResponse admin() {
        long prestataires = expertRepository.count();
        long clients = startupRepository.count();
        long packs = packRepository.count();
        double revenus = abonnementRepository.findByStatut(StatutAbonnement.ACTIF, Pageable.unpaged())
            .getContent().stream().filter(a -> a.getPack() != null).mapToDouble(a -> a.getPack().getPrix()).sum();

        List<KpiDto> kpis = List.of(
            kpi("Prestataires", String.valueOf(prestataires), "inscrits", "bi-people", VIOLET),
            kpi("Clients", String.valueOf(clients), "comptes", "bi-person-badge", BLUE),
            kpi("Packs", String.valueOf(packs), "offres", "bi-box-seam", GREEN),
            kpi("Revenus", String.format("%.0f €", revenus), "abonnements actifs", "bi-cash-coin", AMBER)
        );

        List<DataPoint> prestParStatut = new ArrayList<>();
        for (StatutCompte s : StatutCompte.values()) {
            long n = expertRepository.countByStatutCompte(s);
            if (n > 0) { prestParStatut.add(dp(s.name(), n, couleurCompte(s))); }
        }
        List<DataPoint> aboParStatut = new ArrayList<>();
        for (StatutAbonnement s : StatutAbonnement.values()) {
            aboParStatut.add(dp(s.name(), abonnementRepository.countByStatut(s), couleurAbo(s)));
        }

        return StatistiquesResponse.builder().userType("SUPER_ADMIN").kpis(kpis)
            .charts(List.of(
                chart("donut", "Prestataires par statut", orDefault(prestParStatut)),
                chart("bar", "Abonnements par statut", aboParStatut)
            )).build();
    }

    private StatistiquesResponse client(Long userId) {
        List<Projet> projets = projetRepository.findByStartup_User_Id(userId, Pageable.unpaged()).getContent();
        List<Facture> factures = factureRepository.findByStartup_User_IdOrderByDateEmissionDesc(userId);
        double total = factures.stream().mapToDouble(Facture::getMontantTtc).sum();

        List<KpiDto> kpis = List.of(
            kpi("Mes projets", String.valueOf(projets.size()), "suivis", "bi-kanban", VIOLET),
            kpi("Factures", String.valueOf(factures.size()), "reçues", "bi-receipt", BLUE),
            kpi("Total facturé", String.format("%.0f €", total), "TTC", "bi-cash-coin", GREEN)
        );

        List<DataPoint> projParStatut = new ArrayList<>();
        for (StatutProjet s : StatutProjet.values()) {
            long n = projets.stream().filter(p -> p.getStatut() == s).count();
            if (n > 0) { projParStatut.add(dp(s.name(), n, couleurProjet(s))); }
        }
        List<DataPoint> factParStatut = new ArrayList<>();
        for (StatutFacture s : StatutFacture.values()) {
            double somme = factures.stream().filter(f -> f.getStatut() == s).mapToDouble(Facture::getMontantTtc).sum();
            factParStatut.add(dp(s.name(), Math.round(somme), couleurFacture(s)));
        }

        return StatistiquesResponse.builder().userType("CLIENT").kpis(kpis)
            .charts(List.of(
                chart("donut", "Mes projets par statut", orDefault(projParStatut)),
                chart("bar", "Mes factures par statut (€)", factParStatut)
            )).build();
    }

    // ── Helpers ──
    private List<DataPoint> orDefault(List<DataPoint> pts) {
        return pts.isEmpty() ? List.of(dp("Aucun", 1, GREY)) : pts;
    }

    private KpiDto kpi(String label, String valeur, String sousLabel, String icone, String couleur) {
        return KpiDto.builder().label(label).valeur(valeur).sousLabel(sousLabel).icone(icone).couleur(couleur).build();
    }

    private DataPoint dp(String label, double valeur, String couleur) {
        return DataPoint.builder().label(label).valeur(valeur).couleur(couleur).build();
    }

    private ChartData chart(String type, String titre, List<DataPoint> points) {
        return ChartData.builder().type(type).titre(titre).points(points).build();
    }

    private String couleurProjet(StatutProjet s) {
        return switch (s) {
            case PLANIFIE -> GREY; case EN_COURS -> BLUE; case EN_PAUSE -> AMBER;
            case TERMINE -> GREEN; case ANNULE -> RED;
        };
    }

    private String couleurTache(StatutTache s) {
        return switch (s) {
            case A_FAIRE -> GREY; case EN_COURS -> BLUE; case EN_REVISION -> AMBER; case TERMINE -> GREEN;
        };
    }

    private String couleurFacture(StatutFacture s) {
        return switch (s) {
            case BROUILLON -> GREY; case ENVOYEE -> BLUE; case PAYEE -> GREEN; case ANNULEE -> RED;
        };
    }

    private String couleurCompte(StatutCompte s) {
        return switch (s) {
            case ACTIF -> GREEN; case DESACTIVE -> GREY; case SUSPENDU -> AMBER; case SUPPRIME -> RED;
        };
    }

    private String couleurAbo(StatutAbonnement s) {
        return switch (s) {
            case EN_ATTENTE -> AMBER; case ACTIF -> GREEN; case EXPIRE -> GREY; case ANNULE -> RED;
        };
    }
}

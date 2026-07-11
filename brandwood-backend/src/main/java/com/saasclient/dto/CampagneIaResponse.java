package com.saasclient.dto;

import lombok.*;

import java.util.List;

/** Contenu de campagne généré par l'IA (ou le moteur heuristique local). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampagneIaResponse {
    private String nom;            // nom de campagne proposé
    private String accroche;       // slogan / accroche courte
    private String description;    // message marketing complet
    private String audience;       // audience cible recommandée
    private String canalRecommande;
    private List<String> hashtags;
    private String source;         // "ia" ou "heuristique"
}

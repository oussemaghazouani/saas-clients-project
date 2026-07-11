package com.saasclient.dto;

import lombok.*;

import java.util.List;

/** Analyse de risque (prédiction de retard) d'un projet. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalyseRisqueResponse {

    private String niveau;          // FAIBLE / MOYEN / ELEVE
    private int score;              // 0 (sain) à 100 (critique)
    private String message;
    private List<String> facteurs;  // facteurs de risque détectés
    private String source;          // "heuristique" ou "ia"
}

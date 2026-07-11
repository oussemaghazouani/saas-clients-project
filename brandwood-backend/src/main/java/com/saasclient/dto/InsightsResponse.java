package com.saasclient.dto;

import lombok.*;

import java.util.List;

/** Analyse IA (langage naturel) des indicateurs du tableau de bord. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsightsResponse {
    private List<String> insights;   // observations clés
    private String recommandation;   // conseil d'action principal
    private String source;           // "ia" ou "heuristique"
}

package com.saasclient.dto;

import lombok.*;

import java.util.List;

/** Résultat de la génération intelligente de tâches pour un projet. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GenerationTachesResponse {

    private List<TacheResponse> taches;
    private int nbCreees;
    private String source;   // "ia" (Claude) ou "intelligent" (heuristique)
    private String message;
}

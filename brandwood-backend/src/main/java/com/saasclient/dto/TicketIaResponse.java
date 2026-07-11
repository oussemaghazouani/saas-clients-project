package com.saasclient.dto;

import lombok.*;

/** Analyse IA d'un ticket de support (classification + réponse suggérée). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketIaResponse {
    private String prioriteSuggeree;   // BASSE | MOYENNE | HAUTE | URGENTE
    private String categorie;          // ex: Technique, Facturation, Compte…
    private String sentiment;          // Positif | Neutre | Négatif
    private String resume;             // résumé court du besoin
    private String reponseSuggeree;    // brouillon de réponse au client
    private String source;             // "ia" ou "heuristique"
}

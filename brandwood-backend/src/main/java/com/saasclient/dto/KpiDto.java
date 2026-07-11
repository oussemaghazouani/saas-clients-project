package com.saasclient.dto;

import lombok.*;

/** Indicateur clé affiché sur le tableau de bord. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KpiDto {
    private String label;
    private String valeur;
    private String sousLabel;
    private String icone;   // classe bootstrap-icons (ex: bi-people)
    private String couleur; // code hex
}

package com.saasclient.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneResponse {

    private Long id;
    private String description;
    private Double quantite;
    private Double prixUnitaire;
    private Double montant;
}

package com.saasclient.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FactureResponse {

    private Long id;
    private String numero;
    private Long startupId;
    private String clientNom;
    private Long expertId;
    private String prestataireNom;
    private Long projetId;
    private String projetNom;
    private Double montantHt;
    private Double tauxTva;
    private Double montantTtc;
    private String statut;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private List<LigneResponse> lignes;
    private LocalDateTime createdAt;
}

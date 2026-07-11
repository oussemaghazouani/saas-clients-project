package com.saasclient.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbonnementResponse {

    private Long id;
    private Long expertId;
    private String prestataireNom;
    private Long packId;
    private String packNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private String methodePaiement;
    private boolean essaiGratuit;
}

package com.saasclient.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TacheResponse {

    private Long id;
    private String titre;
    private String description;
    private String statut;
    private String priorite;
    private LocalDate dateEcheance;
    private boolean enRetard;
    private int position;
    private Long projetId;
    private Long membreId;
    private String membreNom;
}

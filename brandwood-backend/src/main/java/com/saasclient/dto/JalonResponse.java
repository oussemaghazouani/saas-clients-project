package com.saasclient.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JalonResponse {

    private Long id;
    private String nom;
    private LocalDate datePrevue;
    /** Statut affiché : EN_RETARD est dérivé si la date est passée et le jalon non ATTEINT. */
    private String statut;
    private String description;
    private Long projetId;
}

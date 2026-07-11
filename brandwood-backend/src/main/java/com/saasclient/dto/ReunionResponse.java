package com.saasclient.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReunionResponse {
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateHeure;
    private Integer dureeMinutes;
    private String lien;
    private String statut;
    private Long projetId;
    private String projetNom;
}

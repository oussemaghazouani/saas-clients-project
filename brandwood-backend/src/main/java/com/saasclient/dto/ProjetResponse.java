package com.saasclient.dto;

import com.saasclient.entity.StatutProjet;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjetResponse {

    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double budget;
    private StatutProjet statut;
    private Double progression;
    private List<String> technologies;
    private Long startupId;
    private String clientNom;
    private Long expertId;
    private String prestataireNom;
    private long nbJalons;
    private long jalonsAtteints;
    private long nbMembres;
    private LocalDateTime createdAt;
}

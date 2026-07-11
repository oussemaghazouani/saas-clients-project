package com.saasclient.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Rapport agrégé d'un projet (Module 8) — destiné à l'affichage et à l'export PDF. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RapportProjetResponse {

    private LocalDateTime generatedAt;

    // Projet
    private Long projetId;
    private String nom;
    private String description;
    private String statut;
    private Double progression;
    private Double budget;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private List<String> technologies;
    private String clientNom;
    private String prestataireNom;

    // Jalons
    private long jalonsTotal;
    private long jalonsAtteints;

    // Tâches
    private long tachesTotal;
    private long tachesTerminees;
    private long tachesEnCours;
    private long tachesEnRetard;

    // Équipe & réunions
    private long nbMembres;
    private long nbReunions;
    private List<MembreResponse> membres;
}

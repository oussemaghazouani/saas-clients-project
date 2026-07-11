package com.saasclient.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampagneResponse {

    private Long id;
    private String nom;
    private String description;
    private String canal;
    private String statut;
    private Double budget;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer impressions;
    private Integer clics;
    private Integer conversions;
    /** conversions / clics * 100. */
    private Double tauxConversion;
    /** clics / impressions * 100 (Click-Through Rate). */
    private Double ctr;
    private Long startupId;
    private String clientNom;
    private Long expertId;
    private String prestataireNom;
    private LocalDateTime createdAt;
}

package com.saasclient.dto;

import com.saasclient.entity.CanalCampagne;
import com.saasclient.entity.StatutCampagne;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampagneRequest {

    @NotBlank(message = "Le nom de la campagne est obligatoire")
    @Size(max = 200)
    private String nom;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Le canal est obligatoire")
    private CanalCampagne canal;

    /** Optionnel : BROUILLON par défaut. */
    private StatutCampagne statut;

    @PositiveOrZero(message = "Le budget doit être positif ou nul")
    private Double budget;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @PositiveOrZero private Integer impressions;
    @PositiveOrZero private Integer clics;
    @PositiveOrZero private Integer conversions;

    /** Client (Startup) ciblé — obligatoire à la création. */
    private Long startupId;
}

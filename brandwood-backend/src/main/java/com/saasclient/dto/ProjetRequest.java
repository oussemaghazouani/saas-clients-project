package com.saasclient.dto;

import com.saasclient.entity.StatutProjet;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjetRequest {

    @NotBlank(message = "Le nom du projet est obligatoire")
    @Size(max = 150)
    private String nom;

    @Size(max = 1000)
    private String description;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @PositiveOrZero(message = "Le budget doit être positif ou nul")
    private Double budget;

    /** Optionnel : PLANIFIE par défaut à la création. */
    private StatutProjet statut;

    private List<String> technologies;

    /** Client (Startup) auquel le projet est rattaché — obligatoire à la création. */
    private Long startupId;
}

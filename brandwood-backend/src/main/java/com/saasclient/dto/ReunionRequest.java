package com.saasclient.dto;

import com.saasclient.entity.StatutReunion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReunionRequest {

    @NotBlank(message = "Le titre de la réunion est obligatoire")
    @Size(max = 200)
    private String titre;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "La date et l'heure sont obligatoires")
    private LocalDateTime dateHeure;

    @Positive(message = "La durée doit être positive")
    private Integer dureeMinutes;

    @Size(max = 500)
    private String lien;

    /** Optionnel : PLANIFIEE par défaut. */
    private StatutReunion statut;
}

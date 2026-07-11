package com.saasclient.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PackRequest {

    @NotBlank(message = "Le nom du pack est obligatoire")
    @Size(max = 100)
    private String nom;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @PositiveOrZero(message = "Le prix doit être positif ou nul")
    private Double prix;

    @NotNull(message = "Le nombre maximum de projets est obligatoire")
    @Positive(message = "Le nombre maximum de projets doit être positif")
    private Integer nbProjetsMax;

    @NotNull(message = "Le nombre maximum de clients est obligatoire")
    @Positive(message = "Le nombre maximum de clients doit être positif")
    private Integer nbClientsMax;

    @NotNull(message = "La durée en mois est obligatoire")
    @Positive(message = "La durée en mois doit être positive")
    private Integer dureeMois;

    /** Optionnel : true par défaut à la création. */
    private Boolean actif;
}

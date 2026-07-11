package com.saasclient.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneRequest {

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 300)
    private String description;

    @NotNull @Positive(message = "La quantité doit être positive")
    private Double quantite;

    @NotNull @PositiveOrZero(message = "Le prix unitaire doit être positif ou nul")
    private Double prixUnitaire;
}

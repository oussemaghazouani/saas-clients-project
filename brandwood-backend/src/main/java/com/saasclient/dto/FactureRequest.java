package com.saasclient.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FactureRequest {

    @NotNull(message = "Le client est obligatoire")
    private Long startupId;

    /** Projet associé (optionnel). */
    private Long projetId;

    @PositiveOrZero
    private Double tauxTva;

    private LocalDate dateEcheance;

    @NotEmpty(message = "Au moins une ligne est requise")
    @Valid
    private List<LigneRequest> lignes;
}

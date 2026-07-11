package com.saasclient.dto;

import com.saasclient.entity.StatutFacture;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FactureStatutRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutFacture statut;
}

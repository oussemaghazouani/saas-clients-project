package com.saasclient.dto;

import com.saasclient.entity.StatutTache;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TacheStatutRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutTache statut;
}

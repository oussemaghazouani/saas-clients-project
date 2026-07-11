package com.saasclient.dto;

import com.saasclient.entity.StatutJalon;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JalonStatutRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutJalon statut;
}
